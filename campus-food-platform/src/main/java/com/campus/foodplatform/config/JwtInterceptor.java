/** JWT 拦截器，校验请求头中的 Token 并注入用户上下文 */
package com.campus.foodplatform.config;

import com.campus.foodplatform.common.BusinessException;
import com.campus.foodplatform.common.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (jwtUtil.validateToken(token)) {
                UserContext.setUserId(jwtUtil.getUserId(token));
                UserContext.setUserRole(jwtUtil.getRole(token));
                return true;
            }
        }
        throw new BusinessException(401, "未登录或登录已过期");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
