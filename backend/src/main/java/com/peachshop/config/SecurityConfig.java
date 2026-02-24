package com.peachshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests((requests) -> requests
                .anyRequest().permitAll() // 允许所有请求匿名访问
            )
            .formLogin(form -> form.disable()) // 禁用表单登录
            .logout(logout -> logout.disable()) // 禁用登出
            .cors(cors -> cors.disable()); // 禁用Security的CORS配置，使用application.properties中的配置

        return http.build();
    }

}
