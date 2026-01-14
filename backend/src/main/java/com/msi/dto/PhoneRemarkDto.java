package com.msi.dto;

public class PhoneRemarkDto {
  private Long id;
  private String remark;

  public PhoneRemarkDto() {}

  public PhoneRemarkDto(Long id, String remark) {
    this.id = id;
    this.remark = remark;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }
}
