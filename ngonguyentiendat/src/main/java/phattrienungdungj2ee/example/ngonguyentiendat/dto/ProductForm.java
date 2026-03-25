package phattrienungdungj2ee.example.ngonguyentiendat.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import phattrienungdungj2ee.example.ngonguyentiendat.model.ProductStockStatus;

import java.math.BigDecimal;

public class ProductForm {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm tối đa 255 ký tự")
    private String name;

    @Size(max = 255, message = "Slug tối đa 255 ký tự")
    private String slug;

    @NotNull(message = "Giá gốc không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá gốc phải lớn hơn 0")
    private BigDecimal originalPrice;

    @NotNull(message = "Giá bán không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá bán phải lớn hơn 0")
    private BigDecimal price;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    private String currentThumbnailUrl;

    private String uploadedImagePath;

    // NONE = không tham gia campaign online
    // FLASHSALE / ONLINE_ONLY / GIFT = tham gia campaign online
    private String promotionType = "NONE";

    private Integer promotionStock;

    @NotNull(message = "Tình trạng sản phẩm không được để trống")
    private ProductStockStatus stockStatus = ProductStockStatus.CON_HANG;

    private BigDecimal rating = new BigDecimal("4.9");

    private String description;

    public ProductForm() {
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCurrentThumbnailUrl() {
        return currentThumbnailUrl;
    }

    public String getUploadedImagePath() {
        return uploadedImagePath;
    }

    public String getPromotionType() {
        return promotionType;
    }

    public Integer getPromotionStock() {
        return promotionStock;
    }

    public ProductStockStatus getStockStatus() {
        return stockStatus;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setCurrentThumbnailUrl(String currentThumbnailUrl) {
        this.currentThumbnailUrl = currentThumbnailUrl;
    }

    public void setUploadedImagePath(String uploadedImagePath) {
        this.uploadedImagePath = uploadedImagePath;
    }

    public void setPromotionType(String promotionType) {
        this.promotionType = promotionType;
    }

    public void setPromotionStock(Integer promotionStock) {
        this.promotionStock = promotionStock;
    }

    public void setStockStatus(ProductStockStatus stockStatus) {
        this.stockStatus = stockStatus;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}