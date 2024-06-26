package ru.coffee.smallchat.config;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.SessionManagementFilter;
import ru.coffee.smallchat.repository.MainRepository;
import ru.coffee.smallchat.service.JwtService;
import ru.coffee.smallchat.web_filter.BlockingUserFilter;
import ru.coffee.smallchat.web_filter.JwtFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public JwtFilter jwtFilter(JwtService jwtFilter,
                               PrometheusMeterRegistry meterRegistry) {
        return new JwtFilter(jwtFilter, meterRegistry);
    }

    @Bean
    public BlockingUserFilter blockingUserFilter(MainRepository mainRepository) {
        return new BlockingUserFilter(mainRepository);
    }

    @Bean
    public SecurityFilterChain config(HttpSecurity http, JwtFilter jwtFilter,
                                      BlockingUserFilter blockingUserFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize) ->
                        authorize
                                .requestMatchers("/swag").permitAll()
                                .requestMatchers("/api/**").permitAll()
                                .requestMatchers("/swagger-ui/**", "/swagger-ui/**.css",
                                        "/swagger-ui/**.js", "/swagger-ui/**.html").permitAll()
                                .requestMatchers("/login/**").permitAll()
                                .requestMatchers("/websocket/endpoint").permitAll()
                                .requestMatchers("/actuator/prometheus").permitAll()
                                .anyRequest().authenticated())
                .addFilterAfter(jwtFilter, SessionManagementFilter.class)
                .addFilterAfter(blockingUserFilter, JwtFilter.class)
                .sessionManagement((session) ->
                        session.maximumSessions(1))
        ;
        return http.build();
    }
}
