package com.humac.album.dao;


import com.humac.album.model.Album;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumDao extends CrudRepository<Album, Long> {
}
