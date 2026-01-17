package com.msi.service;

import com.msi.domain.Product;
import com.msi.domain.Merchant;
import com.msi.domain.MerchantCallRecord;
import com.msi.domain.BuyRequest;
import com.msi.domain.ProductImage;
import com.msi.repository.ProductRepository;
import com.msi.repository.ProductImageRepository;
import com.msi.repository.MerchantCallRecordRepository;
import com.msi.repository.BuyRequestRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import com.msi.repository.CityDictRepository;
import com.msi.domain.CityDict;
import com.msi.dto.CityDto;
import org.springframework.data.redis.core.StringRedisTemplate;

@Service
public class SupplyService {
	private final ProductRepository productRepository;
	private final CityDictRepository cityRepository;
	private final StringRedisTemplate redisTemplate;
	private final MerchantService merchantService;
	private final MerchantCallRecordRepository callRecordRepository;
	private final BuyRequestRepository buyRequestRepository;
	private final ProductImageRepository productImageRepository;

	public SupplyService(ProductRepository productRepository,
			CityDictRepository cityRepository,
			StringRedisTemplate redisTemplate,
			MerchantService merchantService,
			MerchantCallRecordRepository callRecordRepository,
			BuyRequestRepository buyRequestRepository,
			ProductImageRepository productImageRepository) {
		this.productRepository = productRepository;
		this.cityRepository = cityRepository;
		this.redisTemplate = redisTemplate;
		this.merchantService = merchantService;
		this.callRecordRepository = callRecordRepository;
		this.buyRequestRepository = buyRequestRepository;
		this.productImageRepository = productImageRepository;
	}

	public List<CityDto> getCities() {
		List<CityDict> list = cityRepository.findAllByValidOrderBySortAsc(1);
		List<CityDto> cities = list.stream()
				.filter(c -> c.getCityName() != null && !"全国".equals(c.getCityName()))
				.map(c -> new CityDto(c.getCityCode(), c.getCityName(), c.getIsOnline()))
				.toList();
		List<CityDto> result = new ArrayList<>();
		result.add(new CityDto("000000", "全国", 1));
		result.addAll(cities);
		return result;
	}

	public List<Product> search(String cityCode, String brand, String model, String keyword, int page, int size) {
		Specification<Product> spec = Specification.where(null);
		if (cityCode != null && !cityCode.isEmpty() && !"000000".equals(cityCode) && !"全国".equals(cityCode)) {
			spec = spec.and((root, q, cb) -> cb.equal(root.get("cityCode"), cityCode));
		}
		if (brand != null && !brand.isEmpty()) {
			spec = spec.and((root, q, cb) -> cb.like(root.get("description"), "%" + brand + "%"));
		}
		if (model != null && !model.isEmpty()) {
			spec = spec.and((root, q, cb) -> cb.like(root.get("description"), "%" + model + "%"));
		}
		if (keyword != null && !keyword.isEmpty()) {
			spec = spec.and((root, q, cb) -> cb.like(root.get("description"), "%" + keyword + "%"));
		}
		Page<Product> p = productRepository.findAll(spec, PageRequest.of(page, size));
		return p.getContent();
	}

	public Page<Product> searchAvailableProducts(String cityCode,
			Integer productType,
			Long brandId,
			Long seriesId,
			Long modelId,
			Long specId,
			int page,
			int size) {
		if (productType == null || (productType != 0 && productType != 1)) {
			throw new IllegalArgumentException("产品类型不合法");
		}
		if (brandId == null) {
			throw new IllegalArgumentException("品牌ID不能为空");
		}
		if (seriesId == null) {
			throw new IllegalArgumentException("系列ID不能为空");
		}
		if (modelId == null) {
			throw new IllegalArgumentException("型号ID不能为空");
		}
		if (specId == null) {
			throw new IllegalArgumentException("配置ID不能为空");
		}
		if (page < 0) {
			throw new IllegalArgumentException("页码不能小于0");
		}
		if (size <= 0) {
			throw new IllegalArgumentException("每页数量必须大于0");
		}

		Specification<Product> spec = Specification.where(null);
		spec = spec.and((root, q, cb) -> cb.equal(root.get("isValid"), 1));
		spec = spec.and((root, q, cb) -> cb.equal(root.get("state"), 1));

		if (cityCode != null && !cityCode.isEmpty() && !"000000".equals(cityCode) && !"全国".equals(cityCode)) {
			spec = spec.and((root, q, cb) -> cb.equal(root.get("cityCode"), cityCode));
		}

		spec = spec.and((root, q, cb) -> cb.equal(root.get("productType"), productType));
		spec = spec.and((root, q, cb) -> cb.equal(root.get("brandId"), brandId));
		spec = spec.and((root, q, cb) -> cb.equal(root.get("seriesId"), seriesId));
		spec = spec.and((root, q, cb) -> cb.equal(root.get("modelId"), modelId));
		spec = spec.and((root, q, cb) -> cb.equal(root.get("specId"), specId));

		return productRepository.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updateTime")));
	}

