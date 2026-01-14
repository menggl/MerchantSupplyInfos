package com.msi.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "brand")
public class Brand {
  @Id
  private Long id;
  @Column(unique = true)
  private String name;
  private Integer sort;
  private Integer deleted;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

