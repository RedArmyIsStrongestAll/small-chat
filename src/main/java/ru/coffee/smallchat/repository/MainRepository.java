package ru.coffee.smallchat.repository;

import ru.coffee.smallchat.dto.*;
import ru.coffee.smallchat.entity.AbstractRegistry;

import java.time.LocalDateTime;
import java.util.List;

public interface MainRepository {
    String rigestryUser(AbstractRegistry registry);

    Integer saveName(String name, String userId);

    String getPhotoPath(String userId);

    Integer savePhotoPath(String path, String type, String userId);

    Integer deletePhotoPath(String userId);

    UserDTO getUserById(String userId);

    List<UserAuthDTO> getUserByAuthId(String userId);

    Integer updateLastLoginTime(String userId);

    String getLastLoginTime(String userId);

    Integer deleteUser(String userId);

    Integer reDeleteUser(String userId);

    List<PublicMessageResponseDTO> getPublicHistory(Integer offset);

    List<ChatDTO> getPersonalChatProducerId(String userId);

    ChatDTO getPersonalChatProducerId(String producerUserId, String consumerUserId);

    List<PersonalMessageResponseDTO> getPersonalHistory(Long chatId, Integer offset);

    Integer savePublicMessage(String message, String producerUserId, LocalDateTime currentTime);

    Integer savePersonalMessage(String message, LocalDateTime currentTime, Long chatId,
                                String producerUserId, Boolean senderIsProducer);

    Long saveChat(String producerUserId, String consumerUserId);
}
