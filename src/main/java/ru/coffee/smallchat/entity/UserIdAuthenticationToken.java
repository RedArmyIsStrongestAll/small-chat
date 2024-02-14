package ru.coffee.smallchat.entity;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

public class UserIdAuthenticationToken extends AbstractAuthenticationToken {
    private static final long serialVersionUID = 620L;
    private final Long id;


    public UserIdAuthenticationToken(Long id) {
        super(Collections.singletonList(null));
        this.id = id;
    }

    @Override
    public Object getPrincipal() {
        return id;
    }

    @Override
    public Object getCredentials() {
        return null;
    }
}
