package com.msi.admin.dto;

import java.time.LocalDateTime;

public class AdminProductDto {
    private Long id;
    private Long merchantId;
    private String merchantName;
    private String merchantPhone;
    private String merchantAddress;
    private java.math.BigDecimal latitude;
    private java.math.BigDecimal longitude;
    
    private String brandName;
    private String seriesName;
    private String modelName;
    private String specName;
    
    private String cityCode;
    private String cityName;
    private Integer productType; // 0: New, 1: Second-hand
    
    private String remark;
    private String otherRemark;
    
    private String secondHandVersion;
    private String secondHandCondition;
    private String secondHandFunction;
    private Integer batteryStatus;
    
    private String description;
    private Integer price;
    private Integer stock;
    private LocalDateTime listingTime;
    private Integer isValid;
    private LocalDateTime createTime;
    
    private java.util.List<String> imageUrls;

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

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantPhone() {
        return merchantPhone;
    }

    public void setMerchantPhone(String merchantPhone) {
        this.merchantPhone = merchantPhone;
    }

    public String getMerchantAddress() {
        return merchantAddress;
    }

    public void setMerchantAddress(String merchantAddress) {
        this.merchantAddress = merchantAddress;
    }

    public java.math.BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(java.math.BigDecimal latitude) {
        this.latitude = latitude;
    }

    public java.math.BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(java.math.BigDecimal longitude) {
        this.longitude = longitude;
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

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public java.util.List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(java.util.List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
