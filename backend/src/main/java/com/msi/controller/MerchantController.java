package com.msi.controller;

import com.msi.domain.Merchant;
import com.msi.domain.BuyRequest;
import com.msi.domain.Product;
import com.msi.service.MerchantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {
    private static final Logger logger = LoggerFactory.getLogger(MerchantController.class);
    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    /**
     * 新增商家上架新机、二手机
     */
    @PostMapping("/products")
    public ResponseEntity<Product> addProduct(@RequestAttribute("merchant") Merchant currentMerchant, @RequestBody Product product) {
        try {
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            Product added = merchantService.addProduct(currentMerchant, product);
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
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            merchantService.deleteProduct(currentMerchant.getId(), productId);
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
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            merchantService.updateProductState(currentMerchant.getId(), productId, state);
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
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            Product updated = merchantService.updateProduct(currentMerchant.getId(), productId, product);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.error("更新商品信息失败: merchantId={}, productId={}, {}", currentMerchant.getId(), productId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    /**
     * 查询商家某个机型（品牌、系列、型号、配置）的上架信息，前端逻辑：如果商家已经有该型号的产品，就修改该产品，如果没有才允许新增一条产品
     */
    @GetMapping("/products/model")
    public ResponseEntity<Product> getProductsByModel(
            @RequestAttribute("merchant") Merchant currentMerchant,
            @RequestParam Long brandId,
            @RequestParam Long seriesId,
            @RequestParam Long modelId,
            @RequestParam Long specId) {
        try {
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            Product product = merchantService.getMerchantProductByModel(
                    currentMerchant.getId(), brandId, seriesId, modelId, specId);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            logger.error("查询商户某机型商品失败: merchantId={}, {}", 
                    currentMerchant != null ? currentMerchant.getId() : null, e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    /**
     * 查询商户所有已经上架了的商品
     */
    @GetMapping("/products")
    public ResponseEntity<Page<Product>> getProductsByMerchant(
            @RequestAttribute("merchant") Merchant currentMerchant,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            Page<Product> products = merchantService.getMerchantProducts(currentMerchant.getId(), page, size);
            return ResponseEntity.ok(products);
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
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            BuyRequest added = merchantService.addBuyRequest(currentMerchant.getId(), buyRequest);
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
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            merchantService.deleteBuyRequest(currentMerchant.getId(), buyRequestId);
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
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            merchantService.updateBuyRequestState(currentMerchant.getId(), buyRequestId, state);
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
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            BuyRequest updated = merchantService.updateBuyRequest(currentMerchant.getId(), buyRequestId, buyRequest);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.error("更新求购商品信息失败: merchantId={}, buyRequestId={}, {}", 
                    currentMerchant != null ? currentMerchant.getId() : null, buyRequestId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    /**
     * 查询商家某个机型（品牌、系列、型号、配置）的求购信息
     */
    @GetMapping("/products/buy/model")
    public ResponseEntity<BuyRequest> getBuyProductsByModel(
            @RequestAttribute("merchant") Merchant currentMerchant,
            @RequestParam Long brandId,
            @RequestParam Long seriesId,
            @RequestParam Long modelId,
            @RequestParam Long specId) {
        try {
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            BuyRequest buyRequest = merchantService.getMerchantBuyRequestByModel(
                    currentMerchant.getId(), brandId, seriesId, modelId, specId);
            return ResponseEntity.ok(buyRequest);
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
    public ResponseEntity<Page<BuyRequest>> getBuyProductsByMerchant(
            @RequestAttribute("merchant") Merchant currentMerchant,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body(null);
            }
            Page<BuyRequest> buyRequests = merchantService.getMerchantBuyRequests(currentMerchant.getId(), page, size);
            return ResponseEntity.ok(buyRequests);
        } catch (IllegalArgumentException e) {
            logger.error("查询商户求购商品失败: merchantId={}, {}", currentMerchant.getId(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

}
