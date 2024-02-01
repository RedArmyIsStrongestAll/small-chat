package ru.coffee.smallchat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.session.Session;
import org.springframework.session.web.socket.config.annotation.AbstractSessionWebSocketMessageBrokerConfigurer;
import org.springframework.session.web.socket.server.SessionRepositoryMessageInterceptor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig extends AbstractSessionWebSocketMessageBrokerConfigurer<Session> {
    private final Environment env;

    public WebsocketConfig(@Autowired Environment env) {
        this.env = env;
    }

    @Override
    public void configureStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket/endpoint")
                .setAllowedOrigins(env.getProperty("front.url"))
                .setHandshakeHandler(new MyHandshakeHandler());


        registry.addEndpoint("/websocket/endpoint")
                .setAllowedOrigins(env.getProperty("front.url"))
                .setHandshakeHandler(new MyHandshakeHandler())
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setPreservePublishOrder(true)
                .setApplicationDestinationPrefixes("/chat/send")
                .enableStompBrokerRelay("/topic")
                .setRelayHost(env.getProperty("rabbitmq.stomp.host"))
                .setRelayPort(Integer.parseInt(env.getProperty("rabbitmq.stomp.port")))
                .setSystemLogin(env.getProperty("rabbitmq.stomp.username"))
                .setSystemPasscode(env.getProperty("rabbitmq.stomp.password"))
                .setUserDestinationBroadcast("/topic/unresolved-user");
    }

    public static class MyHandshakeHandler extends DefaultHandshakeHandler {
        @Override
        protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
            String sessionUuid = request.getHeaders().getFirst("SESSION");
            SessionRepositoryMessageInterceptor.setSessionId(attributes, sessionUuid);
            return () -> sessionUuid;
        }
    }

}