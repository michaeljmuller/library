package org.themullers.library.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

/**
 * Finds books in my calibre repository that aren't in the object store and uploads them.
 */
public class BookUploader extends CommandLineTool {

    public void uploadBooks() throws IOException {

        var objKeys = osao.listObjects();
        for (var key : objKeys) {
            logger.info("in object store: " + key);
        }

        // walk through all the documents in the calibre directory
        var rootDir = "/Users/mmuller/Documents/Calibre";
        Files.walk(Path.of(rootDir))
                .forEach(p -> {
                    var filename = p.getFileName().toString();
                    var lcFilename = filename.toLowerCase();

                    if ((lcFilename.endsWith(".epub") || lcFilename.endsWith(".mobi")) && !objKeys.contains(filename)) {
                        logger.info("found file to upload: " + filename);
                        osao.uploadObject(p.toFile());
                    }

                });
    }

    public BookUploader() throws IOException, SQLException {
        super();
    }

    public static void main(String[] args) throws Exception {
        new BookUploader().uploadBooks();
    }
}
