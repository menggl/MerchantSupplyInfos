package com.msi.config;

import com.anji.captcha.service.CaptchaCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

public class RedisCaptchaCacheServiceImpl implements CaptchaCacheService {

    private static final Logger logger = LoggerFactory.getLogger(RedisCaptchaCacheServiceImpl.class);

    private StringRedisTemplate getRedisTemplate() {
        return SpringContextHolder.getBean(StringRedisTemplate.class);
    }

    @Override
    public void set(String key, String value, long expiresInSeconds) {
        logger.info("Captcha Cache SET: key={}, value={}, expire={}", key, value, expiresInSeconds);
        getRedisTemplate().opsForValue().set(key, value, expiresInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean exists(String key) {
        return Boolean.TRUE.equals(getRedisTemplate().hasKey(key));
    }

    @Override
    public void delete(String key) {
        getRedisTemplate().delete(key);
    }

    @Override
    public String get(String key) {
        String value = getRedisTemplate().opsForValue().get(key);
        logger.info("Captcha Cache GET: key={}, value={}", key, value);
        return value;
    }

    @Override
    public String type() {
        return "redis";
    }
}
