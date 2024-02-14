package ru.coffee.smallchat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import ru.coffee.smallchat.dto.PersonalMessageRequestDTO;
import ru.coffee.smallchat.dto.PersonalMessageResponseDTO;
import ru.coffee.smallchat.dto.PublicMessageResponseDTO;
import ru.coffee.smallchat.service.MainService;

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

    private Object getUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


    /***
     * отправка /chat/send/public
     * получение /topic/public
     * ошибки /user/topic/public.error
     */
    @MessageMapping("public")
    @SendTo("/topic/public")
    public PublicMessageResponseDTO sendPublicChat(@Payload String message) {
        PublicMessageResponseDTO returnMessage = mainService.savePublicMessage(message, getUserId().toString());
        if (returnMessage == null) {
            messagingTemplate.convertAndSendToUser(getUserId().toString(),
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
    public void sendPersonalChat(@Payload PersonalMessageRequestDTO message) {
        PersonalMessageResponseDTO returnMessage = mainService.savePersonalMessage(message.getMessage(), message.getChatId(),
                message.getConsumerUserId(), getUserId().toString());
        if (returnMessage == null) {
            messagingTemplate.convertAndSendToUser(getUserId().toString(),
                    "/topic/private.error",
                    "Сообщение \"" + message.getMessage() + "\" не отправлено. Внутреняя ошибка сервера.");
        } else {
            messagingTemplate.convertAndSendToUser(message.getConsumerUserId(),
                    "/topic/private", returnMessage);
        }
    }
}

