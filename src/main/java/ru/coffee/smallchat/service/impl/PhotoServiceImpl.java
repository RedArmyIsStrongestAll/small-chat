package ru.coffee.smallchat.service.impl;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.coffee.smallchat.dto.FileForDeleteDTO;
import ru.coffee.smallchat.repository.MainRepository;
import ru.coffee.smallchat.repository.PhotoRepository;
import ru.coffee.smallchat.service.PhotoService;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@EnableScheduling
@Slf4j
public class PhotoServiceImpl implements PhotoService {

    private final ConcurrentLinkedQueue<FileForDeleteDTO> queue;
    private final PhotoRepository photoRepository;
    private final MainRepository mainRepository;
    private final Long userLiveTimeMinutes;
    private final PrometheusMeterRegistry meterRegistry;


    public PhotoServiceImpl(@Autowired PhotoRepository photoRepository,
                            @Autowired MainRepository mainRepository,
                            @Value("${user.live.time.minutes}") Long userLiveTimeMinutes,
                            @Autowired PrometheusMeterRegistry meterRegistry) {
        this.queue = new ConcurrentLinkedQueue<>();
        this.photoRepository = photoRepository;
        this.mainRepository = mainRepository;
        this.userLiveTimeMinutes = userLiveTimeMinutes;
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(fixedDelayString = "#{${user.live.time.minutes} * 1000 * 60}")
    private void scheduledDeletePhoto() {
        //todo Подумай про удаление: удалять ли вообще или это тоже 149
        if (!queue.isEmpty()) {
            FileForDeleteDTO fileForDeleteDTO = queue.poll();

            try {
                mainRepository.getUserById(fileForDeleteDTO.getUserId());
                fileForDeleteDTO.setTime(fileForDeleteDTO.getTime().plusMinutes(userLiveTimeMinutes));
                queue.add(fileForDeleteDTO);
            } catch (EmptyResultDataAccessException e) {

                if (LocalDateTime.now().isAfter(fileForDeleteDTO.getTime())) {
                    log.info("PhotoServiceImpl.scheduledDeletePhoto - удаление фотонрафии id " + fileForDeleteDTO.getUserId());
                    photoRepository.deletePhoto(fileForDeleteDTO.getUserId());
                } else {
                    queue.add(fileForDeleteDTO);
                }
            } catch (DataAccessException e) {
                log.error("Id: " + fileForDeleteDTO.getUserId());
                log.error("PhotoServiceImpl.scheduledDeletePhoto - " + e.getMessage());
                meterRegistry.counter("error_in_service",
                        "method", "scheduledDeletePhoto",
                        "id", fileForDeleteDTO.getUserId()).increment();
            }
        }
    }

    @Override
    public void addPhotoToQueueForDelete(String userId) {
        try {
            LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(userLiveTimeMinutes);
            queue.add(new FileForDeleteDTO(userId, localDateTime));
        } catch (Exception e) {
            log.error("Id: " + userId);
            log.error("PhotoServiceImpl.addPhotoToQueueForDelete - " + e.getMessage());
            meterRegistry.counter("error_in_service",
                    "method", "addPhotoToQueueForDelete",
                    "id", userId).increment();
            throw new RuntimeException(e);
        }
    }

    @Override
    public String savePhoto(String userId, MultipartFile photo) {
        try {
            return photoRepository.savePhoto(userId, photo);
        } catch (RuntimeException e) {
            log.error("Id: " + userId);
            log.error("PhotoServiceImpl.savePhoto - " + e.getMessage());
            return null;
        }
    }

    @Override
    public void deletePhoto(String userId) {
        try {
            photoRepository.deletePhoto(userId);
        } catch (Exception e) {
            log.error("Id: " + userId);
            log.error("PhotoServiceImpl.deletePhoto - " + e.getMessage());
            meterRegistry.counter("error_in_service",
                    "method", "deletePhoto",
                    "id", userId).increment();
            throw new RuntimeException(e);
        }
    }
}
