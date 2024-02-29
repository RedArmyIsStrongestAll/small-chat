package ru.coffee.smallchat.web_filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.NativeMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import ru.coffee.smallchat.entity.WebsocketHeadersQueueEntity;

import java.util.Map;

@Component
public class WebsocketSubscribeInterceptor implements ChannelInterceptor {
    private final WebsocketHeadersQueueEntity headersForQueue;


    public WebsocketSubscribeInterceptor(@Autowired WebsocketHeadersQueueEntity headersForQueue) {
        this.headersForQueue = headersForQueue;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            if (destination != null && destination.contains("/queue")) {
                Map<Object, Object> nativeHeaders = (Map<Object, Object>) message.getHeaders().get(NativeMessageHeaderAccessor.NATIVE_HEADERS);
                nativeHeaders.putAll(headersForQueue.getHeadersConsumer());
            }
        }
        return message;
    }
}
