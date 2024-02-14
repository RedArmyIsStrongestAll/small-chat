package ru.coffee.smallchat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain config(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize) ->
                        authorize
                                //swagger
                                .requestMatchers("/swag").permitAll()
                                .requestMatchers("/api/**").permitAll()
                                .requestMatchers("/swagger-ui/**", "/swagger-ui/**.css",
                                        "/swagger-ui/**.js", "/swagger-ui/**.html").permitAll()
                                //login
                                .requestMatchers("/login/**").permitAll()
                                //authenticated
                                .anyRequest().authenticated())
        ;
        return http.build();
    }
}
