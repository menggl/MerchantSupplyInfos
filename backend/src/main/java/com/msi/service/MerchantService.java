package com.msi.service;

import com.msi.domain.Merchant;
import com.msi.domain.Product;
import com.msi.constants.Constants;
import com.msi.domain.BuyRequest;
import com.msi.domain.ProductImage;
import com.msi.domain.MerchantMemberInfo;
import com.msi.domain.MerchantMemberIntegral;
import com.msi.domain.MerchantMemberIntegralSpend;
import com.msi.enums.IntegralChangeReason;
import com.msi.repository.MerchantRepository;
import com.msi.exception.DailySignInLimitExceededException;
import com.msi.exception.InsufficientIntegralException;

import io.netty.util.internal.ThreadLocalRandom;

import com.msi.repository.CityDictRepository;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import com.msi.repository.MerchantMemberInfoRepository;
import com.msi.repository.MerchantMemberIntegralRepository;
import com.msi.repository.MerchantMemberIntegralSpendRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.LocalDate;
import org.springframework.util.DigestUtils;
import java.nio.charset.StandardCharsets;

@Service
public class MerchantService {
    private static final Logger logger = LoggerFactory.getLogger(MerchantService.class);
    private final MerchantRepository merchantRepository;
    private final StringRedisTemplate redisTemplate;
    @Value("${msi.member.default-days:180}")
    private int defaultMemberDays;
    private final MerchantMemberInfoRepository memberInfoRepository;
    private final CityDictRepository cityDictRepository;
    private final Cache<String, Boolean> cityCodeCache;

    private final WechatService wechatService;
    private final SmsService smsService;
    private final ProductService productService;
    private final MerchantMemberIntegralRepository merchantMemberIntegralRepository;
    private final MerchantMemberIntegralSpendRepository merchantMemberIntegralSpendRepository;

    public MerchantService(MerchantRepository merchantRepository,
                           StringRedisTemplate redisTemplate,
                           MerchantMemberInfoRepository memberInfoRepository,
                           WechatService wechatService,
                           SmsService smsService,
                           ProductService productService,
                           MerchantMemberIntegralRepository merchantMemberIntegralRepository,
                           MerchantMemberIntegralSpendRepository merchantMemberIntegralSpendRepository,
                           CityDictRepository cityDictRepository) {
        this.merchantRepository = merchantRepository;
        this.redisTemplate = redisTemplate;
        this.memberInfoRepository = memberInfoRepository;
        this.wechatService = wechatService;
        this.smsService = smsService;
        this.productService = productService;
        this.merchantMemberIntegralRepository = merchantMemberIntegralRepository;
        this.merchantMemberIntegralSpendRepository = merchantMemberIntegralSpendRepository;
        this.cityDictRepository = cityDictRepository;
        this.cityCodeCache = CacheBuilder.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .maximumSize(1000)
                .build();
    }

    public Merchant loginByWechat(String code) {
        if (code == null || code.isEmpty()) {
            logger.error("Code不能为空");
            throw new IllegalArgumentException("Code不能为空");
        }
        
        // 调用微信接口获取 openid 和 session_key
        WechatService.WechatSession session = wechatService.getSession(code);
        String openid = session.getOpenid();
        if (openid == null || openid.isEmpty()) {
            throw new RuntimeException("微信接口返回缺少 openid");
        }
        
        // 查询数据库表有没有Merchant
        Optional<Merchant> merchantOpt = merchantRepository.findByWechatId(openid);
        Merchant merchant;
        if (merchantOpt.isEmpty()) {
            // 如果不存在，则插入一条数据到数据库表中
            merchant = new Merchant();
            merchant.setWechatId(openid);
            // 默认初始化一些字段
            merchant.setRegistrationDate(java.time.LocalDateTime.now());
            merchant.setIsValid(1);
            merchant.setPublicId(java.util.UUID.randomUUID().toString().replace("-", ""));
            // 首次保存以获取ID
            merchant = merchantRepository.save(merchant);
        } else {
            merchant = merchantOpt.get();
        }
        
        return processLoginSuccess(merchant);
    }

