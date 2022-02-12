package org.themullers.library.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

/**
 * Uploads cover images from the Calibre repository to the library database for
 * books that are missing cover images.
 */
public class CoverUploader extends CommandLineTool {

    public void uploadCovers() throws IOException {

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

    public CoverUploader() throws IOException, SQLException {
        super();
    }

    public static void main(String[] args) throws Exception {
        new CoverUploader().uploadCovers();
    }
}
