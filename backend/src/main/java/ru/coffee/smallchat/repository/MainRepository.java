package ru.coffee.smallchat.repository;

import ru.coffee.smallchat.dto.*;

import java.time.LocalDateTime;
import java.util.List;

public interface MainRepository {
    String rigestryUser(OAuthRegistryDTO registry);

    int saveName(String name, String userId);

    String getPhotoPath(String userId);

    int savePhotoPath(String path, String type, String userId);

    int deletePhotoPath(String userId);

    UserDTO getUserById(String userId);

    List<UserAuthDTO> getUserByAuthId(String userId);

    int updateLastLoginTime(String userId);

    String getLastLoginTime(String userId);

    int deleteUser(String userId);

    int reDeleteUser(String userId);

    List<PublicMessageResponseDTO> getPublicHistory(int offset);

    List<ChatDTO> getListPersonalChatByUserId(String userId);

    ChatAdapterWithFlagProducerDTO getPersonalChatByUserIdAndChatId(long chatId, String userId);

    List<PersonalMessageResponseDTO> getPersonalHistory(long chatId, int offset);

    int savePublicMessage(String message, String producerUserId, LocalDateTime currentTime);

    int savePersonalMessage(String message, LocalDateTime currentTime, long chatId, boolean senderIsProducer);

    long saveChat(String producerUserId, String consumerUserId);

    boolean checkBlocking(String userId);
}
