package com.humac.album.service;


import com.humac.album.dao.AlbumDao;
import com.humac.album.model.Album;
import com.humac.album.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlbumServiceImpl implements AlbumService {

    @Autowired
    private AlbumDao albumDao;

    @Override
    public void save(Album album) {
        albumDao.save(album);
    }

    @Override
    public Iterable<Album> findAll() {
        return albumDao.findAll();
    }

    @Override
    public Album findOne(Long id) {
        return albumDao.findOne(id);
    }

    @Override
    public Iterable<Album> getUserAlbums(User user) {
        return user.getAlbum();
    }
}
