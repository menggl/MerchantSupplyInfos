package com.msi.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantDto {
  private Long merchant_id;

  private String wechat_id;

  private String wechat_name;

  private String merchant_name;

  private String contact_name;

  private String merchant_phone;

  private String merchant_address;

  private java.math.BigDecimal merchant_latitude;

  private java.math.BigDecimal merchant_longitude;

  private Integer is_member;

  private LocalDateTime member_expire_date;


  public Long getMerchant_id() { return merchant_id; }
  public void setMerchant_id(Long merchant_id) { this.merchant_id = merchant_id; }

  public String getWechat_id() { return wechat_id; }
  public void setWechat_id(String wechat_id) { this.wechat_id = wechat_id; }

  public String getWechat_name() { return wechat_name; }
  public void setWechat_name(String wechat_name) { this.wechat_name = wechat_name; }

  public String getMerchant_name() { return merchant_name; }
  public void setMerchant_name(String merchant_name) { this.merchant_name = merchant_name; }

  public String getContact_name() { return contact_name; }
  public void setContact_name(String contact_name) { this.contact_name = contact_name; }

  public String getMerchant_phone() { return merchant_phone; }
  public void setMerchant_phone(String merchant_phone) { this.merchant_phone = merchant_phone; }

  public String getMerchant_address() { return merchant_address; }
  public void setMerchant_address(String merchant_address) { this.merchant_address = merchant_address; }

  public java.math.BigDecimal getMerchant_latitude() { return merchant_latitude; }
  public void setMerchant_latitude(java.math.BigDecimal merchant_latitude) { this.merchant_latitude = merchant_latitude; }

  public java.math.BigDecimal getMerchant_longitude() { return merchant_longitude; }
  public void setMerchant_longitude(java.math.BigDecimal merchant_longitude) { this.merchant_longitude = merchant_longitude; }

  public Integer getIs_member() { return is_member; }
  public void setIs_member(Integer is_member) { this.is_member = is_member; }

  public LocalDateTime getMember_expire_date() { return member_expire_date; }
  public void setMember_expire_date(LocalDateTime member_expire_date) { this.member_expire_date = member_expire_date; }

}
