package ru.coffee.smallchat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {
    private final Environment env;

    public WebsocketConfig(@Autowired Environment env) {
        this.env = env;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket/endpoint")
                .setAllowedOrigins(env.getProperty("front.url"));


        registry.addEndpoint("/websocket/endpoint")
                .setAllowedOrigins(env.getProperty("front.url"))
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
}