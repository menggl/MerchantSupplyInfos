package com.msi.repository;

import com.msi.domain.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductRepositoryImpl implements ProductRepositoryCustom {

    @Autowired
    private EntityManager entityManager;

    @Override
    public Page<Product> findAvailableProductsWithRoundRobin(
            String cityCode,
            Integer productType,
            Long brandId,
            Long seriesId,
            Long modelId,
            Long specId,
            Integer minPrice,
            Integer maxPrice,
            Pageable pageable) {

        StringBuilder whereClause = new StringBuilder(" WHERE is_valid = 1 AND state = 1 AND product_type = :productType");
        Map<String, Object> params = new HashMap<>();
        params.put("productType", productType);

        if (cityCode != null && !cityCode.isEmpty() && !"000000".equals(cityCode) && !"全国".equals(cityCode)) {
            whereClause.append(" AND city_code = :cityCode");
            params.put("cityCode", cityCode);
        }

        // Logic for brand/series/model/spec filters
        // If brandId is -1 (unlimited), we skip these filters
        if (brandId != null && !Long.valueOf(-1).equals(brandId)) {
            whereClause.append(" AND brand_id = :brandId");
            params.put("brandId", brandId);

            if (seriesId != null) {
                whereClause.append(" AND series_id = :seriesId");
                params.put("seriesId", seriesId);
            }
            if (modelId != null) {
                whereClause.append(" AND model_id = :modelId");
                params.put("modelId", modelId);
            }
            if (specId != null) {
                whereClause.append(" AND spec_id = :specId");
                params.put("specId", specId);
            }
        }

        if (minPrice != null) {
            whereClause.append(" AND price >= :minPrice");
            params.put("minPrice", minPrice);
        }
        if (maxPrice != null) {
            whereClause.append(" AND price <= :maxPrice");
            params.put("maxPrice", maxPrice);
        }

        // Count query
        String countSql = "SELECT COUNT(*) FROM merchant_phone_product " + whereClause.toString();
        Query countQuery = entityManager.createNativeQuery(countSql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        long total = ((Number) countQuery.getSingleResult()).longValue();

        if (total == 0) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        // Fetch IDs with Round Robin logic
        // We select only ID from the subquery to avoid mapping issues
        String idSql = "SELECT t.id FROM (" +
                "  SELECT id, ROW_NUMBER() OVER (PARTITION BY merchant_id ORDER BY update_time DESC) as rn, update_time " +
                "  FROM merchant_phone_product " +
                whereClause.toString() +
                ") t " +
                "ORDER BY rn ASC, update_time DESC";

        Query idQuery = entityManager.createNativeQuery(idSql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            idQuery.setParameter(entry.getKey(), entry.getValue());
        }
        idQuery.setFirstResult((int) pageable.getOffset());
        idQuery.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Long> ids = idQuery.getResultList();
        
        // Convert IDs from whatever type the DB returns (BigInteger usually) to Long
        List<Long> longIds = new ArrayList<>();
        for(Object id : ids) {
            longIds.add(((Number)id).longValue());
        }

        if (longIds.isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageable, total);
        }

        // Fetch entities by IDs
        // Note: findByIds doesn't guarantee order, so we need to re-sort
        String entitySql = "SELECT * FROM merchant_phone_product WHERE id IN (:ids)";
        Query entityQuery = entityManager.createNativeQuery(entitySql, Product.class);
        entityQuery.setParameter("ids", longIds);
        
        @SuppressWarnings("unchecked")
        List<Product> products = entityQuery.getResultList();

        // Re-sort products based on the order of ids
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        
        List<Product> sortedProducts = new ArrayList<>();
        for (Long id : longIds) {
            if (productMap.containsKey(id)) {
                sortedProducts.add(productMap.get(id));
            }
        }

        return new PageImpl<>(sortedProducts, pageable, total);
    }
}
