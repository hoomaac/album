package com.humac.album.web.controller;


import com.humac.album.model.Album;
import com.humac.album.model.Image;
import com.humac.album.service.AlbumService;
import com.humac.album.service.ImageService;
import com.humac.album.service.StorageService;
import com.humac.album.service.UserService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class AlbumController {


    @Autowired
    private UserService userService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private AlbumService albumService;



    @RequestMapping("/")
    public String albumList(Model model){

        model.addAttribute("albums", albumService.findAll());
        return "index";

    }


    @RequestMapping("/image/album/")
    public String uploadAlbum(Model model){

        model.addAttribute("album", new Album());

        return "create_album";
    }


    @RequestMapping(value = "/image/album/", method = RequestMethod.POST)
    public String uploadAlbum(Principal principal, @ModelAttribute Album album,
                              HttpSession session, RedirectAttributes rd){


        album.setUser(userService.findByUsername(principal.getName()));

        //replace whitespace with underscore
        album.setName(album.getName().replace(" ", "_"));

        albumService.save(album);


//        rd.addAttribute("album", album);
//        storageService.store(file, principal.getName(), false);

        session.setAttribute("album", album);

        return "redirect:/image/album/upload/";

    }


    @RequestMapping(value = "/image/album/upload/")
    public String uploadImageAlbum(){

        return "uploadImage";
    }


    @RequestMapping(value = "/image/album/upload/", method = RequestMethod.POST)
    public String uploadImageAlbum(@RequestParam("image") MultipartFile file,
                                   RedirectAttributes redirectAttributes, HttpSession session,
                                   Principal principal){

        if(file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Select a file");
            return "uploadImage";
        }


        //get album object from session
        Album album = (Album) session.getAttribute("album");

//        Logger.getLogger(ImageController.class.toString()).log(Level.INFO, "album name: " + album.getName());

        storageService.store(file, principal.getName(), false, album.getName());



        String url = MvcUriComponentsBuilder.fromMethodName(AlbumController.class,
                "serveImage", principal.getName(), album.getName(), file.getOriginalFilename()).build().toString();

        Logger.getLogger(ImageController.class.toString()).log(Level.INFO, "image url: " + Paths.get(url).toFile());
        Logger.getLogger(ImageController.class.toString()).log(Level.INFO, "file name: " + file.getOriginalFilename());

        Image image = new Image();
        image.setImageUrl(url);

        image.setAlbum(album);

        imageService.save(image, false);



        Logger.getLogger(ImageController.class.toString()).log(Level.INFO, "image for album is saved");


        return "redirect:/image/album/upload/";
    }


    /**
     *
     *
     * this method needs some url likes /uploads/alex/album/alex_album/fox.jpg to retrieve the correct -
     * image from storage
     *
     * @param username
     * @param albumName
     * @param filename
     * @return response entity that handles the proper http header and content.
     * @throws IOException
     */


    @RequestMapping("/uploads/{username}/album/{albumName}/{filename}")
    @ResponseBody
    public ResponseEntity<byte[]> serveImage(@PathVariable String username,
                                             @PathVariable String albumName,
                                             @PathVariable String filename) throws IOException {

        Path imageUrl = storageService.getImagePath(username, filename, albumName);

        byte[] image = FileUtils.readFileToByteArray(imageUrl.toFile());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(imageUrl.toUri());

        return new ResponseEntity<>(image, headers, HttpStatus.OK);

    }


    @RequestMapping(value = "/image/album/show", method = RequestMethod.POST)
    public String showAlbum(@RequestParam Long id, Model model){

        //pass album_user to check if current user is album'user, allow him/her to add images to album
        model.addAttribute("album_user", albumService.findOne(id).getUser());

        //pass all the album(the one matches with id) images
        model.addAttribute("images", albumService.findOne(id).getImages());
        return "showAlbum";

    }


}
