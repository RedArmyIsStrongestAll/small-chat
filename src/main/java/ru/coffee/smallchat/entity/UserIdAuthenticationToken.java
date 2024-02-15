package ru.coffee.smallchat.entity;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

public class UserIdAuthenticationToken extends AbstractAuthenticationToken {
    private static final long serialVersionUID = 620L;
    private final String id;


    public UserIdAuthenticationToken(String id) {
        super(Collections.singletonList(new SimpleGrantedAuthority("no")));
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
