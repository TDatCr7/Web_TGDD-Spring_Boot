package phattrienungdungj2ee.example.ngonguyentiendat.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.CartLinePricing;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.CartPricingResult;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.CheckoutRequest;
import phattrienungdungj2ee.example.ngonguyentiendat.model.AppUser;
import phattrienungdungj2ee.example.ngonguyentiendat.model.LoyaltyRedemption;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Order;
import phattrienungdungj2ee.example.ngonguyentiendat.model.OrderDetail;
import phattrienungdungj2ee.example.ngonguyentiendat.model.OrderStatus;
import phattrienungdungj2ee.example.ngonguyentiendat.model.PaymentMethod;
import phattrienungdungj2ee.example.ngonguyentiendat.model.PaymentStatus;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Product;
import phattrienungdungj2ee.example.ngonguyentiendat.repository.AppUserRepository;
import phattrienungdungj2ee.example.ngonguyentiendat.repository.OrderRepository;
import phattrienungdungj2ee.example.ngonguyentiendat.repository.ProductRepository;
import phattrienungdungj2ee.example.ngonguyentiendat.security.CurrentUser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final AppUserRepository appUserRepository;
    private final UserAccountService userAccountService;
    private final LoyaltyService loyaltyService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductRepository productRepository,
                            CartService cartService,
                            AppUserRepository appUserRepository,
                            UserAccountService userAccountService,
                            LoyaltyService loyaltyService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
        this.appUserRepository = appUserRepository;
        this.userAccountService = userAccountService;
        this.loyaltyService = loyaltyService;
    }

    @Override
    @Transactional
    public Order createOrderFromCart(CheckoutRequest request, HttpSession session) {
        validateCheckoutRequest(request);

        CartPricingResult pricing = cartService.calculateCart(
                session,
                defaultIfNull(request.getLoyaltyPointsToUse()),
                request.getVoucherCode()
        );

        if (pricing.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng đang trống.");
        }

        Order savedOrder = createOrderEntity(request, pricing, null,
                request.getPaymentMethod() == PaymentMethod.MOMO ? PaymentStatus.PENDING : PaymentStatus.UNPAID,
                request.getPaymentMethod() == PaymentMethod.MOMO ? OrderStatus.PENDING_PAYMENT : OrderStatus.PROCESSING);

        if (savedOrder.getPaymentMethod() != PaymentMethod.MOMO) {
            deductPromotionStock(savedOrder);
            cartService.clearCart(session);
        }

        return savedOrder;
    }

    @Override
    @Transactional
    public Order createPaidOrderFromCheckout(CheckoutRequest request,
                                             CartPricingResult pricing,
                                             String paymentReference,
                                             HttpSession session) {
        validateCheckoutRequest(request);
        if (pricing == null || pricing.isEmpty()) {
            throw new IllegalStateException("Không có dữ liệu giỏ hàng để tạo đơn MoMo.");
        }

        Order order = createOrderEntity(request, pricing, paymentReference, PaymentStatus.PAID, OrderStatus.PROCESSING);
        deductPromotionStock(order);
        applyBenefitsAfterSuccessfulPayment(order);
        if (session != null) {
            cartService.clearCart(session);
        }
        return order;
    }

    private Order createOrderEntity(CheckoutRequest request,
                                    CartPricingResult pricing,
                                    String paymentReference,
                                    PaymentStatus paymentStatus,
                                    OrderStatus orderStatus) {
        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setCustomerName(request.getCustomerName().trim());
        order.setEmail(blankToNull(request.getEmail()));
        order.setPhoneNumber(request.getPhoneNumber().trim());
        order.setAddress(request.getAddress().trim());
        order.setNotes(blankToNull(request.getNotes()));
        order.setPaymentMethod(request.getPaymentMethod() == null ? PaymentMethod.COD : request.getPaymentMethod());
        order.setSubtotalAmount(pricing.getSubtotal());
        order.setShippingFee(pricing.getShippingFee());
        order.setLoyaltyPointsUsed(pricing.getLoyaltyPointsApplied());
        order.setLoyaltyDiscountAmount(pricing.getLoyaltyDiscountAmount());
        order.setVoucherCode(blankToNull(pricing.getVoucherCode()));
        order.setVoucherDiscountPercent(pricing.getVoucherDiscountPercent());
        order.setVoucherDiscountAmount(pricing.getVoucherDiscountAmount());
        order.setLoyaltyPointsEarned(pricing.getLoyaltyPointsEarned());
        order.setTotalQuantity(pricing.getTotalQuantity());
        order.setTotalAmount(pricing.getTotalAmount());
        order.setPaymentStatus(paymentStatus);
        order.setStatus(orderStatus);
        order.setPaymentReference(blankToNull(paymentReference));
        order.setUser(resolveAuthenticatedUser());

        for (CartLinePricing line : pricing.getItems()) {
            OrderDetail detail = new OrderDetail();
            detail.setProductId(line.getProduct().getId());
            detail.setProductName(line.getProduct().getName());
            detail.setProductSlug(line.getProduct().getSlug());
            detail.setProductThumbnailUrl(line.getProduct().getThumbnailUrl());
            detail.setQuantity(line.getQuantity());
            detail.setPromotionQuantity(line.getPromotionAppliedQuantity());
            detail.setRegularQuantity(line.getRegularAppliedQuantity());
            detail.setPromotionUnitPrice(line.getPromotionUnitPrice());
            detail.setRegularUnitPrice(line.getRegularUnitPrice());
            detail.setLineAmount(line.getLineAmount());
            order.addOrderDetail(detail);
        }

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order finalizePaidOrder(Long orderId, String paymentReference, HttpSession session) {
        Order order = getRequiredOrder(orderId);

        if (order.getPaymentStatus() == PaymentStatus.PAID && order.getStatus() == OrderStatus.PROCESSING) {
            return order;
        }

        if (order.getPaymentMethod() != PaymentMethod.MOMO) {
            return order;
        }

        deductPromotionStock(order);

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setStatus(OrderStatus.PROCESSING);
        order.setPaymentReference(blankToNull(paymentReference));

        Order saved = orderRepository.save(order);
        applyBenefitsAfterSuccessfulPayment(saved);

        if (session != null) {
            cartService.clearCart(session);
        }

        return saved;
    }

    @Override
    @Transactional
    public Order failOrder(Long orderId, String paymentReference) {
        Order order = getRequiredOrder(orderId);

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return order;
        }

        order.setPaymentStatus(PaymentStatus.FAILED);
        order.setStatus(OrderStatus.FAILED);
        order.setPaymentReference(blankToNull(paymentReference));
        order.setUser(resolveAuthenticatedUser());
        return orderRepository.save(order);
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public Order getOrderForView(Long id) {
        return orderRepository.findWithOrderDetailsById(id).orElse(null);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional
    public void updatePaymentStatus(Long orderId, PaymentStatus paymentStatus) {
        Order order = getRequiredOrder(orderId);
        order.setPaymentStatus(paymentStatus);
        orderRepository.save(order);
    }

    private void applyBenefitsAfterSuccessfulPayment(Order order) {
        if (order.getUser() != null && !isBlank(order.getVoucherCode())) {
            LoyaltyRedemption redemption = loyaltyService.findAvailableVoucher(order.getUser().getId(), order.getVoucherCode());
            if (redemption != null) {
                loyaltyService.markVoucherUsed(redemption, order);
            }
        }
    }

    private void deductPromotionStock(Order order) {
        for (OrderDetail detail : order.getOrderDetails()) {
            if (detail.getProductId() == null) {
                continue;
            }

            Product product = productRepository.findByIdForUpdate(detail.getProductId())
                    .orElseThrow(() -> new IllegalStateException("Sản phẩm không còn tồn tại để trừ suất khuyến mãi."));

            int promotionQuantity = resolvePromotionQuantity(detail, product);
            if (promotionQuantity <= 0) {
                continue;
            }

            int currentPromotionStock = product.getPromotionStock() == null ? 0 : product.getPromotionStock();
            if (currentPromotionStock < promotionQuantity) {
                throw new IllegalStateException(
                        "Sản phẩm '" + product.getName() + "' chỉ còn " + currentPromotionStock + " suất khuyến mãi."
                );
            }

            int remainingPromotionStock = currentPromotionStock - promotionQuantity;

            if (remainingPromotionStock > 0) {
                product.setPromotionStock(remainingPromotionStock);
            } else {
                product.setPromotionStock(null);
                if (product.isOnlinePromotion()) {
                    product.setPrice(product.getOriginalPrice());
                    product.setPromotionType("NONE");
                    product.setDiscountPercent(0);
                }
            }

            productRepository.saveAndFlush(product);
        }
    }

    private int resolvePromotionQuantity(OrderDetail detail, Product product) {
        if (detail.getPromotionQuantity() != null && detail.getPromotionQuantity() > 0) {
            return detail.getPromotionQuantity();
        }

        boolean flashSaleProduct = product.getPromotionType() != null
                && "FLASHSALE".equalsIgnoreCase(product.getPromotionType());
        if (!flashSaleProduct) {
            return 0;
        }

        return detail.getQuantity() == null ? 0 : Math.max(0, detail.getQuantity());
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
    }

    private Order getRequiredOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng."));
    }

    private String generateOrderCode() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    private AppUser resolveAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null || !(authentication.getPrincipal() instanceof CurrentUser currentUser)) {
            return null;
        }
        return appUserRepository.findById(currentUser.getId()).orElse(null);
    }

    private String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private int defaultIfNull(Integer value) {
        return value == null ? 0 : value;
    }
}
