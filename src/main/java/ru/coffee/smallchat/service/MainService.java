package ru.coffee.smallchat.service;

import org.springframework.web.multipart.MultipartFile;
import ru.coffee.smallchat.dto.*;

import java.util.List;

public interface MainService {
    ResponseDTO<Void> editProfile(String name, MultipartFile photo, String userId);

    ResponseDTO<UserDTO> getUserById(String lookingUserId, String userId);

    ResponseDTO<List<PublicMessageResponseDTO>> getPublicHistory(String userId, Integer offset);

    ResponseDTO<List<ChatDTO>> getPersonalChatList(String userId);

    ResponseDTO<List<PersonalMessageResponseDTO>> getPersonalHistory(Long chatId, String userId, Integer offset);

    PublicMessageResponseDTO savePublicMessage(String message, String producerUserId);

    PersonalMessageResponseDTO savePersonalMessage(String message, Long chatId, String consumerUserId, String producerUserId);

    void addPUserToQueueForDelete(String userId);
}
