package com.msi.service;

import com.msi.domain.*;
import com.msi.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
	private final ProductRepository productRepository;
	private final BrandRepository brandRepository;
	private final PhoneSeriesRepository seriesRepository;
	private final PhoneModelRepository modelRepository;
	private final PhoneSpecRepository specRepository;
	private final MerchantRepository merchantRepository;
	private final BuyRequestRepository buyRequestRepository;
	private final ProductImageRepository productImageRepository;

	public ProductService(ProductRepository productRepository,
			BrandRepository brandRepository,
			PhoneSeriesRepository seriesRepository,
			PhoneModelRepository modelRepository,
			PhoneSpecRepository specRepository,
			MerchantRepository merchantRepository,
			MerchantMemberInfoRepository memberInfoRepository,
			BuyRequestRepository buyRequestRepository,
			ProductImageRepository productImageRepository) {
		this.productRepository = productRepository;
		this.brandRepository = brandRepository;
		this.seriesRepository = seriesRepository;
		this.modelRepository = modelRepository;
		this.specRepository = specRepository;
		this.merchantRepository = merchantRepository;
		this.buyRequestRepository = buyRequestRepository;
		this.productImageRepository = productImageRepository;
	}

	public Product publishProduct(Product product) {
		if (product.getMerchantId() == null) {
			throw new IllegalArgumentException("商户ID不能为空");
		}
		Merchant merchant = merchantRepository.findById(product.getMerchantId())
				.orElseThrow(() -> new IllegalArgumentException("商户不存在"));

		String merchantCityCode = merchant.getCityCode();
		if (merchantCityCode == null || merchantCityCode.isEmpty()) {
			throw new IllegalArgumentException("城市编码不能为空");
		}

		if (product.getBrandId() == null) {
			throw new IllegalArgumentException("品牌ID不能为空");
		}
		if (product.getSeriesId() == null) {
			throw new IllegalArgumentException("系列ID不能为空");
		}
		if (product.getModelId() == null) {
			throw new IllegalArgumentException("型号ID不能为空");
		}
		if (product.getSpecId() == null) {
			throw new IllegalArgumentException("配置ID不能为空");
		}

		Brand brand = brandRepository.findById(product.getBrandId())
				.orElseThrow(() -> new IllegalArgumentException("品牌ID不存在"));
		PhoneSeries series = seriesRepository.findById(product.getSeriesId())
				.orElseThrow(() -> new IllegalArgumentException("系列ID不存在"));
		PhoneModel model = modelRepository.findById(product.getModelId())
				.orElseThrow(() -> new IllegalArgumentException("型号ID不存在"));
		PhoneSpec spec = specRepository.findById(product.getSpecId())
				.orElseThrow(() -> new IllegalArgumentException("配置ID不存在"));

		if (series.getBrand() == null || !series.getBrand().getId().equals(brand.getId())) {
			throw new IllegalArgumentException("系列不属于该品牌");
		}
		if (model.getSeries() == null || !model.getSeries().getId().equals(series.getId())) {
			throw new IllegalArgumentException("型号不属于该系列");
		}
		if (spec.getModel() == null || !spec.getModel().getId().equals(model.getId())) {
			throw new IllegalArgumentException("配置不属于该型号");
		}

		if (productRepository
				.findFirstByMerchantIdAndBrandIdAndSeriesIdAndModelIdAndSpecIdAndIsValid(
						merchant.getId(), brand.getId(), series.getId(), model.getId(), spec.getId(), 1)
				.isPresent()) {
			throw new IllegalArgumentException("该型号商品已存在");
		}

		if (product.getDescription() == null || product.getDescription().isEmpty()) {
			throw new IllegalArgumentException("产品描述不能为空");
		}
		if (product.getPrice() == null) {
			throw new IllegalArgumentException("产品价格不能为空");
		}
		if (product.getPrice() <= 0) {
			throw new IllegalArgumentException("产品价格必须大于0");
		}
		if (product.getStock() == null) {
			throw new IllegalArgumentException("产品库存不能为空");
		}
		if (product.getStock() < 0) {
			throw new IllegalArgumentException("产品库存不能为负数");
		}

		product.setMerchantId(merchant.getId());
		product.setCityCode(merchantCityCode);
		product.setBrandId(brand.getId());
		product.setSeriesId(series.getId());
		product.setModelId(model.getId());
		product.setSpecId(spec.getId());

		Product saved = productRepository.save(product);

		if (product.getImages() != null) {
			List<ProductImage> imgs = product.getImages().stream()
					.filter(pi -> pi != null && pi.getImageUrl() != null && !pi.getImageUrl().isEmpty())
					.collect(Collectors.toList());
			imgs.forEach(pi -> pi.setProduct(saved));
			if (!imgs.isEmpty()) {
				productImageRepository.saveAll(imgs);
			}
		}

		return saved;
	}

	public Page<Product> findProductsByMerchant(Long merchantId, int page, int size) {
		if (merchantId == null) {
			throw new IllegalArgumentException("商户ID不能为空");
		}
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updateTime"));
		return productRepository.findByMerchantIdAndIsValid(merchantId, 1, pageable);
	}

	public Page<Product> findProductsByMerchantAndModel(Long merchantId,
			Long brandId,
			Long seriesId,
			Long modelId,
			Long specId,
			int page,
			int size) {
		if (merchantId == null) {
			throw new IllegalArgumentException("商户ID不能为空");
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
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updateTime"));
		return productRepository.findByMerchantIdAndBrandIdAndSeriesIdAndModelIdAndSpecIdAndIsValid(
				merchantId, brandId, seriesId, modelId, specId, 1, pageable);
	}

	public Product findProductByMerchantAndModel(Long merchantId,
			Long brandId,
			Long seriesId,
			Long modelId,
			Long specId) {
		if (merchantId == null) {
			throw new IllegalArgumentException("商户ID不能为空");
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
		return productRepository
				.findFirstByMerchantIdAndBrandIdAndSeriesIdAndModelIdAndSpecIdAndIsValid(
						merchantId, brandId, seriesId, modelId, specId, 1)
				.orElseThrow(() -> new IllegalArgumentException("商品不存在"));
	}

	public Page<BuyRequest> findBuyProductsByMerchant(Long merchantId, int page, int size) {
		if (merchantId == null) {
			throw new IllegalArgumentException("商户ID不能为空");
		}
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updateTime"));
		return buyRequestRepository.findByMerchantIdAndIsValid(merchantId, 1, pageable);
	}

	public BuyRequest findBuyRequestByMerchantAndModel(Long merchantId,
			Long brandId,
			Long seriesId,
			Long modelId,
			Long specId) {
		if (merchantId == null) {
			throw new IllegalArgumentException("商户ID不能为空");
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
		return buyRequestRepository
				.findFirstByMerchantIdAndBrandIdAndSeriesIdAndModelIdAndSpecIdAndIsValid(
						merchantId, brandId, seriesId, modelId, specId, 1)
				.orElseThrow(() -> new IllegalArgumentException("求购信息不存在"));
	}

	
	public void updateBuyRequestState(Long buyRequestId, Long merchantId, Integer state) {
		if (buyRequestId == null) {
			throw new IllegalArgumentException("求购ID不能为空");
		}
		if (state == null || (state != 0 && state != 1)) {
			throw new IllegalArgumentException("状态不合法");
		}
		BuyRequest buyRequest = buyRequestRepository.findByIdAndIsValid(buyRequestId, 1)
				.orElseThrow(() -> new IllegalArgumentException("求购信息不存在"));
		if (merchantId != null) {
			if (buyRequest.getMerchantId() == null || !buyRequest.getMerchantId().equals(merchantId)) {
				throw new IllegalArgumentException("无权操作该求购信息");
			}
		}
		buyRequest.setState(state);
		buyRequestRepository.save(buyRequest);
	}

	public void withdrawBuyRequest(Long buyRequestId, Long merchantId) {
		if (buyRequestId == null) {
			throw new IllegalArgumentException("求购ID不能为空");
		}
		BuyRequest buyRequest = buyRequestRepository.findById(buyRequestId)
				.orElseThrow(() -> new IllegalArgumentException("求购信息不存在"));
		if (merchantId != null) {
			if (buyRequest.getMerchantId() == null || !buyRequest.getMerchantId().equals(merchantId)) {
				throw new IllegalArgumentException("无权操作该求购信息");
			}
		}
		if (buyRequest.getIsValid() != null && buyRequest.getIsValid() == 0) {
			return;
		}
		buyRequest.setIsValid(0);
		buyRequestRepository.save(buyRequest);
	}

	public void withdrawProduct(Long productId, Long merchantId) {
		if (productId == null) {
			throw new IllegalArgumentException("商品ID不能为空");
		}
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new IllegalArgumentException("商品不存在"));

		if (merchantId != null) {
			if (product.getMerchantId() == null || !product.getMerchantId().equals(merchantId)) {
				throw new IllegalArgumentException("无权操作该商品");
			}
		}

		if (product.getMerchantId() == null) {
			throw new IllegalArgumentException("商户信息缺失");
		}
		Merchant merchant = merchantRepository.findById(product.getMerchantId())
				.orElseThrow(() -> new IllegalArgumentException("商户信息缺失"));
		if (product.getIsValid() != null && product.getIsValid() == 0) {
			return;
		}
		product.setIsValid(0);
		productRepository.save(product);
	}

	public void updateProductState(Long productId, Long merchantId, Integer state) {
		if (productId == null) {
			throw new IllegalArgumentException("商品ID不能为空");
		}
		if (state == null || (state != 0 && state != 1)) {
			throw new IllegalArgumentException("状态不合法");
		}
		Product product = productRepository.findByIdAndIsValid(productId, 1)
				.orElseThrow(() -> new IllegalArgumentException("商品不存在"));

		if (merchantId != null) {
			if (product.getMerchantId() == null || !product.getMerchantId().equals(merchantId)) {
				throw new IllegalArgumentException("无权操作该商品");
			}
		}

		if (product.getMerchantId() == null) {
			throw new IllegalArgumentException("商户信息缺失");
		}
		Merchant merchant = merchantRepository.findById(product.getMerchantId())
				.orElseThrow(() -> new IllegalArgumentException("商户信息缺失"));

		product.setState(state);
		productRepository.save(product);
	}

	public Product updateProduct(Long productId, Long merchantId, Product newProduct) {
		if (productId == null) {
			throw new IllegalArgumentException("商品ID不能为空");
		}
		if (newProduct == null) {
			throw new IllegalArgumentException("商品信息不能为空");
		}
		Product existing = productRepository.findByIdAndIsValid(productId, 1)
				.orElseThrow(() -> new IllegalArgumentException("商品不存在"));

		if (existing.getIsValid() != null && existing.getIsValid() == 0) {
			throw new IllegalArgumentException("商品已删除");
		}

		if (merchantId != null) {
			if (existing.getMerchantId() == null || !existing.getMerchantId().equals(merchantId)) {
				throw new IllegalArgumentException("无权操作该商品");
			}
		}

		if (newProduct.getBrandId() == null) {
			throw new IllegalArgumentException("品牌ID不能为空");
		}
		if (newProduct.getSeriesId() == null) {
			throw new IllegalArgumentException("系列ID不能为空");
		}
		if (newProduct.getModelId() == null) {
			throw new IllegalArgumentException("型号ID不能为空");
		}
		if (newProduct.getSpecId() == null) {
			throw new IllegalArgumentException("配置ID不能为空");
		}

		Brand brand = brandRepository.findById(newProduct.getBrandId())
				.orElseThrow(() -> new IllegalArgumentException("品牌ID不存在"));
		PhoneSeries series = seriesRepository.findById(newProduct.getSeriesId())
				.orElseThrow(() -> new IllegalArgumentException("系列ID不存在"));
		PhoneModel model = modelRepository.findById(newProduct.getModelId())
				.orElseThrow(() -> new IllegalArgumentException("型号ID不存在"));
		PhoneSpec spec = specRepository.findById(newProduct.getSpecId())
				.orElseThrow(() -> new IllegalArgumentException("配置ID不存在"));

		if (series.getBrand() == null || !series.getBrand().getId().equals(brand.getId())) {
			throw new IllegalArgumentException("系列不属于该品牌");
		}
		if (model.getSeries() == null || !model.getSeries().getId().equals(series.getId())) {
			throw new IllegalArgumentException("型号不属于该系列");
		}
		if (spec.getModel() == null || !spec.getModel().getId().equals(model.getId())) {
			throw new IllegalArgumentException("配置不属于该型号");
		}

		if (newProduct.getDescription() == null || newProduct.getDescription().isEmpty()) {
			throw new IllegalArgumentException("产品描述不能为空");
		}
		if (newProduct.getPrice() == null) {
			throw new IllegalArgumentException("产品价格不能为空");
		}
		if (newProduct.getPrice() <= 0) {
			throw new IllegalArgumentException("产品价格必须大于0");
		}
		if (newProduct.getStock() == null) {
			throw new IllegalArgumentException("产品库存不能为空");
		}
		if (newProduct.getStock() < 0) {
			throw new IllegalArgumentException("产品库存不能为负数");
		}

		existing.setBrandId(brand.getId());
		existing.setSeriesId(series.getId());
		existing.setModelId(model.getId());
		existing.setSpecId(spec.getId());
		existing.setDescription(newProduct.getDescription());
		existing.setProductType(newProduct.getProductType());
		existing.setRemark(newProduct.getRemark());
		existing.setOtherRemark(newProduct.getOtherRemark());
		existing.setSecondHandVersion(newProduct.getSecondHandVersion());
		existing.setSecondHandCondition(newProduct.getSecondHandCondition());
		existing.setSecondHandFunction(newProduct.getSecondHandFunction());
		existing.setBatteryStatus(newProduct.getBatteryStatus());
		existing.setPrice(newProduct.getPrice());
		existing.setStock(newProduct.getStock());

		if (newProduct.getImages() != null) {
			List<ProductImage> imgs = newProduct.getImages().stream()
					.filter(pi -> pi != null && pi.getImageUrl() != null && !pi.getImageUrl().isEmpty())
					.collect(Collectors.toList());
			if (!imgs.isEmpty()) {
				List<ProductImage> existingImages = productImageRepository
						.findByProduct_IdAndIsValid(existing.getId(), 1);
				if (!existingImages.isEmpty()) {
					existingImages.forEach(img -> img.setIsValid(0));
					productImageRepository.saveAll(existingImages);
				}
				imgs.forEach(pi -> pi.setProduct(existing));
				productImageRepository.saveAll(imgs);
			}
		}

		existing.setState(0);
		return productRepository.save(existing);
	}

	public BuyRequest publishBuyRequest(BuyRequest buyRequest) {
		if (buyRequest == null) {
			throw new IllegalArgumentException("求购信息不能为空");
		}
		if (buyRequest.getMerchantId() == null) {
			throw new IllegalArgumentException("商户ID不能为空");
		}
		Merchant merchant = merchantRepository.findById(buyRequest.getMerchantId())
				.orElseThrow(() -> new IllegalArgumentException("商户不存在"));
		validateBuyRequestParams(merchant, buyRequest);

		if (buyRequestRepository
				.findFirstByMerchantIdAndBrandIdAndSeriesIdAndModelIdAndSpecIdAndIsValid(
						merchant.getId(), buyRequest.getBrandId(), buyRequest.getSeriesId(), buyRequest.getModelId(), buyRequest.getSpecId(), 1)
				.isPresent()) {
			throw new IllegalArgumentException("该型号求购信息已存在");
		}

		buyRequest.setMerchantId(merchant.getId());
		buyRequest.setCityCode(merchant.getCityCode());
		return buyRequestRepository.save(buyRequest);
	}
	
	public BuyRequest updateBuyRequest(Long buyRequestId, Long merchantId, BuyRequest newRequest) {
		if (buyRequestId == null) {
			throw new IllegalArgumentException("求购ID不能为空");
		}
		if (merchantId == null) {
			throw new IllegalArgumentException("商户ID不能为空");
		}
		if (newRequest == null) {
			throw new IllegalArgumentException("求购信息不能为空");
		}
		BuyRequest existing = buyRequestRepository.findByIdAndIsValid(buyRequestId, 1)
				.orElseThrow(() -> new IllegalArgumentException("求购信息不存在"));
		if (existing.getMerchantId() == null || !existing.getMerchantId().equals(merchantId)) {
			throw new IllegalArgumentException("无权操作该求购信息");
		}
		Merchant merchant = merchantRepository.findById(merchantId)
				.orElseThrow(() -> new IllegalArgumentException("商户不存在"));
		validateBuyRequestParams(merchant, newRequest);
		existing.setBrandId(newRequest.getBrandId());
		existing.setSeriesId(newRequest.getSeriesId());
		existing.setModelId(newRequest.getModelId());
		existing.setSpecId(newRequest.getSpecId());
		existing.setProductType(newRequest.getProductType());
		existing.setBuyCount(newRequest.getBuyCount());
		existing.setMinPrice(newRequest.getMinPrice());
		existing.setMaxPrice(newRequest.getMaxPrice());
		existing.setDeadline(newRequest.getDeadline());
		existing.setCostIntegral(newRequest.getCostIntegral());
		existing.setCityCode(merchant.getCityCode());
		existing.setState(0);
		return buyRequestRepository.save(existing);
	}
	
	private void validateBuyRequestParams(Merchant merchant, BuyRequest req) {
		String merchantCityCode = merchant.getCityCode();
		if (merchantCityCode == null || merchantCityCode.isEmpty()) {
			throw new IllegalArgumentException("城市编码不能为空");
		}
		if (req.getBrandId() == null) {
			throw new IllegalArgumentException("品牌ID不能为空");
		}
		if (req.getSeriesId() == null) {
			throw new IllegalArgumentException("系列ID不能为空");
		}
		if (req.getModelId() == null) {
			throw new IllegalArgumentException("型号ID不能为空");
		}
		if (req.getSpecId() == null) {
			throw new IllegalArgumentException("配置ID不能为空");
		}
		Brand brand = brandRepository.findById(req.getBrandId())
				.orElseThrow(() -> new IllegalArgumentException("品牌ID不存在"));
		PhoneSeries series = seriesRepository.findById(req.getSeriesId())
				.orElseThrow(() -> new IllegalArgumentException("系列ID不存在"));
		PhoneModel model = modelRepository.findById(req.getModelId())
				.orElseThrow(() -> new IllegalArgumentException("型号ID不存在"));
		PhoneSpec spec = specRepository.findById(req.getSpecId())
				.orElseThrow(() -> new IllegalArgumentException("配置ID不存在"));
		if (series.getBrand() == null || !series.getBrand().getId().equals(brand.getId())) {
			throw new IllegalArgumentException("系列不属于该品牌");
		}
		if (model.getSeries() == null || !model.getSeries().getId().equals(series.getId())) {
			throw new IllegalArgumentException("型号不属于该系列");
		}
		if (spec.getModel() == null || !spec.getModel().getId().equals(model.getId())) {
			throw new IllegalArgumentException("配置不属于该型号");
		}
		if (req.getProductType() == null) {
			throw new IllegalArgumentException("产品类型不能为空");
		}
		Integer buyCount = req.getBuyCount();
		if (buyCount == null || buyCount <= 0) {
			throw new IllegalArgumentException("求购数量必须大于0");
		}
		Integer minPrice = req.getMinPrice();
		Integer maxPrice = req.getMaxPrice();
		if (minPrice == null || maxPrice == null) {
			throw new IllegalArgumentException("求购价格区间不能为空");
		}
		if (minPrice <= 0 || maxPrice <= 0 || maxPrice < minPrice) {
			throw new IllegalArgumentException("求购价格区间不合法");
		}
		if (req.getDeadline() == null) {
			throw new IllegalArgumentException("求购截止时间不能为空");
		}
		if (req.getCostIntegral() == null || req.getCostIntegral() < 0) {
			throw new IllegalArgumentException("求购花费积分不合法");
		}
	}
}
