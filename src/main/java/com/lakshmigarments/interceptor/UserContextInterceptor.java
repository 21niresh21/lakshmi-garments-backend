package com.lakshmigarments.interceptor;

import com.lakshmigarments.context.UserContext;
import com.lakshmigarments.context.UserInfo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        String userId = request.getHeader("X-USER-ID");
        String role = request.getHeader("X-USER-ROLE");

        if (userId != null && role != null) {
            UserContext.set(new UserInfo(userId, role));
        }

        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {

        UserContext.clear(); // ⚠️ VERY IMPORTANT
    }
}
