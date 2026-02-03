package com.msi.dto;

import java.util.List;

public class AddNewPhoneStockRequest {

    // Brand/Series/Model/Spec Info
    private Long brandId;
    private String brandName;
    private Long seriesId;
    private String seriesName;
    private Long modelId;
    private String modelName;
    private Long specId;
    private String specName;

    // Must be 0 for New Phone
    private Integer productType;

    // Remarks
    private Long remarkId;
    private Long otherRemarkId;

    // Prices
    private Integer price; // Selling Price (分)
    private Integer actualPayment; // Actual Payment (分)
    private Integer actualReceipt; // Actual Receipt (分)

    // Stock Price/Count Records
    private List<PriceCountDto> inPrices;
    private List<PriceCountDto> outPrices;

    public static class PriceCountDto {
        private Integer price;
        private Integer count;

        public Integer getPrice() {
            return price;
        }

        public void setPrice(Integer price) {
            this.price = price;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public Long getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(Long seriesId) {
        this.seriesId = seriesId;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Long getSpecId() {
        return specId;
    }

    public void setSpecId(Long specId) {
        this.specId = specId;
    }

    public String getSpecName() {
        return specName;
    }

    public void setSpecName(String specName) {
        this.specName = specName;
    }

    public Integer getProductType() {
        return productType;
    }

    public void setProductType(Integer productType) {
        this.productType = productType;
    }

    public Long getRemarkId() {
        return remarkId;
    }

    public void setRemarkId(Long remarkId) {
        this.remarkId = remarkId;
    }

    public Long getOtherRemarkId() {
        return otherRemarkId;
    }

    public void setOtherRemarkId(Long otherRemarkId) {
        this.otherRemarkId = otherRemarkId;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getActualPayment() {
        return actualPayment;
    }

    public void setActualPayment(Integer actualPayment) {
        this.actualPayment = actualPayment;
    }

    public Integer getActualReceipt() {
        return actualReceipt;
    }

    public void setActualReceipt(Integer actualReceipt) {
        this.actualReceipt = actualReceipt;
    }

    public List<PriceCountDto> getInPrices() {
        return inPrices;
    }

    public void setInPrices(List<PriceCountDto> inPrices) {
        this.inPrices = inPrices;
    }

    public List<PriceCountDto> getOutPrices() {
        return outPrices;
    }

    public void setOutPrices(List<PriceCountDto> outPrices) {
        this.outPrices = outPrices;
    }
}
