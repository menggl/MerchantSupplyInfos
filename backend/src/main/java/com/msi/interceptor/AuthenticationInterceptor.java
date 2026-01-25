package com.msi.interceptor;

import com.msi.constants.ErrorCode;
import com.msi.controller.MerchantController;
import com.msi.controller.SmsController;
import com.msi.controller.SupplyController;
import com.msi.service.MerchantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    private final MerchantService merchantService;

    public AuthenticationInterceptor(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @SuppressWarnings("null")
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            logger.warn("Authentication failed: missing or invalid Authorization header, method={}, uri={}",
                    request.getMethod(), request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"errorCode\": \"" + ErrorCode.UNAUTHORIZED + "\", \"error\": \"Token缺失或无效\"}");
            return false;
        }

        String token = authorization.substring(7);
        com.msi.domain.Merchant merchant = merchantService.getMerchantByToken(token);
        if (merchant == null || merchant.getId() == null || merchant.getIsValid() == 0) {
            logger.warn("Authentication failed: invalid merchant, method={}, uri={}, merchantId={}, isValid={}",
                    request.getMethod(), request.getRequestURI(),
                    merchant != null ? merchant.getId() : null,
                    merchant != null ? merchant.getIsValid() : null);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"errorCode\": \"" + ErrorCode.INVALID_TOKEN + "\", \"error\": \"Token无效或已过期\"}");
            return false;
        }
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Class<?> beanType = handlerMethod.getBeanType();

            // 检查商户会员是否过期并且检查商户是否补充过信息（商户手机号不能为空），
            // 否则不允许调用MerchantController、SupplyController中的方法
            // MerchantController和SupplyController中的所有接口都必须是会员并且会员没到期才允许调用
            if (beanType == MerchantController.class || beanType == SupplyController.class) {
                // 1. 检查商户是否已补充信息（手机号）
                if (merchant.getMerchantPhone() == null || merchant.getMerchantPhone().isEmpty()) {
                    logger.warn("Authentication failed: merchant profile incomplete (missing phone), method={}, uri={}, merchantId={}",
                            request.getMethod(), request.getRequestURI(), merchant.getId());
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"errorCode\": \"" + ErrorCode.PROFILE_INCOMPLETE + "\", \"error\": \"请先完善商户信息（绑定手机号）\"}");
                    return false;
                }

                // 2. 检查会员是否过期
                if (merchantService.isMemberExpired(merchant.getId())) {
                    logger.warn("Authentication failed: merchant member expired, method={}, uri={}, merchantId={}",
                            request.getMethod(), request.getRequestURI(), merchant.getId());
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"errorCode\": \"" + ErrorCode.MEMBER_EXPIRED + "\", \"error\": \"商户会员已过期\"}");
                    return false;
                }
            }
        }

        request.setAttribute("merchant", merchant);
        return true;
    }
}
