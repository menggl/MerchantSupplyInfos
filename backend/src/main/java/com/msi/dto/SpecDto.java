package com.msi.dto;

public class SpecDto {
    private Long brand_id;
    private Long series_id;
    private Long model_id;
    private Long spec_id;
    private String spec_name;

    public SpecDto() {}

    public SpecDto(Long spec_id, String spec_name) {
        this.spec_id = spec_id;
        this.spec_name = spec_name;
    }

    public SpecDto(Long brand_id, Long series_id, Long model_id, Long spec_id, String spec_name) {
        this.brand_id = brand_id;
        this.series_id = series_id;
        this.model_id = model_id;
        this.spec_id = spec_id;
        this.spec_name = spec_name;
    }

    public Long getBrand_id() { return brand_id; }
    public void setBrand_id(Long brand_id) { this.brand_id = brand_id; }

    public Long getSeries_id() { return series_id; }
    public void setSeries_id(Long series_id) { this.series_id = series_id; }

    public Long getModel_id() { return model_id; }
    public void setModel_id(Long model_id) { this.model_id = model_id; }

    public Long getSpec_id() { return spec_id; }
    public void setSpec_id(Long spec_id) { this.spec_id = spec_id; }

    public String getSpec_name() { return spec_name; }
    public void setSpec_name(String spec_name) { this.spec_name = spec_name; }
}
