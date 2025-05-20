package pawa_be.infrastructure.jwt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Value("${cors.pawa_frontend_url}")
    String PAWAFrontendURL;

    @Override
    public void addCorsMappings(CorsRegistry cors) {
        cors.addMapping("/**")
                .allowedOrigins(PAWAFrontendURL)
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}