package com.msi.controller;

import com.msi.service.MerchantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    public static class WxLoginRequest {
        private String code;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
}
