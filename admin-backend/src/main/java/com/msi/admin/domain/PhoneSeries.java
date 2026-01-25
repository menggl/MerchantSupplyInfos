package com.msi.admin.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "phone_series")
public class PhoneSeries {
  @Id
  private Long id;
  @Column(name = "brand_id")
  private Long brandId;
  @Column(name = "series_name")
  private String seriesName;
  private Integer sort;
  private Integer deleted;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getBrandId() {
    return brandId;
  }

  public void setBrandId(Long brandId) {
    this.brandId = brandId;
  }

  public String getSeriesName() {
    return seriesName;
  }

  public void setSeriesName(String seriesName) {
    this.seriesName = seriesName;
  }

  public Integer getSort() {
    return sort;
  }

  public void setSort(Integer sort) {
    this.sort = sort;
  }

  public Integer getDeleted() {
    return deleted;
  }

  public void setDeleted(Integer deleted) {
    this.deleted = deleted;
  }
}