    public Merchant loginByPhone(String phone, String password) {
        if (phone == null || phone.isEmpty()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        // 查询商户
        Optional<Merchant> merchantOpt = merchantRepository.findByMerchantPhone(phone);
        if (merchantOpt.isEmpty()) {
            throw new IllegalArgumentException("商户不存在");
        }
        Merchant merchant = merchantOpt.get();
        
        // 验证密码
        String md5Passwd = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        if (!md5Passwd.equalsIgnoreCase(merchant.getPasswd())) {
            throw new IllegalArgumentException("密码错误");
        }
        
        return processLoginSuccess(merchant);
    }

    private Merchant processLoginSuccess(Merchant merchant) {
        // 检查商户是否已被禁用
        if (merchant.getIsValid() != null && merchant.getIsValid() == 0) {
            throw new IllegalArgumentException("商户已被禁用");
        }
        
        // 生成 Token
        String token = java.util.UUID.randomUUID().toString().replace("-", "");
        merchant.setToken(token);
        merchant = merchantRepository.save(merchant);

        MerchantMemberInfo info = null;
        java.util.Optional<MerchantMemberInfo> infoOpt = memberInfoRepository.findByMerchantId(merchant.getId());
        if (infoOpt.isEmpty()) {
            info = new MerchantMemberInfo();
            info.setMerchantId(merchant.getId());
            info.setStartDate(java.time.LocalDateTime.now());
            info.setEndDate(java.time.LocalDateTime.now().plusDays(defaultMemberDays));
            info.setMemberType(1);
            info.setPaymentAmount(java.math.BigDecimal.ZERO);
            info.setOriginalPrice(java.math.BigDecimal.ZERO);
            info.setDiscountPrice(java.math.BigDecimal.ZERO);
            info.setCommission(java.math.BigDecimal.ZERO);
            info.setIsValid(1);
            info = memberInfoRepository.save(info);
        } else {
            info = infoOpt.get();
        }
        // 更新缓存
        updateLoginCache(merchant, info);
        return merchant;
    }
    
    public List<Merchant> findAll() {
        return merchantRepository.findAll();
    }

    public Optional<Merchant> findById(Long id) {
        return merchantRepository.findById(id);
    }

    public Merchant save(Merchant merchant) {
        return merchantRepository.save(merchant);
    }

    public Merchant getMerchantInfo(Long id) {
        if (id == null) {
            logger.error("商户ID不能为空");
            throw new IllegalArgumentException("商户ID不能为空");
        }
        // todo 测试用，将缓存先清除
        redisTemplate.delete("merchant:info:" + id);

        String json = redisTemplate.opsForValue().get("merchant:info:" + id);
        if (json != null && !json.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
                return mapper.readValue(json, Merchant.class);
            } catch (Exception e) {
                // ignore parse error and fallback to db
            }
        }
        Optional<Merchant> opt = merchantRepository.findById(id);
        if (opt.isEmpty()) {
            logger.error("商户不存在");
            throw new IllegalArgumentException("商户不存在");
        }
        Merchant merchant = opt.get();
        java.util.Optional<MerchantMemberInfo> infoOpt = memberInfoRepository.findByMerchantId(merchant.getId());
        MerchantMemberInfo info = infoOpt.orElse(null);
        updateLoginCache(merchant, info);
        return merchant;
    }

    public Merchant getMerchantInfoByPublicId(String publicId) {
        if (publicId == null || publicId.isEmpty()) {
            logger.error("商户publicId不能为空");
            throw new IllegalArgumentException("商户publicId不能为空");
        }
        Optional<Merchant> opt = merchantRepository.findByPublicId(publicId);
        if (opt.isEmpty()) {
            logger.error("商户不存在");
            throw new IllegalArgumentException("商户不存在");
        }
        Merchant merchant = opt.get();
        Optional<MerchantMemberInfo> infoOpt = memberInfoRepository.findByMerchantId(merchant.getId());
        MerchantMemberInfo info = infoOpt.orElse(null);
        updateLoginCache(merchant, info);
        return merchant;
    }

    public void deleteById(Long id) {
        merchantRepository.deleteById(id);
    }

    public void deleteProduct(Long merchantId, Long productId) {
        productService.withdrawProduct(productId, merchantId);
    }

    public void updateProductState(Long merchantId, Long productId, Integer state) {
        productService.updateProductState(productId, merchantId, state);
    }

