package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.io.IOException;

/*
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-web</artifactId>
            <version>6.1.3</version>
        </dependency>
*/
@SpringBootApplication
public class ProducerApplication {

    public static void main(String[] args) throws IOException, InterruptedException {
        ConfigurableApplicationContext con = SpringApplication.run(ProducerApplication.class, args);

        WebSocketClient webSocketClient = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        String url = "ws://localhost:8080/websocket/endpoint";
        StompSessionHandler sessionHandler = new MyStompSessionHandler();
        WebSocketHttpHeaders wsheaders = new WebSocketHttpHeaders();
        wsheaders.add("Authorization",
                "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyOSIsImlhdCI6MTcwOTEwODU0MywiZXhwIjoxNzE1MTA4NTQzfQ.zqijp_xalgoPN0JnqhQdIgGElXaLlDFIrMmDxDs9lMY");

        stompClient.connect(url, wsheaders, sessionHandler);
    }


    private static class MyStompSessionHandler extends StompSessionHandlerAdapter {

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            super.afterConnected(session, connectedHeaders);

            session.send("/chat/send/public", "текст общий");
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            super.handleFrame(headers, payload);
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            super.handleException(session, command, headers, payload, exception);
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            super.handleTransportError(session, exception);
        }
    }
}
