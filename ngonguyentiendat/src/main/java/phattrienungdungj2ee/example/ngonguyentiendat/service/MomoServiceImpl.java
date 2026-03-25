package phattrienungdungj2ee.example.ngonguyentiendat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import phattrienungdungj2ee.example.ngonguyentiendat.config.MomoProperties;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.CheckoutRequest;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.MomoPaymentResult;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Order;
import phattrienungdungj2ee.example.ngonguyentiendat.model.PaymentMethod;
import phattrienungdungj2ee.example.ngonguyentiendat.repository.OrderRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class MomoServiceImpl implements MomoService {

    private final MomoProperties momoProperties;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public MomoServiceImpl(MomoProperties momoProperties,
                           OrderService orderService,
                           OrderRepository orderRepository) {
        this.momoProperties = momoProperties;
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public MomoPaymentResult createPayment(CheckoutRequest checkoutRequest, HttpSession session) {
        validateCheckoutRequest(checkoutRequest);

        Order pendingOrder = orderService.createOrderFromCart(checkoutRequest, session);

        String requestId = "REQ_" + System.currentTimeMillis();
        String momoOrderId = pendingOrder.getOrderCode();
        String amount = pendingOrder.getTotalAmount()
                .setScale(0, RoundingMode.HALF_UP)
                .toPlainString();

        String extraData = buildExtraData(pendingOrder);
        String orderInfo = "Thanh toan don hang " + pendingOrder.getOrderCode();

        String requestType = momoProperties.getRequestType();
        if (isBlank(requestType)) {
            requestType = "payWithMethod";
        }

        String rawSignature = "accessKey=" + momoProperties.getAccessKey()
                + "&amount=" + amount
                + "&extraData=" + extraData
                + "&ipnUrl=" + momoProperties.getIpnUrl()
                + "&orderId=" + momoOrderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + momoProperties.getPartnerCode()
                + "&redirectUrl=" + momoProperties.getRedirectUrl()
                + "&requestId=" + requestId
                + "&requestType=" + requestType;

        String signature = signHmacSHA256(rawSignature, momoProperties.getSecretKey());

        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("partnerCode", momoProperties.getPartnerCode());
            payload.put("accessKey", momoProperties.getAccessKey());
            payload.put("requestId", requestId);
            payload.put("amount", Long.parseLong(amount));
            payload.put("orderId", momoOrderId);
            payload.put("orderInfo", orderInfo);
            payload.put("redirectUrl", momoProperties.getRedirectUrl());
            payload.put("ipnUrl", momoProperties.getIpnUrl());
            payload.put("extraData", extraData);
            payload.put("requestType", requestType);
            payload.put("lang", momoProperties.getLang());
            payload.put("autoCapture", true);
            payload.put("signature", signature);

            String body = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(momoProperties.getEndpoint()))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Map<?, ?> data = objectMapper.readValue(response.body(), Map.class);

            Integer resultCode = toInteger(data.get("resultCode"));
            String message = toStringValue(data.get("message"));
            String payUrl = toStringValue(data.get("payUrl"));
            String qrCodeUrl = toStringValue(data.get("qrCodeUrl"));
            String deeplink = toStringValue(data.get("deeplink"));

            System.out.println(">>> MOMO create request = " + body);
            System.out.println(">>> MOMO create response = " + response.body());

            if (resultCode == null || resultCode != 0) {
                orderService.failOrder(pendingOrder.getId(), null);
                throw new IllegalStateException("MoMo trả lỗi: " + message);
            }

            if (isBlank(payUrl) && isBlank(qrCodeUrl)) {
                orderService.failOrder(pendingOrder.getId(), null);
                throw new IllegalStateException("MoMo không trả về payUrl hợp lệ.");
            }

            MomoPaymentResult result = new MomoPaymentResult();
            result.setSuccess(true);
            result.setPayUrl(payUrl);
            result.setQrCodeUrl(qrCodeUrl);
            result.setDeeplink(deeplink);
            result.setMessage(message);
            result.setOrderId(pendingOrder.getId());
            return result;
        } catch (Exception e) {
            orderService.failOrder(pendingOrder.getId(), null);
            throw new IllegalStateException("Không thể tạo thanh toán MoMo: " + e.getMessage(), e);
        }
    }

    @Override
    public Order handleReturn(Map<String, String> params, HttpSession session) {
        return processCallback(params, session);
    }

    @Override
    public Order handleIpn(Map<String, String> params) {
        return processCallback(params, null);
    }

    private Order processCallback(Map<String, String> params, HttpSession session) {
        String momoOrderId = params.get("orderId");
        if (isBlank(momoOrderId)) {
            return null;
        }

        Order order = orderRepository.findByOrderCode(momoOrderId).orElse(null);
        if (order == null) {
            return null;
        }

        Map<String, String> verifiedParams = params;

        if (!isValidCallbackSignature(params)) {
            Map<String, String> queried = queryTransaction(momoOrderId);
            if (queried != null && !queried.isEmpty()) {
                verifiedParams = queried;
            } else {
                return null;
            }
        }

        Integer resultCode = toInteger(verifiedParams.get("resultCode"));
        String transId = toStringValue(verifiedParams.get("transId"));

        if (resultCode != null && resultCode == 0) {
            return orderService.finalizePaidOrder(order.getId(), transId, session);
        }

        orderService.failOrder(order.getId(), transId);
        return null;
    }

    private Map<String, String> queryTransaction(String momoOrderId) {
        try {
            if (isBlank(momoProperties.getQueryEndpoint())) {
                return null;
            }

            String requestId = "QUERY_" + System.currentTimeMillis();

            String rawSignature = "accessKey=" + momoProperties.getAccessKey()
                    + "&orderId=" + momoOrderId
                    + "&partnerCode=" + momoProperties.getPartnerCode()
                    + "&requestId=" + requestId;

            String signature = signHmacSHA256(rawSignature, momoProperties.getSecretKey());

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("partnerCode", momoProperties.getPartnerCode());
            payload.put("requestId", requestId);
            payload.put("orderId", momoOrderId);
            payload.put("lang", momoProperties.getLang());
            payload.put("signature", signature);

            String body = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(momoProperties.getQueryEndpoint()))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(35))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Map<?, ?> data = objectMapper.readValue(response.body(), Map.class);

            System.out.println(">>> MOMO query request = " + body);
            System.out.println(">>> MOMO query response = " + response.body());

            Map<String, String> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : data.entrySet()) {
                result.put(String.valueOf(entry.getKey()),
                        entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
            }
            return result;
        } catch (Exception e) {
            System.out.println(">>> MOMO query error = " + e.getMessage());
            return null;
        }
    }

    private boolean isValidCallbackSignature(Map<String, String> params) {
        String signature = params.get("signature");
        if (isBlank(signature)) {
            return false;
        }

        String rawSignature = "amount=" + blank(params.get("amount"))
                + "&extraData=" + blank(params.get("extraData"))
                + "&message=" + blank(params.get("message"))
                + "&orderId=" + blank(params.get("orderId"))
                + "&orderInfo=" + blank(params.get("orderInfo"))
                + "&orderType=" + blank(params.get("orderType"))
                + "&partnerCode=" + blank(params.get("partnerCode"))
                + "&payType=" + blank(params.get("payType"))
                + "&requestId=" + blank(params.get("requestId"))
                + "&responseTime=" + blank(params.get("responseTime"))
                + "&resultCode=" + blank(params.get("resultCode"))
                + "&transId=" + blank(params.get("transId"));

        String expected = signHmacSHA256(rawSignature, momoProperties.getSecretKey());
        return expected.equals(signature);
    }

    private String buildExtraData(Order order) {
        try {
            Map<String, Object> extra = new LinkedHashMap<>();
            extra.put("localOrderId", order.getId());
            extra.put("orderCode", order.getOrderCode());

            return Base64.getEncoder().encodeToString(
                    objectMapper.writeValueAsBytes(extra)
            );
        } catch (Exception e) {
            throw new IllegalStateException("Không thể tạo extraData cho MoMo.", e);
        }
    }

    private void validateCheckoutRequest(CheckoutRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Thông tin đặt hàng không hợp lệ.");
        }
        if (isBlank(request.getCustomerName())) {
            throw new IllegalArgumentException("Vui lòng nhập họ tên người nhận.");
        }
        if (isBlank(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Vui lòng nhập số điện thoại.");
        }
        if (isBlank(request.getAddress())) {
            throw new IllegalArgumentException("Vui lòng nhập địa chỉ giao hàng.");
        }
        if (request.getPaymentMethod() != PaymentMethod.MOMO) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ cho MoMo.");
        }
    }

    private String signHmacSHA256(String data, String secretKey) {
        try {
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            hmacSHA256.init(secretKeySpec);
            byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder result = new StringBuilder();
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Không thể tạo chữ ký MoMo.", e);
        }
    }

    private Integer toInteger(Object value) {
        try {
            if (value == null) {
                return null;
            }
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private String toStringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String blank(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}