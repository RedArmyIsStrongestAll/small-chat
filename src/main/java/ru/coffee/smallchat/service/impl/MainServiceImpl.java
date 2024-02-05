package ru.coffee.smallchat.service.impl;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.coffee.smallchat.dto.*;
import ru.coffee.smallchat.repository.MainRepository;
import ru.coffee.smallchat.repository.impl.PostgresMainRepositoryImpl;
import ru.coffee.smallchat.service.MainService;
import ru.coffee.smallchat.service.PhotoService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MainServiceImpl implements MainService {

    private final MainRepository postgresRepository;
    private final PhotoService photoService;
    private final PrometheusMeterRegistry meterRegistry;

    public MainServiceImpl(@Autowired PostgresMainRepositoryImpl postgresRepository,
                           @Autowired PhotoService photoService,
                           @Autowired PrometheusMeterRegistry meterRegistry) {
        this.postgresRepository = postgresRepository;
        this.photoService = photoService;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public ResponseDTO<String> registry(String name, MultipartFile photo, String userUuid) {
        sleepToFilledInDataBase(userUuid);
        Integer rawNameUpdate = saveUserName(name, userUuid);
        if (rawNameUpdate != 1) {
            log.error("Uuid: " + userUuid);
            log.error("MainServiceImpl.saveUser - неожиданное поведение, " +
                    "не сохарнилось имя у пользователя");
            meterRegistry.counter("error_in_service",
                    "method", "registry",
                    "uuid", userUuid).increment();
        }

        if (photo == null || photo.isEmpty()) {
            String photoPath = getUserPhotoPath(userUuid);
            if (photoPath != null) {
                photoService.deletePhoto(userUuid);
                deleteUserPhotoPath(userUuid);
            }
        } else {
            String photoPath = photoService.savePhoto(userUuid, photo);
            if (photoPath == null) {
                log.error("Uuid: " + userUuid);
                log.error("MainServiceImpl.registry - неожиданное поведение, " +
                        "не сохранился путь к файлу у пользователя");
                meterRegistry.counter("error_in_controller",
                        "method", "registry",
                        "uuid", userUuid).increment();
                return new ResponseDTO<>(400, "Не сохранено изображение на сервере");
            }
            Integer rawPhotoUpdate = saveUserPhotoPath(photoPath, photo.getContentType(), userUuid);
            if (rawPhotoUpdate < 1) {
                log.error("Uuid: " + userUuid);
                log.error("MainServiceImpl.registry - неожиданное поведение, " +
                        "не сохранился путь к файлу у пользователя");
                meterRegistry.counter("error_in_controller",
                        "method", "registry",
                        "uuid", userUuid).increment();
                return new ResponseDTO<>(400, "Не сохранено изображение на сервере");
            }
            photoService.addPhotoToQueueForDelete(userUuid);
        }

        return new ResponseDTO<>(200, userUuid);
    }

    private void sleepToFilledInDataBase(String userUuid) {
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            log.error("Uuid: " + userUuid);
            log.error("MainServiceImpl.sleepToFilledInDataBase - " + e.getMessage());
            meterRegistry.counter("error_in_service",
                    "method", "sleepToFilledInDataBase",
                    "uuid", userUuid).increment();
        }
    }

    private Integer saveUserName(String name, String userUuid) {
        try {
            return postgresRepository.saveName(name, userUuid);
        } catch (DataAccessException e) {
            log.error("Uuid: " + userUuid);
            log.error("MainServiceImpl.saveUserName - " + e.getMessage());
            return 0;
        }
    }

    private String getUserPhotoPath(String userUuid) {
        try {
            return postgresRepository.getPhotoPath(userUuid);
        } catch (DataAccessException e) {
            log.error("Uuid: " + userUuid);
            log.error("MainServiceImpl.getUserPhotoPath - " + e.getMessage());
            meterRegistry.counter("error_in_service",
                    "method", "getUserPhotoPath",
                    "uuid", userUuid).increment();
            return null;
        }
    }

    private Integer saveUserPhotoPath(String path, String type, String userUuid) {
        try {
            return postgresRepository.savePhotoPath(path, type, userUuid);
        } catch (DataAccessException e) {
            log.error("Uuid: " + userUuid);
            log.error("MainServiceImpl.saveUserPhotoPath - " + e.getMessage());
            return 0;
        }
    }

    private void deleteUserPhotoPath(String userUuid) {
        try {
            Integer rawDelete = postgresRepository.deletePhotoPath(userUuid);
            if (rawDelete != 1) {
                log.error("Uuid: " + userUuid);
                log.error("MainServiceImpl.deleteUserPhotoPath - неожиданное поведение, " +
                        "не удалился путь к файлу у пользовтаеля");
                meterRegistry.counter("error_in_service",
                        "method", "deleteUserPhotoPath",
                        "uuid", userUuid).increment();

            }
        } catch (DataAccessException e) {
            log.error("Uuid: " + userUuid);
            log.error("MainServiceImpl.deleteUserPhotoPath - " + e.getMessage());
            meterRegistry.counter("error_in_service",
                    "method", "deleteUserPhotoPath",
                    "uuid", userUuid).increment();
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
            meterRegistry.counter("error_in_controller",
                    "method", "getUserByUuid",
                    "uuid", userUuid).increment();
            return new ResponseDTO<>(400, "Ошибка получения пользователя");
        }
    }

    private void setPhoto(UserDTO user, String userUuid) {
        try {
            String photoPath = user.getPhotoPath();
            Path filePath = Path.of(photoPath);
            user.setPhoto(Files.readAllBytes(filePath));
        } catch (IOException e) {
            log.error("Uuid: " + userUuid);
            log.error("MainServiceImpl.setPhoto - " + e.getMessage());
            meterRegistry.counter("error_in_service",
                    "method", "setPhoto",
                    "uuid", userUuid).increment();
        }
    }

    @Override
    public ResponseDTO<List<PublicMessageResponseDTO>> getPublicHistory(String userUuid, Integer offset) {
        try {
            List<PublicMessageResponseDTO> messageList = postgresRepository.getPublicHistory(offset);
            messageList.forEach(message -> {
                message.setSendTime(convertTimeFromDatabase(message.getSendTime()));
            });
            return new ResponseDTO<>(200, messageList);
        } catch (Exception e) {
            log.error("Uuid: " + userUuid);
            log.error("MainServiceImpl.getPublicHistory - " + e.getMessage());
            meterRegistry.counter("error_in_controller",
                    "method", "getPublicHistory",
                    "uuid", userUuid).increment();
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
                chatDTO.getPartnerUser().setPhotoType(user.getPhotoType());
                return chatDTO;
            }).collect(Collectors.toList());

            return new ResponseDTO<>(200, returnList);
        } catch (Exception e) {
            log.error("Uuid: " + userUuid);
            log.error("MainServiceImpl.getPersonalChatList - " + e.getMessage());
            meterRegistry.counter("error_in_controller",
                    "method", "getPersonalChatList",
                    "uuid", userUuid).increment();
            return new ResponseDTO<>(400, "Ошибка получения списка чатов");
        }
    }

    @Override
    public ResponseDTO<List<PersonalMessageResponseDTO>> getPersonalHistory(Long chatId,
                                                                            String userUuid, Integer offset) {
        try {
            List<PersonalMessageResponseDTO> messageList = postgresRepository.getPersonalHistory(chatId, offset);
            messageList.forEach(message -> {
                Boolean itIsProducer = message.getProducerUserUuid().equals(userUuid);
                message.setItIsProducer(itIsProducer);
                message.setSendTime(convertTimeFromDatabase(message.getSendTime()));
            });
            return new ResponseDTO<>(200, messageList);
        } catch (Exception e) {
            log.error("Uuid: " + userUuid);
            log.error("MainServiceImpl.getPersonalHistory - " + e.getMessage());
            meterRegistry.counter("error_in_controller",
                    "method", "getPersonalHistory",
                    "uuid", userUuid).increment();
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
                meterRegistry.counter("error_in_controller",
                        "method", "savePublicMessage",
                        "uuid", producerUserUuid).increment();
                return null;
            }
            return new PublicMessageResponseDTO(message, convertDateTimeToDatabase(currentTime), producerUserUuid);
        } catch (Exception e) {
            log.error("Uuid: " + producerUserUuid);
            log.error("MainServiceImpl.savePublicMessage - " + e.getMessage());
            meterRegistry.counter("error_in_controller",
                    "method", "savePublicMessage",
                    "uuid", producerUserUuid).increment();
            return null;
        }
    }

    @Override
    public PersonalMessageResponseDTO savePersonalMessage(String message, Long chatId,
                                                          String consumerUserUuid, String producerUserUuid) {
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
                    meterRegistry.counter("error_in_controller",
                            "method", "savePersonalMessage",
                            "uuid", producerUserUuid).increment();
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
                meterRegistry.counter("error_in_controller",
                        "method", "savePersonalMessage",
                        "uuid", producerUserUuid).increment();
                return null;
            }

            return new PersonalMessageResponseDTO(message,
                    convertTimeFromDatabase(convertDateTimeToDatabase(currentTime)),
                    producerUserUuid, consumerUserUuid, chatId, senderIsProducer, !senderIsProducer);
        } catch (Exception e) {
            log.error("Uuid: " + producerUserUuid);
            log.error("MainServiceImpl.savePersonalMessage - " + e.getMessage());
            meterRegistry.counter("error_in_controller",
                    "method", "savePersonalMessage",
                    "uuid", producerUserUuid).increment();
            return null;
        }
    }

    private String convertDateTimeToDatabase(LocalDateTime currentTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        return currentTime.format(formatter);
    }

    private String convertTimeFromDatabase(String dataTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        LocalDateTime time = LocalDateTime.parse(dataTime, formatter);
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
