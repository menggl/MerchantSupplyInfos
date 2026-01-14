package com.msi.admin.dto;

import java.util.List;

public class BrandDto {
    private Long brand_id;
    private String brand_name;
    private Integer sort;
    private List<SeriesDto> seriesArr;

    public BrandDto() {}

    public BrandDto(Long brand_id, String brand_name, Integer sort, List<SeriesDto> seriesArr) {
        this.brand_id = brand_id;
        this.brand_name = brand_name;
        this.sort = sort;
        this.seriesArr = seriesArr;
    }

    public Long getBrand_id() {
        return brand_id;
    }

    public void setBrand_id(Long brand_id) {
        this.brand_id = brand_id;
    }

    public String getBrand_name() {
        return brand_name;
    }

    public void setBrand_name(String brand_name) {
        this.brand_name = brand_name;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public List<SeriesDto> getSeriesArr() {
        return seriesArr;
    }

    public void setSeriesArr(List<SeriesDto> seriesArr) {
        this.seriesArr = seriesArr;
    }
}
