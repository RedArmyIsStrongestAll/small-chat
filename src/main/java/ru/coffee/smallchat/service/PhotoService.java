package ru.coffee.smallchat.service;

import org.springframework.web.multipart.MultipartFile;

public interface PhotoService {
    String savePhoto(String userId, MultipartFile photo);

    void deletePhoto(String userId);

    void addPhotoToQueueForDelete(String photoPath);
}
