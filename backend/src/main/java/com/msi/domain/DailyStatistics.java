package com.msi.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_statistics")
public class DailyStatistics {

    @Id
    @Column(name = "statistics_date", length = 20)
    private String statisticsDate;

    // 每日有效用户（手机号不为空）打开小程序的用户数 (Real-time)
    @Column(name = "daily_active_users")
    private Integer dailyActiveUsers;

    // 每日新增商户数量
    @Column(name = "new_merchant_count")
    private Integer newMerchantCount;

    // 每日商户总数
    @Column(name = "total_merchant_count")
    private Integer totalMerchantCount;

    // 新增有效商户数量
    @Column(name = "new_valid_merchant_count")
    private Integer newValidMerchantCount;

    // 截止当日有效商户总数
    @Column(name = "total_valid_merchant_count")
    private Integer totalValidMerchantCount;

    // --- 新机 ---

    // 新机当日新增上架总数
    @Column(name = "new_product_new_count")
    private Integer newProductNewCount;

    // 新机新用户当日新增上架总数
    @Column(name = "new_product_new_user_count")
    private Integer newProductNewUserCount;

    // 新机老用户当日新增上架总数
    @Column(name = "new_product_old_user_count")
    private Integer newProductOldUserCount;

    // 新机当日更新上架总数
    @Column(name = "new_product_update_count")
    private Integer newProductUpdateCount;

    // 新机新用户当日更新上架总数
    @Column(name = "new_product_new_user_update_count")
    private Integer newProductNewUserUpdateCount;

    // 新机老用户当日更新上架总数
    @Column(name = "new_product_old_user_update_count")
    private Integer newProductOldUserUpdateCount;

    // 新机截止当日总上架数
    @Column(name = "new_product_total_count")
    private Integer newProductTotalCount;

    // 新机截止当日未上架数
    @Column(name = "new_product_total_off_count")
    private Integer newProductTotalOffCount;

    // 新机截止当日求购总数
    @Column(name = "new_product_buy_count")
    private Integer newProductBuyCount;

    // --- 二手机 ---

    // 二手机当日新增上架总数
    @Column(name = "second_hand_product_new_count")
    private Integer secondHandProductNewCount;

    // 二手机新用户当日新增上架总数
    @Column(name = "second_hand_product_new_user_count")
    private Integer secondHandProductNewUserCount;

    // 二手机老用户当日新增上架总数
    @Column(name = "second_hand_product_old_user_count")
    private Integer secondHandProductOldUserCount;

    // 二手机当日更新上架总数
    @Column(name = "second_hand_product_update_count")
    private Integer secondHandProductUpdateCount;

    // 二手机新用户当日更新上架总数
    @Column(name = "second_hand_product_new_user_update_count")
    private Integer secondHandProductNewUserUpdateCount;

    // 二手机老用户当日更新上架总数
    @Column(name = "second_hand_product_old_user_update_count")
    private Integer secondHandProductOldUserUpdateCount;

    // 二手机截止当日总上架数
    @Column(name = "second_hand_product_total_count")
    private Integer secondHandProductTotalCount;

    // 二手机截止当日未上架数
    @Column(name = "second_hand_product_total_off_count")
    private Integer secondHandProductTotalOffCount;

    // 二手机截止当日求购总数
    @Column(name = "second_hand_product_buy_count")
    private Integer secondHandProductBuyCount;

    // --- 沟通 ---

    // 当日商家上架产品沟通次数
    @Column(name = "call_count_product")
    private Integer callCountProduct;

    // 当日商家求购产品沟通次数
    @Column(name = "call_count_buy")
    private Integer callCountBuy;

    // 当日商家上架产品沟通商户数
    @Column(name = "call_merchant_count_product")
    private Integer callMerchantCountProduct;

    // 当日商家求购产品沟通商户数
    @Column(name = "call_merchant_count_buy")
    private Integer callMerchantCountBuy;

    // --- 积分与充值 ---

