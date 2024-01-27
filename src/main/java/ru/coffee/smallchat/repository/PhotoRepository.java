package ru.coffee.smallchat.repository;

import org.springframework.web.multipart.MultipartFile;

public interface PhotoRepository {
    String savePhoto(String userUuid, MultipartFile photo);

    void deletePhoto(String photoPath);
}