    public boolean isMemberExpired(Long merchantId) {
        if (merchantId == null) {
            logger.error("商户ID不能为空");
            throw new IllegalArgumentException("商户ID不能为空");
        }
        Optional<Merchant> opt = merchantRepository.findById(merchantId);
        if (opt.isEmpty()) {
            logger.error("商户不存在");
            throw new IllegalArgumentException("商户不存在");
        }
        Merchant m = opt.get();
        java.util.Optional<com.msi.domain.MerchantMemberInfo> infoOpt = memberInfoRepository.findByMerchantId(m.getId());
        if (infoOpt.isEmpty()) {
            return true;
        }
        LocalDateTime expire = infoOpt.get().getEndDate();
        if (expire == null) {
            return true;
        }
        return expire.isBefore(LocalDateTime.now());
    }

    public Product addProduct(Merchant merchant, Product product) {
        if (merchant == null || merchant.getId() == null) {
            logger.error("商户ID不能为空");
            throw new IllegalArgumentException("商户ID不能为空");
        }
        if (product == null) {
            logger.error("商品信息不能为空");
            throw new IllegalArgumentException("商品信息不能为空");
        }
        validateProductForBusiness(product);
        product.setMerchantId(merchant.getId());
        return productService.publishProduct(product);
    }

    public Product updateProduct(Long merchantId, Long productId, Product product) {
        if (merchantId == null) {
            logger.error("商户ID不能为空");
            throw new IllegalArgumentException("商户ID不能为空");
        }
        if (productId == null) {
            logger.error("商品ID不能为空");
            throw new IllegalArgumentException("商品ID不能为空");
        }
        if (product == null) {
            logger.error("商品信息不能为空");
            throw new IllegalArgumentException("商品信息不能为空");
        }
        validateProductForBusiness(product);
        return productService.updateProduct(productId, merchantId, product);
    }

    private void validateProductForBusiness(Product product) {
        Integer productType = product.getProductType();
        if (productType == null) {
            logger.error("产品类型不能为空");
            throw new IllegalArgumentException("产品类型不能为空");
        }
        if (productType == 0) {
            if (product.getRegion() == null || product.getRegion().isEmpty()) {
                logger.error("新机售卖区域不能为空");
                throw new IllegalArgumentException("新机售卖区域不能为空");
            }
            if (product.getRemark() == null || product.getRemark().isEmpty()) {
                logger.error("备注不能为空");
                throw new IllegalArgumentException("备注不能为空");
            }
            if (product.getOtherRemark() == null || product.getOtherRemark().isEmpty()) {
                logger.error("其它备注不能为空");
                throw new IllegalArgumentException("其它备注不能为空");
            }
        } else if (productType == 1) {
            // 二手机忽略区域字段，不入库
            product.setRegion(null);
            
            if (product.getSecondHandVersion() == null || product.getSecondHandVersion().isEmpty()) {
                logger.error("二手机版本不能为空");
                throw new IllegalArgumentException("二手机版本不能为空");
            }
            if (product.getSecondHandCondition() == null || product.getSecondHandCondition().isEmpty()) {
                logger.error("二手机成色不能为空");
                throw new IllegalArgumentException("二手机成色不能为空");
            }
            if (product.getSecondHandFunction() == null || product.getSecondHandFunction().isEmpty()) {
                logger.error("二手机功能描述不能为空");
                throw new IllegalArgumentException("二手机功能描述不能为空");
            }
            if (product.getBatteryStatus() == null) {
                logger.error("二手机电池性能不能为空");
                throw new IllegalArgumentException("二手机电池性能不能为空");
            }
            boolean hasValidImage = false;
            if (product.getImages() != null) {
                for (ProductImage image : product.getImages()) {
                    if (image != null && image.getImageUrl() != null && !image.getImageUrl().isEmpty()) {
                        hasValidImage = true;
                        break;
                    }
                }
            }
            if (!hasValidImage) {
                logger.error("二手机图片列表不能为空");
                throw new IllegalArgumentException("二手机图片列表不能为空");
            }
        } else {
            logger.error("产品类型不支持");
            throw new IllegalArgumentException("产品类型不支持");
        }
    }

    public Page<Product> getMerchantProducts(Long merchantId, int page, int size) {
        return productService.findProductsByMerchant(merchantId, page, size);
    }

