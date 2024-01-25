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
 *     public void configureMessageBroker(MessageBrokerRegistry config) {
 *         config.setApplicationDestinationPrefixes("/chat/send");
 *         config.enableSimpleBroker("/chat/read");
 *         config.setUserDestinationPrefix("/user");
 *     }
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
     * получение /chat/read/public
     * ошибки /user/chat/read/public/error
     */
    @MessageMapping("public")
    @SendTo("/chat/read/public")
    public PublicMessageResponseDTO sendPublicChat(@Payload String message,
                                                   @Header("simpUser") Principal principalProducerUuid) {
        PublicMessageResponseDTO returnMessage = mainService.savePublicMessage(message, principalProducerUuid.getName());
        if (returnMessage == null) {
            messagingTemplate.convertAndSendToUser(principalProducerUuid.getName(), "/chat/read/public/error",
                    "Сообщение \"" + message + "\" не отправлено. Внутреняя ошибка сервера.");
        }
        return returnMessage;
    }


    /***
     * отправка /chat/send/personal
     * получение /user/chat/read/private
     * ошибки /user/chat/read/private/error
     */
    @MessageMapping("personal")
    public void sendPrivateChat(@Payload PersonalMessageRequestDTO message,
                                @Header("simpUser") Principal principalProducerUuid) {
        PersonalMessageResponseDTO returnMessage = mainService.savePersonalMessage(message.getMessage(), message.getChatId(),
                message.getConsumerUserUuid(), principalProducerUuid.getName());
        if (returnMessage == null) {
            messagingTemplate.convertAndSendToUser(principalProducerUuid.getName(), "/chat/read/private/error",
                    "Сообщение \"" + message.getMessage() + "\" не отправлено. Внутреняя ошибка сервера.");
        } else {
            messagingTemplate.convertAndSendToUser(message.getConsumerUserUuid(), "/chat/read/private", returnMessage);
        }
    }
}

