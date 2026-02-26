package com.msi.repository;

import com.msi.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {
    Page<Product> findAvailableProductsWithRoundRobin(
            String cityCode,
            Integer productType,
            Long brandId,
            Long seriesId,
            Long modelId,
            Long specId,
            Integer minPrice,
            Integer maxPrice,
            Pageable pageable);
}
