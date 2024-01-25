package ru.coffee.smallchat.repository;

import ru.coffee.smallchat.dto.ChatDTO;
import ru.coffee.smallchat.dto.PersonalMessageResponseDTO;
import ru.coffee.smallchat.dto.PublicMessageResponseDTO;
import ru.coffee.smallchat.dto.UserDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface MainRepository {
    Integer saveName(String name, String userUuid);

    String getPhotoPath(String userUuid);

    Integer savePhotoPath(String path, String type, String userUuid);

    Integer deletePhotoPath(String userUuid);

    UserDTO getUserByUuid(String userUuid);

    List<PublicMessageResponseDTO> getPublicHistory(Integer offset);

    List<ChatDTO> getPersonalChatProducerId(String userUuid);

    ChatDTO getPersonalChatProducerId(String producerUserUuid, String consumerUserUuid);

    List<PersonalMessageResponseDTO> getPersonalHistory(Long chatId, Integer offset);

    Integer savePublicMessage(String message, String producerUserUuid, LocalDateTime currentTime);

    Integer savePersonalMessage(String message, LocalDateTime currentTime, Long chatId,
                                String producerUserUuid, Boolean senderIsProducer);

    Long saveChat(String producerUserUuid, String consumerUserUuid);
}
