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

import io.netty.util.internal.ThreadLocalRandom;

import com.msi.repository.MerchantMemberInfoRepository;
import com.msi.repository.MerchantMemberIntegralRepository;
import com.msi.repository.MerchantMemberIntegralSpendRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
public class MerchantService {
    private final MerchantRepository merchantRepository;
    private final StringRedisTemplate redisTemplate;
    @Value("${aliyun.sms.region:cn-hangzhou}")
    private String aliyunRegion;
    @Value("${aliyun.sms.accessKeyId:}")
    private String accessKeyId;
    @Value("${aliyun.sms.accessKeySecret:}")
    private String accessKeySecret;
    @Value("${aliyun.sms.signName:}")
    private String signName;
    @Value("${aliyun.sms.templateCode:}")
    private String templateCode;
    @Value("${msi.member.default-days:30}")
    private int defaultMemberDays;
    private final MerchantMemberInfoRepository memberInfoRepository;

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
                           MerchantMemberIntegralSpendRepository merchantMemberIntegralSpendRepository) {
        this.merchantRepository = merchantRepository;
        this.redisTemplate = redisTemplate;
        this.memberInfoRepository = memberInfoRepository;
        this.wechatService = wechatService;
        this.smsService = smsService;
        this.productService = productService;
        this.merchantMemberIntegralRepository = merchantMemberIntegralRepository;
        this.merchantMemberIntegralSpendRepository = merchantMemberIntegralSpendRepository;
    }

    public static class WechatLoginInfo {
        private Merchant merchant;

        public Merchant getMerchant() { return merchant; }
        public void setMerchant(Merchant merchant) { this.merchant = merchant; }
    }

    public WechatLoginInfo loginByWechat(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Code不能为空");
        }
        
        // 调用微信接口获取 openid 和 session_key
        WechatService.WechatSession session = wechatService.getSession(code);
        String openid = session.getOpenid();
        if (openid == null || openid.isEmpty()) {
            throw new RuntimeException("微信接口返回缺少 openid");
        }
        
        // 生成 Token
        String token = java.util.UUID.randomUUID().toString().replace("-", "");
        
        // 查询数据库表有没有Merchant
        Optional<Merchant> merchantOpt = merchantRepository.findByWechatId(openid);
        Merchant merchant;
        if (merchantOpt.isEmpty()) {
            // 如果不存在，则插入一条数据到数据库表中（将token也保存进去）
            merchant = new Merchant();
            merchant.setWechatId(openid);
            merchant.setToken(token);
            merchant.setWechatName(null);
            merchant.setMerchantName(null);
            merchant.setMerchantPhone(null);
            merchant.setMerchantAddress(null);
            merchant.setMerchantLatitude(null);
            merchant.setMerchantLongitude(null);
            merchant.setContactName(null);
            merchant.setRegistrationDate(java.time.LocalDateTime.now());
            merchant.setCancellationDate(null);
            merchant.setIsValid(1);
            merchant = merchantRepository.save(merchant);
        } else {
            // 有则更新token值
            merchant = merchantOpt.get();
            // 检查商户是否已被禁用
            if (merchant.getIsValid() == 0) {
                throw new IllegalArgumentException("商户已被禁用");
            }
            merchant.setToken(token);
            merchant = merchantRepository.save(merchant);
        }

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

        WechatLoginInfo result = new WechatLoginInfo();
        result.setMerchant(merchant);
        return result;
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
            throw new IllegalArgumentException("商户ID不能为空");
        }
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
            throw new IllegalArgumentException("商户不存在");
        }
        Merchant merchant = opt.get();
        java.util.Optional<MerchantMemberInfo> infoOpt = memberInfoRepository.findByMerchantId(merchant.getId());
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
            throw new IllegalArgumentException("商户ID不能为空");
        }
        Optional<Merchant> opt = merchantRepository.findById(merchantId);
        if (opt.isEmpty()) {
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
            throw new IllegalArgumentException("商户ID不能为空");
        }
        if (product == null) {
            throw new IllegalArgumentException("商品信息不能为空");
        }
        validateProductForBusiness(product);
        product.setMerchantId(merchant.getId());
        return productService.publishProduct(product);
    }

    public Product updateProduct(Long merchantId, Long productId, Product product) {
        if (merchantId == null) {
            throw new IllegalArgumentException("商户ID不能为空");
        }
        if (productId == null) {
            throw new IllegalArgumentException("商品ID不能为空");
        }
        if (product == null) {
            throw new IllegalArgumentException("商品信息不能为空");
        }
        validateProductForBusiness(product);
        return productService.updateProduct(productId, merchantId, product);
    }

    private void validateProductForBusiness(Product product) {
        Integer productType = product.getProductType();
        if (productType == null) {
            throw new IllegalArgumentException("产品类型不能为空");
        }
        if (productType == 0) {
            if (product.getRemark() == null || product.getRemark().isEmpty()) {
                throw new IllegalArgumentException("备注不能为空");
            }
            if (product.getOtherRemark() == null || product.getOtherRemark().isEmpty()) {
                throw new IllegalArgumentException("其它备注不能为空");
            }
        } else if (productType == 1) {
            if (product.getSecondHandVersion() == null || product.getSecondHandVersion().isEmpty()) {
                throw new IllegalArgumentException("二手机版本不能为空");
            }
            if (product.getSecondHandCondition() == null || product.getSecondHandCondition().isEmpty()) {
                throw new IllegalArgumentException("二手机成色不能为空");
            }
            if (product.getSecondHandFunction() == null || product.getSecondHandFunction().isEmpty()) {
                throw new IllegalArgumentException("二手机功能描述不能为空");
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
                throw new IllegalArgumentException("二手机图片列表不能为空");
            }
        } else {
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
                                             Long specId) {
        return productService.findProductByMerchantAndModel(merchantId, brandId, seriesId, modelId, specId);
    }

    @Transactional
    public void signInForIntegral(Long merchantId) {
        if (merchantId == null) {
            throw new IllegalArgumentException("商户ID不能为空");
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
    }

    public Page<BuyRequest> getMerchantBuyRequests(Long merchantId, int page, int size) {
        return productService.findBuyProductsByMerchant(merchantId, page, size);
    }
    
    public BuyRequest getMerchantBuyRequestByModel(Long merchantId,
                                                   Long brandId,
                                                   Long seriesId,
                                                   Long modelId,
                                                   Long specId) {
        return productService.findBuyRequestByMerchantAndModel(merchantId, brandId, seriesId, modelId, specId);
    }

    @Transactional
    public BuyRequest addBuyRequest(Long merchantId, BuyRequest buyRequest) {
        if (merchantId == null) {
            throw new IllegalArgumentException("商户ID不能为空");
        }
        if (buyRequest == null) {
            throw new IllegalArgumentException("求购信息不能为空");
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
        int costIntegral = Constants.BUY_REQUEST_COST;
        MerchantMemberIntegral integral = merchantMemberIntegralRepository.findByMerchantId(merchantId).orElse(null);
        int currentIntegral = 0;
        if (integral != null && integral.getIntegral() != null) {
            currentIntegral = integral.getIntegral();
        }
        if (costIntegral > 0 && currentIntegral < costIntegral) {
            throw new IllegalArgumentException("积分不足");
        }
        buyRequest.setCostIntegral(costIntegral);
        buyRequest.setMerchantId(merchantId);
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
            throw new IllegalArgumentException("商户ID不能为空");
        }
        if (buyRequestId == null) {
            throw new IllegalArgumentException("求购ID不能为空");
        }
        if (state == null || (state != 0 && state != 1)) {
            throw new IllegalArgumentException("求购状态不合法");
        }
        productService.updateBuyRequestState(buyRequestId, merchantId, state);
    }
    
    public BuyRequest updateBuyRequest(Long merchantId, Long buyRequestId, BuyRequest buyRequest) {
        if (merchantId == null) {
            throw new IllegalArgumentException("商户ID不能为空");
        }
        if (buyRequestId == null) {
            throw new IllegalArgumentException("求购ID不能为空");
        }
        if (buyRequest == null) {
            throw new IllegalArgumentException("求购信息不能为空");
        }
        return productService.updateBuyRequest(buyRequestId, merchantId, buyRequest);
    }
    
    public void deleteBuyRequest(Long merchantId, Long buyRequestId) {
        if (merchantId == null) {
            throw new IllegalArgumentException("商户ID不能为空");
        }
        if (buyRequestId == null) {
            throw new IllegalArgumentException("求购ID不能为空");
        }
        productService.withdrawBuyRequest(buyRequestId, merchantId);
    }
    
    public Merchant updateMerchant(Long id, Merchant merchant) {
        if (id == null) {
            throw new IllegalArgumentException("商户ID不能为空");
        }
        Optional<Merchant> opt = merchantRepository.findById(id);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("商户不存在");
        }
        Merchant existing = opt.get();
        String name = merchant.getMerchantName();
        String contact = merchant.getContactName();
        String phone = merchant.getMerchantPhone();
        String address = merchant.getMerchantAddress();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("商户名称不能为空");
        }
        if (contact == null || contact.isEmpty()) {
            throw new IllegalArgumentException("联系人姓名不能为空");
        }
        if (phone == null || phone.isEmpty()) {
            throw new IllegalArgumentException("联系人手机号不能为空");
        }
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("手机号格式错误");
        }
        if (address == null || address.isEmpty()) {
            throw new IllegalArgumentException("地址不能为空");
        }
        if (merchant.getMerchantLatitude() == null || merchant.getMerchantLongitude() == null) {
            throw new IllegalArgumentException("经纬度不能为空");
        }
        /**
         * 手机号变更，每次都要验证
         * 验证完要删除验证码
         */
        if (!phone.equals(existing.getMerchantPhone())) {
            String code = smsService.getCode(existing.getWechatId(), phone);
            if (code == null || !code.equals(merchant.getCode())) {
                throw new IllegalArgumentException("验证码不正确");
            }
            smsService.deleteCode(existing.getWechatId(), phone);
        }
        existing.setMerchantName(name);
        existing.setContactName(contact);
        existing.setMerchantPhone(phone);
        existing.setMerchantAddress(address);
        existing.setMerchantLatitude(merchant.getMerchantLatitude());
        existing.setMerchantLongitude(merchant.getMerchantLongitude());
        return save(existing);
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
        } else {
            merchant.setIsMember(0);
            merchant.setMemberExpireDate(null);
        }
        
        try {
            ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
            String json = mapper.writeValueAsString(merchant);
            // 更新 token -> merchant（完整商户信息JSON）
            redisTemplate.opsForValue().set("token:" + merchant.getToken(), json);
            redisTemplate.opsForValue().set("merchant:info:" + merchant.getId(), json);
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
