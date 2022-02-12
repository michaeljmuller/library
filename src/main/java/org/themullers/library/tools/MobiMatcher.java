package org.themullers.library.tools;

import org.themullers.library.LibUtils;

import java.io.IOException;
import java.sql.SQLException;

/**
 * For each book that has no MOBI, try to find the MOBI in the object store.
 */
public class MobiMatcher extends CommandLineTool {

    public void matchMobis() {
        var objectKeys = osao.listObjects();
        var books = dao.fetchAllBooks();

        for (var book : books) {
            if (book.getMobiObjectKey() == null) {
                var epub = book.getEpubObjectKey();
                logger.info("book has no MOBI: " + epub);
                var mobi = LibUtils.matchingMobi(epub);
                logger.info("looking for MOBI: " + mobi);
                if (objectKeys.contains(mobi)) {
                    logger.info("we have a MOBI for that book!");
                    book.setMobiObjectKey(mobi);
                    dao.updateBook(book);
                }
                else {
                    logger.warn("no mobi :(");
                }
            }
        }
    }

    public MobiMatcher() throws IOException, SQLException {
        super();
    }

    public static void main(String[] args) throws Exception {
        new MobiMatcher().matchMobis();
    }

}
