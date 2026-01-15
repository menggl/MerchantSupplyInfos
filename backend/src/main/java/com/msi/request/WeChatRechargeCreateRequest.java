package com.msi.request;

public class WeChatRechargeCreateRequest {
    private Integer rechargeType;
    private Integer integralAmount;
    private Integer memberMonths;
    private Integer totalAmount;

    public Integer getRechargeType() {
        return rechargeType;
    }

    public void setRechargeType(Integer rechargeType) {
        this.rechargeType = rechargeType;
    }

    public Integer getIntegralAmount() {
        return integralAmount;
    }

    public void setIntegralAmount(Integer integralAmount) {
        this.integralAmount = integralAmount;
    }

    public Integer getMemberMonths() {
        return memberMonths;
    }

    public void setMemberMonths(Integer memberMonths) {
        this.memberMonths = memberMonths;
    }

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }
}

