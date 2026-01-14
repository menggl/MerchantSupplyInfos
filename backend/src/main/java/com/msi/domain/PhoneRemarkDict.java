package com.msi.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "phone_remark_dict")
public class PhoneRemarkDict {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "remark_name", unique = true)
  private String remarkName;

  @Column(name = "sort")
  private Integer sort;

  @Column(name = "valid")
  private Integer valid;

  @Column(name = "type")
  private Integer type;

  @Column(name = "create_time", updatable = false)
  private LocalDateTime createTime;

  @Column(name = "modify_time")
  private LocalDateTime modifyTime;

  @PrePersist
  protected void onCreate() {
    createTime = LocalDateTime.now();
    modifyTime = LocalDateTime.now();
    if (valid == null) {
      valid = 1;
    }
    if (sort == null) {
      sort = 0;
    }
    if (type == null) {
      type = 0;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    modifyTime = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getRemarkName() {
    return remarkName;
  }

  public void setRemarkName(String remarkName) {
    this.remarkName = remarkName;
  }

  public Integer getSort() {
    return sort;
  }

  public void setSort(Integer sort) {
    this.sort = sort;
  }

  public Integer getValid() {
    return valid;
  }

  public void setValid(Integer valid) {
    this.valid = valid;
  }

  public Integer getType() {
    return type;
  }

  public void setType(Integer type) {
    this.type = type;
  }

  public LocalDateTime getCreateTime() {
    return createTime;
  }

  public void setCreateTime(LocalDateTime createTime) {
    this.createTime = createTime;
  }

  public LocalDateTime getModifyTime() {
    return modifyTime;
  }

  public void setModifyTime(LocalDateTime modifyTime) {
    this.modifyTime = modifyTime;
  }
}
