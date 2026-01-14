package com.msi.admin.service;

import com.msi.admin.domain.Merchant;
import com.msi.admin.domain.MerchantMemberInfo;
import com.msi.admin.domain.MerchantMemberRecharge;
import com.msi.admin.domain.MerchantPhoneProduct;
import com.msi.admin.repository.MerchantMemberInfoRepository;
import com.msi.admin.repository.MerchantMemberRechargeRepository;
import com.msi.admin.repository.MerchantPhoneProductRepository;
import com.msi.admin.repository.MerchantRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final MerchantRepository merchantRepository;
    private final MerchantPhoneProductRepository merchantPhoneProductRepository;
    private final MerchantMemberInfoRepository merchantMemberInfoRepository;
    private final MerchantMemberRechargeRepository merchantMemberRechargeRepository;

    public DashboardService(MerchantRepository merchantRepository,
                            MerchantPhoneProductRepository merchantPhoneProductRepository,
                            MerchantMemberInfoRepository merchantMemberInfoRepository,
                            MerchantMemberRechargeRepository merchantMemberRechargeRepository) {
        this.merchantRepository = merchantRepository;
        this.merchantPhoneProductRepository = merchantPhoneProductRepository;
        this.merchantMemberInfoRepository = merchantMemberInfoRepository;
        this.merchantMemberRechargeRepository = merchantMemberRechargeRepository;
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> result = new HashMap<>();

        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(29);
        LocalDateTime fromDateTime = thirtyDaysAgo.atStartOfDay();

        List<LocalDate> dateRange = new ArrayList<>();
        for (LocalDate d = thirtyDaysAgo; !d.isAfter(today); d = d.plusDays(1)) {
            dateRange.add(d);
        }

        List<Merchant> merchants = merchantRepository.findAll();
        List<MerchantPhoneProduct> products = merchantPhoneProductRepository.findAll();
        List<MerchantMemberInfo> memberInfos = merchantMemberInfoRepository.findAll();
        List<MerchantMemberRecharge> recharges = merchantMemberRechargeRepository.findAll();

        List<Merchant> validMerchants = merchants.stream()
                .filter(m -> m.getIsValid() == null || m.getIsValid() == 1)
                .toList();
        List<MerchantPhoneProduct> validProducts = products.stream()
                .filter(p -> p.getIsValid() == null || p.getIsValid() == 1)
                .toList();
        List<MerchantMemberInfo> validMemberInfos = memberInfos.stream()
                .filter(info -> info.getIsValid() == null || info.getIsValid() == 1)
                .toList();
        List<MerchantMemberRecharge> validRecharges = recharges.stream()
                .filter(r -> r.getIsValid() == null || r.getIsValid() == 1)
                .toList();

        Map<LocalDate, Long> merchantDailyCount = validMerchants.stream()
                .filter(m -> m.getCreateTime() != null && !m.getCreateTime().isBefore(fromDateTime))
                .collect(Collectors.groupingBy(m -> m.getCreateTime().toLocalDate(), Collectors.counting()));
        long merchantTotalCount = validMerchants.size();
        long merchantBaseCount = validMerchants.stream()
                .filter(m -> m.getCreateTime() == null || m.getCreateTime().isBefore(fromDateTime))
                .count();

        Map<LocalDate, Long> productDailyCount = validProducts.stream()
                .filter(p -> productEventTime(p) != null && !productEventTime(p).isBefore(fromDateTime))
                .collect(Collectors.groupingBy(p -> productEventTime(p).toLocalDate(), Collectors.counting()));
        long productTotalCount = validProducts.size();
        long productBaseCount = validProducts.stream()
                .filter(p -> productEventTime(p) == null || productEventTime(p).isBefore(fromDateTime))
                .count();

        Map<LocalDate, Long> memberDailyCount = validMemberInfos.stream()
                .filter(info -> memberEventTime(info) != null && !memberEventTime(info).isBefore(fromDateTime))
                .collect(Collectors.groupingBy(info -> memberEventTime(info).toLocalDate(), Collectors.counting()));
        long memberTotalCount = validMemberInfos.size();
        long memberBaseCount = validMemberInfos.stream()
                .filter(info -> memberEventTime(info) == null || memberEventTime(info).isBefore(fromDateTime))
                .count();

        Map<LocalDate, BigDecimal> rechargeDailyAmount = validRecharges.stream()
                .filter(r -> r.getRechargeTime() != null && !r.getRechargeTime().isBefore(fromDateTime))
                .collect(Collectors.groupingBy(r -> r.getRechargeTime().toLocalDate(),
                        Collectors.mapping(r -> r.getRechargeAmount() != null ? r.getRechargeAmount() : BigDecimal.ZERO,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
        BigDecimal rechargeTotalAmount = validRecharges.stream()
                .map(r -> r.getRechargeAmount() != null ? r.getRechargeAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal rechargeBaseAmount = validRecharges.stream()
                .filter(r -> r.getRechargeTime() == null || r.getRechargeTime().isBefore(fromDateTime))
                .map(r -> r.getRechargeAmount() != null ? r.getRechargeAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> merchantStats = buildCountSeries(dateRange, merchantDailyCount, merchantTotalCount, merchantBaseCount);
        Map<String, Object> productStats = buildCountSeries(dateRange, productDailyCount, productTotalCount, productBaseCount);
        Map<String, Object> memberStats = buildCountSeries(dateRange, memberDailyCount, memberTotalCount, memberBaseCount);
        Map<String, Object> rechargeStats = buildAmountSeries(dateRange, rechargeDailyAmount, rechargeTotalAmount, rechargeBaseAmount);

        result.put("merchant", merchantStats);
        result.put("product", productStats);
        result.put("member", memberStats);
        result.put("recharge", rechargeStats);

        return result;
    }

    private static Map<String, Object> buildCountSeries(List<LocalDate> dates,
                                                        Map<LocalDate, Long> dailyCount,
                                                        long totalCount,
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
            currentTotal += dayCount;
            cumulative.add(currentTotal);
        }

        data.put("dates", xAxis);
        data.put("daily", daily);
        data.put("total", totalCount);
        data.put("cumulative", cumulative);
        return data;
    }

    private static Map<String, Object> buildAmountSeries(List<LocalDate> dates,
                                                         Map<LocalDate, BigDecimal> dailyAmount,
                                                         BigDecimal totalAmount,
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
            currentTotal = currentTotal.add(dayAmount);
            cumulative.add(currentTotal);
        }

        data.put("dates", xAxis);
        data.put("daily", daily);
        data.put("total", totalAmount);
        data.put("cumulative", cumulative);
        return data;
    }

    private static LocalDateTime productEventTime(MerchantPhoneProduct product) {
        if (product.getListingTime() != null) {
            return product.getListingTime();
        }
        return product.getCreateTime();
    }

    private static LocalDateTime memberEventTime(MerchantMemberInfo info) {
        if (info.getStartDate() != null) {
            return info.getStartDate();
        }
        return info.getCreateTime();
    }
}
