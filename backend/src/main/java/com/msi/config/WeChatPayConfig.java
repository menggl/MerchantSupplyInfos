package com.msi.config;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.RSAConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WeChatPayConfig {

    private static final Logger logger = LoggerFactory.getLogger(WeChatPayConfig.class);

    @Value("${wechat.pay.mchid:}")
    private String mchId;

    @Value("${wechat.pay.merchant-serial-number:}")
    private String merchantSerialNumber;

    @Value("${wechat.pay.private-key-path:}")
    private String privateKeyPath;

    @Value("${wechat.pay.api-v3-key:}")
    private String apiV3Key;

    @Value("${wechat.pay.public-key-id:}")
    private String wechatPayPublicKeyId;

    @Value("${wechat.pay.public-key-path:}")
    private String wechatPayPublicKeyPath;

    @Bean
    public Config wechatPaySdkConfig() {
        try {
            // 检查必要参数
            if (mchId == null || mchId.isEmpty() || mchId.contains("$") || 
                merchantSerialNumber == null || merchantSerialNumber.isEmpty() || merchantSerialNumber.contains("$") ||
                privateKeyPath == null || privateKeyPath.isEmpty() || privateKeyPath.contains("$")) {
                logger.warn("微信支付基础参数未配置(mchId/merchantSerialNumber/privateKeyPath)，跳过初始化");
                return null;
            }

            // 模式1：公钥模式 (推荐) - 暂时注释，SDK版本问题需排查
            /*
            if (wechatPayPublicKeyId != null && !wechatPayPublicKeyId.isEmpty() && !wechatPayPublicKeyId.contains("$") &&
                wechatPayPublicKeyPath != null && !wechatPayPublicKeyPath.isEmpty() && !wechatPayPublicKeyPath.contains("$")) {
                
                logger.info("使用微信支付公钥模式初始化 SDK");
                return new RSAConfig.Builder()
                        .merchantId(mchId)
                        .privateKeyFromPath(privateKeyPath)
                        .merchantSerialNumber(merchantSerialNumber)
                        // .wechatPayPublicKeyFromPath(wechatPayPublicKeyPath) // 暂不支持公钥模式配置，回退到证书模式
                        // .wechatPayPublicKeyId(wechatPayPublicKeyId)
                        .build();
            }
            */

            // 模式2：平台证书模式 (需要 apiV3Key 下载证书)
            if (apiV3Key != null && !apiV3Key.isEmpty() && !apiV3Key.contains("$")) {
                logger.info("使用微信支付平台证书模式初始化 SDK");
                return new RSAAutoCertificateConfig.Builder()
                        .merchantId(mchId)
                        .privateKeyFromPath(privateKeyPath)
                        .merchantSerialNumber(merchantSerialNumber)
                        .apiV3Key(apiV3Key)
                        .build();
            }

            logger.warn("未配置公钥模式所需公钥信息，也未配置证书模式所需 APIv3Key，跳过初始化");
            return null;

        } catch (Exception e) {
            logger.error("初始化微信支付配置失败: {}", e.getMessage(), e);
            return null;
        }
    }
}
