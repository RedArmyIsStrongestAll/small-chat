package ru.coffee.smallchat.service;

import org.springframework.web.multipart.MultipartFile;

public interface PhotoService {
    String savePhoto(String userUuid, MultipartFile photo);

    void deletePhoto(String photoPath);

    void addPhotoToQueueForDelete(String photoPath);
}
