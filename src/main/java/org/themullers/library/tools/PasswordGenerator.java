package org.themullers.library.tools;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;

/**
 * Generates a new password.
 */
public class PasswordGenerator {
    public static void main(String args[]) {
        var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        var plainPw = args[0];
        var encryptedPw = encoder.encode(plainPw);
        System.out.println(String.format("'%s' encoded is: %s", plainPw, encryptedPw));
    }
}
