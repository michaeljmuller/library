package org.themullers.library;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.themullers.library.db.LibraryDAO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;

public class CoverUploader {
    public static void main(String[] args) throws Exception {

        // turn down the root logging
        var root = (Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.ERROR);

        // set OUR logging at "info"
        var logger = (Logger) org.slf4j.LoggerFactory.getLogger("org.themullers.library.CoverUpdater");
        logger.setLevel(Level.INFO);

        // get a data access object
        var url = "jdbc:mariadb://themullers.org:3306/new_library?user=library&password=library";
        var connection = DriverManager.getConnection(url);
        var jt = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
        var dao = new LibraryDAO(jt);

        // walk through all the documents in the calibre directory
        var rootDir = "/Users/mmuller/Documents/Calibre";
        Files.walk(Path.of(rootDir))
                // find just the cover images
                .filter(p -> p.getFileName().toString().equals("cover.jpg"))
                .forEach(p -> {

                    // look at all the files in the same directory as the cover image to find its epub
                    var coverImageFile = p.toFile();
                    var parentDir = coverImageFile.getParentFile();
                    String epub = null;
                    for (var file : parentDir.list()) {
                        if (file.endsWith(".epub")) {
                            epub = file;
                            break;
                        }
                    }

                    // bail if we couldn't find the book associated with this cover image
                    if (epub == null) {
                        logger.error("can't find the epub for " + p);
                        return;
                    }

                    // find the library database entry for this book
                    var bookId = dao.fetchBookIdForEpub(epub);
                    if (bookId == null) {
                        logger.error("epub not found in DB: " + epub);
                        return;
                    }

                    // skip uploading cover images for books that already have a cover image
                    if (dao.hasCoverImage(bookId)) {
                        logger.info("skipping because cover image already exists: " + epub);
                        return;
                    }

                    // upload the cover image
                    try {
                        dao.insertCoverImage(bookId, "cover.jpg", "image/jpeg", Files.readAllBytes(p));
                    }
                    catch (IOException x) {
                        logger.error("failure uploading cover image", x);
                    }

                    logger.info("uploaded cover image for " + epub);
                });
    }
}
