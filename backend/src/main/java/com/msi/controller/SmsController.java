package com.msi.controller;

import com.msi.constants.ErrorCode;
import com.msi.domain.Merchant;
import com.msi.service.MerchantService;
import com.msi.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sms")
public class SmsController {
    private static final Logger logger = LoggerFactory.getLogger(SmsController.class);
    private final SmsService smsService;

    public SmsController(SmsService smsService, MerchantService merchantService) {
        this.smsService = smsService;
    }

    /**
     * 增加一个发送短信验证码的功能，给指定手机号发送短信验证码，随机生成6位数验证码
     * 6位数验证码保存到redis中，key是微信号Id+手机号，value是手机验证码，有效时间5分钟
     * 一个微信号一天最多发送5次，每次发送的验证码为6位数字，有效期为5分钟
     */ 
    @PostMapping("/send-code")
    public ResponseEntity<Map<String, String>> sendCode(@RequestAttribute("merchant") Merchant merchant,
                                                        @RequestBody SendCodeRequest request) {
        try {
            String wechatId = merchant.getWechatId();
            String phone = request.getPhone();
            smsService.sendCode(wechatId, phone, request.getCaptchaVerification());
            Map<String, String> ok = new java.util.HashMap<>();
            ok.put("status", "success");
            return ResponseEntity.ok(ok);
        } catch (IllegalArgumentException e) {
            logger.error("发送验证码失败: {}", e.getMessage(), e);
            String msg = e.getMessage();
            Map<String, String> err = new java.util.HashMap<>();
            if (msg != null && msg.startsWith("CAPTCHA_")) {
                err.put("errorCode", ErrorCode.CAPTCHA_ERROR);
                err.put("message", "图形验证码错误");
            } else if ("SMS_LIMIT_EXCEEDED".equals(msg)) {
                err.put("errorCode", ErrorCode.SMS_LIMIT_EXCEEDED);
                err.put("message", "今日发送次数已达上限");
            } else {
                err.put("errorCode", "SMS_FAILED");
                err.put("message", msg);
            }
            return ResponseEntity.badRequest().body(err);
        }
    }

    public static class SendCodeRequest {
        private String wechatId;
        private String phone;
        private String captchaVerification;
        public String getWechatId() { return wechatId; }
        public void setWechatId(String wechatId) { this.wechatId = wechatId; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getCaptchaVerification() { return captchaVerification; }
        public void setCaptchaVerification(String captchaVerification) { this.captchaVerification = captchaVerification; }
    }
}
