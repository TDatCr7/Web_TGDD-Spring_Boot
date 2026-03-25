package phattrienungdungj2ee.example.ngonguyentiendat.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.CartItem;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.CartLinePricing;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.CartPricingResult;
import phattrienungdungj2ee.example.ngonguyentiendat.model.LoyaltyRedemption;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Product;
import phattrienungdungj2ee.example.ngonguyentiendat.repository.AppUserRepository;
import phattrienungdungj2ee.example.ngonguyentiendat.repository.ProductRepository;

import phattrienungdungj2ee.example.ngonguyentiendat.security.CurrentUser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    public static final String CART_SESSION_KEY = "SHOPPING_CART";
    public static final BigDecimal FREE_SHIP_MIN_SUBTOTAL = new BigDecimal("1000000");
    public static final int FREE_SHIP_MIN_QUANTITY = 2;
    public static final BigDecimal STANDARD_SHIPPING_FEE = new BigDecimal("30000");
    public static final int EARN_RATE_VND_PER_POINT = 7500;
    public static final int REDEEM_POINTS_STEP = 2;
    public static final BigDecimal REDEEM_VALUE_PER_STEP = new BigDecimal("15000");

    private final ProductRepository productRepository;
    private final AppUserRepository appUserRepository;
    private final LoyaltyService loyaltyService;

    public CartServiceImpl(ProductRepository productRepository, AppUserRepository appUserRepository, LoyaltyService loyaltyService) {
        this.productRepository = productRepository;
        this.appUserRepository = appUserRepository;
        this.loyaltyService = loyaltyService;
    }

    @Override
    public List<CartItem> getCartItems(HttpSession session) {
        Map<Long, Integer> rawCart = getCartMap(session);
        List<CartItem> items = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : rawCart.entrySet()) {
            items.add(new CartItem(entry.getKey(), entry.getValue()));
        }
        return items;
    }

    @Override
    public void addToCart(HttpSession session, Long productId, int quantity) {
        if (productId == null || quantity <= 0) {
            return;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm."));

        Map<Long, Integer> cart = getCartMap(session);
        int updatedQty = cart.getOrDefault(productId, 0) + quantity;
        validateRequestedQuantity(product, updatedQty);
        cart.put(productId, updatedQty);
        saveCartMap(session, cart);
    }

    @Override
    public void updateItemQuantity(HttpSession session, Long productId, int quantity) {
        Map<Long, Integer> cart = getCartMap(session);
        if (productId == null || !cart.containsKey(productId)) {
            return;
        }

        if (quantity <= 0) {
            cart.remove(productId);
            saveCartMap(session, cart);
            return;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm."));

        validateRequestedQuantity(product, quantity);
        cart.put(productId, quantity);
        saveCartMap(session, cart);
    }

    @Override
    public void removeItem(HttpSession session, Long productId) {
        Map<Long, Integer> cart = getCartMap(session);
        cart.remove(productId);
        saveCartMap(session, cart);
    }

    @Override
    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    @Override
    public int getTotalQuantity(HttpSession session) {
        return getCartMap(session).values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public CartPricingResult calculateCart(HttpSession session, int loyaltyPointsToUse, String voucherCode) {
        Map<Long, Integer> cartMap = getCartMap(session);
        if (cartMap.isEmpty()) {
            return new CartPricingResult();
        }

        List<Product> products = productRepository.findAllById(cartMap.keySet());
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        CartPricingResult result = new CartPricingResult();
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalQuantity = 0;
        List<CartLinePricing> lines = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : cartMap.entrySet()) {
            Product product = productMap.get(entry.getKey());
            if (product == null) {
                continue;
            }

            int quantity = Math.max(0, entry.getValue());
            if (quantity == 0) {
                continue;
            }

            validateRequestedQuantity(product, quantity);

            CartLinePricing line = buildLinePricing(product, quantity);
            lines.add(line);
            subtotal = subtotal.add(line.getLineAmount());
            totalQuantity += quantity;
        }

        result.setItems(lines);
        result.setSubtotal(subtotal);
        result.setTotalQuantity(totalQuantity);

        BigDecimal shippingFee = calculateShippingFee(subtotal, totalQuantity);
        result.setShippingFee(shippingFee);

        int sanitizedRequestedPoints = Math.max(0, Math.min(loyaltyPointsToUse, resolveCurrentUserPoints()));
        result.setLoyaltyPointsRequested(sanitizedRequestedPoints);

        int applicablePoints = sanitizeApplicablePoints(sanitizedRequestedPoints);
        BigDecimal loyaltyDiscount = calculateLoyaltyDiscount(applicablePoints);
        BigDecimal maxDiscount = subtotal.add(shippingFee);

        if (loyaltyDiscount.compareTo(maxDiscount) > 0) {
            applicablePoints = adjustPointsToCap(maxDiscount);
            loyaltyDiscount = calculateLoyaltyDiscount(applicablePoints);
        }

        result.setLoyaltyPointsApplied(applicablePoints);
        result.setLoyaltyDiscountAmount(loyaltyDiscount);

        BigDecimal amountAfterPoints = subtotal.add(shippingFee).subtract(loyaltyDiscount);
        if (amountAfterPoints.compareTo(BigDecimal.ZERO) < 0) {
            amountAfterPoints = BigDecimal.ZERO;
        }

        LoyaltyRedemption voucher = resolveVoucher(voucherCode);
        BigDecimal voucherDiscount = loyaltyService.calculateVoucherDiscount(amountAfterPoints, voucher);
        result.setVoucherCode(voucher == null ? null : voucher.getCouponCode());
        result.setVoucherDiscountPercent(voucher == null ? 0 : voucher.getDiscountPercent());
        result.setVoucherDiscountAmount(voucherDiscount);

        BigDecimal totalAmount = amountAfterPoints.subtract(voucherDiscount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        result.setTotalAmount(totalAmount);
        result.setLoyaltyPointsEarned(calculateEarnedPoints(subtotal));

        return result;
    }

    public static BigDecimal calculateShippingFee(BigDecimal subtotal, int totalQuantity) {
        if (subtotal == null) {
            subtotal = BigDecimal.ZERO;
        }
        boolean freeShip = subtotal.compareTo(FREE_SHIP_MIN_SUBTOTAL) >= 0
                && totalQuantity >= FREE_SHIP_MIN_QUANTITY;
        return freeShip ? BigDecimal.ZERO : STANDARD_SHIPPING_FEE;
    }

    public static int calculateEarnedPoints(BigDecimal paidAmount) {
        if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return paidAmount.divideToIntegralValue(BigDecimal.valueOf(EARN_RATE_VND_PER_POINT)).intValue();
    }

    public static BigDecimal calculateLoyaltyDiscount(int appliedPoints) {
        if (appliedPoints <= 0) {
            return BigDecimal.ZERO;
        }
        int steps = appliedPoints / REDEEM_POINTS_STEP;
        return REDEEM_VALUE_PER_STEP.multiply(BigDecimal.valueOf(steps));
    }

    private int sanitizeApplicablePoints(int requestedPoints) {
        if (requestedPoints <= 0) {
            return 0;
        }
        return requestedPoints - (requestedPoints % REDEEM_POINTS_STEP);
    }

    private int adjustPointsToCap(BigDecimal maxDiscount) {
        if (maxDiscount == null || maxDiscount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        int steps = maxDiscount.divideToIntegralValue(REDEEM_VALUE_PER_STEP).intValue();
        return steps * REDEEM_POINTS_STEP;
    }

    private CartLinePricing buildLinePricing(Product product, int quantity) {
        CartLinePricing line = new CartLinePricing();
        line.setProduct(product);
        line.setQuantity(quantity);

        boolean usesPromotionStock = product.isOnlinePromotion() && product.getPromotionStock() != null;
        int promotionAppliedQuantity = 0;
        if (usesPromotionStock) {
            promotionAppliedQuantity = Math.min(quantity, Math.max(0, product.getPromotionStock()));
        }

        int regularAppliedQuantity = Math.max(0, quantity - promotionAppliedQuantity);
        BigDecimal promotionUnitPrice = safeMoney(product.getPrice());
        BigDecimal regularUnitPrice = product.hasDiscount()
                ? safeMoney(product.getOriginalPrice())
                : safeMoney(product.getPrice());

        BigDecimal lineAmount = promotionUnitPrice.multiply(BigDecimal.valueOf(promotionAppliedQuantity))
                .add(regularUnitPrice.multiply(BigDecimal.valueOf(regularAppliedQuantity)));

        line.setPromotionAppliedQuantity(promotionAppliedQuantity);
        line.setRegularAppliedQuantity(regularAppliedQuantity);
        line.setPromotionUnitPrice(promotionUnitPrice);
        line.setRegularUnitPrice(regularUnitPrice);
        line.setLineAmount(lineAmount);

        return line;
    }

    private void validateRequestedQuantity(Product product, int requestedQty) {
        if (requestedQty <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
        }

        if (product.getOriginalPrice() == null || product.getPrice() == null) {
            throw new IllegalArgumentException("Sản phẩm chưa có cấu hình giá hợp lệ.");
        }

        if (product.isOnlinePromotion()) {
            int availablePromotionStock = product.getPromotionStock() == null ? 0 : Math.max(0, product.getPromotionStock());
            if (availablePromotionStock <= 0) {
                throw new IllegalArgumentException("Sản phẩm flash sale đã hết suất khuyến mãi.");
            }
            if (requestedQty > availablePromotionStock) {
                throw new IllegalArgumentException("Sản phẩm chỉ còn " + availablePromotionStock + " suất flash sale.");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getCartMap(HttpSession session) {
        Object rawCart = session.getAttribute(CART_SESSION_KEY);
        if (rawCart instanceof Map<?, ?> map) {
            Map<Long, Integer> cart = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() instanceof Long key
                        && entry.getValue() instanceof Integer value
                        && value > 0) {
                    cart.put(key, value);
                }
            }
            return cart;
        }
        return new LinkedHashMap<>();
    }

    private void saveCartMap(HttpSession session, Map<Long, Integer> cart) {
        session.setAttribute(CART_SESSION_KEY, new LinkedHashMap<>(cart));
    }


    private LoyaltyRedemption resolveVoucher(String voucherCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUser currentUser)) {
            return null;
        }
        return loyaltyService.findAvailableVoucher(currentUser.getId(), voucherCode);
    }

    private int resolveCurrentUserPoints() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUser currentUser)) {
            return 0;
        }
        return appUserRepository.findById(currentUser.getId())
                .map(user -> user.getLoyaltyPoints() == null ? 0 : user.getLoyaltyPoints())
                .orElse(0);
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}