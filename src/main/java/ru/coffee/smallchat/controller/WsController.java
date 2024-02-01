package ru.coffee.smallchat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
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
public class WsController {
    private MainService mainService;
    private SimpMessagingTemplate messagingTemplate;

    public WsController(@Autowired MainService mainService,
                        @Autowired SimpMessagingTemplate messagingTemplate) {
        this.mainService = mainService;
        this.messagingTemplate = messagingTemplate;
    }

    /***
     * отправка /chat/send/public
     * получение /topic/public
     * ошибки /user/topic/public.error
     */
    @MessageMapping("public")
    @SendTo("/topic/public")
    public PublicMessageResponseDTO sendPublicChat(@Payload String message,
                                                   @Header("simpUser") Principal principalProducerUuid) {
        PublicMessageResponseDTO returnMessage = mainService.savePublicMessage(message, principalProducerUuid.getName());
        if (returnMessage == null) {
            messagingTemplate.convertAndSendToUser(principalProducerUuid.getName(),
                    "/topic/public.error",
                    "Сообщение \"" + message + "\" не отправлено. Внутреняя ошибка сервера.");
        }
        return returnMessage;
    }


    /***
     * отправка /chat/send/personal
     * получение /user/topic/private
     * ошибки /user/topic/private.error
     */
    @MessageMapping("personal")
    public void sendPrivateChat(@Payload PersonalMessageRequestDTO message,
                                @Header("simpUser") Principal principalProducerUuid) {
        PersonalMessageResponseDTO returnMessage = mainService.savePersonalMessage(message.getMessage(), message.getChatId(),
                message.getConsumerUserUuid(), principalProducerUuid.getName());
        if (returnMessage == null) {
            messagingTemplate.convertAndSendToUser(principalProducerUuid.getName(),
                    "/topic/private.error",
                    "Сообщение \"" + message.getMessage() + "\" не отправлено. Внутреняя ошибка сервера.");
        } else {
            messagingTemplate.convertAndSendToUser(message.getConsumerUserUuid(),
                    "/topic/private", returnMessage);
        }
    }
}

