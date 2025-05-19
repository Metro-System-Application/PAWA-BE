package pawa_be.infrastructure.jwt.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import pawa_be.infrastructure.jwt.filter.AuthRequestFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Autowired
    private AuthRequestFilter authRequestFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder defaultEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(httpToHttpsRedirectConnector());
        return tomcat;
    }

    private Connector httpToHttpsRedirectConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(8080);
        connector.setSecure(false);
        connector.setRedirectPort(8443);
        return connector;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
//                .requiresChannel(channel -> channel.anyRequest().requiresSecure())
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/auth/register", "/auth/login", "/auth/logout", "/auth/validate-existing-email",
                                "/auth/google-signup-url", "/auth/google", "/auth/fill-google-profile")
                        .permitAll()
                        .requestMatchers("/auth/update-my-info").authenticated()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/profile/**").authenticated()
                        .requestMatchers("/ticket/best-ticket").hasRole(UserRoleConfig.TICKET_AGENT.getRoleName())
                        .requestMatchers("/ticket/ticket-types").permitAll()
                        .requestMatchers("/metro-lines/**").permitAll()
                        .requestMatchers("/stations/**").permitAll()
                        .requestMatchers("/schedule/**").permitAll()
                        .requestMatchers("/suspensions/**").permitAll()
                        .requestMatchers("/payment/success", "/payment/direct-ticket/guest").permitAll()
                        .requestMatchers("/payment/purchase-ticket").hasRole(UserRoleConfig.TICKET_AGENT.getRoleName())
                        .requestMatchers("/invoice/by-email").hasRole(UserRoleConfig.OPERATOR.getRoleName())
                        .requestMatchers("/cart/**").authenticated()
                        .anyRequest().authenticated());

        http.addFilterBefore(authRequestFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}