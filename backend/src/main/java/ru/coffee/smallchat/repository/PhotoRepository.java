package ru.coffee.smallchat.repository;

import org.springframework.web.multipart.MultipartFile;

public interface PhotoRepository {
    String savePhoto(String userId, MultipartFile photo);

    void deletePhoto(String userId);
}
