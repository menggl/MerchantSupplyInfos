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
  private String region;
  private String brandName;
  private String seriesName;
  private String modelName;
  private String specName;
  private LocalDateTime listingTime;
  private Integer isOwner;

  public Integer getIsOwner() {
    return isOwner;
  }

  public void setIsOwner(Integer isOwner) {
    this.isOwner = isOwner;
  }

  public String getBrandName() {
    return brandName;
  }

  public void setBrandName(String brandName) {
    this.brandName = brandName;
  }

  public String getSeriesName() {
    return seriesName;
  }

  public void setSeriesName(String seriesName) {
    this.seriesName = seriesName;
  }

  public String getModelName() {
    return modelName;
  }

  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  public String getSpecName() {
    return specName;
  }

  public void setSpecName(String specName) {
    this.specName = specName;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

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
