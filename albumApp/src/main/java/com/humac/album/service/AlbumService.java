package com.humac.album.service;


import com.humac.album.model.Album;
import com.humac.album.model.User;

public interface AlbumService  {


    void save(Album album);

    Iterable<Album> findAll();

    Album findOne(Long id);

    Iterable<Album> getUserAlbums(User user);

}
