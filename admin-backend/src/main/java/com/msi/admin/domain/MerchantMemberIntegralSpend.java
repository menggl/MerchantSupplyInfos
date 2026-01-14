package com.msi.admin.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "merchant_member_integral_spend")
public class MerchantMemberIntegralSpend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(name = "integral_before_spend")
    private Integer integralBeforeSpend;

    @Column(name = "integral_after_spend")
    private Integer integralAfterSpend;

    @Column(name = "change_amount")
    private Integer changeAmount;

    @Column(name = "change_reason")
    private String changeReason;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "change_time")
    private LocalDateTime changeTime;

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

    public Integer getIntegralBeforeSpend() {
        return integralBeforeSpend;
    }

    public void setIntegralBeforeSpend(Integer integralBeforeSpend) {
        this.integralBeforeSpend = integralBeforeSpend;
    }

    public Integer getIntegralAfterSpend() {
        return integralAfterSpend;
    }

    public void setIntegralAfterSpend(Integer integralAfterSpend) {
        this.integralAfterSpend = integralAfterSpend;
    }

    public Integer getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(Integer changeAmount) {
        this.changeAmount = changeAmount;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getChangeTime() {
        return changeTime;
    }

    public void setChangeTime(LocalDateTime changeTime) {
        this.changeTime = changeTime;
    }
}
