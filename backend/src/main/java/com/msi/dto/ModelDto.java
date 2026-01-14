package com.msi.dto;

import java.util.List;

public class ModelDto {
    private Long brand_id;
    
    private Long series_id;
    
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

    public ModelDto(Long brand_id, Long series_id, Long model_id, String model_name, Integer sort, List<SpecDto> specArr) {
        this.brand_id = brand_id;
        this.series_id = series_id;
        this.model_id = model_id;
        this.model_name = model_name;
        this.sort = sort;
        this.specArr = specArr;
    }

    public Long getBrand_id() {
        return brand_id;
    }

    public void setBrand_id(Long brand_id) {
        this.brand_id = brand_id;
    }

    public Long getSeries_id() {
        return series_id;
    }

    public void setSeries_id(Long series_id) {
        this.series_id = series_id;
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
