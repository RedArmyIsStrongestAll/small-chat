package ru.coffee.smallchat.web_filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import ru.coffee.smallchat.repository.MainRepository;

import java.io.IOException;

public class BlockingUserFilter extends GenericFilterBean {
    private final MainRepository repository;

    public BlockingUserFilter(MainRepository repository) {
        this.repository = repository;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null &&
                !authentication.getClass().equals(AnonymousAuthenticationToken.class)) {
            if (repository.checkBlocking(authentication.getPrincipal().toString())) {
                authentication.setAuthenticated(false);
            }
            ;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
