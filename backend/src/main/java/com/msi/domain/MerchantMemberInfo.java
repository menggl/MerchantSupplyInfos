package com.msi.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "merchant_member_info")
public class MerchantMemberInfo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "merchant_id")
  private Long merchantId;

  @Column(name = "start_date")
  private LocalDateTime startDate;

  @Column(name = "end_date")
  private LocalDateTime endDate;

  @Column(name = "member_type")
  private Integer memberType;

  @Column(name = "payment_amount")
  private BigDecimal paymentAmount;

  @Column(name = "original_price")
  private BigDecimal originalPrice;

  @Column(name = "discount_price")
  private BigDecimal discountPrice;

  @Column(name = "commission")
  private BigDecimal commission;

  @Column(name = "is_valid")
  private Integer isValid;

  @Column(name = "create_time", updatable = false)
  private LocalDateTime createTime;

  @Column(name = "update_time")
  private LocalDateTime updateTime;

  @PrePersist
  protected void onCreate() {
    createTime = LocalDateTime.now();
    updateTime = LocalDateTime.now();
    if (isValid == null) {
      isValid = 1;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updateTime = LocalDateTime.now();
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
  public LocalDateTime getStartDate() {
    return startDate;
  }
  public void setStartDate(LocalDateTime startDate) {
    this.startDate = startDate;
  }
  public LocalDateTime getEndDate() {
    return endDate;
  }
  public void setEndDate(LocalDateTime endDate) {
    this.endDate = endDate;
  }
  public Integer getMemberType() {
    return memberType;
  }
  public void setMemberType(Integer memberType) {
    this.memberType = memberType;
  }
  public BigDecimal getPaymentAmount() {
    return paymentAmount;
  }
  public void setPaymentAmount(BigDecimal paymentAmount) {
    this.paymentAmount = paymentAmount;
  }
  public BigDecimal getOriginalPrice() {
    return originalPrice;
  }
  public void setOriginalPrice(BigDecimal originalPrice) {
    this.originalPrice = originalPrice;
  }
  public BigDecimal getDiscountPrice() {
    return discountPrice;
  }
  public void setDiscountPrice(BigDecimal discountPrice) {
    this.discountPrice = discountPrice;
  }
  public BigDecimal getCommission() {
    return commission;
  }
  public void setCommission(BigDecimal commission) {
    this.commission = commission;
  }
  public Integer getIsValid() {
    return isValid;
  }
  public void setIsValid(Integer isValid) {
    this.isValid = isValid;
  }
  public LocalDateTime getCreateTime() {
    return createTime;
  }
  public void setCreateTime(LocalDateTime createTime) {
    this.createTime = createTime;
  }
  public LocalDateTime getUpdateTime() {
    return updateTime;
  }
  public void setUpdateTime(LocalDateTime updateTime) {
    this.updateTime = updateTime;
  }
}
