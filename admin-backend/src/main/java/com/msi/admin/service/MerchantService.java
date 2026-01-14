package com.msi.admin.service;

import com.msi.admin.domain.CityDict;
import com.msi.admin.domain.Merchant;
import com.msi.admin.domain.MerchantMemberInfo;
import com.msi.admin.domain.MerchantMemberIntegral;
import com.msi.admin.domain.MerchantMemberIntegralSpend;
import com.msi.admin.domain.MerchantMemberRecharge;
import com.msi.admin.repository.CityDictRepository;
import com.msi.admin.repository.MerchantMemberInfoRepository;
import com.msi.admin.repository.MerchantMemberIntegralRepository;
import com.msi.admin.repository.MerchantMemberIntegralSpendRepository;
import com.msi.admin.repository.MerchantMemberRechargeRepository;
import com.msi.admin.repository.MerchantRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

@Service
public class MerchantService {
    private final MerchantRepository merchantRepository;
    private final MerchantMemberInfoRepository merchantMemberInfoRepository;
    private final MerchantMemberRechargeRepository merchantMemberRechargeRepository;
    private final MerchantMemberIntegralRepository merchantMemberIntegralRepository;
    private final MerchantMemberIntegralSpendRepository merchantMemberIntegralSpendRepository;
    private final CityDictRepository cityDictRepository;

    public MerchantService(
        MerchantRepository merchantRepository,
        MerchantMemberInfoRepository merchantMemberInfoRepository,
        MerchantMemberRechargeRepository merchantMemberRechargeRepository,
        MerchantMemberIntegralRepository merchantMemberIntegralRepository,
        MerchantMemberIntegralSpendRepository merchantMemberIntegralSpendRepository,
        CityDictRepository cityDictRepository
    ) {
        this.merchantRepository = merchantRepository;
        this.merchantMemberInfoRepository = merchantMemberInfoRepository;
        this.merchantMemberRechargeRepository = merchantMemberRechargeRepository;
        this.merchantMemberIntegralRepository = merchantMemberIntegralRepository;
        this.merchantMemberIntegralSpendRepository = merchantMemberIntegralSpendRepository;
        this.cityDictRepository = cityDictRepository;
    }

    public Map<String, Object> getMerchantDetail(Long id) {
        Merchant merchant = merchantRepository.findById(id).orElse(null);
        if (merchant == null) {
            return null;
        }

        Map<String, Object> detail = new HashMap<>();
        detail.put("id", merchant.getId());
        detail.put("wechatId", merchant.getWechatId());
        detail.put("wechatName", merchant.getWechatName());
        detail.put("merchantName", merchant.getMerchantName());
        detail.put("contactName", merchant.getContactName());
        detail.put("merchantPhone", merchant.getMerchantPhone());
        detail.put("cityCode", merchant.getCityCode());
        detail.put("cityName", cityDictRepository.findByCityCode(merchant.getCityCode()).map(CityDict::getCityName).orElse(merchant.getCityCode()));
        detail.put("merchantAddress", merchant.getMerchantAddress());
        detail.put("latitude", merchant.getLatitude());
        detail.put("longitude", merchant.getLongitude());
        
        MerchantMemberInfo memberInfo = merchant.getMemberInfo();
        detail.put("registrationDate", memberInfo != null ? memberInfo.getRegistrationDate() : null);
        detail.put("isMember", memberInfo != null ? memberInfo.getIsMember() : 0);
        detail.put("isValid", merchant.getIsValid());
        detail.put("businessLicenseUrl", merchant.getBusinessLicenseUrl());
        detail.put("memberExpireDate", memberInfo != null ? memberInfo.getEndDate() : null);
        detail.put("cancellationDate", memberInfo != null ? memberInfo.getCancellationDate() : null);
        detail.put("createTime", merchant.getCreateTime());
        detail.put("updateTime", merchant.getUpdateTime());

        List<MerchantMemberRecharge> rechargeRecords = merchantMemberRechargeRepository.findByMerchantIdOrderByRechargeTimeDesc(id);
        detail.put("rechargeRecords", rechargeRecords.stream().map(record -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", record.getId());
            item.put("rechargeAmount", record.getRechargeAmount());
            item.put("originalPrice", record.getOriginalPrice());
            item.put("discountAmount", record.getDiscountAmount());
            item.put("rechargeType", record.getRechargeType());
            item.put("rechargeTime", record.getRechargeTime());
            item.put("isValid", record.getIsValid());
            return item;
        }).collect(Collectors.toList()));

