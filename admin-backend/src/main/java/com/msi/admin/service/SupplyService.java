package com.msi.admin.service;

import com.msi.admin.domain.*;
import com.msi.admin.dto.AdminProductDto;
import com.msi.admin.repository.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SupplyService {
    private final MerchantPhoneProductRepository productRepository;
    private final MerchantRepository merchantRepository;
    private final BrandRepository brandRepository;
    private final PhoneSeriesRepository seriesRepository;
    private final PhoneModelRepository modelRepository;
    private final PhoneSpecRepository specRepository;
    private final MerchantProductImageRepository imageRepository;
    private final CityDictRepository cityDictRepository;

    public SupplyService(MerchantPhoneProductRepository productRepository,
                         MerchantRepository merchantRepository,
                         BrandRepository brandRepository,
                         PhoneSeriesRepository seriesRepository,
                         PhoneModelRepository modelRepository,
                         PhoneSpecRepository specRepository,
                         MerchantProductImageRepository imageRepository,
                         CityDictRepository cityDictRepository) {
        this.productRepository = productRepository;
        this.merchantRepository = merchantRepository;
        this.brandRepository = brandRepository;
        this.seriesRepository = seriesRepository;
        this.modelRepository = modelRepository;
        this.specRepository = specRepository;
        this.imageRepository = imageRepository;
        this.cityDictRepository = cityDictRepository;
    }

    public Map<String, Object> listProducts(int page, int size, Long id, Integer productType, String merchantName, String cityCode) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createTime").descending());

        Specification<MerchantPhoneProduct> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (id != null) {
                predicates.add(cb.equal(root.get("id"), id));
            }
            if (productType != null) {
                predicates.add(cb.equal(root.get("productType"), productType));
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
                    // Force empty result if no merchants match
                    predicates.add(cb.disjunction());
                } else {
                    predicates.add(root.get("merchantId").in(merchantIds));
                }
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<MerchantPhoneProduct> pageResult = productRepository.findAll(spec, pageable);
        
        List<AdminProductDto> dtos = pageResult.getContent().stream().map(this::convertToDto).collect(Collectors.toList());

        return Map.of(
            "total", pageResult.getTotalElements(),
            "list", dtos
        );
    }
    
    @Transactional
    public void updateProductStatus(Long id, Integer status) {
        productRepository.findById(id).ifPresent(product -> {
            product.setIsValid(status);
            productRepository.save(product);
        });
    }

    private AdminProductDto convertToDto(MerchantPhoneProduct product) {
        AdminProductDto dto = new AdminProductDto();
        dto.setId(product.getId());
        dto.setMerchantId(product.getMerchantId());
        
        if (product.getMerchantId() != null) {
            merchantRepository.findById(product.getMerchantId()).ifPresent(m -> {
                dto.setMerchantName(m.getMerchantName());
                dto.setMerchantPhone(m.getMerchantPhone());
                dto.setMerchantAddress(m.getMerchantAddress());
                dto.setLatitude(m.getLatitude());
                dto.setLongitude(m.getLongitude());
            });
        }
        
        if (product.getBrandId() != null) {
            brandRepository.findById(product.getBrandId()).ifPresent(b -> dto.setBrandName(b.getName()));
        }
        
        if (product.getSeriesId() != null) {
            seriesRepository.findById(product.getSeriesId()).ifPresent(s -> dto.setSeriesName(s.getSeriesName()));
        }
        
        if (product.getModelId() != null) {
            modelRepository.findById(product.getModelId()).ifPresent(m -> dto.setModelName(m.getModelName()));
        }
        
        if (product.getSpecId() != null) {
            specRepository.findById(product.getSpecId()).ifPresent(s -> dto.setSpecName(s.getSpecName()));
        }
        
        dto.setCityCode(product.getCityCode());
        if (StringUtils.hasText(product.getCityCode())) {
            cityDictRepository.findByCityCode(product.getCityCode()).ifPresent(c -> dto.setCityName(c.getCityName()));
        }
        
        dto.setProductType(product.getProductType());
        dto.setRemark(product.getRemark());
        dto.setOtherRemark(product.getOtherRemark());
        dto.setSecondHandVersion(product.getSecondHandVersion());
        dto.setSecondHandCondition(product.getSecondHandCondition());
        dto.setSecondHandFunction(product.getSecondHandFunction());
        dto.setBatteryStatus(product.getBatteryStatus());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setListingTime(product.getListingTime());
        dto.setIsValid(product.getIsValid());
        dto.setCreateTime(product.getCreateTime());
        
        List<MerchantProductImage> images = imageRepository.findByProductIdAndIsValid(product.getId(), 1);
        dto.setImageUrls(images.stream().map(MerchantProductImage::getImageUrl).collect(Collectors.toList()));
        
        return dto;
    }
}
