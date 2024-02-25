package ru.coffee.smallchat.service.impl;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
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
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MainServiceImpl implements MainService {

    private final ConcurrentLinkedQueue<UserForDeleteDTO> queue;
    private final MainRepository postgresRepository;
    private final PhotoService photoService;
    private final PrometheusMeterRegistry meterRegistry;
    private final Long userLiveTimeMinutes;
    private final Long loginSessionTimeMinutes;
    private final DateTimeFormatter dateTimeFormatter;

    public MainServiceImpl(@Autowired PostgresMainRepositoryImpl postgresRepository,
                           @Autowired PhotoService photoService,
                           @Autowired PrometheusMeterRegistry meterRegistry,
                           @Value("${user.live.time.minutes}") Long userLiveTimeMinutes,
                           @Value("${jwt.live.time.minutes}") Long loginSessionTimeMinutes) {
        this.queue = new ConcurrentLinkedQueue<>();
        this.postgresRepository = postgresRepository;
        this.photoService = photoService;
        this.meterRegistry = meterRegistry;
        this.userLiveTimeMinutes = userLiveTimeMinutes;
        this.loginSessionTimeMinutes = loginSessionTimeMinutes;
        this.dateTimeFormatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd HH:mm:ss")
                .appendFraction(ChronoField.MICRO_OF_SECOND, 0, 6, true)
                .toFormatter();
    }

    @Scheduled(fixedDelayString = "#{${user.live.time.minutes} * 1000 * 60}")
    private void scheduledDeleteUser() {
        try {
            while (!queue.isEmpty()) {
                UserForDeleteDTO userForDeleteDTO = queue.poll();

                String lastLoginDateTimeString = postgresRepository.getLastLoginTime(userForDeleteDTO.getUserId());
                LocalDateTime lastLoginDateTime = convertStringToLocalDateTime(lastLoginDateTimeString);
                if (LocalDateTime.now().minusMinutes(loginSessionTimeMinutes).isBefore(lastLoginDateTime)) {
                    userForDeleteDTO.setTimeDelete(LocalDateTime.now().plusMinutes(userLiveTimeMinutes));
                    queue.add(userForDeleteDTO);
                    continue;
                }

                if (LocalDateTime.now().isAfter(userForDeleteDTO.getTimeDelete())) {
                    Integer rawNameUpdate = postgresRepository.deleteUser(userForDeleteDTO.getUserId());
                    if (rawNameUpdate != 1) {
                        log.error("Id: " + userForDeleteDTO.getUserId());
                        log.error("MainServiceImpl.scheduledDeleteUser - неожиданное поведение, " +
                                "не удалился пользователь");
                        meterRegistry.counter("error_in_service",
                                "method", "scheduledDeleteUser",
                                "id", userForDeleteDTO.getUserId()).increment();
                    }
                    photoService.deletePhoto(userForDeleteDTO.getUserId());
                } else {
                    queue.add(userForDeleteDTO);
                    return;
                }
            }

        } catch (DataAccessException e) {
            log.error("MainServiceImpl.scheduledDeleteUser - " + e.getMessage());
        }
    }

    @Override
    public void addPUserToQueueForDelete(String userId) {
        try {
            LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(userLiveTimeMinutes);
            queue.add(new UserForDeleteDTO(userId, localDateTime));
        } catch (Exception e) {
            log.error("Id: " + userId);
            log.error("MainServiceImpl.addPUserToQueueForDelete - " + e.getMessage());
            meterRegistry.counter("error_in_service",
                    "method", "addPUserToQueueForDelete",
                    "id", userId).increment();
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseDTO<Void> editProfile(String name, MultipartFile photo, String userId) {
        sleepToFilledInDataBase(userId);
        Integer rawNameUpdate = saveUserName(name, userId);
        if (rawNameUpdate != 1) {
            log.error("Id: " + userId);
            log.error("MainServiceImpl.saveUser - неожиданное поведение, " +
                    "не сохарнилось имя у пользователя");
            meterRegistry.counter("error_in_service",
                    "method", "registry",
                    "id", userId).increment();
        }

        if (photo == null || photo.isEmpty()) {
            String photoPath = getUserPhotoPath(userId);
            if (photoPath != null) {
                photoService.deletePhoto(userId);
                deleteUserPhotoPath(userId);
            }
        } else {
            String photoPath = photoService.savePhoto(userId, photo);
            if (photoPath == null) {
                log.error("Id: " + userId);
                log.error("MainServiceImpl.registry - неожиданное поведение, " +
                        "не сохранился путь к файлу у пользователя");
                meterRegistry.counter("error_in_controller",
                        "method", "registry",
                        "id", userId).increment();
                return new ResponseDTO<>(400, "Не сохранено изображение на сервере");
            }
            Integer rawPhotoUpdate = saveUserPhotoPath(photoPath, photo.getContentType(), userId);
            if (rawPhotoUpdate < 1) {
                log.error("Id: " + userId);
                log.error("MainServiceImpl.registry - неожиданное поведение, " +
                        "не сохранился путь к файлу у пользователя");
                meterRegistry.counter("error_in_controller",
                        "method", "registry",
                        "id", userId).increment();
                return new ResponseDTO<>(400, "Не сохранено изображение на сервере");
            }
        }

        return new ResponseDTO<>(200, null);
    }

    private void sleepToFilledInDataBase(String userId) {
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            log.error("Id: " + userId);
            log.error("MainServiceImpl.sleepToFilledInDataBase - " + e.getMessage());
            meterRegistry.counter("error_in_service",
                    "method", "sleepToFilledInDataBase",
                    "id", userId).increment();
        }
    }

    private Integer saveUserName(String name, String userId) {
        try {
            return postgresRepository.saveName(name, userId);
        } catch (DataAccessException e) {
            log.error("Id: " + userId);
            log.error("MainServiceImpl.saveUserName - " + e.getMessage());
            return 0;
        }
    }

    private String getUserPhotoPath(String userId) {
        try {
            return postgresRepository.getPhotoPath(userId);
        } catch (DataAccessException e) {
            log.error("Id: " + userId);
            log.error("MainServiceImpl.getUserPhotoPath - " + e.getMessage());
            meterRegistry.counter("error_in_service",
                    "method", "getUserPhotoPath",
                    "id", userId).increment();
            return null;
        }
    }

    private Integer saveUserPhotoPath(String path, String type, String userId) {
        try {
            return postgresRepository.savePhotoPath(path, type, userId);
        } catch (DataAccessException e) {
            log.error("Id: " + userId);
            log.error("MainServiceImpl.saveUserPhotoPath - " + e.getMessage());
            return 0;
        }
    }

    private void deleteUserPhotoPath(String userId) {
        try {
            Integer rawDelete = postgresRepository.deletePhotoPath(userId);
            if (rawDelete != 1) {
                log.error("Id: " + userId);
                log.error("MainServiceImpl.deleteUserPhotoPath - неожиданное поведение, " +
                        "не удалился путь к файлу у пользовтаеля");
                meterRegistry.counter("error_in_service",
                        "method", "deleteUserPhotoPath",
                        "id", userId).increment();

            }
        } catch (DataAccessException e) {
            log.error("Id: " + userId);
            log.error("MainServiceImpl.deleteUserPhotoPath - " + e.getMessage());
            meterRegistry.counter("error_in_service",
                    "method", "deleteUserPhotoPath",
                    "id", userId).increment();
        }
    }

    @Override
    public ResponseDTO<UserDTO> getUserById(String lookingUserId, String userId) {
        try {
            UserDTO user = postgresRepository.getUserById(lookingUserId);
            setPhoto(user, userId);
            return new ResponseDTO<>(200, user);
        } catch (Exception e) {
            log.error("Id: " + userId);
            log.error("MainServiceImpl.getUserById - " + e.getMessage());
            meterRegistry.counter("error_in_controller",
                    "method", "getUserById",
                    "id", userId).increment();
            return new ResponseDTO<>(400, "Ошибка получения пользователя");
        }
    }

    private void setPhoto(UserDTO user, String userId) {
        try {
            String photoPath = user.getPhotoPath();
            if (photoPath != null) {
                Path filePath = Path.of(photoPath);
                user.setPhoto(Files.readAllBytes(filePath));
            }
        } catch (IOException e) {
            log.error("Id: " + userId);
            log.error("MainServiceImpl.setPhoto - " + e.getMessage());
            meterRegistry.counter("error_in_service",
                    "method", "setPhoto",
                    "id", userId).increment();
        }
    }

    @Override
    public ResponseDTO<List<PublicMessageResponseDTO>> getPublicHistory(String userId, Integer offset) {
        try {
            List<PublicMessageResponseDTO> messageList = postgresRepository.getPublicHistory(offset);
            messageList.forEach(message -> {
                message.setSendTime(convertTimeFromDatabase(message.getSendTime()));
            });
            return new ResponseDTO<>(200, messageList);
        } catch (Exception e) {
            log.error("Id: " + userId);
            log.error("MainServiceImpl.getPublicHistory - " + e.getMessage());
            meterRegistry.counter("error_in_controller",
                    "method", "getPublicHistory",
                    "id", userId).increment();
            return new ResponseDTO<>(400, "Ошибка получения сообщений");
        }
    }

    @Override
    public ResponseDTO<List<ChatDTO>> getPersonalChatList(String userId) {
        try {
            List<ChatDTO> privateChatList = postgresRepository.getListPersonalChatByUserId(userId);

            List<ChatDTO> returnList = privateChatList.stream().map((chatDTO) -> {
                UserDTO user = postgresRepository.getUserById(chatDTO.getPartnerUser().getUserId());
                setPhoto(user, userId);
                chatDTO.getPartnerUser().setName(user.getName());
                chatDTO.getPartnerUser().setPhoto(user.getPhoto());
                chatDTO.getPartnerUser().setPhotoType(user.getPhotoType());
                return chatDTO;
            }).collect(Collectors.toList());

            return new ResponseDTO<>(200, returnList);
        } catch (Exception e) {
            log.error("Id: " + userId);
            log.error("MainServiceImpl.getPersonalChatList - " + e.getMessage());
            meterRegistry.counter("error_in_controller",
                    "method", "getPersonalChatList",
                    "id", userId).increment();
            return new ResponseDTO<>(400, "Ошибка получения списка чатов");
        }
    }

    @Override
    public ResponseDTO<List<PersonalMessageResponseDTO>> getPersonalHistory(Long chatId,
                                                                            String userId, Integer offset) {
        try {
            List<PersonalMessageResponseDTO> messageList = postgresRepository.getPersonalHistory(chatId, offset);
            messageList.forEach(message -> {
                Boolean itIsProducer = message.getProducerUserId().equals(userId);
                message.setUserIsProducerInChat(itIsProducer);
                message.setSendTime(convertTimeFromDatabase(message.getSendTime()));
            });
            return new ResponseDTO<>(200, messageList);
        } catch (Exception e) {
            log.error("Id: " + userId);
            log.error("MainServiceImpl.getPersonalHistory - " + e.getMessage());
            meterRegistry.counter("error_in_controller",
                    "method", "getPersonalHistory",
                    "id", userId).increment();
            return new ResponseDTO<>(400, "Ошибка получения сообщений");
        }
    }

    @Override
    public PublicMessageResponseDTO savePublicMessage(String message, String producerUserId) {
        try {
            LocalDateTime currentTime = LocalDateTime.now();
            Integer rawInsert = postgresRepository.savePublicMessage(message, producerUserId, currentTime);
            if (rawInsert != 1) {
                log.error("Id: " + producerUserId);
                log.error("MainServiceImpl.savePublicMessage - неожиданное поведение, " +
                        "не сохранилось public сообщение");
                meterRegistry.counter("error_in_controller",
                        "method", "savePublicMessage",
                        "id", producerUserId).increment();
                return null;
            }
            return new PublicMessageResponseDTO(message, convertDateTimeToDatabase(currentTime), producerUserId);
        } catch (Exception e) {
            log.error("Id: " + producerUserId);
            log.error("MainServiceImpl.savePublicMessage - " + e.getMessage());
            meterRegistry.counter("error_in_controller",
                    "method", "savePublicMessage",
                    "id", producerUserId).increment();
            return null;
        }
    }

    @Override
    public PersonalMessageResponseDTO savePersonalMessage(String message, Long chatId,
                                                          String consumerUserId, String producerUserId) {
        try {
            LocalDateTime currentTime = LocalDateTime.now();

            Boolean senderIsProducer;
            if (chatId == null) {
                chatId = postgresRepository.saveChat(producerUserId, consumerUserId);
                if (chatId == null) {
                    log.error("Id: " + producerUserId);
                    log.error("MainServiceImpl.savePersonalMessage - неожиданное поведение, " +
                            "не сохранён новый чат в таблицу chats");
                    meterRegistry.counter("error_in_controller",
                            "method", "savePersonalMessage",
                            "id", producerUserId).increment();
                    return null;
                }
                senderIsProducer = true;
            } else {
                ChatAdapterWithFlagProducerDTO chatDTO = postgresRepository.getPersonalChatByUserIdAndChatId(chatId, producerUserId);
                if (chatDTO == null) {
                    log.error("Id: " + producerUserId);
                    log.error("MainServiceImpl.savePersonalMessage - неожиданное поведение, " +
                            "не получен второй участник чата по chatId");
                    meterRegistry.counter("error_in_controller",
                            "method", "savePersonalMessage",
                            "id", producerUserId).increment();
                    return null;
                }
                consumerUserId = chatDTO.getChatDTO().getPartnerUser().getUserId();
                senderIsProducer = chatDTO.getUserIsProducerInChat();
            }

            Integer rawInsertPersonalMessage = postgresRepository.savePersonalMessage(message, currentTime,
                    chatId, senderIsProducer);
            if (rawInsertPersonalMessage != 1) {
                log.error("Id: " + producerUserId);
                log.error("MainServiceImpl.savePersonalMessage - неожиданное поведение, " +
                        "не создалось personal сообщение");
                meterRegistry.counter("error_in_controller",
                        "method", "savePersonalMessage",
                        "id", producerUserId).increment();
                return null;
            }

            return new PersonalMessageResponseDTO(message,
                    convertTimeFromDatabase(convertDateTimeToDatabase(currentTime)),
                    producerUserId, consumerUserId, chatId, senderIsProducer, !senderIsProducer);
        } catch (Exception e) {
            log.error("Id: " + producerUserId);
            log.error("MainServiceImpl.savePersonalMessage - " + e.getMessage());
            meterRegistry.counter("error_in_controller",
                    "method", "savePersonalMessage",
                    "id", producerUserId).increment();
            return null;
        }
    }

    private String convertDateTimeToDatabase(LocalDateTime currentTime) {
        return currentTime.format(dateTimeFormatter);
    }

    private String convertTimeFromDatabase(String dataTime) {
        LocalDateTime time = LocalDateTime.parse(dataTime, dateTimeFormatter);
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private LocalDateTime convertStringToLocalDateTime(String dataTime) {
        return LocalDateTime.parse(dataTime, dateTimeFormatter);
    }
}
