package com.msi.dto;

import java.util.List;

public class SeriesDto {
    private Long brand_id;
    private Long series_id;
    private String series_name;
    private List<ModelDto> modelArr;

    public SeriesDto() {}

    public SeriesDto(Long series_id, String series_name, List<ModelDto> modelArr) {
        this.series_id = series_id;
        this.series_name = series_name;
        this.modelArr = modelArr;
    }

    public SeriesDto(Long brand_id, Long series_id, String series_name, List<ModelDto> modelArr) {
        this.brand_id = brand_id;
        this.series_id = series_id;
        this.series_name = series_name;
        this.modelArr = modelArr;
    }

    public Long getBrand_id() { return brand_id; }
    public void setBrand_id(Long brand_id) { this.brand_id = brand_id; }

    public Long getSeries_id() { return series_id; }
    public void setSeries_id(Long series_id) { this.series_id = series_id; }

    public String getSeries_name() { return series_name; }
    public void setSeries_name(String series_name) { this.series_name = series_name; }

    public List<ModelDto> getModelArr() { return modelArr; }
    public void setModelArr(List<ModelDto> modelArr) { this.modelArr = modelArr; }
}
