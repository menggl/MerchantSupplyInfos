package com.msi.service;

import com.msi.domain.*;
import com.msi.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.LocalDateTime;

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
  private final MerchantMemberInfoRepository memberInfoRepository;
  private final BuyRequestRepository buyRequestRepository;

  public ProductService(ProductRepository productRepository,
                        BrandRepository brandRepository,
                        PhoneSeriesRepository seriesRepository,
                        PhoneModelRepository modelRepository,
                        PhoneSpecRepository specRepository,
                        MerchantRepository merchantRepository,
                        MerchantMemberInfoRepository memberInfoRepository,
                        BuyRequestRepository buyRequestRepository) {
    this.productRepository = productRepository;
    this.brandRepository = brandRepository;
    this.seriesRepository = seriesRepository;
    this.modelRepository = modelRepository;
    this.specRepository = specRepository;
    this.merchantRepository = merchantRepository;
    this.memberInfoRepository = memberInfoRepository;
    this.buyRequestRepository = buyRequestRepository;
  }

  public Product publishProduct(Product product) {
    if (product.getMerchant() == null || product.getMerchant().getId() == null) {
      throw new IllegalArgumentException("商户ID不能为空");
    }
    Merchant merchant = merchantRepository.findById(product.getMerchant().getId())
        .orElseThrow(() -> new IllegalArgumentException("商户不存在"));

    String merchantCityCode = merchant.getCityCode();
    if (merchantCityCode == null || merchantCityCode.isEmpty()) {
      throw new IllegalArgumentException("城市编码不能为空");
    }

    if (product.getBrand() == null || product.getBrand().getId() == null) {
      throw new IllegalArgumentException("品牌ID不能为空");
    }
    if (product.getSeries() == null || product.getSeries().getId() == null) {
      throw new IllegalArgumentException("系列ID不能为空");
    }
    if (product.getModel() == null || product.getModel().getId() == null) {
      throw new IllegalArgumentException("型号ID不能为空");
    }
    if (product.getSpec() == null || product.getSpec().getId() == null) {
      throw new IllegalArgumentException("配置ID不能为空");
    }

    Brand brand = brandRepository.findById(product.getBrand().getId())
        .orElseThrow(() -> new IllegalArgumentException("品牌ID不存在"));
    PhoneSeries series = seriesRepository.findById(product.getSeries().getId())
        .orElseThrow(() -> new IllegalArgumentException("系列ID不存在"));
    PhoneModel model = modelRepository.findById(product.getModel().getId())
        .orElseThrow(() -> new IllegalArgumentException("型号ID不存在"));
    PhoneSpec spec = specRepository.findById(product.getSpec().getId())
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

    product.setMerchant(merchant);
    product.setCityCode(merchantCityCode);
    product.setBrand(brand);
    product.setSeries(series);
    product.setModel(model);
    product.setSpec(spec);

    if (product.getImages() != null) {
      List<ProductImage> imgs = product.getImages().stream()
          .filter(pi -> pi.getImageUrl() != null && !pi.getImageUrl().isEmpty())
          .collect(Collectors.toList());
      imgs.forEach(pi -> pi.setProduct(product));
      product.setImages(imgs);
    }

    return productRepository.save(product);
  }

  public Page<Product> findProductsByMerchant(Long merchantId, int page, int size) {
    if (merchantId == null) {
      throw new IllegalArgumentException("商户ID不能为空");
    }
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updateTime"));
    return productRepository.findByMerchant_IdAndIsValid(merchantId, 1, pageable);
  }

  public Page<BuyRequest> findBuyProductsByMerchant(Long merchantId, int page, int size) {
    if (merchantId == null) {
      throw new IllegalArgumentException("商户ID不能为空");
    }
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updateTime"));
    return buyRequestRepository.findByMerchantIdAndIsValid(merchantId, 1, pageable);
  }

  public void withdrawProduct(Long productId) {
    if (productId == null) {
      throw new IllegalArgumentException("商品ID不能为空");
    }
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("商品不存在"));
    Merchant merchant = product.getMerchant();
    if (merchant == null || merchant.getId() == null) {
      throw new IllegalArgumentException("商户信息缺失");
    }
    Merchant upToDateMerchant = merchantRepository.findById(merchant.getId())
        .orElseThrow(() -> new IllegalArgumentException("商户不存在"));
    java.util.Optional<MerchantMemberInfo> infoOpt = memberInfoRepository.findByMerchant_Id(upToDateMerchant.getId());
    LocalDateTime expire = infoOpt.map(MerchantMemberInfo::getEndDate).orElse(null);
    Integer valid = infoOpt.map(MerchantMemberInfo::getIsValid).orElse(0);
    if (valid == 0 || expire == null || expire.isBefore(LocalDateTime.now())) {
      throw new IllegalArgumentException("商户会员已过期，拒绝下架");
    }
    if (product.getIsValid() != null && product.getIsValid() == 0) {
      return;
    }
    product.setIsValid(0);
    productRepository.save(product);
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

    String merchantCityCode = merchant.getCityCode();
    if (merchantCityCode == null || merchantCityCode.isEmpty()) {
      throw new IllegalArgumentException("城市编码不能为空");
    }

    if (buyRequest.getBrandId() == null) {
      throw new IllegalArgumentException("品牌ID不能为空");
    }
    if (buyRequest.getSeriesId() == null) {
      throw new IllegalArgumentException("系列ID不能为空");
    }
    if (buyRequest.getModelId() == null) {
      throw new IllegalArgumentException("型号ID不能为空");
    }
    if (buyRequest.getSpecId() == null) {
      throw new IllegalArgumentException("配置ID不能为空");
    }

    Brand brand = brandRepository.findById(buyRequest.getBrandId())
        .orElseThrow(() -> new IllegalArgumentException("品牌ID不存在"));
    PhoneSeries series = seriesRepository.findById(buyRequest.getSeriesId())
        .orElseThrow(() -> new IllegalArgumentException("系列ID不存在"));
    PhoneModel model = modelRepository.findById(buyRequest.getModelId())
        .orElseThrow(() -> new IllegalArgumentException("型号ID不存在"));
    PhoneSpec spec = specRepository.findById(buyRequest.getSpecId())
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
    if (buyRequest.getProductType() == null) {
      throw new IllegalArgumentException("产品类型不能为空");
    }
    Integer buyCount = buyRequest.getBuyCount();
    if (buyCount == null || buyCount <= 0) {
      throw new IllegalArgumentException("求购数量必须大于0");
    }
    Integer minPrice = buyRequest.getMinPrice();
    Integer maxPrice = buyRequest.getMaxPrice();
    if (minPrice == null || maxPrice == null) {
      throw new IllegalArgumentException("求购价格区间不能为空");
    }
    if (minPrice <= 0 || maxPrice <= 0 || maxPrice < minPrice) {
      throw new IllegalArgumentException("求购价格区间不合法");
    }
    if (buyRequest.getDeadline() == null) {
      throw new IllegalArgumentException("求购截止时间不能为空");
    }
    if (buyRequest.getCostIntegral() == null || buyRequest.getCostIntegral() < 0) {
      throw new IllegalArgumentException("求购花费积分不合法");
    }

    buyRequest.setMerchantId(merchant.getId());
    buyRequest.setCityCode(merchantCityCode);
    return buyRequestRepository.save(buyRequest);
  }
}
