package ru.coffee.smallchat.service.impl;

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

    public PhotoServiceImpl(@Autowired PhotoRepository photoRepository,
                            @Autowired MainRepository mainRepository,
                            @Value("${user.live.time.minutes}") Long userLiveTimeMinutes) {
        this.queue = new ConcurrentLinkedQueue<>();
        this.photoRepository = photoRepository;
        this.mainRepository = mainRepository;
        this.userLiveTimeMinutes = userLiveTimeMinutes;
    }

    @Scheduled(fixedDelayString = "#{${user.live.time.minutes} * 1000 * 60}")
    private void scheduledDeletePhoto() {
        if (!queue.isEmpty()) {
            FileForDeleteDTO fileForDeleteDTO = queue.poll();

            try {
                mainRepository.getUserByUuid(fileForDeleteDTO.getUserUuid());
                fileForDeleteDTO.setTime(fileForDeleteDTO.getTime().plusMinutes(userLiveTimeMinutes));
                queue.add(fileForDeleteDTO);
            } catch (EmptyResultDataAccessException e) {

                if (LocalDateTime.now().isAfter(fileForDeleteDTO.getTime())) {
                    log.info("PhotoServiceImpl.scheduledDeletePhoto - удаление фотонрафии uuid " + fileForDeleteDTO.getUserUuid());
                    photoRepository.deletePhoto(fileForDeleteDTO.getUserUuid());
                } else {
                    queue.add(fileForDeleteDTO);
                }
            } catch (DataAccessException e) {
                log.error("Uuid: " + fileForDeleteDTO.getUserUuid());
                log.error("PhotoServiceImpl.scheduledDeletePhoto - " + e.getMessage());
            }
        }
    }

    @Override
    public void addPhotoToQueueForDelete(String userUuid) {
        try {
            LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(userLiveTimeMinutes);
            queue.add(new FileForDeleteDTO(userUuid, localDateTime));
        } catch (Exception e) {
            log.error("Uuid: " + userUuid);
            log.error("PhotoServiceImpl.addPhotoToQueueForDelete - " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public String savePhoto(String userUuid, MultipartFile photo) {
        try {
            return photoRepository.savePhoto(userUuid, photo);
        } catch (RuntimeException e) {
            log.error("Uuid: " + userUuid);
            log.error("PhotoServiceImpl.savePhoto - " + e.getMessage());
            return null;
        }
    }

    @Override
    public void deletePhoto(String userUuid) {
        try {
            photoRepository.deletePhoto(userUuid);
        } catch (Exception e) {
            log.error("Uuid: " + userUuid);
            log.error("PhotoServiceImpl.deletePhoto - " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
