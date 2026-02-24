package com.msi.service;

import com.msi.domain.UserFeedback;
import com.msi.repository.UserFeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserFeedbackService {

    private static final Logger logger = LoggerFactory.getLogger(UserFeedbackService.class);

    private static final String[] FORBIDDEN_SQL_PATTERNS = {
            "select ", "insert ", "update ", "delete ", "drop ",
            "truncate ", "alter ", "create ", "exec ", "union ",
            "--", "/*", "*/", ";"
    };

    private final UserFeedbackRepository userFeedbackRepository;

    public UserFeedbackService(UserFeedbackRepository userFeedbackRepository) {
        this.userFeedbackRepository = userFeedbackRepository;
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
    }
}
