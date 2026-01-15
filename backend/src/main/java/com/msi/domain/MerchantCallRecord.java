package com.msi.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "merchant_call_record")
public class MerchantCallRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "caller_merchant_id")
  private Long callerMerchantId;

  @Column(name = "callee_merchant_id")
  private Long calleeMerchantId;

  @Column(name = "product_id")
  private Long productId;

  @Column(name = "call_type")
  private Integer callType;

  @Column(name = "call_time")
  private LocalDateTime callTime;

  @Column(name = "create_time", updatable = false)
  private LocalDateTime createTime;

  @Column(name = "update_time")
  private LocalDateTime updateTime;

  @PrePersist
  protected void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    createTime = now;
    updateTime = now;
    if (callTime == null) {
      callTime = now;
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

  public Long getCallerMerchantId() {
    return callerMerchantId;
  }

  public void setCallerMerchantId(Long callerMerchantId) {
    this.callerMerchantId = callerMerchantId;
  }

  public Long getCalleeMerchantId() {
    return calleeMerchantId;
  }

  public void setCalleeMerchantId(Long calleeMerchantId) {
    this.calleeMerchantId = calleeMerchantId;
  }

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public Integer getCallType() {
    return callType;
  }

  public void setCallType(Integer callType) {
    this.callType = callType;
  }

  public LocalDateTime getCallTime() {
    return callTime;
  }

  public void setCallTime(LocalDateTime callTime) {
    this.callTime = callTime;
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
