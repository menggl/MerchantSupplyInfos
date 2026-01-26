package com.msi.dto;

import java.util.List;

public class BrandDetailsDto {
    private Long brandId;
    private List<SeriesItem> seriesArr;

    public BrandDetailsDto() {
    }

    public BrandDetailsDto(Long brandId, List<SeriesItem> seriesArr) {
        this.brandId = brandId;
        this.seriesArr = seriesArr;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public List<SeriesItem> getSeriesArr() {
        return seriesArr;
    }

    public void setSeriesArr(List<SeriesItem> seriesArr) {
        this.seriesArr = seriesArr;
    }

    public static class SeriesItem {
        private Long id;
        private String series_name;
        private Integer sort;
        private List<ModelItem> modelArr;

        public SeriesItem() {
        }

        public SeriesItem(Long id, String series_name, Integer sort, List<ModelItem> modelArr) {
            this.id = id;
            this.series_name = series_name;
            this.sort = sort;
            this.modelArr = modelArr;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getSeries_name() {
            return series_name;
        }

        public void setSeries_name(String series_name) {
            this.series_name = series_name;
        }

        public Integer getSort() {
            return sort;
        }

        public void setSort(Integer sort) {
            this.sort = sort;
        }

        public List<ModelItem> getModelArr() {
            return modelArr;
        }

        public void setModelArr(List<ModelItem> modelArr) {
            this.modelArr = modelArr;
        }
    }

    public static class ModelItem {
        private Long id;
        private String model_name;
        private Integer sort;
        private List<SpecItem> specArr;

        public ModelItem() {
        }

        public ModelItem(Long id, String model_name, Integer sort, List<SpecItem> specArr) {
            this.id = id;
            this.model_name = model_name;
            this.sort = sort;
            this.specArr = specArr;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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

        public List<SpecItem> getSpecArr() {
            return specArr;
        }

        public void setSpecArr(List<SpecItem> specArr) {
            this.specArr = specArr;
        }
    }

    public static class SpecItem {
        private Long id;
        private String spec_name;
        private Integer sort;

        public SpecItem() {
        }

        public SpecItem(Long id, String spec_name, Integer sort) {
            this.id = id;
            this.spec_name = spec_name;
            this.sort = sort;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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
}
