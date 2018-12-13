package com.humac.album.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface StorageService {


    void store(MultipartFile file, String username, boolean isProPicture, String albumName);


    String getExtension(Path imagePath);
    String getFileName(Path imagePath);

    Path getUserPath(String username);

    Path createUserDir(String root, String username, String albumName);

    Path getAvatar(String username);

    Path getImagePath(String username, String filename, String albumName);


    boolean updateUserDir(String path, String newUsername, String oldUsername);

}
