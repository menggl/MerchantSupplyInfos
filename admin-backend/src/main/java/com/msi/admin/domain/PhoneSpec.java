package com.msi.admin.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "phone_spec")
public class PhoneSpec {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne
  @JoinColumn(name = "brand_id")
  private Brand brand;
  @ManyToOne
  @JoinColumn(name = "series_id")
  private PhoneSeries series;
  @ManyToOne
  @JoinColumn(name = "model_id")
  private PhoneModel model;
  @Column(name = "spec_name")
  private String specName;
  private Integer sort;
  private Integer deleted;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public PhoneModel getModel() {
    return model;
  }

  public void setModel(PhoneModel model) {
    this.model = model;
  }

  public Brand getBrand() {
    return brand;
  }

  public void setBrand(Brand brand) {
    this.brand = brand;
  }

  public PhoneSeries getSeries() {
    return series;
  }

  public void setSeries(PhoneSeries series) {
    this.series = series;
  }

  public String getSpecName() {
    return specName;
  }

  public void setSpecName(String specName) {
    this.specName = specName;
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

