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
     * 商户信息修改，包括商户名称、联系人姓名、联系人手机号、地址等信息
     */
    @PutMapping("/update")
    public ResponseEntity<Merchant> updateMerchant(@RequestAttribute("merchant") Merchant currentMerchant, @RequestBody Merchant merchant) {
        try {
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body((Merchant) null);
            }
            Merchant updated = merchantService.updateMerchant(currentMerchant.getId(), merchant);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            if ("商户不存在".equals(e.getMessage())) {
                return ResponseEntity.status(404).body((Merchant) null);
            }
            logger.error("更新商户失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body((Merchant) null);
        }
    }

    /**
     * 查询商户已经上架了的商品
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

    /**
     * 商家上架新机、二手机
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
     * 商家发布求购信息
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
}
