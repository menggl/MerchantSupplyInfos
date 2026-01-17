package com.msi.dto;

import java.time.LocalDateTime;

public class MerchantProductModelDto {
  private Long productId;
  private Long brandId;
  private Long seriesId;
  private Long modelId;
  private Long specId;
  private String brandName;
  private String seriesName;
  private String modelName;
  private String specName;
  private String merchantName;
  private String merchantAddress;
  private Integer productType;
  private LocalDateTime updateTime;
  private Integer price;
  private String secondHandCondition;
  private String remark;
  private String otherRemark;

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public Long getBrandId() {
    return brandId;
  }

  public void setBrandId(Long brandId) {
    this.brandId = brandId;
  }

  public Long getSeriesId() {
    return seriesId;
  }

  public void setSeriesId(Long seriesId) {
    this.seriesId = seriesId;
  }

  public Long getModelId() {
    return modelId;
  }

  public void setModelId(Long modelId) {
    this.modelId = modelId;
  }

  public Long getSpecId() {
    return specId;
  }

  public void setSpecId(Long specId) {
    this.specId = specId;
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

  public Integer getProductType() {
    return productType;
  }

  public void setProductType(Integer productType) {
    this.productType = productType;
  }

  public LocalDateTime getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(LocalDateTime updateTime) {
    this.updateTime = updateTime;
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
}
