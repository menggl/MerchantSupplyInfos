package com.msi.admin.dto;

public class SpecDto {
    private Long spec_id;
    private String spec_name;
    private Integer sort;

    public SpecDto() {}

    public SpecDto(Long spec_id, String spec_name, Integer sort) {
        this.spec_id = spec_id;
        this.spec_name = spec_name;
        this.sort = sort;
    }

    public Long getSpec_id() {
        return spec_id;
    }

    public void setSpec_id(Long spec_id) {
        this.spec_id = spec_id;
    }

    public String getSpec_name() {
        return spec_name;
    }

    public void setSpec_name(String spec_name) {
        this.spec_name = spec_name;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}
