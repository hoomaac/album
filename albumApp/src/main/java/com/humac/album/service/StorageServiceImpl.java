package com.humac.album.service;


import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@PropertySource("application.properties")
public class StorageServiceImpl implements StorageService{

    @Autowired
    private Environment env;

    private final Path rootLocation;

    @Autowired
    public StorageServiceImpl(Environment env){
        this.rootLocation = Paths.get(env.getProperty("upload.rootLocation"));
    }


    @Override
    public void store(MultipartFile file, String username, boolean isProPicture, String albumName) {


        String filename = StringUtils.cleanPath(file.getOriginalFilename());

//        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase().replaceAll(" ", "");

        String extension = FilenameUtils.getExtension(filename).toLowerCase();
        System.out.println("ext: " + extension);

        if (!extension.matches("png|jpg"))
            throw new SecurityException("file f");


        if (isProPicture) {
            String origName = filename.substring(0, filename.lastIndexOf("."));

            //replace old username  with new one for folder
            filename = filename.replace(origName, username + "_avatar");
        }


        //check if file is not null , it's necessary
        if (!file.isEmpty()) {


            try {

                //if propicture
                InputStream inputStream = file.getInputStream();

                //use standard copy option to delete before if it exists already

                if(isProPicture) {
                    Path userDir = createUserDir(this.rootLocation.toString(), username, null);
                    Files.copy(inputStream, userDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

                }

                else {

                    Path albumDir = createUserDir(this.rootLocation.toString(), username, albumName);

                    Logger.getLogger(StorageServiceImpl.class.toString()).log(Level.INFO,
                            "Album path is created"+ albumDir.toString());

                    Files.copy(file.getInputStream(), albumDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
                }


            } catch (IOException e) {
                e.printStackTrace();
            }//outer if

        }
    }



    @Override
    public String getExtension(Path imagePath) {

        String filename = getFileName(imagePath);

        return filename.substring(filename.lastIndexOf(".") + 1);

    }

    @Override
    public String getFileName(Path imagePath) {

        return Paths.get(imagePath.toUri()).toFile().getName();
    }

    @Override
    public Path getUserPath(String username) {

        return Paths.get(this.rootLocation + File.separator + username);

    }


    //get the user's avatar path
    public Path getAvatar(String username){


        File dir = new File(getUserPath(username).toString());
        Logger.getLogger(StorageService.class.toString()).log(Level.INFO, "user dir: " + dir);

        File[] pictures =  dir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.getName().contains("_avatar");
            }
        });

        if(pictures != null)
            return pictures[0].toPath();
        else
            return null;
    }



    /**
     *
     * @param username
     * @param filename
     * @param albumName
     * @return the image path(url) in the storage directory if it exists or null otherwise
     */


    @Override
    public Path getImagePath(String username, String filename, String albumName) {


        File albumDir =  new File(Paths.get(getUserPath(username).toString() + "/album/"+ albumName).toString());
        Logger.getLogger(StorageService.class.toString()).log(Level.INFO, "album dir: " + albumDir);

        //if file path contains filename -> /uploads/username/filename contains filename return that file
        File[] image =  albumDir.listFiles(pathname -> pathname.toString().contains(filename));

        //we only need the only file that exist in image file array
        if(image != null)
            return image[0].toPath();
        else
            return null;

    }


    /**
     *
     * @param path: the user path
     * @param newUsername: new username that path will be changed to this username
     * @param oldUsername: the one will be replaced with new username
     * @return true if substitution goes well and false otherwise
     */

    @Override
    public boolean updateUserDir(String path, String newUsername, String oldUsername) {

        String newPath = path.replace(oldUsername, newUsername);
        Logger.getLogger(StorageServiceImpl.class.toString()).log(Level.INFO, "user new path: " + newPath );

        try {

            Files.createDirectories(Paths.get(newPath));
            Files.move(Paths.get(path), Paths.get(newPath), StandardCopyOption.REPLACE_EXISTING);

            Files.deleteIfExists(Paths.get(path));

            Files.deleteIfExists(Paths.get(path).getParent());

            return true;


        } catch (IOException e) {
            e.printStackTrace();
            Logger.getLogger(StorageServiceImpl.class.toString()).log(Level.WARNING, "User update path failed");
        }

        return false;

    }


    /**
     *
     * @param root string root location of upload directory
     * @param username string
     * @return null if path cannot be created
     */

    @Override
    public Path createUserDir(String root, String username, String albumName) {

        Path toBeCreated;

        if(albumName != null && !albumName.isEmpty())
          toBeCreated  = Paths.get(root + File.separator + username + "/album" + File.separator + albumName);
        else
            toBeCreated = Paths.get(root + File.separator + username);

        try {

            return Files.createDirectories(toBeCreated);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


}
