package com.msi.domain;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "merchant_new_phone_stock")
public class MerchantNewPhoneStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(name = "brand_map_id")
    private Long brandMapId;

    // '0共有配置 1商户自己维护的配置'
    @Column(name = "spec_type")
    private Integer specType;

    @Column(name = "remark_id")
    private Long remarkId;

    @Column(name = "other_remark_id")
    private Long otherRemarkId;

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

    public Long getRemarkId() {
        return remarkId;
    }

    public void setRemarkId(Long remarkId) {
        this.remarkId = remarkId;
    }

    public Long getOtherRemarkId() {
        return otherRemarkId;
    }

    public void setOtherRemarkId(Long otherRemarkId) {
        this.otherRemarkId = otherRemarkId;
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
