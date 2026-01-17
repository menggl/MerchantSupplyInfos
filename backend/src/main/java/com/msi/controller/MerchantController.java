package com.msi.controller;

import com.msi.domain.Merchant;
import com.msi.domain.BuyRequest;
import com.msi.domain.Product;
import com.msi.dto.MerchantBuyRequestDto;
import com.msi.dto.MerchantBuyRequestModelDto;
import com.msi.dto.MerchantProductDto;
import com.msi.dto.MerchantProductModelDto;
import com.msi.service.DictService;
import com.msi.service.MerchantService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {
    private static final Logger logger = LoggerFactory.getLogger(MerchantController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final MerchantService merchantService;
    private final DictService dictService;

    public MerchantController(MerchantService merchantService, DictService dictService) {
        this.merchantService = merchantService;
        this.dictService = dictService;
    }

    private String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize object for logging", e);
            return String.valueOf(value);
        }
    }

    /**
     * 新增商家上架新机、二手机
     */
    @PostMapping("/products")
    public ResponseEntity<Product> addProduct(@RequestAttribute("merchant") Merchant currentMerchant, @RequestBody Product product) {
        try {
            logger.info("addProduct request: {}", toJson(product));
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            Product added = merchantService.addProduct(currentMerchant, product);
            logger.info("addProduct response: {}", toJson(added));
            return ResponseEntity.ok(added);
        } catch (IllegalArgumentException e) {
            logger.error("上架商品失败: merchantId={}, {}", currentMerchant.getId(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    /**
     * 删除某个上架产品，修改valid的字段为0
     */
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Void> deleteProduct(@RequestAttribute("merchant") Merchant currentMerchant, @PathVariable Long productId) {
        try {
            logger.info("deleteProduct request: {}", toJson(productId));
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            merchantService.deleteProduct(currentMerchant.getId(), productId);
            logger.info("deleteProduct response: {}", toJson("ok"));
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.error("删除商品失败: merchantId={}, {}", currentMerchant.getId(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    /**
     * 修改产品上架状态 state 1上架 0下架
     */
    @PutMapping("/products/{productId}/state")
    public ResponseEntity<Void> updateProductState(@RequestAttribute("merchant") Merchant currentMerchant, @PathVariable Long productId, @RequestParam Integer state) {
        try {
            logger.info("updateProductState request: {}", toJson(Map.of(
                    "merchantId", currentMerchant != null ? currentMerchant.getId() : null,
                    "productId", productId,
                    "state", state
            )));
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            merchantService.updateProductState(currentMerchant.getId(), productId, state);
            logger.info("updateProductState response: {}", toJson("ok"));
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.error("更新商品状态失败: merchantId={}, productId={}, {}", currentMerchant.getId(), productId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    /**
     * 修改一条已经存在的商品信息，修改完成后需要再次调用上架接口进行上架，修改完成后产品的状态是下架
     */
    @PutMapping("/products/{productId}")
    public ResponseEntity<Product> updateProduct(@RequestAttribute("merchant") Merchant currentMerchant, @PathVariable Long productId, @RequestBody Product product) {
        try {
            logger.info("updateProduct request: {}", toJson(Map.of(
                    "merchantId", currentMerchant != null ? currentMerchant.getId() : null,
                    "productId", productId,
                    "product", product
            )));
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            Product updated = merchantService.updateProduct(currentMerchant.getId(), productId, product);
            logger.info("updateProduct response: {}", toJson(updated));
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.error("更新商品信息失败: merchantId={}, productId={}, {}", currentMerchant.getId(), productId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    /**
     * 查询商家某个机型（品牌、系列、型号、配置）的上架信息，用于判断是否是重复上架了同型号的产品
     * 返回字段列表如下：
     * productId, brandId, seriesId, modelId, specId, brandName, seriesName, modelName, specName
     * 商家名称、商家地址、新机还是二手机字段、产品更新时间、价格
     * 二手机：成色
     * 新机：备注、其它备注
     * 使用一个dto MerchantProductModelDto 来返回，一个单独的convert方法来转换数据
     */
    @GetMapping("/products/model")
    public ResponseEntity<MerchantProductModelDto> getProductsByModel(
            @RequestAttribute("merchant") Merchant currentMerchant,
            @RequestParam Long brandId,
            @RequestParam Long seriesId,
            @RequestParam Long modelId,
            @RequestParam Long specId) {
        try {
            logger.info("getProductsByModel request: {}", toJson(Map.of(
                    "merchantId", currentMerchant != null ? currentMerchant.getId() : null,
                    "brandId", brandId,
                    "seriesId", seriesId,
                    "modelId", modelId,
                    "specId", specId
            )));
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            Product product = merchantService.getMerchantProductByModel(
                    currentMerchant.getId(), brandId, seriesId, modelId, specId);
            MerchantProductModelDto dto = convertToMerchantProductModelDto(product);
            logger.info("getProductsByModel response: {}", toJson(dto));
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            logger.error("查询商户某机型商品失败: merchantId={}, {}", 
                    currentMerchant != null ? currentMerchant.getId() : null, e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    /**
     * 查询商户所有已经上架了的商品列表
     * 返回的数据用一个单独的dto来封装，dto中的属性字段如下：
     * productId, brandId, seriesId, modelId, specId, brandName, seriesName, modelName, specName
     * 新机还是二手机字段、产品更新时间、价格、库存
     * 二手机：版本、成色、拆修和功能
     * 新机：备注、其它备注
     */
    @GetMapping("/products")
    public ResponseEntity<Page<MerchantProductDto>> getProductsByMerchant(
            @RequestAttribute("merchant") Merchant currentMerchant,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            logger.info("getProductsByMerchant request: {}", toJson(Map.of(
                    "merchantId", currentMerchant != null ? currentMerchant.getId() : null,
                    "page", page,
                    "size", size
            )));
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            Page<Product> products = merchantService.getMerchantProducts(currentMerchant.getId(), page, size);
            Page<MerchantProductDto> result = products.map(this::convertToMerchantProductDto);
            logger.info("getProductsByMerchant response: {}", toJson(result));
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.error("查询商户商品失败: merchantId={}, {}", currentMerchant.getId(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    



    /**
     * 增加：商家发布求购信息
     */
    @PostMapping("/products/buy")
    public ResponseEntity<BuyRequest> addBuyProduct(@RequestAttribute("merchant") Merchant currentMerchant, @RequestBody BuyRequest buyRequest) {
        try {
            logger.info("addBuyProduct request: {}", toJson(buyRequest));
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            BuyRequest added = merchantService.addBuyRequest(currentMerchant.getId(), buyRequest);
            logger.info("addBuyProduct response: {}", toJson(added));
            return ResponseEntity.ok(added);
        } catch (IllegalArgumentException e) {
            logger.error("发布求购商品失败: merchantId={}, {}", currentMerchant.getId(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    /**
     * 删除：商家移除某条求购信息，永远不可见（逻辑删除）,valid=0
     */
    @DeleteMapping("/products/buy/{buyRequestId}")
    public ResponseEntity<Void> deleteBuyProduct(@RequestAttribute("merchant") Merchant currentMerchant,
                                                 @PathVariable Long buyRequestId) {
        try {
            logger.info("deleteBuyProduct request: {}", toJson(Map.of(
                    "merchantId", currentMerchant != null ? currentMerchant.getId() : null,
                    "buyRequestId", buyRequestId
            )));
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            merchantService.deleteBuyRequest(currentMerchant.getId(), buyRequestId);
            logger.info("deleteBuyProduct response: {}", toJson("ok"));
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.error("删除求购信息失败: merchantId={}, {}", currentMerchant.getId(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    /**
     * 修改商家发布的某条求购信息，state状态,state: 0 已下架状态 1已上架状态
     */
    @PutMapping("/products/buy/{buyRequestId}/state")
    public ResponseEntity<Void> updateBuyProductState(
            @RequestAttribute("merchant") Merchant currentMerchant,
            @PathVariable Long buyRequestId,
            @RequestParam Integer state) {
        try {
            logger.info("updateBuyProductState request: {}", toJson(Map.of(
                    "merchantId", currentMerchant != null ? currentMerchant.getId() : null,
                    "buyRequestId", buyRequestId,
                    "state", state
            )));
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            merchantService.updateBuyRequestState(currentMerchant.getId(), buyRequestId, state);
            logger.info("updateBuyProductState response: {}", toJson("ok"));
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.error("更新求购商品状态失败: merchantId={}, buyRequestId={}, {}", 
                    currentMerchant != null ? currentMerchant.getId() : null, buyRequestId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    /**
     * 修改商家已经存在的一条求购信息，修改完成后，该求购信息的状态为下架状态，state=0，需要再手动调用一下上架接口
     */
    @PutMapping("/products/buy/{buyRequestId}")
    public ResponseEntity<BuyRequest> updateBuyProduct(
            @RequestAttribute("merchant") Merchant currentMerchant,
            @PathVariable Long buyRequestId,
            @RequestBody BuyRequest buyRequest) {
        try {
            logger.info("updateBuyProduct request: {}", toJson(Map.of(
                    "merchantId", currentMerchant != null ? currentMerchant.getId() : null,
                    "buyRequestId", buyRequestId,
                    "buyRequest", buyRequest
            )));
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            BuyRequest updated = merchantService.updateBuyRequest(currentMerchant.getId(), buyRequestId, buyRequest);
            logger.info("updateBuyProduct response: {}", toJson(updated));
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.error("更新求购商品信息失败: merchantId={}, buyRequestId={}, {}", 
                    currentMerchant != null ? currentMerchant.getId() : null, buyRequestId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    /**
     * 查询商家某个机型（品牌、系列、型号、配置）的求购信息，用户去重查询
     * 返回信息用一个单独的dto类来封装，dto中的字段包含下面的信息：
     * buyRequestId, brandId, seriesId, modelId, specId, brandName, seriesName, modelName, specName
     * 求购状态（上架还是下架）、求购更新时间、求购截止时间、求购数量、求购价格范围（最低价、最高价）、当前登录商家的联系电话，当前登录商家的地址
     * dto使用一个专门的convert方法来转换
     * 最后更新接口文档
     */
    @GetMapping("/products/buy/model")
    public ResponseEntity<MerchantBuyRequestModelDto> getBuyProductsByModel(
            @RequestAttribute("merchant") Merchant currentMerchant,
            @RequestParam Long brandId,
            @RequestParam Long seriesId,
            @RequestParam Long modelId,
            @RequestParam Long specId) {
        try {
            logger.info("getBuyProductsByModel request: {}", toJson(Map.of(
                    "merchantId", currentMerchant != null ? currentMerchant.getId() : null,
                    "brandId", brandId,
                    "seriesId", seriesId,
                    "modelId", modelId,
                    "specId", specId
            )));
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            BuyRequest buyRequest = merchantService.getMerchantBuyRequestByModel(
                    currentMerchant.getId(), brandId, seriesId, modelId, specId);
            MerchantBuyRequestModelDto dto = convertToMerchantBuyRequestModelDto(buyRequest);
            logger.info("getBuyProductsByModel response: {}", toJson(dto));
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            logger.error("查询商户某机型求购商品失败: merchantId={}, {}", 
                    currentMerchant != null ? currentMerchant.getId() : null, e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    /**
     * 查询商户发布的求购商品信息
     */
    @GetMapping("/products/buy")
    public ResponseEntity<Page<MerchantBuyRequestDto>> getBuyProductsByMerchant(
            @RequestAttribute("merchant") Merchant currentMerchant,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            logger.info("getBuyProductsByMerchant request: {}", toJson(Map.of(
                    "merchantId", currentMerchant != null ? currentMerchant.getId() : null,
                    "page", page,
                    "size", size
            )));
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            Page<BuyRequest> buyRequests = merchantService.getMerchantBuyRequests(currentMerchant.getId(), page, size);
            Page<MerchantBuyRequestDto> result = buyRequests.map(this::convertToMerchantBuyRequestDto);
            logger.info("getBuyProductsByMerchant response: {}", toJson(result));
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.error("查询商户求购商品失败: merchantId={}, {}", currentMerchant.getId(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    private MerchantBuyRequestModelDto convertToMerchantBuyRequestModelDto(BuyRequest buyRequest) {
        MerchantBuyRequestModelDto dto = new MerchantBuyRequestModelDto();
        dto.setBuyRequestId(buyRequest.getId());
        Long brandId = buyRequest.getBrandId();
        Long seriesId = buyRequest.getSeriesId();
        Long modelId = buyRequest.getModelId();
        Long specId = buyRequest.getSpecId();
        dto.setBrandId(brandId);
        dto.setSeriesId(seriesId);
        dto.setModelId(modelId);
        dto.setSpecId(specId);
        dto.setBrandName(dictService.getBrandNameById(brandId));
        dto.setSeriesName(dictService.getSeriesNameById(seriesId));
        dto.setModelName(dictService.getModelNameById(modelId));
        dto.setSpecName(dictService.getSpecNameById(specId));
        dto.setState(buyRequest.getState());
        dto.setUpdateTime(buyRequest.getUpdateTime());
        dto.setDeadline(buyRequest.getDeadline());
        dto.setBuyCount(buyRequest.getBuyCount());
        dto.setMinPrice(buyRequest.getMinPrice());
        dto.setMaxPrice(buyRequest.getMaxPrice());
        Merchant merchant = null;
        if (buyRequest.getMerchantId() != null) {
            try {
                merchant = merchantService.getMerchantInfo(buyRequest.getMerchantId());
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (merchant != null) {
            dto.setContactPhone(merchant.getMerchantPhone());
            dto.setContactAddress(merchant.getMerchantAddress());
        }
        return dto;
    }

    private MerchantProductDto convertToMerchantProductDto(Product product) {
        MerchantProductDto dto = new MerchantProductDto();
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
        dto.setProductType(product.getProductType());
        dto.setUpdateTime(product.getUpdateTime());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        Integer type = product.getProductType();
        if (type != null && type == 1) {
            dto.setSecondHandVersion(product.getSecondHandVersion());
            dto.setSecondHandCondition(product.getSecondHandCondition());
            dto.setSecondHandFunction(product.getSecondHandFunction());
            dto.setRemark(null);
            dto.setOtherRemark(null);
        } else {
            dto.setSecondHandVersion(null);
            dto.setSecondHandCondition(null);
            dto.setSecondHandFunction(null);
            dto.setRemark(product.getRemark());
            dto.setOtherRemark(product.getOtherRemark());
        }
        return dto;
    }

    private MerchantProductModelDto convertToMerchantProductModelDto(Product product) {
        MerchantProductModelDto dto = new MerchantProductModelDto();
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
        dto.setProductType(product.getProductType());
        dto.setUpdateTime(product.getUpdateTime());
        dto.setPrice(product.getPrice());
        Integer type = product.getProductType();
        if (type != null && type == 1) {
            dto.setSecondHandCondition(product.getSecondHandCondition());
            dto.setRemark(null);
            dto.setOtherRemark(null);
        } else {
            dto.setSecondHandCondition(null);
            dto.setRemark(product.getRemark());
            dto.setOtherRemark(product.getOtherRemark());
        }
        return dto;
    }

    /**
     * 商户签到随机送积分，2积分或者3积分
     */
    @PostMapping("/merchant/sign-in")
    public ResponseEntity<Void> signInForIntegral(@RequestAttribute("merchant") Merchant currentMerchant) {
        try {
            logger.info("signInForIntegral request: {}", toJson(Map.of(
                    "merchantId", currentMerchant != null ? currentMerchant.getId() : null
            )));
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            merchantService.signInForIntegral(currentMerchant.getId());
            logger.info("signInForIntegral response: {}", toJson("ok"));
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.error("商户签到送积分失败: merchantId={}, {}", currentMerchant.getId(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    private MerchantBuyRequestDto convertToMerchantBuyRequestDto(BuyRequest buyRequest) {
        MerchantBuyRequestDto dto = new MerchantBuyRequestDto();
        dto.setBuyRequestId(buyRequest.getId());
        dto.setBrandId(buyRequest.getBrandId());
        dto.setSeriesId(buyRequest.getSeriesId());
        dto.setModelId(buyRequest.getModelId());
        dto.setSpecId(buyRequest.getSpecId());
        dto.setBrandName(dictService.getBrandNameById(buyRequest.getBrandId()));
        dto.setSeriesName(dictService.getSeriesNameById(buyRequest.getSeriesId()));
        dto.setModelName(dictService.getModelNameById(buyRequest.getModelId()));
        dto.setSpecName(dictService.getSpecNameById(buyRequest.getSpecId()));
        dto.setProductType(buyRequest.getProductType());
        dto.setState(buyRequest.getState());
        dto.setBuyCount(buyRequest.getBuyCount());
        dto.setMinPrice(buyRequest.getMinPrice());
        dto.setMaxPrice(buyRequest.getMaxPrice());
        dto.setDeadline(buyRequest.getDeadline());
        Merchant merchant = null;
        if (buyRequest.getMerchantId() != null) {
            try {
                merchant = merchantService.getMerchantInfo(buyRequest.getMerchantId());
            } catch (IllegalArgumentException ignore) {
            }
        }
        if (merchant != null) {
            dto.setContactPhone(merchant.getMerchantPhone());
            dto.setContactAddress(merchant.getMerchantAddress());
        }
        return dto;
    }

}
