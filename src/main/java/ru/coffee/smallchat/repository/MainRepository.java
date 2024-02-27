package ru.coffee.smallchat.repository;

import ru.coffee.smallchat.dto.*;

import java.time.LocalDateTime;
import java.util.List;

public interface MainRepository {
    String rigestryUser(OAuthRegistryDTO registry);

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

    List<ChatDTO> getListPersonalChatByUserId(String userId);

    ChatAdapterWithFlagProducerDTO getPersonalChatByUserIdAndChatId(Long chatId, String userId);

    List<PersonalMessageResponseDTO> getPersonalHistory(Long chatId, Integer offset);

    Integer savePublicMessage(String message, String producerUserId, LocalDateTime currentTime);

    Integer savePersonalMessage(String message, LocalDateTime currentTime, Long chatId, Boolean senderIsProducer);

    Long saveChat(String producerUserId, String consumerUserId);
}
