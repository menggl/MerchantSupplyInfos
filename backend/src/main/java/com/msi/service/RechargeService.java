package com.msi.service;

import com.msi.domain.Merchant;
import com.msi.domain.MerchantMemberInfo;
import com.msi.domain.MerchantMemberIntegral;
import com.msi.domain.MerchantMemberIntegralSpend;
import com.msi.enums.IntegralChangeReason;
import com.msi.repository.MerchantMemberInfoRepository;
import com.msi.repository.MerchantMemberIntegralRepository;
import com.msi.repository.MerchantMemberIntegralSpendRepository;
import com.msi.repository.MerchantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RechargeService {

    private final MerchantRepository merchantRepository;
    private final MerchantMemberIntegralRepository merchantMemberIntegralRepository;
    private final MerchantMemberIntegralSpendRepository merchantMemberIntegralSpendRepository;
    private final MerchantMemberInfoRepository memberInfoRepository;
    private final MerchantService merchantService;

    public RechargeService(MerchantRepository merchantRepository,
                           MerchantMemberIntegralRepository merchantMemberIntegralRepository,
                           MerchantMemberIntegralSpendRepository merchantMemberIntegralSpendRepository,
                           MerchantMemberInfoRepository memberInfoRepository,
                           MerchantService merchantService) {
        this.merchantRepository = merchantRepository;
        this.merchantMemberIntegralRepository = merchantMemberIntegralRepository;
        this.merchantMemberIntegralSpendRepository = merchantMemberIntegralSpendRepository;
        this.memberInfoRepository = memberInfoRepository;
        this.merchantService = merchantService;
    }

    @Transactional
    public void rechargeIntegral(Long merchantId, Integer integralAmount) {
        if (merchantId == null) {
            throw new IllegalArgumentException("商户ID不能为空");
        }
        if (integralAmount == null || integralAmount <= 0) {
            throw new IllegalArgumentException("充值积分必须大于0");
        }
        Optional<Merchant> opt = merchantRepository.findById(merchantId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("商户不存在");
        }
        MerchantMemberIntegral integral = merchantMemberIntegralRepository.findByMerchantId(merchantId).orElse(null);
        int before = 0;
        if (integral != null && integral.getIntegral() != null) {
            before = integral.getIntegral();
        }
        int after = before + integralAmount;
        if (integral == null) {
            integral = new MerchantMemberIntegral();
            integral.setMerchantId(merchantId);
        }
        integral.setIntegral(after);
        merchantMemberIntegralRepository.save(integral);

        MerchantMemberIntegralSpend record = new MerchantMemberIntegralSpend();
        record.setMerchantId(merchantId);
        record.setIntegralBeforeSpend(before);
        record.setIntegralAfterSpend(after);
        record.setChangeAmount(integralAmount);
        record.setChangeReason(IntegralChangeReason.RECHARGE.getDescription());
        record.setOrderId(null);
        record.setChangeTime(LocalDateTime.now());
        merchantMemberIntegralSpendRepository.save(record);
    }

    @Transactional
    public void rechargeMember(Long merchantId, Integer months) {
        if (merchantId == null) {
            throw new IllegalArgumentException("商户ID不能为空");
        }
        if (months == null || months <= 0) {
            throw new IllegalArgumentException("充值会员时长必须大于0");
        }
        Optional<Merchant> opt = merchantRepository.findById(merchantId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("商户不存在");
        }
        Merchant merchant = opt.get();
        
        Optional<MerchantMemberInfo> infoOpt = memberInfoRepository.findByMerchantId(merchantId);
        MerchantMemberInfo info = infoOpt.orElse(null);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;
        LocalDateTime end;
        if (info == null || info.getEndDate() == null || info.getEndDate().isBefore(now)) {
            start = now;
            end = now.plusMonths(months);
            if (info == null) {
                info = new MerchantMemberInfo();
                info.setMerchantId(merchantId);
            }
        } else {
            start = info.getStartDate() != null ? info.getStartDate() : now;
            end = info.getEndDate().plusMonths(months);
        }
        info.setStartDate(start);
        info.setEndDate(end);
        info.setIsValid(1);
        memberInfoRepository.save(info);
        merchantService.refreshLoginCache(merchant, info);
    }
}
