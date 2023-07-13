package org.themullers.library.tools;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.themullers.library.auth.PasswordGenerator;

import java.io.IOException;
import java.sql.SQLException;

public class PasswordSetter extends CommandLineTool {

    public void setPassword() {
        var pwGenerator = new PasswordGenerator();
        var password = pwGenerator.generate(10, true, false, true, false);
        System.out.println(password);
        var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        var encryptedPw = encoder.encode(password);
        dao.setPassword(10000, encryptedPw);
    }

    public PasswordSetter() throws IOException, SQLException {
    }

    public static void main(String[] args) throws Exception {
        new PasswordSetter().setPassword();
    }
}
