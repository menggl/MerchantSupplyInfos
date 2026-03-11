package com.msi.admin.service;

import com.msi.admin.domain.CityDict;
import com.msi.admin.domain.Merchant;
import com.msi.admin.domain.MerchantMemberInfo;
import com.msi.admin.domain.MerchantMemberIntegral;
import com.msi.admin.domain.MerchantMemberIntegralSpend;
import com.msi.admin.domain.MerchantRechargeOrder;
import com.msi.admin.repository.CityDictRepository;
import com.msi.admin.repository.MerchantMemberInfoRepository;
import com.msi.admin.repository.MerchantMemberIntegralRepository;
import com.msi.admin.repository.MerchantMemberIntegralSpendRepository;
import com.msi.admin.repository.MerchantRechargeOrderRepository;
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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import java.util.stream.Collectors;

@Service
public class MerchantService {
    private final MerchantRepository merchantRepository;
    private final MerchantMemberInfoRepository merchantMemberInfoRepository;
    private final MerchantRechargeOrderRepository merchantRechargeOrderRepository;
    private final MerchantMemberIntegralRepository merchantMemberIntegralRepository;
    private final MerchantMemberIntegralSpendRepository merchantMemberIntegralSpendRepository;
    private final CityDictRepository cityDictRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public MerchantService(
        MerchantRepository merchantRepository,
        MerchantMemberInfoRepository merchantMemberInfoRepository,
        MerchantRechargeOrderRepository merchantRechargeOrderRepository,
        MerchantMemberIntegralRepository merchantMemberIntegralRepository,
        MerchantMemberIntegralSpendRepository merchantMemberIntegralSpendRepository,
        CityDictRepository cityDictRepository
    ) {
        this.merchantRepository = merchantRepository;
        this.merchantMemberInfoRepository = merchantMemberInfoRepository;
        this.merchantRechargeOrderRepository = merchantRechargeOrderRepository;
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
        detail.put("invitationCode", merchant.getInvitationCode());
        long invitationCount = 0L;
        if (merchant.getInvitationCode() != null && !merchant.getInvitationCode().isEmpty()) {
            Object v = entityManager.createNativeQuery("SELECT COUNT(*) FROM merchant_invitation_code WHERE invitation_code = ? AND is_valid = 1")
                    .setParameter(1, merchant.getInvitationCode())
                    .getSingleResult();
            if (v instanceof Number) {
                invitationCount = ((Number) v).longValue();
            } else if (v != null) {
                try {
                    invitationCount = Long.parseLong(v.toString());
                } catch (Exception ignore) {}
            }
        }
        detail.put("invitationCount", invitationCount);

        List<MerchantRechargeOrder> rechargeRecords = merchantRechargeOrderRepository.findByMerchantIdOrderByCreateTimeDesc(id);
        detail.put("rechargeRecords", rechargeRecords.stream().map(record -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", record.getId());
            item.put("rechargeAmount", record.getTotalAmount() != null ? new BigDecimal(record.getTotalAmount()).divide(new BigDecimal(100)) : BigDecimal.ZERO);
            item.put("originalPrice", record.getTotalAmount() != null ? new BigDecimal(record.getTotalAmount()).divide(new BigDecimal(100)) : BigDecimal.ZERO);
            item.put("discountAmount", BigDecimal.ZERO);
            item.put("rechargeType", record.getRechargeType());
            item.put("memberMonths", record.getMemberMonths());
            item.put("integralAmount", record.getIntegralAmount());
            item.put("rechargeTime", record.getCreateTime());
            item.put("isValid", record.getStatus() == 1 ? 1 : 0);
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
    public boolean updateInvitationCode(Long id, String invitationCode) {
        return merchantRepository.findById(id).map(merchant -> {
            merchant.setInvitationCode(invitationCode);
            merchantRepository.save(merchant);
            return true;
        }).orElse(false);
    }

    @Transactional
    public boolean deleteInvitationCode(Long id) {
        return merchantRepository.findById(id).map(merchant -> {
            merchant.setInvitationCode(null);
            merchantRepository.save(merchant);
            return true;
        }).orElse(false);
    }

    @Transactional
    public String rotateInvitationCode(Long id) {
        Merchant merchant = merchantRepository.findById(id).orElse(null);
        if (merchant == null) {
            return null;
        }
        String oldCode = merchant.getInvitationCode();
        String newCode = generateUniqueCode();
        if (oldCode != null && !oldCode.isEmpty()) {
            entityManager.createNativeQuery("UPDATE merchant_invitation_code SET invitation_code = ? WHERE invitation_code = ?")
                    .setParameter(1, newCode)
                    .setParameter(2, oldCode)
                    .executeUpdate();
        }
        merchant.setInvitationCode(newCode);
        merchantRepository.save(merchant);
        return newCode;
    }

    private String generateUniqueCode() {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        for (;;) {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(rnd.nextInt(chars.length())));
            }
            String code = sb.toString();
            if (!merchantRepository.existsByInvitationCode(code)) {
                return code;
            }
        }
    }

    @Transactional
    public boolean updateMerchantStatus(Long id, Integer isValid) {
        return merchantRepository.findById(id).map(merchant -> {
            merchant.setIsValid(isValid);
            merchantRepository.save(merchant);
            return true;
        }).orElse(false);
    }

    public Page<MerchantMemberIntegralSpend> getMerchantIntegralLogs(Long merchantId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        return merchantMemberIntegralSpendRepository.findByMerchantIdOrderByChangeTimeDesc(merchantId, pageRequest);
    }
}
