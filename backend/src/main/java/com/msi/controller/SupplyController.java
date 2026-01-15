package com.msi.controller;

import com.msi.domain.Merchant;
import com.msi.domain.Product;
import com.msi.domain.BuyRequest;
import com.msi.domain.ProductImage;
import com.msi.dto.BuyRequestHallDto;
import com.msi.dto.SupplyProductDto;
import com.msi.dto.SupplyProductDetailDto;
import com.msi.service.SupplyService;
import com.msi.service.ProductService;
import com.msi.service.MerchantService;
import com.msi.service.DictService;
import org.springframework.data.domain.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class SupplyController {
	private static final Logger logger = LoggerFactory.getLogger(SupplyController.class);

	private final SupplyService supplyService;
	private final MerchantService merchantService;
	private final DictService dictService;

	public SupplyController(SupplyService supplyService,
			MerchantService merchantService,
			ProductService productService,
			DictService dictService) {
		this.supplyService = supplyService;
		this.merchantService = merchantService;
		this.dictService = dictService;
	}

	/**
	 * 查询上架产品列表
	 * 根据城市Id、二手/新机、品牌、系列、型号、配置条件查询上架产品（新机或者二手机）列表
	 * 二手手机的返回字段：产品Id、商家名称、商家所在城市、商家地址、产品价格、二手手机成色、二手机上架更新时间
	 * 新机返回字段：产品Id，商家名称、商家所在城市、商家地址、产品价格、新机备注信息、新机其它备注信息、新机上架更新时间
	 */
	@GetMapping("/supply/products")
	public ResponseEntity<Page<SupplyProductDto>> searchAvailableProducts(
			@RequestAttribute("merchant") Merchant currentMerchant,
			@RequestParam String cityCode,
			@RequestParam Integer productType,
			@RequestParam Long brandId,
			@RequestParam Long seriesId,
			@RequestParam Long modelId,
			@RequestParam Long specId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		try {
			if (currentMerchant == null || currentMerchant.getId() == null) {
				return ResponseEntity.status(401).body(null);
			}
			Page<Product> products = supplyService.searchAvailableProducts(
					cityCode, productType, brandId, seriesId, modelId, specId, page, size);
			Page<SupplyProductDto> result = products.map(this::convertToDto);
			return ResponseEntity.ok(result);
		} catch (IllegalArgumentException e) {
			logger.error("查询上架产品列表失败: merchantId={}, {}",
					currentMerchant != null ? currentMerchant.getId() : null, e.getMessage(), e);
			return ResponseEntity.badRequest().body(null);
		}
	}

	/**
	 * 查询上架产品详情
	 * 根据产品ID查询某个上架产品的详细信息
	 * 二手手机的返回字段：产品Id、商家名称、商家所在城市、商家地址、产品价格、数量/库存、二手手机成色、二手手机版本、二手手机拆修和功能、电池信息、二手机上架更新时间、图片链接列表
	 * 新机返回字段：产品Id，商家名称、商家所在城市、商家地址、产品价格、数量/库存、新机备注信息、新机其它备注信息、新机上架更新时间
	 */
	@GetMapping("/supply/products/{productId}")
	public ResponseEntity<SupplyProductDetailDto> getProductDetail(
			@RequestAttribute("merchant") Merchant currentMerchant,
			@PathVariable Long productId) {
		try {
			if (currentMerchant == null || currentMerchant.getId() == null) {
				return ResponseEntity.status(401).body(null);
			}
			Product product = supplyService.getAvailableProductById(productId);
			SupplyProductDetailDto dto = convertToDetailDto(product);
			return ResponseEntity.ok(dto);
		} catch (IllegalArgumentException e) {
			logger.error("查询上架产品详情失败: merchantId={}, productId={}, {}",
					currentMerchant != null ? currentMerchant.getId() : null, productId, e.getMessage(), e);
			return ResponseEntity.badRequest().body(null);
		}
	}

	/**
	 * 查询商户电话信息
	 * 根据商户ID查询商户的电话信息，同时传递进来因为哪个产品打的电话，同时将该调用记录保存到数据库中，以便于后续统计使用
	 */
	@GetMapping("/supply/merchant/phone")
	public ResponseEntity<String> getMerchantPhone(
			@RequestAttribute("merchant") Merchant currentMerchant,
			@RequestParam Long merchantId,
			@RequestParam Long productId) {
		try {
			if (currentMerchant == null || currentMerchant.getId() == null) {
				return ResponseEntity.status(401).body(null);
			}
			String phone = supplyService.getMerchantPhoneAndRecordCall(currentMerchant, merchantId, productId);
			return ResponseEntity.ok(phone);
		} catch (IllegalArgumentException e) {
			logger.error("查询商户电话信息失败: callerMerchantId={}, merchantId={}, productId={}, {}",
					currentMerchant != null ? currentMerchant.getId() : null, merchantId, productId, e.getMessage(), e);
			return ResponseEntity.badRequest().body(null);
		}
	}


	/**
	 * 查询求购大厅中的求购信息列表数据
	 * 求购信息列表中每一个求购信息都包含的字段：求购信息Id、求购商户Id、求购城市、
	 * 求购品牌、求购系列、求购型号、求购配置、
	 * 求购数量、求购价格范围，联系电话（中间四位用****替代），联系地址
	 * 求购创建时间
	 * 只能查询到当前城市中的求购信息
	 */
	@GetMapping("/supply/buy-requests")
	public ResponseEntity<Page<BuyRequestHallDto>> getBuyRequestHallList(
			@RequestAttribute("merchant") Merchant currentMerchant,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		try {
			if (currentMerchant == null || currentMerchant.getId() == null) {
				return ResponseEntity.status(401).body(null);
			}
			Page<BuyRequest> buyRequests = supplyService.getBuyRequestHallList(currentMerchant.getCityCode(), page, size);
			Page<BuyRequestHallDto> result = buyRequests.map(this::convertToBuyRequestHallDto);
			return ResponseEntity.ok(result);
		} catch (IllegalArgumentException e) {
			logger.error("查询求购大厅列表失败: merchantId={}, {}",
					currentMerchant != null ? currentMerchant.getId() : null, e.getMessage(), e);
			return ResponseEntity.badRequest().body(null);
		}
	}

	/**
	 * 查询求购商户电话信息
	 * 根据求购商户ID查询商户的电话信息，同时传递进来因为哪个求购信息打的电话，同时将该调用记录保存到数据库中，以便于后续统计使用
	 */
	@GetMapping("/supply/merchant/buy-request/phone")
	public ResponseEntity<String> getMerchantPhoneByBuyRequest(
			@RequestAttribute("merchant") Merchant currentMerchant,
			@RequestParam Long merchantId,
			@RequestParam Long buyRequestId) {
		try {
			if (currentMerchant == null || currentMerchant.getId() == null) {
				return ResponseEntity.status(401).body(null);
			}
			String phone = supplyService.getMerchantPhoneByBuyRequestAndRecordCall(currentMerchant, merchantId, buyRequestId);
			return ResponseEntity.ok(phone);
		} catch (IllegalArgumentException e) {
			logger.error("查询求购商户电话信息失败: callerMerchantId={}, merchantId={}, buyRequestId={}, {}",
					currentMerchant != null ? currentMerchant.getId() : null, merchantId, buyRequestId, e.getMessage(), e);
			return ResponseEntity.badRequest().body(null);
		}
	}


	private SupplyProductDto convertToDto(Product product) {
		SupplyProductDto dto = new SupplyProductDto();
		dto.setProductId(product.getId());

		Merchant merchant = null;
		if (product.getMerchantId() != null) {
			try {
				merchant = merchantService.getMerchantInfo(product.getMerchantId());
			} catch (IllegalArgumentException ignored) {
			}
		}
		if (merchant != null) {
			dto.setMerchantName(merchant.getMerchantName());
			dto.setMerchantCity(supplyService.getCityNameByCode(merchant.getCityCode()));
			dto.setMerchantAddress(merchant.getMerchantAddress());
		}
		// 价格
		dto.setPrice(product.getPrice());
		Integer type = product.getProductType();
		if (type != null && type == 1) {
			// 二手机成色
			dto.setSecondHandCondition(product.getSecondHandCondition());
			dto.setRemark(null);
			dto.setOtherRemark(null);
		} else {
			dto.setSecondHandCondition(null);
			// 新机备注
			dto.setRemark(product.getRemark());
			// 新机其它备注
			dto.setOtherRemark(product.getOtherRemark());
		}
		// 上架更新时间
		dto.setListingTime(product.getUpdateTime());
		return dto;
	}

	private SupplyProductDetailDto convertToDetailDto(Product product) {
		SupplyProductDetailDto dto = new SupplyProductDetailDto();
		// 新机、二手机商品ID
		dto.setProductId(product.getId());

		Long brandId = product.getBrandId();
		Long seriesId = product.getSeriesId();
		Long modelId = product.getModelId();
		Long specId = product.getSpecId();
		dto.setBrandId(brandId);
		dto.setSeriesId(seriesId);
		dto.setModelId(modelId);
		dto.setSpecId(specId);
		dto.setBrandName(dictService.getBrandNameById(brandId));
		dto.setSeriesName(dictService.getSeriesNameById(seriesId));
		dto.setModelName(dictService.getModelNameById(modelId));
		dto.setSpecName(dictService.getSpecNameById(specId));

		Merchant merchant = null;
		if (product.getMerchantId() != null) {
			try {
				merchant = merchantService.getMerchantInfo(product.getMerchantId());
			} catch (IllegalArgumentException ignored) {
			}
		}
		if (merchant != null) {
			dto.setMerchantName(merchant.getMerchantName());
			dto.setMerchantAddress(merchant.getMerchantAddress());
			dto.setMerchantCity(supplyService.getCityNameByCode(merchant.getCityCode()));
		}
		dto.setPrice(product.getPrice());
		dto.setStock(product.getStock());
		Integer type = product.getProductType();
		if (type != null && type == 1) {
			dto.setSecondHandCondition(product.getSecondHandCondition());
			dto.setSecondHandVersion(product.getSecondHandVersion());
			dto.setSecondHandFunction(product.getSecondHandFunction());
			dto.setRemark(null);
			dto.setOtherRemark(null);
			if (product.getImages() != null) {
				dto.setImageUrls(product.getImages().stream()
						.filter(img -> img.getImageUrl() != null && !img.getImageUrl().isEmpty())
						.map(ProductImage::getImageUrl)
						.toList());
			}
			dto.setBatteryStatus(product.getBatteryStatus());
		} else {
			dto.setSecondHandCondition(null);
			dto.setSecondHandVersion(null);
			dto.setSecondHandFunction(null);
			dto.setRemark(product.getRemark());
			dto.setOtherRemark(product.getOtherRemark());
			dto.setImageUrls(null);
			dto.setBatteryStatus(null);
		}
		dto.setListingTime(product.getListingTime());
		return dto;
	}

	private BuyRequestHallDto convertToBuyRequestHallDto(BuyRequest buyRequest) {
		BuyRequestHallDto dto = new BuyRequestHallDto();
		// 求购信息ID
		dto.setBuyRequestId(buyRequest.getId());

		// 品牌、系列、型号、规格
		dto.setBrandId(buyRequest.getBrandId());
		dto.setSeriesId(buyRequest.getSeriesId());
		dto.setModelId(buyRequest.getModelId());
		dto.setSpecId(buyRequest.getSpecId());
		dto.setBrandName(dictService.getBrandNameById(buyRequest.getBrandId()));
		dto.setSeriesName(dictService.getSeriesNameById(buyRequest.getSeriesId()));
		dto.setModelName(dictService.getModelNameById(buyRequest.getModelId()));
		dto.setSpecName(dictService.getSpecNameById(buyRequest.getSpecId()));
		// 求购数量
		dto.setBuyCount(buyRequest.getBuyCount());
		// 最低价格、最高价格
		dto.setMinPrice(buyRequest.getMinPrice());
		dto.setMaxPrice(buyRequest.getMaxPrice());
		// 截止时间，格式化时间，比如：2026-01-08 18:00
		dto.setDeadline(buyRequest.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
		// 求购人联系电话、求购人联系地址
		if (buyRequest.getMerchantId() != null) {
			try {
				Merchant merchant = merchantService.getMerchantInfo(buyRequest.getMerchantId());
				dto.setContactPhone(maskPhone(merchant.getMerchantPhone()));
				dto.setContactAddress(merchant.getMerchantAddress());
			} catch (IllegalArgumentException ignore) {
			}
		}
		// 求购商户ID
		dto.setMerchantId(buyRequest.getMerchantId());
		return dto;
	}

	private String maskPhone(String phone) {
		if (phone == null) {
			return null;
		}
		if (phone.length() != 11) {
			return phone;
		}
		return phone.substring(0, 3) + "****" + phone.substring(7);
	}
}
