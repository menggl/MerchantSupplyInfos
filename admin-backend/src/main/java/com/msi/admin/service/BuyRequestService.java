package com.msi.admin.service;

import com.msi.admin.domain.BuyRequest;
import com.msi.admin.domain.Merchant;
import com.msi.admin.dto.BuyRequestDto;
import com.msi.admin.repository.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@Service
public class BuyRequestService {
    private final BuyRequestRepository buyRequestRepository;
    private final MerchantRepository merchantRepository;
    private final BrandRepository brandRepository;
    private final PhoneSeriesRepository seriesRepository;
    private final PhoneModelRepository modelRepository;
    private final PhoneSpecRepository specRepository;
    private final CityDictRepository cityDictRepository;

    public BuyRequestService(BuyRequestRepository buyRequestRepository,
                             MerchantRepository merchantRepository,
                             BrandRepository brandRepository,
                             PhoneSeriesRepository seriesRepository,
                             PhoneModelRepository modelRepository,
                             PhoneSpecRepository specRepository,
                             CityDictRepository cityDictRepository) {
        this.buyRequestRepository = buyRequestRepository;
        this.merchantRepository = merchantRepository;
        this.brandRepository = brandRepository;
        this.seriesRepository = seriesRepository;
        this.modelRepository = modelRepository;
        this.specRepository = specRepository;
        this.cityDictRepository = cityDictRepository;
    }

    public Map<String, Object> listBuyRequests(int page, int size, Long id, String merchantName, String cityCode) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createTime").descending());

        Specification<BuyRequest> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (id != null) {
                predicates.add(cb.equal(root.get("id"), id));
            }
            if (StringUtils.hasText(cityCode)) {
                predicates.add(cb.equal(root.get("cityCode"), cityCode));
            }

            if (StringUtils.hasText(merchantName)) {
                List<Long> merchantIds = merchantRepository.findByMerchantNameContaining(merchantName)
                        .stream()
                        .map(Merchant::getId)
                        .collect(Collectors.toList());

                if (merchantIds.isEmpty()) {
                    predicates.add(cb.disjunction());
                } else {
                    predicates.add(root.get("merchantId").in(merchantIds));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<BuyRequest> pageResult = buyRequestRepository.findAll(spec, pageable);

        List<BuyRequestDto> dtos = pageResult.getContent().stream().map(this::convertToDto).collect(Collectors.toList());

        return Map.of(
                "total", pageResult.getTotalElements(),
                "list", dtos
        );
    }

    @Transactional
    public void updateBuyRequestStatus(Long id, Integer status) {
        buyRequestRepository.findById(id).ifPresent(request -> {
            request.setIsValid(status);
            buyRequestRepository.save(request);
        });
    }

    private BuyRequestDto convertToDto(BuyRequest request) {
        BuyRequestDto dto = new BuyRequestDto();
        dto.setId(request.getId());
        dto.setMerchantId(request.getMerchantId());

        if (request.getMerchantId() != null) {
            merchantRepository.findById(request.getMerchantId()).ifPresent(m -> {
                dto.setMerchantName(m.getMerchantName());
                dto.setMerchantPhone(m.getMerchantPhone());
            });
        }

        if (request.getBrandId() != null) {
            brandRepository.findById(request.getBrandId()).ifPresent(b -> dto.setBrandName(b.getName()));
        }

        if (request.getSeriesId() != null) {
            seriesRepository.findById(request.getSeriesId()).ifPresent(s -> dto.setSeriesName(s.getSeriesName()));
        }

        if (request.getModelId() != null) {
            modelRepository.findById(request.getModelId()).ifPresent(m -> dto.setModelName(m.getModelName()));
        }

        if (request.getSpecId() != null) {
            specRepository.findById(request.getSpecId()).ifPresent(s -> dto.setSpecName(s.getSpecName()));
        }

        dto.setCityCode(request.getCityCode());
        if (StringUtils.hasText(request.getCityCode())) {
            cityDictRepository.findByCityCode(request.getCityCode()).ifPresent(c -> dto.setCityName(c.getCityName()));
        }

        dto.setProductType(request.getProductType());
        dto.setBuyCount(request.getBuyCount());
        dto.setMinPrice(request.getMinPrice());
        dto.setMaxPrice(request.getMaxPrice());
        dto.setDeadline(request.getDeadline());
        dto.setCostIntegral(request.getCostIntegral());
        dto.setIsValid(request.getIsValid());
        dto.setCreateTime(request.getCreateTime());

        return dto;
    }
}
