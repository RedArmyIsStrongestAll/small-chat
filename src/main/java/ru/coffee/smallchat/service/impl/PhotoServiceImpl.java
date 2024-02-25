package ru.coffee.smallchat.service.impl;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.coffee.smallchat.repository.PhotoRepository;
import ru.coffee.smallchat.service.PhotoService;

@Service
@EnableScheduling
@Slf4j
public class PhotoServiceImpl implements PhotoService {

    private final PhotoRepository photoRepository;
    private final PrometheusMeterRegistry meterRegistry;


    public PhotoServiceImpl(@Autowired PhotoRepository photoRepository,
                            @Autowired PrometheusMeterRegistry meterRegistry) {
        this.photoRepository = photoRepository;
        this.meterRegistry = meterRegistry;
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
