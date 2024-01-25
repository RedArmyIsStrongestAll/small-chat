package ru.coffee.smallchat.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.coffee.smallchat.dto.*;
import ru.coffee.smallchat.repository.MainRepository;
import ru.coffee.smallchat.repository.impl.PostgresRepositoryImpl;
import ru.coffee.smallchat.service.MainService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MainServiceImpl implements MainService {

    private String savedPhotoDir;
    private MainRepository postgresRepository;

    public MainServiceImpl(@Value("${saved.photo.dir}") String savedPhotoDir,
                           @Autowired PostgresRepositoryImpl postgresRepository) {
        if (savedPhotoDir.isEmpty()) {
            this.savedPhotoDir = System.getProperty("user.dir");
        } else {
            this.savedPhotoDir = savedPhotoDir;
        }
        this.postgresRepository = postgresRepository;
    }

    @Override
    public ResponseDTO<String> saveUser(String name, MultipartFile photo, String userUuid) {
        sleepToFilledInDataBase(userUuid);
        Integer rawNameUpdate = saveUserName(name, userUuid);
        if (rawNameUpdate != 1) {
            log.error("Uuid: " + userUuid);
            log.error("MainServiceImpl.saveUser - неожиданное поведение, " +
                    "не сохарнилось имя у пользователя");
        }

        if (photo == null || photo.isEmpty()) {
            String photoPath = getUserPhotoPath(userUuid);
            if (photoPath != null) {
                deleteUserPhotoFile(photoPath, userUuid);
                deleteUserPhotoPath(userUuid);
            }
        } else {
            String photoPath = saveUserPhotoFile(photo, userUuid);
            if (photoPath == null) {
                return new ResponseDTO<>(400, "Не сохранено изображение на сервере");
            }
            Integer rawPhotoUpdate = saveUserPhotoPath(photoPath, photo.getContentType(), userUuid);
            if (rawPhotoUpdate < 1) {
                return new ResponseDTO<>(400, "Не сохранено изображение на сервере");
            }
            if (rawPhotoUpdate != 1) {
                log.error("Uuid: " + userUuid);
                log.error("MainServiceImpl.saveUser - неожиданное поведение, " +
                        "не сохранился путь к файлу у пользователя");
            }
        }

        return new ResponseDTO<>(200, userUuid);
    }

    private void sleepToFilledInDataBase(String userUuid) {
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            log.error("Uuid: " + userUuid);
            log.error("MainServiceImpl.sleepToFilledInDataBase - " + e.getMessage());
        }
    }

    public String saveUserPhotoFile(MultipartFile photo, String userUuid) {
        try {
            Path filePath = Path.of(savedPhotoDir, "user_photos", userUuid);
            Files.write(filePath, photo.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return filePath.toString();
        } catch (IOException e) {
            log.error("Uuid: " + userUuid);
            log.error("MainServiceImpl.saveUserPhotoFile - " + e.getMessage());
            return null;
        }
    }

    @Override
    public void deleteUserPhotoFile(String photoPath, String userUuid) {
        Path filePath = Path.of(photoPath);
        try {
            Files.delete(filePath);
        } catch (IOException e) {
            log.error("Uuid: " + userUuid);
            log.error("MainServiceImpl.deleteUserPhotoFile - " + e.getMessage());
        }
    }

    public Integer saveUserName(String name, String userUuid) {
        try {
            return postgresRepository.saveName(name, userUuid);
        } catch (DataAccessException e) {
            log.error("Uuid: " + userUuid);
            log.error("MainServiceImpl.saveUserName - " + e.getMessage());
            return 0;
        }
    }

    public String getUserPhotoPath(String userUuid) {
        try {
            return postgresRepository.getPhotoPath(userUuid);
        } catch (DataAccessException e) {
            log.error("Uuid: " + userUuid);
            log.error("MainServiceImpl.getUserPhotoPath - " + e.getMessage());
            return null;
        }
    }

    public Integer saveUserPhotoPath(String path, String type, String userUuid) {
        try {
            return postgresRepository.savePhotoPath(path, type, userUuid);
        } catch (DataAccessException e) {
            log.error("Uuid: " + userUuid);
            log.error("MainServiceImpl.saveUserPhotoPath - " + e.getMessage());
            return 0;
        }
    }

    public void deleteUserPhotoPath(String userUuid) {
        try {
            Integer rawDelete = postgresRepository.deletePhotoPath(userUuid);
            if (rawDelete != 1) {
                log.error("Uuid: " + userUuid);
                log.error("MainServiceImpl.deleteUserPhotoPath - неожиданное поведение, " +
                        "не удалился путь к файлу у пользовтаеля");
            }
        } catch (DataAccessException e) {
            log.error("Uuid: " + userUuid);
            log.error("MainServiceImpl.deleteUserPhotoPath - " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<UserDTO> getUserByUuid(String lookingUserUuid, String userUuid) {
        try {
            UserDTO user = postgresRepository.getUserByUuid(lookingUserUuid);
            setPhoto(user, userUuid);
            return new ResponseDTO<>(200, user);
        } catch (Exception e) {
            log.error("Uuid: " + userUuid);
            log.error("MainServiceImpl.getUserByUuid - " + e.getMessage());
            return new ResponseDTO<>(400, "Ошибка получения пользователя");
        }
    }

    public void setPhoto(UserDTO user, String userUuid) {
        try {
            String photoPath = user.getPhotoPath();
            Path filePath = Path.of(photoPath);
            user.setPhoto(Files.readAllBytes(filePath));
        } catch (IOException e) {
            log.error("Uuid: " + userUuid);
            log.error("MainServiceImpl.setPhoto - " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<PublicMessageResponseDTO>> getPublicHistory(String userUuid, Integer offset) {
        try {
            List<PublicMessageResponseDTO> messageList = postgresRepository.getPublicHistory(offset);
            return new ResponseDTO<>(200, messageList);
        } catch (Exception e) {
            log.error("Uuid: " + userUuid);
            log.error("MainServiceImpl.getPublicHistory - " + e.getMessage());
            return new ResponseDTO<>(400, "Ошибка получения сообщений");
        }
    }

    @Override
    public ResponseDTO<List<ChatDTO>> getPersonalChatList(String userUuid) {
        try {
            List<ChatDTO> privateChatList = postgresRepository.getPersonalChatProducerId(userUuid);

            List<ChatDTO> returnList = privateChatList.stream().map((chatDTO) -> {
                UserDTO user = postgresRepository.getUserByUuid(chatDTO.getPartnerUser().getUserUuid());
                setPhoto(user, userUuid);
                chatDTO.getPartnerUser().setName(user.getName());
                chatDTO.getPartnerUser().setPhoto(user.getPhoto());
                return chatDTO;
            }).collect(Collectors.toList());

            return new ResponseDTO<>(200, returnList);
        } catch (Exception e) {
            log.error("Uuid: " + userUuid);
            log.error("MainServiceImpl.getPersonalChatList - " + e.getMessage());
            return new ResponseDTO<>(400, "Ошибка получения списка чатов");
        }
    }

    @Override
    public ResponseDTO<List<PersonalMessageResponseDTO>> getPersonalHistory(Long chatId, String userUuid, Integer offset) {
        try {
            List<PersonalMessageResponseDTO> messageList = postgresRepository.getPersonalHistory(chatId, offset);
            messageList.forEach(message -> {
                Boolean itIsProducer = message.getProducerUserUuid().equals(userUuid);
                message.setItIsProducer(itIsProducer);
            });
            return new ResponseDTO<>(200, messageList);
        } catch (Exception e) {
            log.error("Uuid: " + userUuid);
            log.error("MainServiceImpl.getPersonalHistory - " + e.getMessage());
            return new ResponseDTO<>(400, "Ошибка получения сообщений");
        }
    }

    @Override
    public PublicMessageResponseDTO savePublicMessage(String message, String producerUserUuid) {
        try {
            LocalDateTime currentTime = LocalDateTime.now();
            Integer rawInsert = postgresRepository.savePublicMessage(message, producerUserUuid, currentTime);
            if (rawInsert != 1) {
                log.error("Uuid: " + producerUserUuid);
                log.error("MainServiceImpl.savePublicMessage - неожиданное поведение, " +
                        "не сохранилось public сообщение");
                return null;
            }
            return new PublicMessageResponseDTO(message, convertLocalDataTime(currentTime), producerUserUuid);
        } catch (Exception e) {
            log.error("Uuid: " + producerUserUuid);
            log.error("MainServiceImpl.savePublicMessage - " + e.getMessage());
            return null;
        }
    }

    @Override
    public PersonalMessageResponseDTO savePersonalMessage(String message, Long chatId, String consumerUserUuid, String producerUserUuid) {
        try {
            LocalDateTime currentTime = LocalDateTime.now();

            Boolean senderIsProducer;
            if (chatId == null) {
                chatId = postgresRepository.saveChat(producerUserUuid, consumerUserUuid);
                senderIsProducer = true;
                if (chatId == null) {
                    log.error("Uuid: " + producerUserUuid);
                    log.error("MainServiceImpl.savePersonalMessage - неожиданное поведение, " +
                            "не сохранён новый чат в таблицу chats");
                    return null;
                }
            } else {
                ChatDTO chatDTO = postgresRepository.getPersonalChatProducerId(producerUserUuid, consumerUserUuid);
                if (chatDTO.getPartnerUser().getUserUuid().equals(producerUserUuid)) {
                    senderIsProducer = true;
                } else {
                    senderIsProducer = false;
                }
            }

            Integer rawInsertPersonalMessage = postgresRepository.savePersonalMessage(message, currentTime,
                    chatId, producerUserUuid, senderIsProducer);
            if (rawInsertPersonalMessage != 1) {
                log.error("Uuid: " + producerUserUuid);
                log.error("MainServiceImpl.savePersonalMessage - неожиданное поведение, " +
                        "не создалось personal сообщение");
                return null;
            }

            return new PersonalMessageResponseDTO(message, convertLocalDataTime(currentTime),
                    producerUserUuid, consumerUserUuid, chatId, senderIsProducer, !senderIsProducer);
        } catch (Exception e) {
            log.error("Uuid: " + producerUserUuid);
            log.error("MainServiceImpl.savePersonalMessage - " + e.getMessage());
            return null;
        }
    }

    public String convertLocalDataTime(LocalDateTime currentTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return currentTime.format(formatter);
    }
}
