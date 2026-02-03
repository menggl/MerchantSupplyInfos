package com.msi.repository;

import com.msi.dto.NewPhoneStockSummaryDto;
import com.msi.domain.MerchantNewPhoneStock;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MerchantNewPhoneStockRepositoryImpl implements MerchantNewPhoneStockRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<NewPhoneStockSummaryDto> findSummaries(Long merchantId, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("s.brand_map_id, ");
        sql.append("MAX(mbm.spec_type) as spec_type, ");
        sql.append("MAX(COALESCE(b.name, mcb.brand_name)) as brand_name, ");
        sql.append("MAX(COALESCE(pse.name, mcb.series_name)) as series_name, ");
        sql.append("MAX(COALESCE(pm.name, mcb.model_name)) as model_name, ");
        sql.append("MAX(COALESCE(ps.name, mcb.spec_name)) as spec_name, ");
        sql.append("MAX(b.id) as brand_id, ");
        sql.append("MAX(pse.id) as series_id, ");
        sql.append("MAX(pm.id) as model_id, ");
        sql.append("MAX(ps.id) as spec_id, ");
        sql.append("SUM(s.stock_count) as total_stock, ");
        sql.append("SUM(CASE WHEN s.stock_status = 0 THEN s.stock_count ELSE 0 END) as unlisted_stock, ");
        sql.append("SUM(s.stock_count) as unsold_stock "); // unsold = total stock in current inventory
        
        sql.append("FROM merchant_new_phone_stock s ");
        sql.append("JOIN merchant_brand_map mbm ON s.brand_map_id = mbm.id ");
        // Public Path
        sql.append("LEFT JOIN phone_spec ps ON mbm.phone_spec_id = ps.id AND mbm.spec_type = 0 ");
        sql.append("LEFT JOIN phone_model pm ON ps.model_id = pm.id ");
        sql.append("LEFT JOIN phone_series pse ON pm.series_id = pse.id ");
        sql.append("LEFT JOIN brand b ON pse.brand_id = b.id ");
        // Custom Path
        sql.append("LEFT JOIN merchant_custom_brand mcb ON mbm.custom_brand_id = mcb.id AND mbm.spec_type = 1 ");
        
        sql.append("WHERE s.merchant_id = :merchantId AND s.valid = 1 ");
        sql.append("GROUP BY s.brand_map_id ");
        sql.append("ORDER BY MAX(s.create_time) DESC");

        // Count Query
        String countSql = "SELECT COUNT(*) FROM (SELECT s.brand_map_id FROM merchant_new_phone_stock s WHERE s.merchant_id = :merchantId AND s.valid = 1 GROUP BY s.brand_map_id) as count_table";
        Query countQuery = entityManager.createNativeQuery(countSql);
        countQuery.setParameter("merchantId", merchantId);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        // Main Query
        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("merchantId", merchantId);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Object[]> results = query.getResultList();
        List<NewPhoneStockSummaryDto> dtos = new ArrayList<>();

        for (Object[] row : results) {
            Long brandMapId = ((Number) row[0]).longValue();
            Integer specType = row[1] != null ? ((Number) row[1]).intValue() : null;
            String brandName = (String) row[2];
            String seriesName = (String) row[3];
            String modelName = (String) row[4];
            String specName = (String) row[5];
            Long brandId = row[6] != null ? ((Number) row[6]).longValue() : null;
            Long seriesId = row[7] != null ? ((Number) row[7]).longValue() : null;
            Long modelId = row[8] != null ? ((Number) row[8]).longValue() : null;
            Long specId = row[9] != null ? ((Number) row[9]).longValue() : null;
            Long totalStock = row[10] != null ? ((Number) row[10]).longValue() : 0L;
            Long unlistedStock = row[11] != null ? ((Number) row[11]).longValue() : 0L;
            Long unsoldCount = row[12] != null ? ((Number) row[12]).longValue() : 0L;

            dtos.add(new NewPhoneStockSummaryDto(
                brandMapId, specType, 
                brandName, seriesName, modelName, specName, 
                brandId, seriesId, modelId, specId, 
                totalStock, unlistedStock, unsoldCount
            ));
        }

        return new PageImpl<>(dtos, pageable, total);
    }

    @Override
    public List<NewPhoneStockSummaryDto> findAllSummaries(Long merchantId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("s.brand_map_id, ");
        sql.append("MAX(mbm.spec_type) as spec_type, ");
        sql.append("MAX(COALESCE(b.name, mcb.brand_name)) as brand_name, ");
        sql.append("MAX(COALESCE(pse.name, mcb.series_name)) as series_name, ");
        sql.append("MAX(COALESCE(pm.name, mcb.model_name)) as model_name, ");
        sql.append("MAX(COALESCE(ps.name, mcb.spec_name)) as spec_name, ");
        sql.append("MAX(b.id) as brand_id, ");
        sql.append("MAX(pse.id) as series_id, ");
        sql.append("MAX(pm.id) as model_id, ");
        sql.append("MAX(ps.id) as spec_id, ");
        sql.append("SUM(s.stock_count) as total_stock, ");
        sql.append("SUM(CASE WHEN s.stock_status = 0 THEN s.stock_count ELSE 0 END) as unlisted_stock, ");
        sql.append("SUM(s.stock_count) as unsold_stock "); // unsold = total stock in current inventory
        
        sql.append("FROM merchant_new_phone_stock s ");
        sql.append("JOIN merchant_brand_map mbm ON s.brand_map_id = mbm.id ");
        // Public Path
        sql.append("LEFT JOIN phone_spec ps ON mbm.phone_spec_id = ps.id AND mbm.spec_type = 0 ");
        sql.append("LEFT JOIN phone_model pm ON ps.model_id = pm.id ");
        sql.append("LEFT JOIN phone_series pse ON pm.series_id = pse.id ");
        sql.append("LEFT JOIN brand b ON pse.brand_id = b.id ");
        // Custom Path
        sql.append("LEFT JOIN merchant_custom_brand mcb ON mbm.custom_brand_id = mcb.id AND mbm.spec_type = 1 ");
        
        sql.append("WHERE s.merchant_id = :merchantId AND s.valid = 1 ");
        sql.append("GROUP BY s.brand_map_id ");
        // Sorting: Brand Name, Series Name, Model Name, Spec Name (Ascending)
        sql.append("ORDER BY brand_name ASC, series_name ASC, model_name ASC, spec_name ASC");

        // Main Query
        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("merchantId", merchantId);

        List<Object[]> results = query.getResultList();
        List<NewPhoneStockSummaryDto> dtos = new ArrayList<>();

        for (Object[] row : results) {
            Long brandMapId = ((Number) row[0]).longValue();
            Integer specType = row[1] != null ? ((Number) row[1]).intValue() : null;
            String brandName = (String) row[2];
            String seriesName = (String) row[3];
            String modelName = (String) row[4];
            String specName = (String) row[5];
            Long brandId = row[6] != null ? ((Number) row[6]).longValue() : null;
            Long seriesId = row[7] != null ? ((Number) row[7]).longValue() : null;
            Long modelId = row[8] != null ? ((Number) row[8]).longValue() : null;
            Long specId = row[9] != null ? ((Number) row[9]).longValue() : null;
            Long totalStock = row[10] != null ? ((Number) row[10]).longValue() : 0L;
            Long unlistedStock = row[11] != null ? ((Number) row[11]).longValue() : 0L;
            Long unsoldCount = row[12] != null ? ((Number) row[12]).longValue() : 0L;

            dtos.add(new NewPhoneStockSummaryDto(
                brandMapId, specType, 
                brandName, seriesName, modelName, specName, 
                brandId, seriesId, modelId, specId, 
                totalStock, unlistedStock, unsoldCount
            ));
        }

        return dtos;
    }

    @Override
    public Page<MerchantNewPhoneStock> search(
            Long merchantId, 
            Long brandId, String brandName,
            Long seriesId, String seriesName,
            Long modelId, String modelName,
            Long specId, String specName,
            Pageable pageable) {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT s.* FROM merchant_new_phone_stock s ");
        sql.append("LEFT JOIN merchant_brand_map mbm ON s.brand_map_id = mbm.id ");
        // Public Path
        sql.append("LEFT JOIN phone_spec ps ON mbm.phone_spec_id = ps.id AND mbm.spec_type = 0 ");
        sql.append("LEFT JOIN phone_model pm ON ps.model_id = pm.id ");
        sql.append("LEFT JOIN phone_series pse ON pm.series_id = pse.id ");
        sql.append("LEFT JOIN brand b ON pse.brand_id = b.id ");
        // Custom Path
        sql.append("LEFT JOIN merchant_custom_brand mcb ON mbm.custom_brand_id = mcb.id AND mbm.spec_type = 1 ");
        
        sql.append("WHERE s.merchant_id = :merchantId AND s.valid = 1 ");

        Map<String, Object> params = new HashMap<>();
        params.put("merchantId", merchantId);

        // Brand Filter
        if (brandId != null || (brandName != null && !brandName.isEmpty())) {
            sql.append("AND (");
            boolean added = false;
            if (brandId != null) {
                sql.append("((s.spec_type = 0 AND b.id = :brandId) OR (s.spec_type = 1 AND mcb.brand_id = :brandId))");
                params.put("brandId", brandId);
                added = true;
            }
            if (brandName != null && !brandName.isEmpty()) {
                if (added) sql.append(" OR ");
                sql.append("(s.spec_type = 1 AND mcb.brand_name = :brandName)");
                params.put("brandName", brandName);
            }
            sql.append(") ");
        }

        // Series Filter
        if (seriesId != null || (seriesName != null && !seriesName.isEmpty())) {
            sql.append("AND (");
            boolean added = false;
            if (seriesId != null) {
                sql.append("((s.spec_type = 0 AND pse.id = :seriesId) OR (s.spec_type = 1 AND mcb.series_id = :seriesId))");
                params.put("seriesId", seriesId);
                added = true;
            }
            if (seriesName != null && !seriesName.isEmpty()) {
                if (added) sql.append(" OR ");
                sql.append("(s.spec_type = 1 AND mcb.series_name = :seriesName)");
                params.put("seriesName", seriesName);
            }
            sql.append(") ");
        }

        // Model Filter
        if (modelId != null || (modelName != null && !modelName.isEmpty())) {
            sql.append("AND (");
            boolean added = false;
            if (modelId != null) {
                sql.append("((s.spec_type = 0 AND pm.id = :modelId) OR (s.spec_type = 1 AND mcb.model_id = :modelId))");
                params.put("modelId", modelId);
                added = true;
            }
            if (modelName != null && !modelName.isEmpty()) {
                if (added) sql.append(" OR ");
                sql.append("(s.spec_type = 1 AND mcb.model_name = :modelName)");
                params.put("modelName", modelName);
            }
            sql.append(") ");
        }

        // Spec Filter
        if (specId != null || (specName != null && !specName.isEmpty())) {
            sql.append("AND (");
            boolean added = false;
            if (specId != null) {
                // Assuming custom spec doesn't map to public spec ID usually, but if it does (via spec_id column)
                sql.append("((s.spec_type = 0 AND ps.id = :specId) OR (s.spec_type = 1 AND mcb.spec_id = :specId))");
                params.put("specId", specId);
                added = true;
            }
            if (specName != null && !specName.isEmpty()) {
                if (added) sql.append(" OR ");
                sql.append("(s.spec_type = 1 AND mcb.spec_name = :specName)");
                params.put("specName", specName);
            }
            sql.append(") ");
        }

        // Count Query
        String countSql = "SELECT COUNT(*) FROM (" + sql.toString() + ") as count_table";
        Query countQuery = entityManager.createNativeQuery(countSql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        long total = ((Number) countQuery.getSingleResult()).longValue();

        // Sorting
        // 1. Off-shelf (status=0) & Stock>0
        // 2. On-shelf (status=1) & Stock>0
        // 3. Stock=0 (Any status)
        sql.append("ORDER BY ");
        sql.append("CASE ");
        sql.append("WHEN s.stock_count > 0 AND s.stock_status = 0 THEN 1 ");
        sql.append("WHEN s.stock_count > 0 AND s.stock_status = 1 THEN 2 ");
        sql.append("ELSE 3 ");
        sql.append("END ASC, ");
        sql.append("s.create_time DESC");

        // Main Query
        Query query = entityManager.createNativeQuery(sql.toString(), MerchantNewPhoneStock.class);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<MerchantNewPhoneStock> resultList = query.getResultList();

        return new PageImpl<>(resultList, pageable, total);
    }
}
