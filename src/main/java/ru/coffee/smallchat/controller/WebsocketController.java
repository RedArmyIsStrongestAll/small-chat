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
import ru.coffee.smallchat.service.MainService;

import java.security.Principal;

/***
 * регистрация /websocket/endpoint
 */
@Controller
public class WebsocketController {
    private final MainService mainService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebsocketController(@Autowired MainService mainService,
                               @Autowired SimpMessagingTemplate messagingTemplate) {
        this.mainService = mainService;
        this.messagingTemplate = messagingTemplate;
    }

    /***
     * отправка /chat/send/public
     * получение /topic/public
     * подписка на ошибки для отправителя /topic/public.error.{user_id}
     */
    @MessageMapping("public")
    public void sendPublicChat(@Payload String message,
                               @Header("simpUser") Principal principalProducerUuid) {
        String userId = principalProducerUuid.getName();
        PublicMessageResponseDTO returnMessage = mainService.savePublicMessage(message, userId);
        if (returnMessage == null) {
            messagingTemplate.convertAndSend(
                    "/topic/public.error." + userId,
                    "Сообщение \"" + message + "\" не отправлено. Внутреняя ошибка сервера.");
        }
        messagingTemplate.convertAndSend("/topic/public", returnMessage);
    }


    /***
     * отправка /chat/send/personal
     * получение /topic/personal.{user_id}
     * подписка на ошибки для отправителя /topic/private.error.{user_id}
     */
    @MessageMapping("personal")
    public void sendPersonalChat(@Payload PersonalMessageRequestDTO message,
                                 @Header("simpUser") Principal principalProducerUuid) {
        String userId = principalProducerUuid.getName();
        PersonalMessageResponseDTO returnMessage = mainService.savePersonalMessage(message.getMessage(), message.getChatId(),
                message.getConsumerUserId(), userId);
        if (returnMessage == null) {
            messagingTemplate.convertAndSend(
                    "/topic/personal.error." + userId,
                    "Сообщение \"" + message.getMessage() + "\" не отправлено. Внутреняя ошибка сервера.");
        } else {
            messagingTemplate.convertAndSend("/topic/personal." + message.getConsumerUserId(),
                    returnMessage);
        }
    }
}

