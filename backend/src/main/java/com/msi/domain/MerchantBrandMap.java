package com.msi.domain;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "merchant_brand_map")
public class MerchantBrandMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(name = "spec_type")
    private Integer specType; // 0共有配置 1商户自己维护的配置

    @Column(name = "phone_spec_id")
    private Long phoneSpecId;

    @Column(name = "custom_brand_id")
    private Long customBrandId;

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

    public Integer getSpecType() {
        return specType;
    }

    public void setSpecType(Integer specType) {
        this.specType = specType;
    }

    public Long getPhoneSpecId() {
        return phoneSpecId;
    }

    public void setPhoneSpecId(Long phoneSpecId) {
        this.phoneSpecId = phoneSpecId;
    }

    public Long getCustomBrandId() {
        return customBrandId;
    }

    public void setCustomBrandId(Long customBrandId) {
        this.customBrandId = customBrandId;
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
