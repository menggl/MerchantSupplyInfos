package com.msi.controller;

import com.msi.service.MerchantService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final MerchantService merchantService;

    public LoginController(MerchantService merchantService) {
        this.merchantService = merchantService;
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


    @PostMapping("/wx-login")
    public ResponseEntity<Merchant> wxLogin(@RequestBody WxLoginRequest request) {
        try {
            logger.info("wxLogin request: {}", toJson(request));
            Merchant result = merchantService.loginByWechat(request.getCode());
            logger.info("wxLogin response: {}", toJson(result));
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.error("微信登录参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("微信登录失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/phone-login")
    public ResponseEntity<Merchant> phoneLogin(@RequestBody PhoneLoginRequest request) {
        try {
            logger.info("phoneLogin request: {}", toJson(request));
            Merchant result = merchantService.loginByPhone(request.getPhone(), request.getPassword());
            logger.info("phoneLogin response: {}", toJson(result));
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.error("手机号登录参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("手机号登录失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 商户信息修改，包括商户名称、联系人姓名、联系人手机号、地址等信息
     */
    @PutMapping("/merchants/update")
    public ResponseEntity<?> updateMerchant(@RequestAttribute("merchant") Merchant currentMerchant, @RequestBody Merchant merchant) {
        try {
            logger.info("updateMerchant request: {}", toJson(merchant));
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body("用户未登录");
            }
            Merchant updated = merchantService.updateMerchant(currentMerchant.getId(), merchant);
            logger.info("updateMerchant response: {}", toJson(updated));
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            if ("商户不存在".equals(e.getMessage())) {
                return ResponseEntity.status(404).body("商户不存在");
            }
            logger.error("更新商户失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    /**
     * 更新商户头像信息，在商户登录状态下
     */
    @PutMapping("/merchants/avatar")
    public ResponseEntity<?> updateMerchantAvatar(@RequestAttribute("merchant") Merchant currentMerchant,
                                                  @RequestBody AvatarUpdateRequest request) {
        try {
            logger.info("updateMerchantAvatar request: {}", toJson(request));
            if (currentMerchant == null || currentMerchant.getId() == null) {
                return ResponseEntity.status(401).body("用户未登录");
            }
            if (request == null || request.getAvatarPhotoUrl() == null || request.getAvatarPhotoUrl().isEmpty()) {
                return ResponseEntity.badRequest().body("头像URL不能为空");
            }
            if (request.getAvatarPhotoUrl().length() > 150) {
                return ResponseEntity.badRequest().body("头像URL长度不能超过150字符");
            }
            merchantService.updateMerchantAvatar(currentMerchant.getId(), request.getAvatarPhotoUrl());
            logger.info("updateMerchantAvatar response: {}", "ok");
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            if ("商户不存在".equals(e.getMessage())) {
                return ResponseEntity.status(404).body("商户不存在");
            }
            logger.error("更新商户头像失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public static class WxLoginRequest {
        private String code;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }

    public static class AvatarUpdateRequest {
        private String avatarPhotoUrl;

        public String getAvatarPhotoUrl() { return avatarPhotoUrl; }
        public void setAvatarPhotoUrl(String avatarPhotoUrl) { this.avatarPhotoUrl = avatarPhotoUrl; }
    }

    public static class PhoneLoginRequest {
        private String phone;
        private String password;

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