    public Page<Product> getMerchantProductsByModel(Long merchantId,
                                                    Long brandId,
                                                    Long seriesId,
                                                    Long modelId,
                                                    Long specId,
                                                    int page,
                                                    int size) {
        return productService.findProductsByMerchantAndModel(merchantId, brandId, seriesId, modelId, specId, page, size);
    }

    public Product getMerchantProductByModel(Long merchantId,
                                             Long brandId,
                                             Long seriesId,
                                             Long modelId,
                                             Long specId,
                                             Integer productType) {
        return productService.findProductByMerchantAndModel(merchantId, brandId, seriesId, modelId, specId, productType);
    }

    public Product getMerchantProduct(Long merchantId, Long productId) {
        return productService.findProductByMerchantAndId(merchantId, productId);
    }

    @Transactional
    public int signInForIntegral(Long merchantId) {
        if (merchantId == null) {
            logger.error("商户ID不能为空");
            throw new IllegalArgumentException("商户ID不能为空");
        }
        Optional<Merchant> opt = merchantRepository.findById(merchantId);
        if (opt.isEmpty()) {
            logger.error("商户不存在");
            throw new IllegalArgumentException("商户不存在");
        }
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        long todaySignInCount = merchantMemberIntegralSpendRepository
                .countByMerchantIdAndChangeReasonAndChangeTimeBetween(
                        merchantId,
                        IntegralChangeReason.SIGN_IN.getDescription(),
                        startOfDay,
                        endOfDay
                );
        if (todaySignInCount > 0) {
            throw new DailySignInLimitExceededException("今日已签到，不能重复签到");
        }
        MerchantMemberIntegral integral = merchantMemberIntegralRepository.findByMerchantId(merchantId).orElse(null);
        int before = 0;
        if (integral != null && integral.getIntegral() != null) {
            before = integral.getIntegral();
        }
        int change = ThreadLocalRandom.current().nextInt(2, 4);
        int after = before + change;
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
        record.setChangeAmount(change);
        record.setChangeReason(IntegralChangeReason.SIGN_IN.getDescription());
        record.setOrderId(null);
        record.setChangeTime(LocalDateTime.now());
        merchantMemberIntegralSpendRepository.save(record);
        return change;
    }

    public com.msi.dto.MerchantIntegralDto getMerchantIntegral(Long merchantId) {
        if (merchantId == null) {
            logger.error("商户ID不能为空");
            throw new IllegalArgumentException("商户ID不能为空");
        }
        Optional<Merchant> opt = merchantRepository.findById(merchantId);
        if (opt.isEmpty()) {
            logger.error("商户不存在");
            throw new IllegalArgumentException("商户不存在");
        }
        MerchantMemberIntegral integral = merchantMemberIntegralRepository.findByMerchantId(merchantId).orElse(null);
        if (integral == null) {
            integral = new MerchantMemberIntegral();
            integral.setMerchantId(merchantId);
            integral.setIntegral(0);
            integral.setIsValid(1);
            integral.setCreateTime(LocalDateTime.now());
            integral.setUpdateTime(LocalDateTime.now());
            merchantMemberIntegralRepository.save(integral);
        }
        int value = 0;
        if (integral.getIntegral() != null) {
            value = integral.getIntegral();
        }
        com.msi.dto.MerchantIntegralDto dto = new com.msi.dto.MerchantIntegralDto();
        dto.setIntegral(value);
        dto.setCostIntegral(Constants.BUY_REQUEST_COST);
        return dto;
    }

    public Page<BuyRequest> getMerchantBuyRequests(Long merchantId, int page, int size) {
        return productService.findBuyProductsByMerchant(merchantId, page, size);
    }
    
    public BuyRequest getMerchantBuyRequestByModel(Long merchantId,
                                                   Long brandId,
                                                   Long seriesId,
                                                   Long modelId,
                                                   Long specId,
                                                   Integer productType) {
        return productService.findBuyRequestByMerchantAndModel(merchantId, brandId, seriesId, modelId, specId, productType);
    }

