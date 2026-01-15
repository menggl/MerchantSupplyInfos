package com.msi.controller;

import com.msi.service.MerchantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msi.domain.Merchant;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;

@RestController
@RequestMapping("/api")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final MerchantService merchantService;

    public LoginController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }


    @PostMapping("/wx-login")
    public ResponseEntity<MerchantService.WechatLoginInfo> wxLogin(@RequestBody WxLoginRequest request) {
        try {
            MerchantService.WechatLoginInfo result = merchantService.loginByWechat(request.getCode());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.error("微信登录参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("微信登录失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 商户信息修改，包括商户名称、联系人姓名、联系人手机号、地址等信息
     */
    @PutMapping("/merchants/update")
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

    public static class WxLoginRequest {
        private String code;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
}
