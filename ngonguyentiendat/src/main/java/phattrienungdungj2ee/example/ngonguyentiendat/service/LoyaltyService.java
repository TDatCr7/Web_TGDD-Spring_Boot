package phattrienungdungj2ee.example.ngonguyentiendat.service;

import phattrienungdungj2ee.example.ngonguyentiendat.model.LoyaltyRedemption;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Order;

import java.math.BigDecimal;
import java.util.List;

public interface LoyaltyService {
    int REDEEM_OPTION_10_PERCENT = 1000;
    int REDEEM_OPTION_20_PERCENT = 2000;

    List<LoyaltyRedemption> getHistory(Long userId);
    BigDecimal estimateRedeemValue(int points);
    LoyaltyRedemption redeemAfterOtp(Long userId, int points);
    boolean isSupportedRedeemPoints(int points);
    LoyaltyRedemption findAvailableVoucher(Long userId, String couponCode);
    java.math.BigDecimal calculateVoucherDiscount(java.math.BigDecimal baseAmount, LoyaltyRedemption redemption);
    void markVoucherUsed(LoyaltyRedemption redemption, Order order);
}
