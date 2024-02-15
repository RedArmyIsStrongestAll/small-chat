package ru.coffee.smallchat.web_filter;


import io.micrometer.prometheus.PrometheusMeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.coffee.smallchat.service.JwtService;

import java.io.IOException;


@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private static final String TOKEN_PREFIX = "Bearer ";
    private final JwtService jwtService;
    private final PrometheusMeterRegistry meterRegistry;

    public JwtFilter(JwtService jwtService,
                     PrometheusMeterRegistry meterRegistry) {
        this.jwtService = jwtService;
        this.meterRegistry = meterRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String jwtToken = jwtService.getFromHeader(request);
        if (jwtToken != null && jwtToken.startsWith(TOKEN_PREFIX)) {
            jwtToken = jwtToken.replace(TOKEN_PREFIX, "");

            if (jwtService.validation(jwtToken)) {
                try {
                    Authentication authentication = jwtService.getAuthentication(jwtToken);
                    authentication.setAuthenticated(true);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (Exception e) {
                    log.error("JwtFilter.doFilterInternal - " + e.getMessage());
                    meterRegistry.counter("error_in_service",
                            "method", "JwtFilter.doFilterInternal").increment();
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}

