package phattrienungdungj2ee.example.ngonguyentiendat.dto;

import phattrienungdungj2ee.example.ngonguyentiendat.model.Product;

import java.math.BigDecimal;

public class CartLinePricing {
    private Product product;
    private Integer quantity;
    private Integer promotionAppliedQuantity;
    private Integer regularAppliedQuantity;
    private BigDecimal promotionUnitPrice;
    private BigDecimal regularUnitPrice;
    private BigDecimal lineAmount;

    public Product getProduct() {
        return product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getPromotionAppliedQuantity() {
        return promotionAppliedQuantity;
    }

    public Integer getRegularAppliedQuantity() {
        return regularAppliedQuantity;
    }

    public BigDecimal getPromotionUnitPrice() {
        return promotionUnitPrice;
    }

    public BigDecimal getRegularUnitPrice() {
        return regularUnitPrice;
    }

    public BigDecimal getLineAmount() {
        return lineAmount;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setPromotionAppliedQuantity(Integer promotionAppliedQuantity) {
        this.promotionAppliedQuantity = promotionAppliedQuantity;
    }

    public void setRegularAppliedQuantity(Integer regularAppliedQuantity) {
        this.regularAppliedQuantity = regularAppliedQuantity;
    }

    public void setPromotionUnitPrice(BigDecimal promotionUnitPrice) {
        this.promotionUnitPrice = promotionUnitPrice;
    }

    public void setRegularUnitPrice(BigDecimal regularUnitPrice) {
        this.regularUnitPrice = regularUnitPrice;
    }

    public void setLineAmount(BigDecimal lineAmount) {
        this.lineAmount = lineAmount;
    }
}