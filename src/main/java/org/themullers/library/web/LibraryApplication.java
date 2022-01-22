package org.themullers.library.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "org.themullers.library" })
public class LibraryApplication {

    /**
     * This is the main entry point for the application.  Any arguments are passed
     * through to Spring Boot.
     *
     * @param args parameters provided on the command line when this application was launched
     */
    public static void main(String args[]) {
        SpringApplication.run(LibraryApplication.class, args);
    }

}
