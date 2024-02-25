package ru.coffee.smallchat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import ru.coffee.smallchat.dto.PersonalMessageRequestDTO;
import ru.coffee.smallchat.dto.PersonalMessageResponseDTO;
import ru.coffee.smallchat.dto.PublicMessageResponseDTO;
import ru.coffee.smallchat.entity.WebsocketHeadersQueueEntity;
import ru.coffee.smallchat.service.MainService;

import java.security.Principal;

/***
 * регистрация /websocket/endpoint
 */
@Controller
public class WebsocketController {
    private final MainService mainService;
    private final SimpMessagingTemplate messagingTemplate;
    private final WebsocketHeadersQueueEntity headersForQueue;

    public WebsocketController(@Autowired MainService mainService,
                               @Autowired SimpMessagingTemplate messagingTemplate,
                               @Autowired WebsocketHeadersQueueEntity headersForQueue) {
        this.mainService = mainService;
        this.messagingTemplate = messagingTemplate;
        this.headersForQueue = headersForQueue;
    }

    /***
     * отправка /chat/send/public
     * получение /topic/public
     * подписка на ошибки для отправителя /user/queue/public.error
     */
    @MessageMapping("public")
    public void sendPublicChat(@Payload String message,
                               @Header("simpUser") Principal principalProducerUuid) {
        String userId = principalProducerUuid.getName();
        PublicMessageResponseDTO returnMessage = mainService.savePublicMessage(message, userId);
        if (returnMessage == null) {
            messagingTemplate.convertAndSendToUser(userId, "/queue/public.error",
                    "Сообщение \"" + message + "\" не отправлено. Внутреняя ошибка сервера.",
                    headersForQueue.getHeadersConsumer());
        }
        messagingTemplate.convertAndSend("/topic/public", returnMessage);
    }


    /***
     * отправка /chat/send/personal
     * получение /user/queue/personal
     * подписка на ошибки для отправителя /queue/private.error
     */
    @MessageMapping("personal")
    public void sendPersonalChat(@Payload PersonalMessageRequestDTO message,
                                 @Header("simpUser") Principal principalProducerUuid) {
        String userId = principalProducerUuid.getName();
        PersonalMessageResponseDTO returnMessage = mainService.savePersonalMessage(message.getMessage(), message.getChatId(),
                message.getConsumerUserId(), userId);
        if (returnMessage == null) {
            messagingTemplate.convertAndSendToUser(userId, "/queue/personal.error",
                    "Сообщение \"" + message.getMessage() + "\" не отправлено. Внутреняя ошибка сервера.",
                    headersForQueue.getHeadersPublisher());
        } else {
            messagingTemplate.convertAndSendToUser(returnMessage.getConsumerUserId(), "/queue/personal",
                    returnMessage, headersForQueue.getHeadersPublisher());
        }
    }
}

