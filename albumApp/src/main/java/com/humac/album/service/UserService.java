package com.humac.album.service;

import com.humac.album.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {



    User findByUsername(String username);
    void save(User user); // save or update user
}
