package com.msi.dto;

public class NewPhoneStockSummaryDto {
    private Long brandMapId;
    private Integer specType; // 0=Public, 1=Custom

    // Names
    private String brandName;
    private String seriesName;
    private String modelName;
    private String specName;

    // IDs (for public specs)
    private Long brandId;
    private Long seriesId;
    private Long modelId;
    private Long specId;

    // Counts
    private Long newPhoneCount;    // Total stock
    private Long unlistedCount;    // Stock with status 0
    private Long unsoldCount;      // Unsold (Total stock)

    public NewPhoneStockSummaryDto() {
    }

    public NewPhoneStockSummaryDto(Long brandMapId, Integer specType, 
                                   String brandName, String seriesName, String modelName, String specName,
                                   Long brandId, Long seriesId, Long modelId, Long specId,
                                   Long newPhoneCount, Long unlistedCount, Long unsoldCount) {
        this.brandMapId = brandMapId;
        this.specType = specType;
        this.brandName = brandName;
        this.seriesName = seriesName;
        this.modelName = modelName;
        this.specName = specName;
        this.brandId = brandId;
        this.seriesId = seriesId;
        this.modelId = modelId;
        this.specId = specId;
        this.newPhoneCount = newPhoneCount;
        this.unlistedCount = unlistedCount;
        this.unsoldCount = unsoldCount;
    }

    // Getters and Setters
    public Long getBrandMapId() { return brandMapId; }
    public void setBrandMapId(Long brandMapId) { this.brandMapId = brandMapId; }

    public Integer getSpecType() { return specType; }
    public void setSpecType(Integer specType) { this.specType = specType; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public String getSeriesName() { return seriesName; }
    public void setSeriesName(String seriesName) { this.seriesName = seriesName; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getSpecName() { return specName; }
    public void setSpecName(String specName) { this.specName = specName; }

    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }

    public Long getSeriesId() { return seriesId; }
    public void setSeriesId(Long seriesId) { this.seriesId = seriesId; }

    public Long getModelId() { return modelId; }
    public void setModelId(Long modelId) { this.modelId = modelId; }

    public Long getSpecId() { return specId; }
    public void setSpecId(Long specId) { this.specId = specId; }

    public Long getNewPhoneCount() { return newPhoneCount; }
    public void setNewPhoneCount(Long newPhoneCount) { this.newPhoneCount = newPhoneCount; }

    public Long getUnlistedCount() { return unlistedCount; }
    public void setUnlistedCount(Long unlistedCount) { this.unlistedCount = unlistedCount; }

    public Long getUnsoldCount() { return unsoldCount; }
    public void setUnsoldCount(Long unsoldCount) { this.unsoldCount = unsoldCount; }
}
