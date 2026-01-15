package com.msi.interceptor;

import com.msi.controller.MerchantController;
import com.msi.controller.SupplyController;
import com.msi.service.MerchantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

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
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String token = authorization.substring(7);
        com.msi.domain.Merchant merchant = merchantService.getMerchantByToken(token);
        if (merchant == null || merchant.getId() == null || merchant.getIsValid() == 0) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 检查商户会员是否过期
        // MerchantController和SupplyController中的所有接口都必须是会员并且会员没到期才允许调用
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Class<?> beanType = handlerMethod.getBeanType();
            if ((beanType == MerchantController.class || beanType == SupplyController.class) 
                    && merchantService.isMemberExpired(merchant.getId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"商户会员已过期\"}");
                return false;
            }
        }

        request.setAttribute("merchant", merchant);
        return true;
    }
}
