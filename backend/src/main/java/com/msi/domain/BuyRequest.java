package com.msi.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "buy_request")
public class BuyRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "merchant_id")
  private Long merchantId;

  @Column(name = "brand_id")
  private Long brandId;

  @Column(name = "series_id")
  private Long seriesId;

  @Column(name = "model_id")
  private Long modelId;

  @Column(name = "spec_id")
  private Long specId;

  @Column(name = "city_code")
  private String cityCode;

  @Column(name = "product_type")
  private Integer productType;

  @Column(name = "buy_count")
  private Integer buyCount;

  @Column(name = "min_price")
  private Integer minPrice;

  @Column(name = "max_price")
  private Integer maxPrice;

  @Column(name = "deadline")
  private LocalDateTime deadline;

  @Column(name = "cost_integral")
  private Integer costIntegral;

  @Column(name = "is_valid")
  private Integer isValid;
  
  @Column(name = "state")
  private Integer state;

  @Column(name = "create_time", updatable = false)
  private LocalDateTime createTime;

  @Column(name = "update_time")
  private LocalDateTime updateTime;

  @Transient
  private String merchantPublicId;

  public String getMerchantPublicId() {
    return merchantPublicId;
  }

  public void setMerchantPublicId(String merchantPublicId) {
    this.merchantPublicId = merchantPublicId;
  }

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

  public String getCityCode() {
    return cityCode;
  }

  public void setCityCode(String cityCode) {
    this.cityCode = cityCode;
  }

  public Integer getProductType() {
    return productType;
  }

  public void setProductType(Integer productType) {
    this.productType = productType;
  }

  public Integer getBuyCount() {
    return buyCount;
  }

  public void setBuyCount(Integer buyCount) {
    this.buyCount = buyCount;
  }

  public Integer getMinPrice() {
    return minPrice;
  }

  public void setMinPrice(Integer minPrice) {
    this.minPrice = minPrice;
  }

  public Integer getMaxPrice() {
    return maxPrice;
  }

  public void setMaxPrice(Integer maxPrice) {
    this.maxPrice = maxPrice;
  }

  public LocalDateTime getDeadline() {
    return deadline;
  }

  public void setDeadline(LocalDateTime deadline) {
    this.deadline = deadline;
  }

  public Integer getCostIntegral() {
    return costIntegral;
  }

  public void setCostIntegral(Integer costIntegral) {
    this.costIntegral = costIntegral;
  }

  public Integer getIsValid() {
    return isValid;
  }

  public void setIsValid(Integer isValid) {
    this.isValid = isValid;
  }
  
  public Integer getState() {
    return state;
  }
  
  public void setState(Integer state) {
    this.state = state;
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
