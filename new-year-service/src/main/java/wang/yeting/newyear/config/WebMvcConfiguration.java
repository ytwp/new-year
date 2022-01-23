package wang.yeting.newyear.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import wang.yeting.newyear.Interceptor.AuthenticationInterceptor;
import wang.yeting.newyear.resolver.CurrentUserResolver;

import java.util.List;

/**
 * @author weipeng
 */
@SpringBootConfiguration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.getAuthenticationInterceptor()).addPathPatterns(new String[]{"/**"}).excludePathPatterns("/login", "/info","/registry");
    }

    @Bean
    public AuthenticationInterceptor getAuthenticationInterceptor() {
        return new AuthenticationInterceptor();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentUserResolver());
    }
}

