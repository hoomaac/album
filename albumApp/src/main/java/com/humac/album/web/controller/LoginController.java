package com.humac.album.web.controller;

import com.humac.album.config.SecurityConfig;
import com.humac.album.model.Album;
import com.humac.album.model.Image;
import com.humac.album.model.User;
import com.humac.album.service.AlbumService;
import com.humac.album.service.ImageService;
import com.humac.album.service.StorageService;
import com.humac.album.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 *
 * Login controller is responsible for registering, login, editing and show user page.
 *
 *
 */

@Controller
public class LoginController {

    @Autowired
    private UserService userService;


    @Autowired
    private SecurityConfig securityConfig;


    @Autowired
    private StorageService storageService;


    @Autowired
    private AlbumService albumService;

    @Autowired
    private ImageService imageService;




    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String register(Model model) {

        model.addAttribute("user", new User());

        return "register";
    }


    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(@ModelAttribute User user, HttpServletRequest request,
                           BindingResult bindingResult,
                           Model model) {


        if(bindingResult.hasErrors()) {
            return "/register";
        }


        String username = user.getUsername();
        String password = user.getPassword();

        userService.save(user);

        try {
            request.login(username, password);

        } catch (ServletException e) {
            System.out.println(e.toString());
        }

        return "redirect:/";
    }


    @RequestMapping(path = "/edit", method = RequestMethod.GET)
    public String edit(Model model) {

        model.addAttribute("user", new User());

        return "edit";
    }


    @RequestMapping(path = "/edit", method = RequestMethod.POST)
    public String edit(@ModelAttribute User user, Principal principal, Model model, RedirectAttributes redirectAttributes) {

        /*

        Get the original userID and replace the new userID with that,
        inorder to update the user not save the new one.

         */

        Long id = userService.findByUsername(principal.getName()).getId();

        //get the original user's password to check with new entered password
        User origUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(user.getUsername().isEmpty())
            user.setUsername(principal.getName());


        if(user.getPassword().isEmpty()){
            model.addAttribute("error", "Password field should be filled");
            return "redirect:/edit";
        }


        //if entered password doesn't match with old password redirect to edit page with an error
        if(!(securityConfig.passwordEncoder().matches(user.getPassword(), origUser.getPassword()))){
            redirectAttributes.addFlashAttribute("error", "Password doesn't match with your password");
            return "redirect:/edit";
        }


        if(userService.findByUsername(user.getUsername()) != null) {
            redirectAttributes.addFlashAttribute("error", "user exists already");
            return "redirect:/edit";
        }


        //rename old directory name which was for old username
//        storageService.renameUserDir(Paths.get(storageService.getAvatar(principal.getName()).toString()), user.getUsername());


        //check if user has any image avatar
        if(imageService.findByUsername(principal.getName()) != null) {

            String oldpath = storageService.getAvatar(principal.getName()).toString();

            storageService.updateUserDir(oldpath, user.getUsername(), principal.getName());


            String newImageUrl = MvcUriComponentsBuilder.fromMethodName(
                    ImageController.class, "serveAvatar",user.getUsername()
                    ).build().toString();


            //make new image
            Image image = new Image();

            //set new url for old avatar image
            image.setImageUrl(newImageUrl);

            //save to Data base
            imageService.save(image, true);

            //update user's avatar
            user.setAvatarImage(image);

        }else {

            //if user doesn't have any avatar yet
            String oldUserPath = storageService.getUserPath(principal.getName()).toString();

            storageService.updateUserDir(oldUserPath, user.getUsername(), principal.getName());
        }



        // set the id of current user inorder to hibernate updates current user
        user.setId(id);

        //update current user
        userService.save(user);


        String username = user.getUsername();

        //Then authenticate the current user again with new info
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, user.getPassword() , user.getAuthorities()));


        return "redirect:/user/" + username + "/";
    }





    /*
        User profile method shows the user page and its activities; albums(view and create) and edit the profile -
        and change avatar image.

     */

    @RequestMapping(path = "/user/{username}/", method = RequestMethod.GET)
    public String userProfile(@PathVariable String username, Model model){

        User user = userService.findByUsername(username);

        //check null user is necessary

        if (user != null) {


            String avatarUrl = null; // user avatar url

            //check if user has any avatar image
            if(user.getAvatarImage() != null)
                avatarUrl = user.getAvatarImage().getImageUrl();


            /*
                if user has any avatar image pass the url to template
                otherwise pass the default avatar holder
             */

            if(avatarUrl != null) {
                Logger.getLogger(LoginController.class.toString()).log(Level.INFO, "Image avatar path: " + avatarUrl);
                model.addAttribute("image", avatarUrl);
            }

            else
                model.addAttribute("image", "/assets/images/picproholder.svg");


            model.addAttribute("user", user);


            //if user has any album get them from database and pass it to template
            Iterable<Album> albums = albumService.getUserAlbums(user);

            if(albums != null)
                model.addAttribute("albums", albums);

            return "user";

        }

        return "redirect:/";

    }


    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String loginForm(Model model, HttpServletRequest request) {
        model.addAttribute("user", new User());
        try {
            Object flash = request.getSession().getAttribute("flash");
            model.addAttribute("flash", flash);

            request.getSession().removeAttribute("flash");
        } catch (Exception ex) {
            // "flash" session attribute must not exist...do nothing and proceed normally
        }
        return "login";
    }

    @RequestMapping("/access_denied")
    public String accessDenied() {
        return "access_denied";
    }
}
