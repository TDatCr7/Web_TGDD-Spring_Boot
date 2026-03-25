package phattrienungdungj2ee.example.ngonguyentiendat.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_redemptions")
public class LoyaltyRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "points_redeemed", nullable = false)
    private Integer pointsRedeemed;

    @Column(name = "value_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal valueAmount;

    @Column(name = "coupon_code", length = 50, unique = true)
    private String couponCode;

    @Column(name = "discount_percent")
    private Integer discountPercent;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "used", nullable = false)
    private Boolean used;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_order_id")
    private Order usedOrder;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (used == null) used = false;
        if (discountPercent == null) discountPercent = 0;
        if (status == null || status.isBlank()) status = "SUCCESS";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }

    public Integer getPointsRedeemed() { return pointsRedeemed; }
    public void setPointsRedeemed(Integer pointsRedeemed) { this.pointsRedeemed = pointsRedeemed; }

    public BigDecimal getValueAmount() { return valueAmount; }
    public void setValueAmount(BigDecimal valueAmount) { this.valueAmount = valueAmount; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public Integer getDiscountPercent() { return discountPercent == null ? 0 : discountPercent; }
    public void setDiscountPercent(Integer discountPercent) { this.discountPercent = discountPercent; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isUsed() { return Boolean.TRUE.equals(used); }
    public Boolean getUsed() { return used; }
    public void setUsed(Boolean used) { this.used = used; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }

    public Order getUsedOrder() { return usedOrder; }
    public void setUsedOrder(Order usedOrder) { this.usedOrder = usedOrder; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}