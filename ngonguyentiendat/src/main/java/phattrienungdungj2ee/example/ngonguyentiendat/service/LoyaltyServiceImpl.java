package phattrienungdungj2ee.example.ngonguyentiendat.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import phattrienungdungj2ee.example.ngonguyentiendat.model.LoyaltyRedemption;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Order;
import phattrienungdungj2ee.example.ngonguyentiendat.repository.LoyaltyRedemptionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class LoyaltyServiceImpl implements LoyaltyService {
    private final LoyaltyRedemptionRepository loyaltyRedemptionRepository;
    private final UserAccountService userAccountService;

    public LoyaltyServiceImpl(LoyaltyRedemptionRepository loyaltyRedemptionRepository, UserAccountService userAccountService) {
        this.loyaltyRedemptionRepository = loyaltyRedemptionRepository;
        this.userAccountService = userAccountService;
    }

    @Override
    public List<LoyaltyRedemption> getHistory(Long userId) {
        return userId == null ? List.of() : loyaltyRedemptionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public BigDecimal estimateRedeemValue(int points) {
        if (points == REDEEM_OPTION_10_PERCENT) {
            return BigDecimal.TEN;
        }
        if (points == REDEEM_OPTION_20_PERCENT) {
            return BigDecimal.valueOf(20);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public LoyaltyRedemption redeemAfterOtp(Long userId, int points) {
        return userAccountService.redeemPoints(userId, points, "Đổi điểm tích lũy thành mã khuyến mãi qua OTP email");
    }

    @Override
    public boolean isSupportedRedeemPoints(int points) {
        return points == REDEEM_OPTION_10_PERCENT || points == REDEEM_OPTION_20_PERCENT;
    }

    @Override
    public LoyaltyRedemption findAvailableVoucher(Long userId, String couponCode) {
        if (userId == null || couponCode == null || couponCode.isBlank()) {
            return null;
        }
        LoyaltyRedemption redemption = loyaltyRedemptionRepository
                .findByUserIdAndCouponCodeIgnoreCase(userId, couponCode.trim())
                .orElseGet(() -> findLegacyVoucher(userId, couponCode));

        if (redemption == null) {
            return null;
        }
        if (redemption.isUsed()) {
            throw new IllegalStateException("Voucher đã được sử dụng.");
        }
        return redemption;
    }

    @Override
    public BigDecimal calculateVoucherDiscount(BigDecimal baseAmount, LoyaltyRedemption redemption) {
        if (baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) <= 0 || redemption == null) {
            return BigDecimal.ZERO;
        }
        int percent = redemption.getDiscountPercent();
        if (percent <= 0) {
            percent = redemption.getValueAmount() == null ? 0 : redemption.getValueAmount().intValue();
        }
        if (percent <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount = baseAmount.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100));
        return discount.compareTo(baseAmount) > 0 ? baseAmount : discount;
    }

    @Override
    @Transactional
    public void markVoucherUsed(LoyaltyRedemption redemption, Order order) {
        if (redemption == null || redemption.isUsed()) {
            return;
        }
        redemption.setUsed(true);
        redemption.setUsedAt(LocalDateTime.now());
        redemption.setUsedOrder(order);
        redemption.setStatus("USED");
        loyaltyRedemptionRepository.save(redemption);
    }

    private LoyaltyRedemption findLegacyVoucher(Long userId, String couponCode) {
        String normalized = normalizeCode(couponCode);
        if (normalized == null) {
            return null;
        }
        return getHistory(userId).stream()
                .filter(item -> !item.isUsed())
                .filter(item -> normalized.equals(normalizeCode(extractCouponCode(item))))
                .findFirst()
                .orElse(null);
    }

    private String extractCouponCode(LoyaltyRedemption item) {
        if (item.getCouponCode() != null && !item.getCouponCode().isBlank()) {
            return item.getCouponCode();
        }
        String note = item.getNote();
        if (note == null || note.isBlank()) {
            return null;
        }
        int index = note.lastIndexOf(':');
        return index >= 0 && index < note.length() - 1 ? note.substring(index + 1).trim() : note.trim();
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase(Locale.ROOT);
    }
}
