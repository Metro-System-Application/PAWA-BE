package com.example.pawa_be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
//                .csrf(Customizer.withDefaults())
                .authorizeHttpRequests(
                request -> request.anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
        ;
        return http.build();
    }

//    @Bean
//    public PasswordEncoder passwordEncoder(){
//        return new BCryptPasswordEncoder();
//    }

    @Bean
    public UserDetailsService userDetailsService(){
        UserDetails test1 = User.withUsername("test1").password("{noop}password").roles("USER").build();
        UserDetails test2 = User.withUsername("test2").password("{noop}password").roles("USER").build();
        UserDetails test3 = User.withUsername("test3").password("{noop}password").roles("USER").build();
        UserDetails test4 = User.withUsername("test4").password("{noop}password").roles("USER").build();
        return new InMemoryUserDetailsManager(test1, test2, test3, test4);
    }
}
