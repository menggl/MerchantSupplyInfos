package com.msi.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msi.admin.domain.AdminUser;
import com.msi.admin.repository.AdminUserRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AdminAuthService {
    private static final int MAX_DAILY_FAILURES = 6;
    private static final String LOGIN_FAIL_PREFIX = "admin:login:fail:";
    private static final String TOKEN_PREFIX = "admin:token:";

    private final AdminUserRepository adminUserRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public AdminAuthService(AdminUserRepository adminUserRepository,
                            StringRedisTemplate redisTemplate,
                            ObjectMapper objectMapper) {
        this.adminUserRepository = adminUserRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Map<String, Object> login(String username, String password, String ip) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "用户名和密码不能为空");
        }
        
        // 检查今日错误次数
        String dateStr = LocalDate.now().toString();
        String failKey = LOGIN_FAIL_PREFIX + username + ":" + dateStr;
        
        String failCountStr = redisTemplate.opsForValue().get(failKey);
        int failCount = 0;
        if (StringUtils.hasText(failCountStr)) {
            failCount = Integer.parseInt(failCountStr);
        }

        if (failCount >= MAX_DAILY_FAILURES) {
            throw new AuthException(HttpStatus.TOO_MANY_REQUESTS, "今日错误次数过多，请明日再试");
        }

        AdminUser user = adminUserRepository.findByUsernameAndIsValid(username, 1).orElse(null);
        boolean ok = user != null && md5Hex(password).equalsIgnoreCase(user.getPassword());

        if (!ok) {
            Long newCount = redisTemplate.opsForValue().increment(failKey);
            // 设置过期时间为1天（或直到明天0点，这里简单设为24小时或更长，只要覆盖“今日”即可）
            // 为了精确控制“今日”，可以计算到明天0点的秒数
            long secondsUntilTomorrow = Duration.between(LocalDateTime.now(), LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN)).getSeconds();
            redisTemplate.expire(failKey, secondsUntilTomorrow, TimeUnit.SECONDS);
            
            if (newCount != null && newCount >= MAX_DAILY_FAILURES) {
                throw new AuthException(HttpStatus.TOO_MANY_REQUESTS, "今日错误次数过多，请明日再试");
            }
            throw new AuthException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        // 登录成功，清除错误次数
        redisTemplate.delete(failKey);

        String token = UUID.randomUUID().toString().replace("-", "");
        // 设置过期时间到今日结束
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        long secondsUntilEndOfDay = Duration.between(LocalDateTime.now(), endOfDay).getSeconds();
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("token", token);
        
        try {
            String userInfoJson = objectMapper.writeValueAsString(userInfo);
            String tokenKey = TOKEN_PREFIX + token;
            redisTemplate.opsForValue().set(tokenKey, userInfoJson, secondsUntilEndOfDay, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON serialization failed", e);
        }

        return Map.of(
                "token", token,
                "expireTime", endOfDay
        );
    }

    public void logout(String token) {
        if (!StringUtils.hasText(token)) {
            return;
        }
        redisTemplate.delete(TOKEN_PREFIX + token);
    }

    public boolean isTokenValid(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_PREFIX + token));
    }

    private static String md5Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static class AuthException extends RuntimeException {
        private final HttpStatus status;

        public AuthException(HttpStatus status, String message) {
            super(message);
            this.status = status;
        }

        public HttpStatus getStatus() {
            return status;
        }
    }
}
