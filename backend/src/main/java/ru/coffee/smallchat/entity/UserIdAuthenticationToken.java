package ru.coffee.smallchat.entity;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

public class UserIdAuthenticationToken extends AbstractAuthenticationToken {
    private static final long serialVersionUID = 620L;
    private final String userId;


    public UserIdAuthenticationToken(String userId) {
        super(Collections.singletonList(new SimpleGrantedAuthority("no")));
        this.userId = userId;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }

    @Override
    public Object getCredentials() {
        return null;
    }
}
