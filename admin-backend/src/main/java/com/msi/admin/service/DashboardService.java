package com.msi.admin.service;

import com.msi.admin.domain.DailyStatistics;
import com.msi.admin.repository.DailyStatisticsRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final DailyStatisticsRepository dailyStatisticsRepository;

    public DashboardService(DailyStatisticsRepository dailyStatisticsRepository) {
        this.dailyStatisticsRepository = dailyStatisticsRepository;
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> result = new HashMap<>();

        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(29);
        String startDateStr = thirtyDaysAgo.toString();
        String endDateStr = today.toString();

        List<DailyStatistics> statsList = dailyStatisticsRepository.findByStatisticsDateBetweenOrderByStatisticsDateAsc(startDateStr, endDateStr);
        
        // Base counts from before the period
        DailyStatistics baseStats = dailyStatisticsRepository.findTopByStatisticsDateLessThanOrderByStatisticsDateDesc(startDateStr).orElse(null);
        
        long merchantBase = baseStats != null && baseStats.getTotalValidMerchantCount() != null ? baseStats.getTotalValidMerchantCount() : 0L;
        long productBase = baseStats != null ? (baseStats.getNewProductTotalCount() != null ? baseStats.getNewProductTotalCount() : 0) + (baseStats.getSecondHandProductTotalCount() != null ? baseStats.getSecondHandProductTotalCount() : 0) : 0L;
        long memberBase = baseStats != null && baseStats.getTotalMemberCount() != null ? baseStats.getTotalMemberCount() : 0L;
        BigDecimal rechargeBase = baseStats != null && baseStats.getTotalRechargeAmount() != null ? new BigDecimal(baseStats.getTotalRechargeAmount()).divide(new BigDecimal(100)) : BigDecimal.ZERO;
        
        Map<LocalDate, Long> merchantDaily = new HashMap<>();
        Map<LocalDate, Long> merchantTotal = new HashMap<>();
        
        Map<LocalDate, Long> productDaily = new HashMap<>();
        Map<LocalDate, Long> productTotal = new HashMap<>();
        
        Map<LocalDate, Long> memberDaily = new HashMap<>();
        Map<LocalDate, Long> memberTotal = new HashMap<>();
        
        Map<LocalDate, BigDecimal> rechargeDaily = new HashMap<>();
        Map<LocalDate, BigDecimal> rechargeTotal = new HashMap<>();
        
        for (DailyStatistics stat : statsList) {
            LocalDate date = LocalDate.parse(stat.getStatisticsDate());
            
            // Merchant
            merchantDaily.put(date, stat.getNewValidMerchantCount() != null ? stat.getNewValidMerchantCount().longValue() : 0L);
            merchantTotal.put(date, stat.getTotalValidMerchantCount() != null ? stat.getTotalValidMerchantCount().longValue() : null);
            
            // Product
            long dailyProd = (stat.getNewProductNewCount() != null ? stat.getNewProductNewCount() : 0) + (stat.getSecondHandProductNewCount() != null ? stat.getSecondHandProductNewCount() : 0);
            productDaily.put(date, dailyProd);
            long totalProd = (stat.getNewProductTotalCount() != null ? stat.getNewProductTotalCount() : 0) + (stat.getSecondHandProductTotalCount() != null ? stat.getSecondHandProductTotalCount() : 0);
            productTotal.put(date, totalProd);
            
            // Member
            memberDaily.put(date, stat.getDailyNewMemberCount() != null ? stat.getDailyNewMemberCount().longValue() : 0L);
            memberTotal.put(date, stat.getTotalMemberCount() != null ? stat.getTotalMemberCount().longValue() : null);
            
            // Recharge
            rechargeDaily.put(date, stat.getDailyRechargeAmount() != null ? new BigDecimal(stat.getDailyRechargeAmount()).divide(new BigDecimal(100)) : BigDecimal.ZERO);
            rechargeTotal.put(date, stat.getTotalRechargeAmount() != null ? new BigDecimal(stat.getTotalRechargeAmount()).divide(new BigDecimal(100)) : null);
        }
        
        List<LocalDate> dateRange = new ArrayList<>();
        for (LocalDate d = thirtyDaysAgo; !d.isAfter(today); d = d.plusDays(1)) {
            dateRange.add(d);
        }
        
        Map<String, Object> merchantStats = buildCountSeries(dateRange, merchantDaily, merchantTotal, merchantBase);
        Map<String, Object> productStats = buildCountSeries(dateRange, productDaily, productTotal, productBase);
        Map<String, Object> memberStats = buildCountSeries(dateRange, memberDaily, memberTotal, memberBase);
        Map<String, Object> rechargeStats = buildAmountSeries(dateRange, rechargeDaily, rechargeTotal, rechargeBase);
        
        result.put("merchant", merchantStats);
        result.put("product", productStats);
        result.put("member", memberStats);
        result.put("recharge", rechargeStats);
        
        return result;
    }

    private static Map<String, Object> buildCountSeries(List<LocalDate> dates,
                                                        Map<LocalDate, Long> dailyCount,
                                                        Map<LocalDate, Long> totalCount,
                                                        long baseCount) {
        Map<String, Object> data = new LinkedHashMap<>();
        List<String> xAxis = new ArrayList<>();
        List<Long> daily = new ArrayList<>();
        List<Long> cumulative = new ArrayList<>();

        long currentTotal = baseCount;
        for (LocalDate d : dates) {
            xAxis.add(d.toString());
            long dayCount = dailyCount.getOrDefault(d, 0L);
            daily.add(dayCount);
            
            if (totalCount.containsKey(d) && totalCount.get(d) != null) {
                currentTotal = totalCount.get(d);
            } else {
                currentTotal += dayCount;
            }
            cumulative.add(currentTotal);
        }

        data.put("dates", xAxis);
        data.put("daily", daily);
        data.put("total", currentTotal);
        data.put("cumulative", cumulative);
        return data;
    }

    private static Map<String, Object> buildAmountSeries(List<LocalDate> dates,
                                                         Map<LocalDate, BigDecimal> dailyAmount,
                                                         Map<LocalDate, BigDecimal> totalAmount,
                                                         BigDecimal baseAmount) {
        Map<String, Object> data = new LinkedHashMap<>();
        List<String> xAxis = new ArrayList<>();
        List<BigDecimal> daily = new ArrayList<>();
        List<BigDecimal> cumulative = new ArrayList<>();

        BigDecimal currentTotal = baseAmount != null ? baseAmount : BigDecimal.ZERO;
        for (LocalDate d : dates) {
            xAxis.add(d.toString());
            BigDecimal dayAmount = dailyAmount.getOrDefault(d, BigDecimal.ZERO);
            daily.add(dayAmount);
            
            if (totalAmount.containsKey(d) && totalAmount.get(d) != null) {
                currentTotal = totalAmount.get(d);
            } else {
                currentTotal = currentTotal.add(dayAmount);
            }
            cumulative.add(currentTotal);
        }

        data.put("dates", xAxis);
        data.put("daily", daily);
        data.put("total", currentTotal);
        data.put("cumulative", cumulative);
        return data;
    }
}
