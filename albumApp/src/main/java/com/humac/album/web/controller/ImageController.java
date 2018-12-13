package com.humac.album.web.controller;


import com.humac.album.model.Image;
import com.humac.album.model.User;
import com.humac.album.service.ImageService;
import com.humac.album.service.StorageService;
import com.humac.album.service.UserService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Path;
import java.security.Principal;



/**
 *
 * This controller is in charge of every uploading process
 */


@Controller
public class ImageController {



    @Autowired
    private StorageService storageService;

    @Autowired
    private ImageService imageService;


    @Autowired
    private UserService userService;

    //path to save the user's uploads in unix OS
    private static final String UPLOADED_FOLDER = "uploads";


    @RequestMapping(value = "/image/upload")
    public String upload() {

        return "upload_avatar";
    }


    @RequestMapping(value ="/image/upload" , method = RequestMethod.POST)
    public String upload(
            @RequestParam("file") MultipartFile file,
            Principal principal, RedirectAttributes rdAttributes) {


        if (file.isEmpty()) {

            rdAttributes.addFlashAttribute("message", "please select a file");
            return "redirect:/image/upload";

        }


        storageService.store(file, principal.getName(), true, null);


        String uri = MvcUriComponentsBuilder.fromMethodName(ImageController.class,
                "serveAvatar", principal.getName()).build().toString();

        Image image = new Image();
        image.setImageUrl(uri);

        User user = userService.findByUsername(principal.getName());
        user.setAvatarImage(image);

        //save image as avatar; true argument for avatar
        imageService.save(image, true);

        userService.save(user);


        return "redirect:/user/" + principal.getName() + "/";

    }


    /**
     *
     * This method build internal url for every images uploaded in upload directory.
     * This method is called by userProfile and produce url using @param username and its request map.
     * It returns ResponseEntity byte[].
     * @param username
     * @return ResponseEntity
     * @throws IOException
     */

    @RequestMapping("/uploads/{username}")
    @ResponseBody
    public ResponseEntity<byte[]> serveAvatar(@PathVariable String username) throws IOException {

        /*

        Get the file uri and then convert it to byte[](we need it to show in the page)

         */

        Path uriImage = storageService.getAvatar(username);

        byte[] image = FileUtils.readFileToByteArray(uriImage.toFile());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriImage.toUri());

        //set content and headers and headers status
        return new ResponseEntity<>(image, headers, HttpStatus.OK);

    }




}