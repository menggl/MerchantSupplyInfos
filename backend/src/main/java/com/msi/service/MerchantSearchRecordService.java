package com.msi.service;

import com.msi.domain.MerchantSearchRecord;
import com.msi.repository.MerchantSearchRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MerchantSearchRecordService {
    private static final Logger logger = LoggerFactory.getLogger(MerchantSearchRecordService.class);

    private final MerchantSearchRecordRepository repository;

    public MerchantSearchRecordService(MerchantSearchRecordRepository repository) {
        this.repository = repository;
    }

    @Async
    public void saveSearchRecord(Long merchantId, Integer productType, String cityCode,
                                 Long brandId, Long seriesId, Long modelId, Long specId, String keyword) {
        try {
            MerchantSearchRecord record = new MerchantSearchRecord();
            record.setMerchantId(merchantId);
            record.setProductType(productType);
            record.setCityCode(cityCode);
            record.setBrandId(brandId);
            record.setSeriesId(seriesId);
            record.setModelId(modelId);
            record.setSpecId(specId);
            record.setSearchKeyword(keyword);
            record.setSearchTime(LocalDateTime.now());
            
            repository.save(record);
        } catch (Exception e) {
            logger.error("Failed to save merchant search record", e);
        }
    }
}
