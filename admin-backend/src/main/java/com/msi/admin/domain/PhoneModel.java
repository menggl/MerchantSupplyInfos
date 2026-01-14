package com.msi.admin.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "phone_model")
public class PhoneModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne
  @JoinColumn(name = "brand_id")
  private Brand brand;
  @ManyToOne
  @JoinColumn(name = "series_id")
  private PhoneSeries series;
  @Column(name = "model_name")
  private String modelName;
  private Integer sort;
  private Integer deleted;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public PhoneSeries getSeries() {
    return series;
  }

  public void setSeries(PhoneSeries series) {
    this.series = series;
  }

  public Brand getBrand() {
    return brand;
  }

  public void setBrand(Brand brand) {
    this.brand = brand;
  }

  public String getModelName() {
    return modelName;
  }

  public void setModelName(String modelName) {
    this.modelName = modelName;
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

