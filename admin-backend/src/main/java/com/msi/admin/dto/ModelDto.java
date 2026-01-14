package com.msi.admin.dto;

import java.util.List;

public class ModelDto {
    private Long model_id;
    private String model_name;
    private Integer sort;
    private List<SpecDto> specArr;

    public ModelDto() {}

    public ModelDto(Long model_id, String model_name, Integer sort, List<SpecDto> specArr) {
        this.model_id = model_id;
        this.model_name = model_name;
        this.sort = sort;
        this.specArr = specArr;
    }

    public Long getModel_id() {
        return model_id;
    }

    public void setModel_id(Long model_id) {
        this.model_id = model_id;
    }

    public String getModel_name() {
        return model_name;
    }

    public void setModel_name(String model_name) {
        this.model_name = model_name;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public List<SpecDto> getSpecArr() {
        return specArr;
    }

    public void setSpecArr(List<SpecDto> specArr) {
        this.specArr = specArr;
    }
}
