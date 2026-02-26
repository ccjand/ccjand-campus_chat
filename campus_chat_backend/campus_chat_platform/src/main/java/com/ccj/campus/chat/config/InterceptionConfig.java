package com.ccj.campus.chat.config;

import com.ccj.campus.chat.interceptor.CollectionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author ccj
 * @Date 2024-04-08 15:46
 * @Description
 */
@Configuration
@RequiredArgsConstructor
public class InterceptionConfig implements WebMvcConfigurer {

    private final CollectionInterceptor collectionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //收集用户信息
        registry.addInterceptor(collectionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/users/login");
    }
}
