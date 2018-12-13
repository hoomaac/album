package com.humac.album.service;

import com.humac.album.dao.ImageDao;
import com.humac.album.dao.UserDao;
import com.humac.album.model.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImageServiceImpl implements ImageService {

    @Autowired
    private ImageDao imageDao;

    @Autowired
    private UserDao userDao;


    @Override
    public void save(Image image, boolean isAvatar)
    {
        image.setType(isAvatar);
        imageDao.save(image);
    }



    @Override
    public Image findById(Long id) {
        return imageDao.findOne(id);
    }

    @Override
    public Image findByUsername(String username) {

        return imageDao.findOne(userDao.findByUsername(username).getId());
    }

    @Override
    public Iterable<Image> findAll() {
      return  imageDao.findAll();
    }


}
