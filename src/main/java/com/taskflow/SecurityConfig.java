package com.taskflow;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http

            // CSRF disable
            .csrf(csrf -> csrf.disable())

            // API access allow
            .authorizeHttpRequests(auth -> auth

                .requestMatchers("/api/**").permitAll()

                .requestMatchers("/login").permitAll()

                .anyRequest().authenticated()
            )

            // Login form
            .formLogin(Customizer.withDefaults())

            // Logout
            .logout(logout -> logout.permitAll());

        return http.build();
    }
}