        MerchantMemberIntegral integral = merchantMemberIntegralRepository.findByMerchantId(id).orElse(null);
        detail.put("integral", integral != null ? integral.getIntegral() : 0);

        List<MerchantMemberIntegralSpend> integralRecords = merchantMemberIntegralSpendRepository.findByMerchantIdOrderByChangeTimeDesc(id);
        detail.put("integralRecords", integralRecords);

        return detail;
    }

    public Map<String, Object> listMerchants(int page, int size, String merchantName, String phone, LocalDateTime startDate, LocalDateTime endDate, Integer isMember, String cityCode) {
        List<CityDict> cities = cityDictRepository.findAll();
        Map<String, String> cityNameByCode = new HashMap<>();
        for (CityDict city : cities) {
            if (city.getCityCode() != null && city.getCityName() != null) {
                cityNameByCode.putIfAbsent(city.getCityCode(), city.getCityName());
            }
        }

        Specification<Merchant> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Merchant, MerchantMemberInfo> memberInfoJoin = root.join("memberInfo", JoinType.LEFT);

            if (merchantName != null && !merchantName.trim().isEmpty()) {
                predicates.add(cb.like(root.get("merchantName"), "%" + merchantName.trim() + "%"));
            }
            if (phone != null && !phone.trim().isEmpty()) {
                predicates.add(cb.like(root.get("merchantPhone"), "%" + phone.trim() + "%"));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(memberInfoJoin.get("registrationDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(memberInfoJoin.get("registrationDate"), endDate));
            }
            if (isMember != null) {
                if (isMember == 1) {
                    // isMember = 1 means endDate > now
                    predicates.add(cb.greaterThan(memberInfoJoin.get("endDate"), LocalDateTime.now()));
                } else {
                    // isMember = 0 means endDate <= now or endDate is null
                    Predicate endDateExpired = cb.lessThanOrEqualTo(memberInfoJoin.get("endDate"), LocalDateTime.now());
                    Predicate endDateNull = cb.isNull(memberInfoJoin.get("endDate"));
                    predicates.add(cb.or(endDateExpired, endDateNull));
                }
            }
            if (cityCode != null && !cityCode.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("cityCode"), cityCode.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Merchant> pageResult = merchantRepository.findAll(spec, PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id")));

        List<Map<String, Object>> list = new ArrayList<>();
        for (Merchant merchant : pageResult.getContent()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", merchant.getId());
            item.put("wechatId", merchant.getWechatId());
            item.put("merchantName", merchant.getMerchantName());
            item.put("contactName", merchant.getContactName());
            item.put("merchantPhone", merchant.getMerchantPhone());
            item.put("cityCode", merchant.getCityCode());
            item.put("cityName", cityNameByCode.getOrDefault(merchant.getCityCode(), merchant.getCityCode()));
            item.put("merchantAddress", merchant.getMerchantAddress());
            item.put("latitude", merchant.getLatitude());
            item.put("longitude", merchant.getLongitude());
            
            MerchantMemberInfo memberInfo = merchant.getMemberInfo();
            item.put("registrationDate", memberInfo != null ? memberInfo.getRegistrationDate() : null);
            item.put("isMember", memberInfo != null ? memberInfo.getIsMember() : 0);
            item.put("isValid", merchant.getIsValid());

            item.put("memberRegisterDate", memberInfo != null && Integer.valueOf(1).equals(memberInfo.getIsValid()) ? memberInfo.getStartDate() : null);
            item.put("memberExpireDate", memberInfo != null ? memberInfo.getEndDate() : null);

            list.add(item);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", pageResult.getTotalElements());
        return result;
    }

    @Transactional
    public boolean deleteMerchant(Long merchantId) {
        if (merchantId == null || !merchantRepository.existsById(merchantId)) {
            return false;
        }
        merchantMemberInfoRepository.deleteByMerchant_Id(merchantId);
        merchantRepository.deleteById(merchantId);
        return true;
    }

    @Transactional
    public boolean updateMerchantStatus(Long id, Integer isValid) {
        return merchantRepository.findById(id).map(merchant -> {
            merchant.setIsValid(isValid);
            merchantRepository.save(merchant);
            return true;
        }).orElse(false);
    }
}

