package org.themullers.library.tools;

import java.io.IOException;
import java.sql.SQLException;

public class BookChecker extends CommandLineTool {

    public void execute() throws Exception {
        for (var book : dao.fetchAllBooks()) {
            if (book.getMobiObjectKey() == null) {
                logger.info("missing MOBI: " + book.getTitle());
            }
        }
    }

    public BookChecker() throws IOException, SQLException {
        super();
    }

    public static void main(String[] args) throws Exception {
        new BookChecker().execute();
    }
}
