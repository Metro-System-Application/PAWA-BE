package pawa_be.infrastructure.jwt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Value("${cors.opwa_backend_url}")
    String OPWABackendURL;

    @Value("${cors.pawa_frontend_url}")
    String PAWAFrontendURL;

    @Override
    public void addCorsMappings(CorsRegistry cors) {
        cors.addMapping("/**")
                .allowedOrigins(OPWABackendURL, PAWAFrontendURL)
                .allowedMethods("GET", "POST", "PUT", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true).maxAge(15 * 60); // Cookies 15 mins max
    }
}