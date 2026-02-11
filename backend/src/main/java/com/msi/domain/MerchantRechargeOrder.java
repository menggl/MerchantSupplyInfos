package com.msi.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "merchant_recharge_order")
public class MerchantRechargeOrder {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_no", unique = true, nullable = false, length = 64)
  private String orderNo;

  @Column(name = "merchant_id", nullable = false)
  private Long merchantId;

  @Column(name = "recharge_type", nullable = false)
  private Integer rechargeType;

  @Column(name = "integral_amount")
  private Integer integralAmount;

  @Column(name = "member_months")
  private Integer memberMonths;

  @Column(name = "total_amount", nullable = false)
  private Integer totalAmount;

  @Column(name = "pay_status", nullable = false)
  private Integer payStatus;

  @Column(name = "wechat_transaction_id")
  private String wechatTransactionId;

  @Column(name = "app_id")
  private String appId;

  @Column(name = "prepay_id")
  private String prepayId;

  @Column(name = "nonce_str")
  private String nonceStr;

  @Column(name = "time_stamp")
  private String timeStamp;

  @Column(name = "package_val")
  private String packageVal;

  @Column(name = "sign_type")
  private String signType;

  @Column(name = "pay_sign")
  private String paySign;

  @Column(name = "create_time")
  private LocalDateTime createTime;

  @Column(name = "update_time")
  private LocalDateTime updateTime;

  @PrePersist
  protected void onCreate() {
    if (createTime == null) {
      createTime = LocalDateTime.now();
    }
    if (updateTime == null) {
      updateTime = LocalDateTime.now();
    }
    if (payStatus == null) {
      payStatus = 0;
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

  public String getOrderNo() {
    return orderNo;
  }

  public void setOrderNo(String orderNo) {
    this.orderNo = orderNo;
  }

  public Long getMerchantId() {
    return merchantId;
  }

  public void setMerchantId(Long merchantId) {
    this.merchantId = merchantId;
  }

  public Integer getRechargeType() {
    return rechargeType;
  }

  public void setRechargeType(Integer rechargeType) {
    this.rechargeType = rechargeType;
  }

  public Integer getIntegralAmount() {
    return integralAmount;
  }

  public void setIntegralAmount(Integer integralAmount) {
    this.integralAmount = integralAmount;
  }
  
  public Integer getMemberMonths() {
    return memberMonths;
  }

  public void setMemberMonths(Integer memberMonths) {
    this.memberMonths = memberMonths;
  }

  public Integer getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(Integer totalAmount) {
    this.totalAmount = totalAmount;
  }

  public Integer getPayStatus() {
    return payStatus;
  }

  public void setPayStatus(Integer payStatus) {
    this.payStatus = payStatus;
  }

  public String getWechatTransactionId() {
    return wechatTransactionId;
  }

  public void setWechatTransactionId(String wechatTransactionId) {
    this.wechatTransactionId = wechatTransactionId;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getPrepayId() {
    return prepayId;
  }

  public void setPrepayId(String prepayId) {
    this.prepayId = prepayId;
  }

  public String getNonceStr() {
    return nonceStr;
  }

  public void setNonceStr(String nonceStr) {
    this.nonceStr = nonceStr;
  }

  public String getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(String timeStamp) {
    this.timeStamp = timeStamp;
  }

  public String getPackageVal() {
    return packageVal;
  }

  public void setPackageVal(String packageVal) {
    this.packageVal = packageVal;
  }

  public String getSignType() {
    return signType;
  }

  public void setSignType(String signType) {
    this.signType = signType;
  }

  public String getPaySign() {
    return paySign;
  }

  public void setPaySign(String paySign) {
    this.paySign = paySign;
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