	public Product getAvailableProductById(Long productId) {
		if (productId == null) {
			throw new IllegalArgumentException("产品ID不能为空");
		}
		Product product = productRepository.findByIdAndIsValid(productId, 1)
				.orElseThrow(() -> new IllegalArgumentException("商品不存在"));
		if (product.getState() == null || product.getState() != 1) {
			throw new IllegalArgumentException("商品未上架");
		}

		List<ProductImage> images = productImageRepository.findByProduct_IdAndIsValid(productId, 1);
		product.setImages(images);

		return product;
	}

    public String getMerchantPhoneAndRecordCall(Merchant currentMerchant, String publicId, Long productId) {
        if (currentMerchant == null || currentMerchant.getId() == null) {
            throw new IllegalArgumentException("商户未登录");
        }
        if (publicId == null || publicId.isEmpty()) {
            throw new IllegalArgumentException("商户publicId不能为空");
        }
        if (productId == null) {
            throw new IllegalArgumentException("产品ID不能为空");
        }
        Product product = getAvailableProductById(productId);
        Merchant targetMerchant = merchantService.getMerchantInfoByPublicId(publicId);
        Long merchantId = targetMerchant.getId();
        if (product.getMerchantId() == null || !product.getMerchantId().equals(merchantId)) {
            throw new IllegalArgumentException("商品不属于该商户");
        }
        MerchantCallRecord record = new MerchantCallRecord();
        record.setCallerMerchantId(currentMerchant.getId());
        record.setCalleeMerchantId(merchantId);
        record.setProductId(productId);
        record.setCallType(0);// 因为产品打电话，所以是0
        callRecordRepository.save(record);
        return targetMerchant.getMerchantPhone();
    }

    public String getMerchantPhoneByBuyRequestAndRecordCall(Merchant currentMerchant, String publicId,
            Long buyRequestId) {
        if (currentMerchant == null || currentMerchant.getId() == null) {
            throw new IllegalArgumentException("商户未登录");
        }
        if (publicId == null || publicId.isEmpty()) {
            throw new IllegalArgumentException("商户publicId不能为空");
        }
        if (buyRequestId == null) {
            throw new IllegalArgumentException("求购ID不能为空");
        }
        BuyRequest buyRequest = buyRequestRepository.findByIdAndIsValid(buyRequestId, 1)
                .orElseThrow(() -> new IllegalArgumentException("求购信息不存在"));
        Merchant targetMerchant = merchantService.getMerchantInfoByPublicId(publicId);
        Long merchantId = targetMerchant.getId();
        if (buyRequest.getMerchantId() == null || !buyRequest.getMerchantId().equals(merchantId)) {
            throw new IllegalArgumentException("求购信息不属于该商户");
        }
        MerchantCallRecord record = new MerchantCallRecord();
        record.setCallerMerchantId(currentMerchant.getId());
        record.setCalleeMerchantId(merchantId);
		record.setProductId(buyRequestId);
		record.setCallType(1);// 因为求购电话，所以是1
		callRecordRepository.save(record);
		return targetMerchant.getMerchantPhone();
	}

	public Page<BuyRequest> getBuyRequestHallList(String cityCode, int page, int size) {
		if (cityCode == null || cityCode.isEmpty()) {
			throw new IllegalArgumentException("城市编码不能为空");
		}
		if (page < 0) {
			throw new IllegalArgumentException("页码不能小于0");
		}
		if (size <= 0) {
			throw new IllegalArgumentException("每页数量必须大于0");
		}
		PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
		return buyRequestRepository.findByCityCodeAndIsValidAndState(cityCode, 1, 1, pageable);
	}

	public String getCityNameByCode(String cityCode) {
		if (cityCode == null || cityCode.isEmpty()) {
			return null;
		}
		String key = "city:name:" + cityCode;
		String cached = redisTemplate.opsForValue().get(key);
		if (cached != null && !cached.isEmpty()) {
			return cached;
		}
		String name = cityRepository.findByCityCode(cityCode)
				.map(CityDict::getCityName)
				.orElse(cityCode);
		if (name != null && !name.isEmpty()) {
			redisTemplate.opsForValue().set(key, name);
		}
		return name;
	}
}
