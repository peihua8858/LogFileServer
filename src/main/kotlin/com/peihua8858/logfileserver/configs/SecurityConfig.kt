package com.peihua8858.logfileserver.configs

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/",
                        "/logcat/**",
                        "/upload/**",
                        "/uploadFile/**",
                        "/static/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/common/**",
                        "/favicon.ico",
                        "/websocket/**",
                        "/app/**",
                        "/file/**"
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .formLogin { form ->
                form.permitAll()
            }

        return http.build()
    }
}
