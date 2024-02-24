package ru.coffee.smallchat.web_filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import ru.coffee.smallchat.service.JwtService;

import java.security.Principal;
import java.util.Map;

@Component
public class SessionHandshakeHandler extends DefaultHandshakeHandler {

    private final JwtFilter jwtFilter;

    public SessionHandshakeHandler(@Autowired JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String jwtToken = request.getHeaders().getFirst(JwtService.HEADER_NAME);
        Authentication authentication = jwtFilter.getAuthentication(jwtToken);

        return () -> authentication.getName();
    }
}
