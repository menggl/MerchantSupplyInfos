package com.msi.repository;

import com.msi.domain.MerchantNewPhoneStock;
import com.msi.dto.NewPhoneStockSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MerchantNewPhoneStockRepositoryCustom {
    Page<MerchantNewPhoneStock> search(
            Long merchantId, 
            Long brandId, String brandName,
            Long seriesId, String seriesName,
            Long modelId, String modelName,
            Long specId, String specName,
            Pageable pageable);

    Page<NewPhoneStockSummaryDto> findSummaries(Long merchantId, Pageable pageable);

    List<NewPhoneStockSummaryDto> findAllSummaries(Long merchantId);
}
