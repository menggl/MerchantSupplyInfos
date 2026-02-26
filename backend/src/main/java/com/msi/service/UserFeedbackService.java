package com.msi.service;

import com.msi.domain.Merchant;
import com.msi.domain.UserFeedback;
import com.msi.repository.MerchantRepository;
import com.msi.repository.UserFeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserFeedbackService {

    private static final Logger logger = LoggerFactory.getLogger(UserFeedbackService.class);

    private static final String[] FORBIDDEN_SQL_PATTERNS = {
            "select ", "insert ", "update ", "delete ", "drop ",
            "truncate ", "alter ", "create ", "exec ", "union ",
            "--", "/*", "*/", ";"
    };

    private final UserFeedbackRepository userFeedbackRepository;
    private final MerchantRepository merchantRepository;
    private final RestTemplate restTemplate;

    @Value("${wecom.webhook.url:}")
    private String wecomWebhookUrl;

    public UserFeedbackService(UserFeedbackRepository userFeedbackRepository, MerchantRepository merchantRepository) {
        this.userFeedbackRepository = userFeedbackRepository;
        this.merchantRepository = merchantRepository;
        this.restTemplate = new RestTemplate();
    }

    public void submitFeedback(Long merchantId, String content) {
        if (merchantId == null) {
            throw new IllegalArgumentException("商户未登录");
        }
        if (content == null) {
            throw new IllegalArgumentException("反馈内容不能为空");
        }
        String trimmed = content.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("反馈内容不能为空");
        }
        if (trimmed.length() > 300) {
            throw new IllegalArgumentException("反馈内容不能超过300个字");
        }
        String lower = trimmed.toLowerCase();
        for (String pattern : FORBIDDEN_SQL_PATTERNS) {
            if (lower.contains(pattern)) {
                logger.warn("USER_FEEDBACK_SQL_INJECTION_BLOCKED: merchantId={}, pattern={}, contentSnippet={}",
                        merchantId, pattern, trimmed.substring(0, Math.min(trimmed.length(), 100)));
                return;
            }
        }

        UserFeedback feedback = new UserFeedback();
        feedback.setMerchantId(merchantId);
        feedback.setFeedbackContent(trimmed);
        userFeedbackRepository.save(feedback);

        sendWecomNotification(merchantId, trimmed);
    }

    private void sendWecomNotification(Long merchantId, String content) {
        if (wecomWebhookUrl == null || wecomWebhookUrl.isEmpty()) {
            return;
        }
        try {
            Merchant merchant = merchantRepository.findById(merchantId).orElse(null);
            String merchantName = merchant != null && merchant.getMerchantName() != null
                    ? merchant.getMerchantName()
                    : "未知商户";
            String merchantPhone = merchant != null && merchant.getMerchantPhone() != null
                    ? merchant.getMerchantPhone()
                    : "无";

            Map<String, Object> text = new HashMap<>();
            String messageContent = String.format("【用户反馈】\n商户：%s (ID: %d)\n电话：%s\n内容：%s",
                    merchantName, merchantId, merchantPhone, content);
            text.put("content", messageContent);

            Map<String, Object> body = new HashMap<>();
            body.put("msgtype", "text");
            body.put("text", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(wecomWebhookUrl, request, String.class);
            logger.info("Sent WeCom notification for feedback from merchant {}", merchantId);
        } catch (Exception e) {
            logger.error("Failed to send WeCom notification", e);
        }
    }
}
