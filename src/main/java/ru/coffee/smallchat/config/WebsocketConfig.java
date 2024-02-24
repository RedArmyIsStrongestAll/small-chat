package ru.coffee.smallchat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import ru.coffee.smallchat.web_filter.SessionHandshakeHandler;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {
    private final Environment env;
    private final SessionHandshakeHandler handshakeHandler;

    public WebsocketConfig(@Autowired Environment env,
                           @Autowired SessionHandshakeHandler handshakeHandler) {
        this.env = env;
        this.handshakeHandler = handshakeHandler;
    }


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(env.getProperty("websocket.connect.url"))
                .setAllowedOrigins(env.getProperty("cors.url"))
                .setHandshakeHandler(handshakeHandler);


        registry.addEndpoint(env.getProperty("websocket.connect.url"))
                .setAllowedOrigins(env.getProperty("cors.url"))
                .setHandshakeHandler(handshakeHandler)
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setPreservePublishOrder(true)
                .setApplicationDestinationPrefixes("/chat/send")
                .enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(env.getProperty("rabbitmq.stomp.host"))
                .setRelayPort(Integer.parseInt(env.getProperty("rabbitmq.stomp.port")))
                .setSystemLogin(env.getProperty("rabbitmq.stomp.username"))
                .setSystemPasscode(env.getProperty("rabbitmq.stomp.password"))
                .setUserDestinationBroadcast("/topic/unresolved-user");
    }
}