/** MVC 配置类，注册 JWT 拦截器和静态资源上传路径映射 */
package com.campus.foodplatform.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    @Value("${app.upload.path}")
    private String uploadPath;

    /** 将配置的路径解析为绝对路径 */
    private String resolveAbsolutePath() {
        File f = new File(uploadPath);
        return f.isAbsolute() ? f.getAbsolutePath() + File.separator
                : new File(System.getProperty("user.dir"), uploadPath).getAbsolutePath() + File.separator;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/shops/list",
                        "/api/shops/top/**",
                        "/api/shops/[0-9]*",
                        "/api/shops/[0-9]*/discounts",
                        "/api/notes/list",
                        "/api/notes/[0-9]*",
                        "/api/discounts/list",
                        "/uploads/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absPath = resolveAbsolutePath();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absPath);
    }
}
