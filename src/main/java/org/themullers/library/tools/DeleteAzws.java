package org.themullers.library.tools;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Delete all the AZWs from the object store.
 */
public class DeleteAzws extends CommandLineTool {

    public void execute() {
        var objects = osao.listObjects();
        for (var obj : objects) {
            if (obj.endsWith(".azw3")) {
                logger.info("deleting " + obj);
                osao.deleteObject(obj);
            }
        }
    }

    public DeleteAzws() throws IOException, SQLException {
        super();
    }

    public static void main(String[] args) throws Exception {
        new DeleteAzws().execute();
    }
}
