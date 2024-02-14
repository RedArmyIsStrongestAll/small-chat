package ru.coffee.smallchat.repository.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import ru.coffee.smallchat.repository.PhotoRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Repository
public class FilePhotoRepository implements PhotoRepository {

    private final String savedPhotoDir;

    public FilePhotoRepository(@Value("${saved.photo.dir}") String savedPhotoDir) {
        if (savedPhotoDir.isEmpty()) {
            this.savedPhotoDir = System.getProperty("user.dir");
        } else {
            this.savedPhotoDir = savedPhotoDir;
        }
    }

    @Override
    public String savePhoto(String userId, MultipartFile photo) throws RuntimeException {
        try {
            Path filePath = Path.of(savedPhotoDir, "user_photos", userId);
            Files.write(filePath, photo.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deletePhoto(String userId) throws RuntimeException {
        try {
            Path filePath = Path.of(savedPhotoDir, "user_photos", userId);
            Files.delete(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
