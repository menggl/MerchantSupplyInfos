package com.msi.config;

import com.anji.captcha.service.CaptchaCacheService;
import com.anji.captcha.service.impl.DefaultCaptchaServiceImpl;

public class CustomCaptchaServiceImpl extends DefaultCaptchaServiceImpl {

    private final CaptchaCacheService customCacheService;

    public CustomCaptchaServiceImpl(CaptchaCacheService customCacheService) {
        this.customCacheService = customCacheService;
    }

    @Override
    protected CaptchaCacheService getCacheService(String type) {
        return customCacheService;
    }
}
