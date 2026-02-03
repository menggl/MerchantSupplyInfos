package com.msi.service;

import com.msi.domain.SmsLog;
import com.msi.repository.SmsLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class SmsService {
    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);
    private final StringRedisTemplate redisTemplate;
    private final SmsLogRepository smsLogRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${yunpian.sms.apikey:}")
    private String apikey;
    
    // 短信内容模板，例如：【美机汇】您的验证码是%s。如非本人操作，请忽略本短信
    @Value("${yunpian.sms.text-template:【美机汇】您的验证码是%s。如非本人操作，请忽略本短信}")
    private String textTemplate;

    public SmsService(StringRedisTemplate redisTemplate, SmsLogRepository smsLogRepository) {
        this.redisTemplate = redisTemplate;
        this.smsLogRepository = smsLogRepository;
    }

    public static class CaptchaResult {
        private String imageBase64;

        public CaptchaResult(String imageBase64) {
            this.imageBase64 = imageBase64;
        }

        public String getImageBase64() { return imageBase64; }
    }

    public CaptchaResult generateCaptcha(String wechatId, String phone) {
        if (wechatId == null || wechatId.isEmpty()) {
            throw new IllegalArgumentException("微信ID不能为空");
        }
        if (phone == null || phone.isEmpty()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("手机号格式错误");
        }
        // 使用 EasyCaptcha 生成验证码
        com.wf.captcha.SpecCaptcha captcha = new com.wf.captcha.SpecCaptcha(130, 48, 4);
        captcha.setCharType(com.wf.captcha.base.Captcha.TYPE_ONLY_NUMBER); // 纯数字更容易输入

        String key = wechatId + ":" + phone;
        String code = captcha.text().toLowerCase();

        // 存入 Redis，5分钟有效
        redisTemplate.opsForValue().set("captcha:" + key, code, 5, TimeUnit.MINUTES);

        return new CaptchaResult(captcha.toBase64());
    }

    public void sendCode(String wechatId, String phone, String captchaCode) {
        if (wechatId == null || wechatId.isEmpty()) {
            throw new IllegalArgumentException("微信ID不能为空");
        }
        if (phone == null || phone.isEmpty()) {
            throw new IllegalArgumentException("手机号不能为空");
        }

        // 校验图形验证码
        if (captchaCode == null || captchaCode.isEmpty()) {
            throw new IllegalArgumentException("CAPTCHA_REQUIRED");
        }
        String captchaKey = "captcha:" + wechatId + ":" + phone;
        String correctCode = redisTemplate.opsForValue().get(captchaKey);
        if (correctCode == null) {
            throw new IllegalArgumentException("CAPTCHA_EXPIRED");
        }
        if (!correctCode.equalsIgnoreCase(captchaCode)) {
            throw new IllegalArgumentException("CAPTCHA_INVALID");
        }
        // 验证通过后删除
        redisTemplate.delete(captchaKey);

        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("手机号格式错误");
        }
        
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        long cnt = smsLogRepository.countByWechatIdAndSendTimeBetween(wechatId, start, end);
        // todo 测试使用，先将发送次数上限改为100次
        if (cnt >= 100) {
            throw new IllegalArgumentException("今日发送次数已达上限");
        }
        int code = (int)(Math.random() * 900000) + 100000; // 6位随机码
        String codeKey = buildCodeKey(wechatId, phone);
        redisTemplate.opsForValue().set(codeKey, String.valueOf(code), 5, TimeUnit.MINUTES);

        // 打印日志，便于调试
        logger.info("发送短信验证码 {} 到手机号 {}，验证码 {} 有效时间 5 分钟", wechatId, phone, code);

        // 将短信发送保存到数据库
        SmsLog smsLog = new SmsLog();
        smsLog.setWechatId(wechatId);
        smsLog.setPhone(phone);
        smsLog.setCode(String.valueOf(code));
        smsLog.setSendTime(LocalDateTime.now());
        smsLogRepository.save(smsLog);

        // 集成云片网短信服务，配置存在时才发送
        if (apikey != null && !apikey.isEmpty()) {
            String url = "https://sms.yunpian.com/v2/sms/single_send.json";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Accept", "application/json;charset=utf-8");

            String text = String.format(textTemplate, code);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("apikey", apikey);
            params.add("mobile", phone);
            params.add("text", text);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            try {
                ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
                Map body = response.getBody();
                
                if (body == null) {
                    throw new IllegalArgumentException("短信发送失败: 无响应");
                }
                
                Integer resultCode = (Integer) body.get("code");
                if (resultCode != null && resultCode != 0) {
                     String msg = (String) body.get("msg");
                     throw new IllegalArgumentException("短信发送失败: " + msg);
                }
            } catch (Exception ex) {
                logger.error("云片短信发送异常", ex);
                throw new IllegalArgumentException("短信发送异常: " + ex.getMessage());
            }
        }
    }

    public String getCode(String wechatId, String phone) {
        if (wechatId == null || wechatId.isEmpty() || phone == null || phone.isEmpty()) {
            return null;
        }
        String codeKey = buildCodeKey(wechatId, phone);
        return redisTemplate.opsForValue().get(codeKey);
    }

    public void deleteCode(String wechatId, String phone) {
        if (wechatId == null || wechatId.isEmpty()) {
            throw new IllegalArgumentException("微信ID不能为空");
        }
        if (phone == null || phone.isEmpty()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        String codeKey = buildCodeKey(wechatId, phone);
        redisTemplate.delete(codeKey);
    }

    private String buildCodeKey(String wechatId, String phone) {
        return "sms:code:" + wechatId + ":" + phone;
    }
}
