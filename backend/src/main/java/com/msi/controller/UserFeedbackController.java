package com.msi.controller;

import com.msi.domain.Merchant;
import com.msi.request.UserFeedbackRequest;
import com.msi.service.UserFeedbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserFeedbackController {

    private static final Logger logger = LoggerFactory.getLogger(UserFeedbackController.class);

    private final UserFeedbackService userFeedbackService;

    public UserFeedbackController(UserFeedbackService userFeedbackService) {
        this.userFeedbackService = userFeedbackService;
    }

    @PostMapping("/user-feedback")
    public ResponseEntity<Map<String, Object>> submitFeedback(
            @RequestAttribute("merchant") Merchant currentMerchant,
            @RequestBody UserFeedbackRequest request) {
        try {
            Long merchantId = currentMerchant != null ? currentMerchant.getId() : null;
            int length = request != null && request.getContent() != null ? request.getContent().length() : 0;
            logger.info("submitFeedback request: merchantId={}, contentLength={}", merchantId, length);

            if (merchantId == null) {
                Map<String, Object> body = new HashMap<>();
                body.put("success", false);
                body.put("message", "未登录");
                return ResponseEntity.status(401).body(body);
            }

            userFeedbackService.submitFeedback(merchantId, request != null ? request.getContent() : null);

            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            logger.warn("提交用户反馈失败: {}", e.getMessage());
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(body);
        } catch (Exception e) {
            logger.error("提交用户反馈异常", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

