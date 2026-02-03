package com.msi.domain;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "merchant_payment_record")
public class MerchantPaymentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(name = "stock_id")
    private Long stockId;

    @Column(name = "actual_payment")
    private Integer actualPayment;

    @Column(name = "actual_receipt")
    private Integer actualReceipt;

    @Column(name = "remark")
    private String remark;

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

    public Long getStockId() {
        return stockId;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    public Integer getActualPayment() {
        return actualPayment;
    }

    public void setActualPayment(Integer actualPayment) {
        this.actualPayment = actualPayment;
    }

    public Integer getActualReceipt() {
        return actualReceipt;
    }

    public void setActualReceipt(Integer actualReceipt) {
        this.actualReceipt = actualReceipt;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
