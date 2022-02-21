package org.themullers.library.tools;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Check for missing MOBI and/or EPUB, asset reference not in S3, etc.
 */
public class BookChecker extends CommandLineTool {

    public void execute() throws Exception {

        var assets = osao.listObjects();

        for (var book : dao.fetchAllBooks()) {

            var epub = book.getEpubObjectKey();
            var mobi = book.getMobiObjectKey();
            var audio = book.getAudiobookObjectKey();
            var hasCoverImage = dao.hasCoverImage(book.getId());
            var pubYear = book.getPublicationYear();

            // no epub specified in database
            if (epub == null) {
                logger.error("missing EPUB: " + book.getTitle());
            }

            // specified epub not in S3
            else {
                if (!assets.contains(epub)) {
                    logger.error("missing EPUB asset (book " + book.getId() + "): " + epub);
                }
            }

            // no mobi specified in database
            if (mobi == null) {
                logger.error("missing MOBI: " + book.getTitle());
            }

            // specified mobi not in s3
            else {
                if (!assets.contains(mobi)) {
                    logger.error("missing MOBI asset (book " + book.getId() + "): " + mobi);
                }
            }

            // audiobook specified, but not in s3
            if (audio != null && !assets.contains(audio)) {
                logger.error("missing audiobook asset (book " + book.getId() + "): " + audio);
            }

            if (!hasCoverImage) {
                logger.error("cover image missing for book " + book.getId() + " (" + epub + ")");
            }

            if (pubYear < 1000) {
                logger.error("bad publication year (" + pubYear + ") for book " + book.getId() + " (" + epub + ")");
            }
        }

        for (var epub : dao.epubsInMultipleBooks()) {
            logger.error("epub is used by more than one book: " + epub);
        }

        for (var mobi : dao.mobisInMultipleBooks()) {
            logger.error("epub is used by more than one book: " + mobi);
        }

        for (var audiobook : dao.audiobooksInMultipleBooks()) {
            logger.error("audiobook is used by more than one book: " + audiobook);
        }
    }

    public BookChecker() throws IOException, SQLException {
        super();
    }

    public static void main(String[] args) throws Exception {
        new BookChecker().execute();
    }
}
