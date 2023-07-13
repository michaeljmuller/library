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

        // configure ebedded tomcat to allow encoded slashes
        // this is to support urls like this:
        // https://myhost.com/tag/Award%20Winning%20%2F%20Nominated
        System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");

        SpringApplication.run(LibraryApplication.class, args);
    }
}
