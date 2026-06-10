package com.huit.CN_Java.config;

import com.huit.CN_Java.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
@RequiredArgsConstructor
public class CartCountInterceptor implements HandlerInterceptor {

    private final CartService cartService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView mav) {
        if (mav != null) {
            mav.addObject("cartCount", cartService.getCartCount(request.getSession()));
        }
    }
}