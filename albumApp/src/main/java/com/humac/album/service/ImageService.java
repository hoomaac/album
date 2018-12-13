package com.humac.album.service;

import com.humac.album.model.Image;

public interface ImageService {

    void save(Image image, boolean isAvatar);

    Image findById(Long id);

    Image findByUsername(String username);

    Iterable<Image> findAll();
}
