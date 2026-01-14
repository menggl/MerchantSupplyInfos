package com.msi.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "city_dict")
public class CityDict {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "city_code", unique = true)
  private String cityCode;

  @Column(name = "city_name")
  private String cityName;

  @Column(name = "sort")
  private Integer sort;

  @Column(name = "valid")
  private Integer valid;

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
  }

  @PreUpdate
  protected void onUpdate() {
    modifyTime = LocalDateTime.now();
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getCityCode() { return cityCode; }
  public void setCityCode(String cityCode) { this.cityCode = cityCode; }

  public String getCityName() { return cityName; }
  public void setCityName(String cityName) { this.cityName = cityName; }

  public Integer getSort() { return sort; }
  public void setSort(Integer sort) { this.sort = sort; }

  public Integer getValid() { return valid; }
  public void setValid(Integer valid) { this.valid = valid; }

  public LocalDateTime getCreateTime() { return createTime; }
  public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

  public LocalDateTime getModifyTime() { return modifyTime; }
  public void setModifyTime(LocalDateTime modifyTime) { this.modifyTime = modifyTime; }
}
