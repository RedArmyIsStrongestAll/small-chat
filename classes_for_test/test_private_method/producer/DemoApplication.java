package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) throws IOException, InterruptedException {
        ConfigurableApplicationContext con = SpringApplication.run(DemoApplication.class, args);

        String filePath = "C:\\D\\desktop\\theory\\gradle\\задачи 9.png";
        String serverUrl = "http://localhost:8080/sign/in";
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        File file = new File(filePath);
        FileSystemResource fileSystemResource = new FileSystemResource(file);
        body.set("name", "ZxcProducerPrivate");
        body.add("photo", fileSystemResource);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Object> response = restTemplate.postForEntity(serverUrl, requestEntity, Object.class);
        String uuid = extractSessionValue(response.getHeaders().get("Set-Cookie").get(0));
        System.out.println(uuid);


        WebSocketClient webSocketClient = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        String url = "ws://localhost:8080/websocket/endpoint";
        StompSessionHandler sessionHandler = new MyStompSessionHandler(uuid);
        WebSocketHttpHeaders wsheaders = new WebSocketHttpHeaders();
        wsheaders.add("SESSION", uuid);

        stompClient.connect(url, wsheaders, sessionHandler);
    }


    private static class MyStompSessionHandler extends StompSessionHandlerAdapter {
        private String uuid;
        private String consumer = "8cae6145-9fa5-4729-9c51-8c65272c8daf";

        public MyStompSessionHandler(String uuid) {
            this.uuid = uuid;
        }

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            super.afterConnected(session, connectedHeaders);

            session.send("/chat/send/personal",
                    new PersonalMessageRequestDTO(uuid,
                            "оыбщий привет",
                            consumer, null));
        }

    }

    public static String extractSessionValue(String cookieHeader) {
        Pattern pattern = Pattern.compile("SESSION=([^;]+);");
        Matcher matcher = pattern.matcher(cookieHeader);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    static class PersonalMessageRequestDTO {

        private String producerUserUuid;

        public PersonalMessageRequestDTO(String producerUserUuid, String message, String consumerUserUuid, Long chatId) {
            this.producerUserUuid = producerUserUuid;
            this.message = message;
            this.consumerUserUuid = consumerUserUuid;
            this.chatId = chatId;
        }

        private String message;

        public String getProducerUserUuid() {
            return producerUserUuid;
        }

        public void setProducerUserUuid(String producerUserUuid) {
            this.producerUserUuid = producerUserUuid;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getConsumerUserUuid() {
            return consumerUserUuid;
        }

        public void setConsumerUserUuid(String consumerUserUuid) {
            this.consumerUserUuid = consumerUserUuid;
        }

        public Long getChatId() {
            return chatId;
        }

        public void setChatId(Long chatId) {
            this.chatId = chatId;
        }

        private String consumerUserUuid;
        private Long chatId;
    }


}
