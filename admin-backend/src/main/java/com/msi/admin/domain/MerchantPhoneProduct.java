package com.msi.admin.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "merchant_phone_product")
public class MerchantPhoneProduct {
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
    private Integer productType; // 0: New, 1: Second-hand

    @Column(name = "remark")
    private String remark;

    @Column(name = "other_remark")
    private String otherRemark;

    @Column(name = "second_hand_version")
    private String secondHandVersion;

    @Column(name = "second_hand_condition")
    private String secondHandCondition;

    @Column(name = "second_hand_function")
    private String secondHandFunction;

    @Column(name = "battery_status")
    private Integer batteryStatus;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price")
    private Integer price;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "listing_time")
    private LocalDateTime listingTime;

    @Column(name = "is_valid")
    private Integer isValid; // 1: Valid, 0: Invalid

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

    public String getSecondHandVersion() {
        return secondHandVersion;
    }

    public void setSecondHandVersion(String secondHandVersion) {
        this.secondHandVersion = secondHandVersion;
    }

    public String getSecondHandCondition() {
        return secondHandCondition;
    }

    public void setSecondHandCondition(String secondHandCondition) {
        this.secondHandCondition = secondHandCondition;
    }

    public String getSecondHandFunction() {
        return secondHandFunction;
    }

    public void setSecondHandFunction(String secondHandFunction) {
        this.secondHandFunction = secondHandFunction;
    }

    public Integer getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(Integer batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getListingTime() {
        return listingTime;
    }

    public void setListingTime(LocalDateTime listingTime) {
        this.listingTime = listingTime;
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
