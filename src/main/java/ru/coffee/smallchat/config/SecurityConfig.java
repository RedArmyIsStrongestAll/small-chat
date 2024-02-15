package ru.coffee.smallchat.config;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.SessionManagementFilter;
import ru.coffee.smallchat.service.JwtService;
import ru.coffee.smallchat.web_filter.JwtFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain config(HttpSecurity http,
                                      JwtService jwtFilter,
                                      PrometheusMeterRegistry meterRegistry) throws Exception {
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
                .addFilterAfter(new JwtFilter(jwtFilter, meterRegistry), SessionManagementFilter.class)
        ;
        return http.build();
    }
}
