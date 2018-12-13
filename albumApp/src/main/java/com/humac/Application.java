package com.humac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

@EnableAutoConfiguration
@ComponentScan(basePackages = "com.humac.album")
@PropertySource("application.properties")
public class Application {

    public static void main(String[] args) {
		SpringApplication.run(Application.class, args);


		//create upload directory when application starts
		//uploads is the name of upload directory
		Path uploads = Paths.get("uploads");


		try {
			Files.createDirectories(uploads);

		} catch (IOException e) {
			Logger.getLogger(Application.class.toString()).log(Level.WARNING, "Upload directory is not created");
			e.printStackTrace();
		}
	}
}
