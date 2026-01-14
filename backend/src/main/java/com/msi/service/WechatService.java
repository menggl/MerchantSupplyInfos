package com.msi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Service
public class WechatService {
    private static final Logger logger = LoggerFactory.getLogger(WechatService.class);

    @Value("${wechat.miniapp.appid}")
    private String appid;

    @Value("${wechat.miniapp.secret}")
    private String secret;

    private final RestTemplate restTemplate;

    public WechatService() {
        this.restTemplate = new RestTemplate();
    }

    public WechatSession getSession(String code) {
        if (appid == null || appid.isEmpty() || secret == null || secret.isEmpty()) {
            // For development/test without real credentials
            logger.warn("WeChat AppID/Secret not configured. Returning mock session.");
            if ("mock_code".equals(code)) {
                 WechatSession mock = new WechatSession();
                 mock.setOpenid("mock_openid_123456");
                 mock.setSessionKey("mock_session_key");
                 return mock;
            }
        }
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appid +
                "&secret=" + secret +
                "&js_code=" + code +
                "&grant_type=authorization_code";
        try {
            // 调用微信接口获取 openid  session_key
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null) {
                throw new RuntimeException("微信接口返回为空");
            }

            if (body.containsKey("errcode") && (Integer) body.get("errcode") != 0) {
                throw new RuntimeException("微信接口错误: " + body.get("errmsg"));
            }

            WechatSession session = new WechatSession();
            // 检查是否包含 openid 和 session_key
            if (!body.containsKey("openid") || !body.containsKey("session_key")) {
                throw new RuntimeException("微信接口返回缺少 openid 或 session_key");
            }
            session.setOpenid((String) body.get("openid"));
            session.setSessionKey((String) body.get("session_key"));
            session.setUnionid((String) body.get("unionid")); // May be null
            return session;
        } catch (Exception e) {
            logger.error("Failed to get WeChat session", e);
            throw new RuntimeException("获取微信Session失败: " + e.getMessage());
        }
    }

    public static class WechatSession {
        private String openid;
        private String sessionKey;
        private String unionid;

        public String getOpenid() { return openid; }
        public void setOpenid(String openid) { this.openid = openid; }
        public String getSessionKey() { return sessionKey; }
        public void setSessionKey(String sessionKey) { this.sessionKey = sessionKey; }
        public String getUnionid() { return unionid; }
        public void setUnionid(String unionid) { this.unionid = unionid; }
    }
}
