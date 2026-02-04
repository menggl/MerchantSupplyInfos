package com.msi.config;

import com.anji.captcha.model.common.Const;
import com.anji.captcha.service.CaptchaService;
import com.anji.captcha.service.impl.CaptchaServiceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.Properties;

@Configuration
public class CaptchaConfig {

    @Bean
    @DependsOn("springContextHolder")
    public CaptchaService captchaService() {
        Properties config = new Properties();
        config.put(Const.CAPTCHA_CACHETYPE, "redis");
        config.put(Const.CAPTCHA_WATER_MARK, "美机汇");
        config.put(Const.CAPTCHA_SLIP_OFFSET, "5");
        config.put(Const.CAPTCHA_AES_STATUS, "true");
        config.put(Const.CAPTCHA_TYPE, "blockPuzzle");
        config.put(Const.CAPTCHA_FONT_TYPE, "WenQuanZhengHei.ttf");
        
        // 使用自定义实现，强制注入 RedisCacheService，避开 SPI 加载问题
        CustomCaptchaServiceImpl service = new CustomCaptchaServiceImpl(new RedisCaptchaCacheService());
        service.init(config);
        return service;
    }
}
