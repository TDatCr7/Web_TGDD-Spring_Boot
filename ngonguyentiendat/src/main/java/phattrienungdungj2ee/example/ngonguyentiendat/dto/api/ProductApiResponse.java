package phattrienungdungj2ee.example.ngonguyentiendat.dto.api;

import phattrienungdungj2ee.example.ngonguyentiendat.model.Product;

import java.math.BigDecimal;

public class ProductApiResponse {

    private Long id;
    private String name;
    private String slug;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String description;
    private String thumbnailUrl;
    private String promotionType;
    private Integer promotionStock;
    private Integer discountPercent;
    private BigDecimal rating;
    private String stockStatus;
    private Long categoryId;
    private String categoryName;

    public static ProductApiResponse from(Product product) {
        ProductApiResponse response = new ProductApiResponse();
        response.id = product.getId();
        response.name = product.getName();
        response.slug = product.getSlug();
        response.price = product.getPrice();
        response.originalPrice = product.getOriginalPrice();
        response.description = product.getDescription();
        response.thumbnailUrl = product.getThumbnailUrl();
        response.promotionType = product.getPromotionType();
        response.promotionStock = product.getPromotionStock();
        response.discountPercent = product.getDiscountPercent();
        response.rating = product.getRating();
        response.stockStatus = product.getStockStatus() != null ? product.getStockStatus().name() : null;
        response.categoryId = product.getCategory() != null ? product.getCategory().getId() : null;
        response.categoryName = product.getCategory() != null ? product.getCategory().getName() : null;
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getPromotionType() {
        return promotionType;
    }

    public Integer getPromotionStock() {
        return promotionStock;
    }

    public Integer getDiscountPercent() {
        return discountPercent;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }
}