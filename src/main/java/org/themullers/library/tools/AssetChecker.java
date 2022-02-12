package org.themullers.library.tools;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Finds assets (not AZW3s) that aren't associated with any books in the library.
 */
public class AssetChecker extends CommandLineTool {

    public void checkAssets() throws Exception {
        var assets = osao.listObjects();
        var books = dao.fetchAllBooks();

        for (var book : books) {
            assets.remove(book.getEpubObjectKey());
            assets.remove(book.getMobiObjectKey());
            assets.remove(book.getAudiobookObjectKey());
        }

        for (var asset : assets) {
            if (!asset.endsWith(".azw3")) {
                System.out.println(asset);
            }
        }
    }

    public AssetChecker() throws IOException, SQLException {
        super();
    }

    public static void main(String[] args) throws Exception {
        new AssetChecker().checkAssets();
    }


}
