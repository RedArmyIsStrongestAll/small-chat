package ru.coffee.smallchat.entity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class WebsocketHeadersQueueEntity {
    private final Map<String, Object> headersConsumer;
    private final Map<String, Object> headersPublisher;

    public WebsocketHeadersQueueEntity(@Value("${user.live.time.minutes}") Long userLiveTimeMinutes) {
        this.headersConsumer = new LinkedHashMap<>();
        headersConsumer.put("auto-delete", List.of("true"));
        headersConsumer.put("durable", List.of("false"));
        headersConsumer.put("exclusive", List.of("false"));
        headersConsumer.put("x-expires", List.of(String.valueOf(userLiveTimeMinutes * 60 * 1000)));

        this.headersPublisher = new HashMap<>();
        headersPublisher.put("auto-delete", "true");
        headersPublisher.put("durable", "false");
        headersPublisher.put("exclusive", "false");
        headersPublisher.put("x-expires", userLiveTimeMinutes * 60 * 1000);
    }

    public Map<String, Object> getHeadersConsumer() {
        return headersConsumer;
    }

    public Map<String, Object> getHeadersPublisher() {
        return headersPublisher;
    }
}
