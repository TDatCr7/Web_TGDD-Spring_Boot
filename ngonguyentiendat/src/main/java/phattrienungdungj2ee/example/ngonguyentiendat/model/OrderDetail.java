package phattrienungdungj2ee.example.ngonguyentiendat.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_details")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name", length = 255)
    private String productName;

    @Column(name = "product_slug", length = 255)
    private String productSlug;

    @Column(name = "product_thumbnail_url", length = 700)
    private String productThumbnailUrl;

    @Column
    private Integer quantity = 0;

    @Column(name = "promotion_quantity")
    private Integer promotionQuantity = 0;

    @Column(name = "regular_quantity")
    private Integer regularQuantity = 0;

    @Column(name = "promotion_unit_price", precision = 15, scale = 2)
    private BigDecimal promotionUnitPrice = BigDecimal.ZERO;

    @Column(name = "regular_unit_price", precision = 15, scale = 2)
    private BigDecimal regularUnitPrice = BigDecimal.ZERO;

    @Column(name = "line_amount", precision = 15, scale = 2)
    private BigDecimal lineAmount = BigDecimal.ZERO;

    @PrePersist
    @PreUpdate
    public void normalizeDefaults() {
        if (quantity == null) quantity = 0;
        if (promotionQuantity == null) promotionQuantity = 0;
        if (regularQuantity == null) regularQuantity = 0;
        if (promotionUnitPrice == null) promotionUnitPrice = BigDecimal.ZERO;
        if (regularUnitPrice == null) regularUnitPrice = BigDecimal.ZERO;
        if (lineAmount == null) lineAmount = BigDecimal.ZERO;
    }

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductSlug() {
        return productSlug;
    }

    public String getProductThumbnailUrl() {
        return productThumbnailUrl;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getPromotionQuantity() {
        return promotionQuantity;
    }

    public Integer getRegularQuantity() {
        return regularQuantity;
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setProductSlug(String productSlug) {
        this.productSlug = productSlug;
    }

    public void setProductThumbnailUrl(String productThumbnailUrl) {
        this.productThumbnailUrl = productThumbnailUrl;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setPromotionQuantity(Integer promotionQuantity) {
        this.promotionQuantity = promotionQuantity;
    }

    public void setRegularQuantity(Integer regularQuantity) {
        this.regularQuantity = regularQuantity;
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