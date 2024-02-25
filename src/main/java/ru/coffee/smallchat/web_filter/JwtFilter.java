package ru.coffee.smallchat.web_filter;


import io.micrometer.prometheus.PrometheusMeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import ru.coffee.smallchat.service.JwtService;

import java.io.IOException;


@Slf4j
public class JwtFilter extends GenericFilterBean {
    private final JwtService jwtService;
    private final PrometheusMeterRegistry meterRegistry;
    private final String requestWebsocketURL;

    public JwtFilter(JwtService jwtService,
                     PrometheusMeterRegistry meterRegistry,
                     Environment environment) {
        this.jwtService = jwtService;
        this.meterRegistry = meterRegistry;
        this.requestWebsocketURL = environment.getProperty("websocket.connect.url");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String jwtToken = jwtService.getFromHeader((HttpServletRequest) servletRequest);
        Authentication authentication = jwtParsing(jwtToken);
        if (authentication != null) {
            authentication.setAuthenticated(true);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public Authentication getAuthentication(String jwtToken) {
        return jwtParsing(jwtToken);
    }

    private Authentication jwtParsing(String jwtToken) {
        if (jwtToken != null && jwtToken.startsWith(JwtService.TOKEN_PREFIX)) {
            jwtToken = jwtToken.replace(JwtService.TOKEN_PREFIX, "");

            if (jwtService.validation(jwtToken)) {
                try {
                    Authentication authentication = jwtService.getAuthentication(jwtToken);
                    authentication.setAuthenticated(true);
                    return authentication;
                } catch (Exception e) {
                    log.error("JwtFilter.jwtParsing - " + e.getMessage());
                    meterRegistry.counter("error_in_service",
                            "method", "JwtFilter.doFilterInternal").increment();
                }
            }
        }
        return null;
    }
}