    @Transactional
    public BuyRequest addBuyRequest(Long merchantId, BuyRequest buyRequest) {
        if (merchantId == null) {
            logger.error("商户ID不能为空");
            throw new IllegalArgumentException("商户ID不能为空");
        }
        if (buyRequest == null) {
            logger.error("求购信息不能为空");
            throw new IllegalArgumentException("求购信息不能为空");
        }
        if (buyRequest.getBrandId() == null) {
            logger.error("品牌ID不能为空");
            throw new IllegalArgumentException("品牌ID不能为空");
        }
        if (buyRequest.getSeriesId() == null) {
            logger.error("系列ID不能为空");
            throw new IllegalArgumentException("系列ID不能为空");
        }
        if (buyRequest.getModelId() == null) {
            logger.error("型号ID不能为空");
            throw new IllegalArgumentException("型号ID不能为空");
        }
        if (buyRequest.getSpecId() == null) {
            logger.error("配置ID不能为空");
            throw new IllegalArgumentException("配置ID不能为空");
        }
        if (buyRequest.getProductType() == null) {
            logger.error("产品类型不能为空");
            throw new IllegalArgumentException("产品类型不能为空");
        }
        Integer buyCount = buyRequest.getBuyCount();
        if (buyCount == null || buyCount <= 0) {
            logger.error("求购数量必须大于0");
            throw new IllegalArgumentException("求购数量必须大于0");
        }
        Integer minPrice = buyRequest.getMinPrice();
        Integer maxPrice = buyRequest.getMaxPrice();
        if (minPrice == null || maxPrice == null) {
            logger.error("求购价格区间不能为空");
            throw new IllegalArgumentException("求购价格区间不能为空");
        }
        if (minPrice <= 0 || maxPrice <= 0 || maxPrice < minPrice) {
            logger.error("求购价格区间不合法");
            throw new IllegalArgumentException("求购价格区间不合法");
        }
        if (buyRequest.getDeadline() == null) {
            logger.error("求购截止时间不能为空");
            throw new IllegalArgumentException("求购截止时间不能为空");
        }
        int costIntegral = Constants.BUY_REQUEST_COST;
        MerchantMemberIntegral integral = merchantMemberIntegralRepository.findByMerchantId(merchantId).orElse(null);
        int currentIntegral = 0;
        if (integral != null && integral.getIntegral() != null) {
            currentIntegral = integral.getIntegral();
        }
        if (costIntegral > 0 && currentIntegral < costIntegral) {
            logger.error("积分不足");
            throw new InsufficientIntegralException("积分不足");
        }
        buyRequest.setCostIntegral(costIntegral);
        buyRequest.setMerchantId(merchantId);
        buyRequest.setState(1);
        BuyRequest saved = productService.publishBuyRequest(buyRequest);

        if (costIntegral > 0) {
            int afterIntegral = currentIntegral - costIntegral;
            if (integral == null) {
                integral = new MerchantMemberIntegral();
                integral.setMerchantId(merchantId);
            }
            integral.setIntegral(afterIntegral);
            merchantMemberIntegralRepository.save(integral);
            
            MerchantMemberIntegralSpend record = new MerchantMemberIntegralSpend();
            record.setMerchantId(merchantId);
            record.setIntegralBeforeSpend(currentIntegral);
            record.setIntegralAfterSpend(afterIntegral);
            record.setChangeAmount(-costIntegral);
            record.setChangeReason(IntegralChangeReason.SPEND_BUY_REQUEST.getDescription());
            record.setOrderId(saved.getId());
            record.setChangeTime(LocalDateTime.now());
            merchantMemberIntegralSpendRepository.save(record);
        }
        return saved;
    }
    
    
    public void updateBuyRequestState(Long merchantId, Long buyRequestId, Integer state) {
        if (merchantId == null) {
            logger.error("商户ID不能为空");
            throw new IllegalArgumentException("商户ID不能为空");
        }
        if (buyRequestId == null) {
            logger.error("求购ID不能为空");
            throw new IllegalArgumentException("求购ID不能为空");
        }
        if (state == null || (state != 0 && state != 1)) {
            logger.error("求购状态不合法");
            throw new IllegalArgumentException("求购状态不合法");
        }
        productService.updateBuyRequestState(buyRequestId, merchantId, state);
    }
    
    public BuyRequest updateBuyRequest(Long merchantId, Long buyRequestId, BuyRequest buyRequest) {
        if (merchantId == null) {
            logger.error("商户ID不能为空");
            throw new IllegalArgumentException("商户ID不能为空");
        }
        if (buyRequestId == null) {
            logger.error("求购ID不能为空");
            throw new IllegalArgumentException("求购ID不能为空");
        }
        if (buyRequest == null) {
            logger.error("求购信息不能为空");
            throw new IllegalArgumentException("求购信息不能为空");
        }
        return productService.updateBuyRequest(buyRequestId, merchantId, buyRequest);
    }
    
