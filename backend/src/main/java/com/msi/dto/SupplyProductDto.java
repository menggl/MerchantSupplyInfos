package com.msi.dto;

import java.time.LocalDateTime;

public class SupplyProductDto {
  private String merchantPublicId;
  private Long productId;
  private String merchantName;
  private String merchantAddress;
  private String merchantCity;
  private Integer price;
  private String secondHandCondition;
  private String remark;
  private String otherRemark;
  private LocalDateTime listingTime;

  public String getMerchantPublicId() {
    return merchantPublicId;
  }

  public void setMerchantPublicId(String merchantPublicId) {
    this.merchantPublicId = merchantPublicId;
  }

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public String getMerchantName() {
    return merchantName;
  }

  public void setMerchantName(String merchantName) {
    this.merchantName = merchantName;
  }

  public String getMerchantAddress() {
    return merchantAddress;
  }

  public void setMerchantAddress(String merchantAddress) {
    this.merchantAddress = merchantAddress;
  }

  public String getMerchantCity() {
    return merchantCity;
  }

  public void setMerchantCity(String merchantCity) {
    this.merchantCity = merchantCity;
  }

  public Integer getPrice() {
    return price;
  }

  public void setPrice(Integer price) {
    this.price = price;
  }

  public String getSecondHandCondition() {
    return secondHandCondition;
  }

  public void setSecondHandCondition(String secondHandCondition) {
    this.secondHandCondition = secondHandCondition;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }

  public String getOtherRemark() {
    return otherRemark;
  }

  public void setOtherRemark(String otherRemark) {
    this.otherRemark = otherRemark;
  }

  public LocalDateTime getListingTime() {
    return listingTime;
  }

  public void setListingTime(LocalDateTime listingTime) {
    this.listingTime = listingTime;
  }
}