    // 当日签到数量
    @Column(name = "daily_sign_in_count")
    private Integer dailySignInCount;

    // 当日充值次数
    @Column(name = "daily_recharge_count")
    private Integer dailyRechargeCount;

    // 截止当天总共充值金额
    @Column(name = "total_recharge_amount")
    private Long totalRechargeAmount;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }

    // Getters and Setters

    public String getStatisticsDate() {
        return statisticsDate;
    }

    public void setStatisticsDate(String statisticsDate) {
        this.statisticsDate = statisticsDate;
    }

    public Integer getDailyActiveUsers() {
        return dailyActiveUsers;
    }

    public void setDailyActiveUsers(Integer dailyActiveUsers) {
        this.dailyActiveUsers = dailyActiveUsers;
    }

    public Integer getNewMerchantCount() {
        return newMerchantCount;
    }

    public void setNewMerchantCount(Integer newMerchantCount) {
        this.newMerchantCount = newMerchantCount;
    }

    public Integer getTotalMerchantCount() {
        return totalMerchantCount;
    }

    public void setTotalMerchantCount(Integer totalMerchantCount) {
        this.totalMerchantCount = totalMerchantCount;
    }

    public Integer getNewValidMerchantCount() {
        return newValidMerchantCount;
    }

    public void setNewValidMerchantCount(Integer newValidMerchantCount) {
        this.newValidMerchantCount = newValidMerchantCount;
    }

    public Integer getTotalValidMerchantCount() {
        return totalValidMerchantCount;
    }

    public void setTotalValidMerchantCount(Integer totalValidMerchantCount) {
        this.totalValidMerchantCount = totalValidMerchantCount;
    }

    public Integer getNewProductNewCount() {
        return newProductNewCount;
    }

    public void setNewProductNewCount(Integer newProductNewCount) {
        this.newProductNewCount = newProductNewCount;
    }

    public Integer getNewProductNewUserCount() {
        return newProductNewUserCount;
    }

    public void setNewProductNewUserCount(Integer newProductNewUserCount) {
        this.newProductNewUserCount = newProductNewUserCount;
    }

    public Integer getNewProductOldUserCount() {
        return newProductOldUserCount;
    }

    public void setNewProductOldUserCount(Integer newProductOldUserCount) {
        this.newProductOldUserCount = newProductOldUserCount;
    }

    public Integer getNewProductUpdateCount() {
        return newProductUpdateCount;
    }

    public void setNewProductUpdateCount(Integer newProductUpdateCount) {
        this.newProductUpdateCount = newProductUpdateCount;
    }

    public Integer getNewProductNewUserUpdateCount() {
        return newProductNewUserUpdateCount;
    }

    public void setNewProductNewUserUpdateCount(Integer newProductNewUserUpdateCount) {
        this.newProductNewUserUpdateCount = newProductNewUserUpdateCount;
    }

    public Integer getNewProductOldUserUpdateCount() {
        return newProductOldUserUpdateCount;
    }

    public void setNewProductOldUserUpdateCount(Integer newProductOldUserUpdateCount) {
        this.newProductOldUserUpdateCount = newProductOldUserUpdateCount;
    }

    public Integer getNewProductTotalCount() {
        return newProductTotalCount;
    }

    public void setNewProductTotalCount(Integer newProductTotalCount) {
        this.newProductTotalCount = newProductTotalCount;
    }

    public Integer getNewProductTotalOffCount() {
        return newProductTotalOffCount;
    }

    public void setNewProductTotalOffCount(Integer newProductTotalOffCount) {
        this.newProductTotalOffCount = newProductTotalOffCount;
    }

    public Integer getNewProductBuyCount() {
        return newProductBuyCount;
    }

    public void setNewProductBuyCount(Integer newProductBuyCount) {
        this.newProductBuyCount = newProductBuyCount;
    }

    public Integer getSecondHandProductNewCount() {
        return secondHandProductNewCount;
    }

    public void setSecondHandProductNewCount(Integer secondHandProductNewCount) {
        this.secondHandProductNewCount = secondHandProductNewCount;
    }

    public Integer getSecondHandProductNewUserCount() {
        return secondHandProductNewUserCount;
    }

    public void setSecondHandProductNewUserCount(Integer secondHandProductNewUserCount) {
        this.secondHandProductNewUserCount = secondHandProductNewUserCount;
    }

    public Integer getSecondHandProductOldUserCount() {
        return secondHandProductOldUserCount;
    }

    public void setSecondHandProductOldUserCount(Integer secondHandProductOldUserCount) {
        this.secondHandProductOldUserCount = secondHandProductOldUserCount;
    }

    public Integer getSecondHandProductUpdateCount() {
        return secondHandProductUpdateCount;
    }

    public void setSecondHandProductUpdateCount(Integer secondHandProductUpdateCount) {
        this.secondHandProductUpdateCount = secondHandProductUpdateCount;
    }

    public Integer getSecondHandProductNewUserUpdateCount() {
        return secondHandProductNewUserUpdateCount;
    }

    public void setSecondHandProductNewUserUpdateCount(Integer secondHandProductNewUserUpdateCount) {
        this.secondHandProductNewUserUpdateCount = secondHandProductNewUserUpdateCount;
    }

    public Integer getSecondHandProductOldUserUpdateCount() {
        return secondHandProductOldUserUpdateCount;
    }

    public void setSecondHandProductOldUserUpdateCount(Integer secondHandProductOldUserUpdateCount) {
        this.secondHandProductOldUserUpdateCount = secondHandProductOldUserUpdateCount;
    }

    public Integer getSecondHandProductTotalCount() {
        return secondHandProductTotalCount;
    }

    public void setSecondHandProductTotalCount(Integer secondHandProductTotalCount) {
        this.secondHandProductTotalCount = secondHandProductTotalCount;
    }

    public Integer getSecondHandProductTotalOffCount() {
        return secondHandProductTotalOffCount;
    }

    public void setSecondHandProductTotalOffCount(Integer secondHandProductTotalOffCount) {
        this.secondHandProductTotalOffCount = secondHandProductTotalOffCount;
    }

    public Integer getSecondHandProductBuyCount() {
        return secondHandProductBuyCount;
    }

    public void setSecondHandProductBuyCount(Integer secondHandProductBuyCount) {
        this.secondHandProductBuyCount = secondHandProductBuyCount;
    }

    public Integer getCallCountProduct() {
        return callCountProduct;
    }

    public void setCallCountProduct(Integer callCountProduct) {
        this.callCountProduct = callCountProduct;
    }

    public Integer getCallCountBuy() {
        return callCountBuy;
    }

    public void setCallCountBuy(Integer callCountBuy) {
        this.callCountBuy = callCountBuy;
    }

    public Integer getCallMerchantCountProduct() {
        return callMerchantCountProduct;
    }

    public void setCallMerchantCountProduct(Integer callMerchantCountProduct) {
        this.callMerchantCountProduct = callMerchantCountProduct;
    }

    public Integer getCallMerchantCountBuy() {
        return callMerchantCountBuy;
    }

    public void setCallMerchantCountBuy(Integer callMerchantCountBuy) {
        this.callMerchantCountBuy = callMerchantCountBuy;
    }

    public Integer getDailySignInCount() {
        return dailySignInCount;
    }

    public void setDailySignInCount(Integer dailySignInCount) {
        this.dailySignInCount = dailySignInCount;
    }

    public Integer getDailyRechargeCount() {
        return dailyRechargeCount;
    }

    public void setDailyRechargeCount(Integer dailyRechargeCount) {
        this.dailyRechargeCount = dailyRechargeCount;
    }

    public Long getTotalRechargeAmount() {
        return totalRechargeAmount;
    }

    public void setTotalRechargeAmount(Long totalRechargeAmount) {
        this.totalRechargeAmount = totalRechargeAmount;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
