package com.humac.album.service;

import com.humac.album.config.SecurityConfig;
import com.humac.album.dao.RoleDao;
import com.humac.album.dao.UserDao;
import com.humac.album.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {


    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private SecurityConfig securityConfig;


//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder(10);
//    }


    @Override
    public User findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    @Override
    public void save(User user) {

        //encrypt the raw password
        user.setPassword(securityConfig.passwordEncoder().encode(user.getPassword()));

        user.setEnabled(true);
        user.setRole(roleDao.findOne(1L)); // set ROLE_USER for new user
        userDao.save(user);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Load user from the database (throw exception if not found)

        User user = userDao.findByUsername(username);
        if(user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        // Return user object
        return user;
    }
}
