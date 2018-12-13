package com.humac.album.dao;


import com.humac.album.model.Image;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageDao extends CrudRepository<Image, Long> {

}
