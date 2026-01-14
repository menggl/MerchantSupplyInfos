package com.msi.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "merchant_info")
public class Merchant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wechat_id")
    private String wechatId;

    @Column(name = "token", unique = true)
    private String token;

    @Column(name = "wechat_name")
    private String wechatName;

    @Column(name = "merchant_name")
    private String merchantName;

    @Column(name = "merchant_phone")
    private String merchantPhone;

    @Column(name = "merchant_address")
    private String merchantAddress;

    @Column(name = "city_code")
    private String cityCode;

    @Column(name = "latitude")
    private java.math.BigDecimal merchantLatitude;

    @Column(name = "longitude")
    private java.math.BigDecimal merchantLongitude;

    @Column(name = "contact_name")
    private String contactName;
 
    @Transient
    private Integer isMember; // 1: Yes, 0: No

    @Transient
    private LocalDateTime memberExpireDate;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "cancellation_date")
    private LocalDateTime cancellationDate;

    @Column(name = "is_valid")
    private Integer isValid; // 1: Valid, 0: Invalid

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Transient
    private String code;

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

    public String getWechatId() {
        return wechatId;
    }

    public void setWechatId(String wechatId) {
        this.wechatId = wechatId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getWechatName() {
        return wechatName;
    }

    public void setWechatName(String wechatName) {
        this.wechatName = wechatName;
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

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public java.math.BigDecimal getMerchantLatitude() {
        return merchantLatitude;
    }

    public void setMerchantLatitude(java.math.BigDecimal merchantLatitude) {
        this.merchantLatitude = merchantLatitude;
    }

    public java.math.BigDecimal getMerchantLongitude() {
        return merchantLongitude;
    }

    public void setMerchantLongitude(java.math.BigDecimal merchantLongitude) {
        this.merchantLongitude = merchantLongitude;
    }

    public String getContactName() {
        return contactName;
    }

     public void setContactName(String contactName) {
         this.contactName = contactName;
     }
 
     public Integer getIsMember() {
         return isMember;
     }

    public void setIsMember(Integer isMember) {
        this.isMember = isMember;
    }

    public LocalDateTime getMemberExpireDate() {
        return memberExpireDate;
    }

    public void setMemberExpireDate(LocalDateTime memberExpireDate) {
        this.memberExpireDate = memberExpireDate;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDateTime getCancellationDate() {
        return cancellationDate;
    }

    public void setCancellationDate(LocalDateTime cancellationDate) {
        this.cancellationDate = cancellationDate;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
