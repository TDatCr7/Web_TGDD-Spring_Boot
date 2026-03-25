package phattrienungdungj2ee.example.ngonguyentiendat.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartPricingResult {
    private List<CartLinePricing> items = new ArrayList<>();
    private Integer totalQuantity = 0;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal shippingFee = BigDecimal.ZERO;
    private Integer loyaltyPointsRequested = 0;
    private Integer loyaltyPointsApplied = 0;
    private BigDecimal loyaltyDiscountAmount = BigDecimal.ZERO;
    private String voucherCode;
    private Integer voucherDiscountPercent = 0;
    private BigDecimal voucherDiscountAmount = BigDecimal.ZERO;
    private Integer loyaltyPointsEarned = 0;
    private BigDecimal totalAmount = BigDecimal.ZERO;

    public List<CartLinePricing> getItems() {
        return items;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public Integer getLoyaltyPointsRequested() {
        return loyaltyPointsRequested;
    }

    public Integer getLoyaltyPointsApplied() {
        return loyaltyPointsApplied;
    }

    public BigDecimal getLoyaltyDiscountAmount() {
        return loyaltyDiscountAmount;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public Integer getVoucherDiscountPercent() {
        return voucherDiscountPercent;
    }

    public BigDecimal getVoucherDiscountAmount() {
        return voucherDiscountAmount;
    }

    public Integer getLoyaltyPointsEarned() {
        return loyaltyPointsEarned;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setItems(List<CartLinePricing> items) {
        this.items = items;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public void setLoyaltyPointsRequested(Integer loyaltyPointsRequested) {
        this.loyaltyPointsRequested = loyaltyPointsRequested;
    }

    public void setLoyaltyPointsApplied(Integer loyaltyPointsApplied) {
        this.loyaltyPointsApplied = loyaltyPointsApplied;
    }

    public void setLoyaltyDiscountAmount(BigDecimal loyaltyDiscountAmount) {
        this.loyaltyDiscountAmount = loyaltyDiscountAmount;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public void setVoucherDiscountPercent(Integer voucherDiscountPercent) {
        this.voucherDiscountPercent = voucherDiscountPercent;
    }

    public void setVoucherDiscountAmount(BigDecimal voucherDiscountAmount) {
        this.voucherDiscountAmount = voucherDiscountAmount;
    }

    public void setLoyaltyPointsEarned(Integer loyaltyPointsEarned) {
        this.loyaltyPointsEarned = loyaltyPointsEarned;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
}