package phattrienungdungj2ee.example.ngonguyentiendat.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm tối đa 255 ký tự")
    @Column(nullable = false, length = 255)
    private String name;

    @Size(max = 255, message = "Slug tối đa 255 ký tự")
    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @NotNull(message = "Giá bán không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá bán phải lớn hơn 0")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Giá gốc không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá gốc phải lớn hơn 0")
    @Column(name = "original_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "thumbnail_url", length = 700)
    private String thumbnailUrl;

    @Column(name = "promotion_type", length = 30)
    private String promotionType = "NONE";

    @Column(name = "discount_percent")
    private Integer discountPercent = 0;

    @Column(name = "promotion_stock")
    private Integer promotionStock;

    @Column(name = "rating_value", precision = 3, scale = 1)
    private BigDecimal rating = new BigDecimal("4.9");

    @Enumerated(EnumType.STRING)
    @Column(name = "stock_status", nullable = false, length = 30)
    private ProductStockStatus stockStatus = ProductStockStatus.CON_HANG;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Product() {
    }

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        normalizeData();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        normalizeData();
    }

    private void normalizeData() {
        if (promotionType == null || promotionType.isBlank()) {
            promotionType = "NONE";
        } else {
            promotionType = promotionType.trim().toUpperCase();
        }

        if (originalPrice != null && price != null && price.compareTo(originalPrice) > 0) {
            price = originalPrice;
        }

        discountPercent = calculateDiscountPercent();

        if ("NONE".equals(promotionType)) {
            promotionStock = null;
        } else if (promotionStock != null && promotionStock <= 0) {
            promotionStock = null;
        }

        if (stockStatus == null) {
            stockStatus = ProductStockStatus.CON_HANG;
        }

        if (rating == null) {
            rating = new BigDecimal("4.9");
        }
    }

    private Integer calculateDiscountPercent() {
        if (originalPrice == null || price == null) return 0;
        if (originalPrice.compareTo(BigDecimal.ZERO) <= 0) return 0;
        if (price.compareTo(originalPrice) >= 0) return 0;

        BigDecimal discountAmount = originalPrice.subtract(price);
        BigDecimal percent = discountAmount
                .multiply(BigDecimal.valueOf(100))
                .divide(originalPrice, 0, RoundingMode.HALF_UP);

        return percent.intValue();
    }

    public boolean hasDiscount() {
        return originalPrice != null && price != null && price.compareTo(originalPrice) < 0;
    }

    public boolean isOnlinePromotion() {
        return promotionType != null && !"NONE".equalsIgnoreCase(promotionType);
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

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getPromotionType() {
        return promotionType;
    }

    public Integer getDiscountPercent() {
        return discountPercent;
    }

    public Integer getPromotionStock() {
        return promotionStock;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public ProductStockStatus getStockStatus() {
        return stockStatus;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setPromotionType(String promotionType) {
        this.promotionType = promotionType;
    }

    public void setDiscountPercent(Integer discountPercent) {
        this.discountPercent = discountPercent;
    }

    public void setPromotionStock(Integer promotionStock) {
        this.promotionStock = promotionStock;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public void setStockStatus(ProductStockStatus stockStatus) {
        this.stockStatus = stockStatus;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}