    public void deleteBuyRequest(Long merchantId, Long buyRequestId) {
        if (merchantId == null) {
            logger.error("商户ID不能为空");
            throw new IllegalArgumentException("商户ID不能为空");
        }
        if (buyRequestId == null) {
            logger.error("求购ID不能为空");
            throw new IllegalArgumentException("求购ID不能为空");
        }
        productService.withdrawBuyRequest(buyRequestId, merchantId);
    }
    
    public Merchant updateMerchant(Long id, Merchant merchant) {
        if (id == null) {
            logger.error("商户ID不能为空");
            throw new IllegalArgumentException("商户ID不能为空");
        }
        // 添加城市code入参校验
        String cityCode = merchant.getCityCode();
        if (cityCode == null || cityCode.isEmpty()) {
            logger.error("城市code不能为空");
            throw new IllegalArgumentException("城市code不能为空");
        }
        // 判断cityCode是否在字典表中
        try {
            if (!cityCodeCache.get(cityCode, () -> cityDictRepository.existsByCityCode(cityCode))) {
                logger.error("城市code不存在");
                throw new IllegalArgumentException("城市code不存在");
            }
        } catch (ExecutionException e) {
            throw new RuntimeException("验证城市Code失败", e);
        }

        Optional<Merchant> opt = merchantRepository.findById(id);
        if (opt.isEmpty()) {
            logger.error("商户不存在");
            throw new IllegalArgumentException("商户不存在");
        }
        Merchant existing = opt.get();
        String name = merchant.getMerchantName();
        String contact = merchant.getContactName();
        String phone = merchant.getMerchantPhone();
        String address = merchant.getMerchantAddress();
        String businessLicenseUrl = merchant.getBusinessLicenseUrl();
        String storePhotoUrl = merchant.getStorePhotoUrl();
        String idCardPhotoUrl = merchant.getIdCardPhotoUrl();
        if (name == null || name.isEmpty()) {
            logger.error("商户名称不能为空");
            throw new IllegalArgumentException("商户名称不能为空");
        }
        if (contact == null || contact.isEmpty()) {
            logger.error("联系人姓名不能为空");
            throw new IllegalArgumentException("联系人姓名不能为空");
        }
        if (phone == null || phone.isEmpty()) {
            logger.error("联系人手机号不能为空");
            throw new IllegalArgumentException("联系人手机号不能为空");
        }
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            logger.error("手机号格式错误");
            throw new IllegalArgumentException("手机号格式错误");
        }
        if (address == null || address.isEmpty()) {
            logger.error("地址不能为空");
            throw new IllegalArgumentException("地址不能为空");
        }
        if (businessLicenseUrl != null && !businessLicenseUrl.isEmpty()) {
            existing.setBusinessLicenseUrl(businessLicenseUrl);
        }
        if (storePhotoUrl != null && !storePhotoUrl.isEmpty()) {
            existing.setStorePhotoUrl(storePhotoUrl);
        }
        if (idCardPhotoUrl != null && !idCardPhotoUrl.isEmpty()) {
            existing.setIdCardPhotoUrl(idCardPhotoUrl);
        }
        
        // 更新密码逻辑
        String passwd = merchant.getPasswd();
        if (passwd != null && !passwd.isEmpty()) {
            // 对用户上传的密码进行MD5加密
            String md5Passwd = DigestUtils.md5DigestAsHex(passwd.getBytes(StandardCharsets.UTF_8));
            existing.setPasswd(md5Passwd);
        }
        /**
         * 手机号变更，每次都要验证
         * 验证完要删除验证码
         */
        if (!phone.equals(existing.getMerchantPhone())) {
            String code = smsService.getCode(existing.getWechatId(), phone);
            if (code == null || !code.equals(merchant.getCode())) {
                logger.error("验证码不正确");
                throw new IllegalArgumentException("验证码不正确");
            }
            smsService.deleteCode(existing.getWechatId(), phone);
        }

        Optional<MerchantMemberInfo> infoOpt = memberInfoRepository.findByMerchantId(existing.getId());
        MerchantMemberInfo info = infoOpt.orElse(null);

