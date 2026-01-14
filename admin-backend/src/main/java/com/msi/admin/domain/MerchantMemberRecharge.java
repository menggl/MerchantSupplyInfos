package com.msi.admin.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "merchant_member_recharge")
public class MerchantMemberRecharge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(name = "recharge_amount")
    private BigDecimal rechargeAmount;

    @Column(name = "original_price")
    private BigDecimal originalPrice;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "recharge_type")
    private Integer rechargeType; // 1: Month, 2: Year, 3: Lifetime

    @Column(name = "recharge_time")
    private LocalDateTime rechargeTime;

    @Column(name = "is_valid")
    private Integer isValid; // 1: Valid, 0: Invalid

    @PrePersist
    protected void onCreate() {
        if (rechargeTime == null) {
            rechargeTime = LocalDateTime.now();
        }
        if (isValid == null) {
            isValid = 1;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public BigDecimal getRechargeAmount() {
        return rechargeAmount;
    }

    public void setRechargeAmount(BigDecimal rechargeAmount) {
        this.rechargeAmount = rechargeAmount;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Integer getRechargeType() {
        return rechargeType;
    }

    public void setRechargeType(Integer rechargeType) {
        this.rechargeType = rechargeType;
    }

    public LocalDateTime getRechargeTime() {
        return rechargeTime;
    }

    public void setRechargeTime(LocalDateTime rechargeTime) {
        this.rechargeTime = rechargeTime;
    }

    public Integer getIsValid() {
        return isValid;
    }

    public void setIsValid(Integer isValid) {
        this.isValid = isValid;
    }
}
