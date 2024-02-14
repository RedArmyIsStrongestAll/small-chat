package ru.coffee.smallchat.service;

import org.springframework.web.multipart.MultipartFile;
import ru.coffee.smallchat.dto.*;

import java.util.List;

public interface MainService {
    //todo rename -> editUser
    ResponseDTO<String> registry(String name, MultipartFile photo, String userUuid);

    //todo rename -> byId
    ResponseDTO<UserDTO> getUserByUuid(String lookingUserUuid, String userUuid);

    ResponseDTO<List<PublicMessageResponseDTO>> getPublicHistory(String userUuid, Integer offset);

    ResponseDTO<List<ChatDTO>> getPersonalChatList(String userUuid);

    ResponseDTO<List<PersonalMessageResponseDTO>> getPersonalHistory(Long chatId, String userUuid, Integer offset);

    PublicMessageResponseDTO savePublicMessage(String message, String producerUserUuid);

    PersonalMessageResponseDTO savePersonalMessage(String message, Long chatId, String consumerUserUuid, String producerUserUuid);
}