        boolean firstProfileUpdate =
                (existing.getMerchantName() == null || existing.getMerchantName().isEmpty()) &&
                (existing.getMerchantPhone() == null || existing.getMerchantPhone().isEmpty())
                || info == null;

        MerchantMemberInfo updatedMemberInfo = info;
        if (firstProfileUpdate) {
            LocalDateTime now = LocalDateTime.now();
            existing.setRegistrationDate(now);

            if (info == null) {
                info = new MerchantMemberInfo();
                info.setMerchantId(existing.getId());
                info.setMemberType(1);
                info.setPaymentAmount(java.math.BigDecimal.ZERO);
                info.setOriginalPrice(java.math.BigDecimal.ZERO);
                info.setDiscountPrice(java.math.BigDecimal.ZERO);
                info.setCommission(java.math.BigDecimal.ZERO);
            }
            info.setStartDate(now);
            info.setEndDate(now.plusDays(defaultMemberDays));
            info.setIsValid(1);
            updatedMemberInfo = memberInfoRepository.save(info);
        }

        existing.setMerchantName(name);
        existing.setContactName(contact);
        existing.setMerchantPhone(phone);
        existing.setMerchantAddress(address);
        existing.setMerchantLatitude(merchant.getMerchantLatitude());
        existing.setMerchantLongitude(merchant.getMerchantLongitude());
        existing.setCityCode(cityCode);

        Merchant saved = save(existing);

        if (updatedMemberInfo != null) {
            updateLoginCache(saved, updatedMemberInfo);
        }
        return saved;
    }

    public void refreshLoginCache(Merchant merchant, MerchantMemberInfo info) {
        updateLoginCache(merchant, info);
    }

    private void updateLoginCache(Merchant merchant, MerchantMemberInfo info) {
        // 如果没有token，生成并保存
        if (merchant.getToken() == null || merchant.getToken().isEmpty()) {
            String token = java.util.UUID.randomUUID().toString().replace("-", "");
            merchant.setToken(token);
            merchantRepository.save(merchant);
        }
        
        // 更新 merchantId -> merchantInfo（完整商户信息JSON）
        if (info != null && info.getIsValid() != null && info.getIsValid() == 1 && 
            info.getEndDate() != null && info.getEndDate().isAfter(java.time.LocalDateTime.now())) {
            merchant.setIsMember(1);
            merchant.setMemberExpireDate(info.getEndDate());
            merchant.setMemberStartDate(info.getStartDate());
        } else {
            merchant.setIsMember(0);
            merchant.setMemberExpireDate(null);
        }
        // 查询用户积分表merchant_member_integral，更新到merchant对象中
        MerchantMemberIntegral integral = merchantMemberIntegralRepository.findByMerchantId(merchant.getId()).orElse(null);
        if (integral != null) {
            merchant.setIntegral(integral.getIntegral());
        } else {
            merchant.setIntegral(0);
        }

        
        try {
            ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
            String json = mapper.writeValueAsString(merchant);
            // 更新 token -> merchant（完整商户信息JSON），设置1天过期
            redisTemplate.opsForValue().set("token:" + merchant.getToken(), json, 1, TimeUnit.DAYS);
            redisTemplate.opsForValue().set("merchant:info:" + merchant.getId(), json, 1, TimeUnit.DAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public com.msi.domain.Merchant getMerchantByToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        String v = redisTemplate.opsForValue().get("token:" + token);
        if (v != null && !v.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
                return mapper.readValue(v, com.msi.domain.Merchant.class);
            } catch (Exception ignore) {
                // 如果不是JSON，尝试当作ID处理（兼容旧缓存）
                try {
                    Long id = Long.parseLong(v);
                    String json = redisTemplate.opsForValue().get("merchant:info:" + id);
                    if (json != null) {
                        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
                        return mapper.readValue(json, com.msi.domain.Merchant.class);
                    }
                } catch (Exception ignored) {}
            }
        }
        // 缓存未命中，走数据库
        java.util.Optional<Merchant> opt = merchantRepository.findByToken(token);
        if (opt.isEmpty()) {
            return null;
        }
        Merchant m = opt.get();
        java.util.Optional<MerchantMemberInfo> infoOpt = memberInfoRepository.findByMerchantId(m.getId());
        updateLoginCache(m, infoOpt.orElse(null));
        return m;
    }

}
