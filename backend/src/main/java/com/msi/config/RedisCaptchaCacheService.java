package com.msi.config;

import com.anji.captcha.service.CaptchaCacheService;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisCaptchaCacheService implements CaptchaCacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisCaptchaCacheService.class);

    private StringRedisTemplate getStringRedisTemplate() {
        return SpringContextHolder.getBean(StringRedisTemplate.class);
    }

    @Override
    public void set(String key, String value, long expiresInSeconds) {
        logger.info("Captcha Redis SET: key={}, value={}, expire={}", key, value, expiresInSeconds);
        try {
            getStringRedisTemplate().opsForValue().set(key, value, expiresInSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Captcha Redis SET failed", e);
        }
    }

    @Override
    public boolean exists(String key) {
        boolean exists = Boolean.TRUE.equals(getStringRedisTemplate().hasKey(key));
        logger.info("Captcha Redis EXISTS: key={}, result={}", key, exists);
        return exists;
    }

    @Override
    public void delete(String key) {
        logger.info("Captcha Redis DELETE: key={}", key);
        getStringRedisTemplate().delete(key);
    }

    @Override
    public String get(String key) {
        String value = getStringRedisTemplate().opsForValue().get(key);
        logger.info("Captcha Redis GET: key={}, result={}", key, value);
        return value;
    }

    @Override
    public String type() {
        return "redis";
    }
}
