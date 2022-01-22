package org.themullers.library;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.themullers.library.db.LibraryDAO;
import org.themullers.library.web.LibraryApplication;

@SpringBootTest(classes = LibraryApplication.class)
class LibraryApplicationTests {

    @Autowired
    LibraryDAO dao;

    @Test
    void contextLoads() {
        // this is disabled until i set up a test database
        /*
        dao.storePasswordResetToken(1, "foo");
        var tokenInfo = dao.fetchPasswordResetTokenForUser(1);
        assert("foo".equals(tokenInfo.token));
         */
    }

}
