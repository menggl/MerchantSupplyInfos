package com.msi.domain;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "merchant_second_phone_stock")
public class MerchantSecondPhoneStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(name = "brand_map_id")
    private Long brandMapId;

    @Column(name = "spec_type")
    private Integer specType;

    @Column(name = "version_id")
    private Long versionId;

    @Column(name = "condition_id")
    private Long conditionId;

    @Column(name = "repair_function_id")
    private Long repairFunctionId;

    @Column(name = "battery_health_id")
    private Integer batteryHealthId;

    @Column(name = "price")
    private Integer price;

    @Column(name = "stock_count")
    private Integer stockCount;

    @Column(name = "stock_status")
    private Integer stockStatus; // 0下架 1上架

    @Column(name = "valid")
    private Integer valid; // 1有效 0无效

    @Column(name = "create_time", insertable = false, updatable = false)
    private Date createTime;

    @Column(name = "update_time", insertable = false, updatable = false)
    private Date updateTime;

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

    public Long getBrandMapId() {
        return brandMapId;
    }

    public void setBrandMapId(Long brandMapId) {
        this.brandMapId = brandMapId;
    }

    public Integer getSpecType() {
        return specType;
    }

    public void setSpecType(Integer specType) {
        this.specType = specType;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public Long getConditionId() {
        return conditionId;
    }

    public void setConditionId(Long conditionId) {
        this.conditionId = conditionId;
    }

    public Long getRepairFunctionId() {
        return repairFunctionId;
    }

    public void setRepairFunctionId(Long repairFunctionId) {
        this.repairFunctionId = repairFunctionId;
    }

    public Integer getBatteryHealthId() {
        return batteryHealthId;
    }

    public void setBatteryHealthId(Integer batteryHealthId) {
        this.batteryHealthId = batteryHealthId;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getStockCount() {
        return stockCount;
    }

    public void setStockCount(Integer stockCount) {
        this.stockCount = stockCount;
    }

    public Integer getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(Integer stockStatus) {
        this.stockStatus = stockStatus;
    }

    public Integer getValid() {
        return valid;
    }

    public void setValid(Integer valid) {
        this.valid = valid;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
