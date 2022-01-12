package org.themullers.library.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;
import org.themullers.library.db.LibraryDAO;

@Service
public class Authenticator implements AuthenticationManager {

    LibraryDAO dao;

    @Autowired
    public Authenticator(LibraryDAO dao) {
        this.dao = dao;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var un = authentication.getPrincipal();
        var pw = authentication.getCredentials();
        var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        var auth = new UsernamePasswordAuthenticationToken(un, pw, authentication.getAuthorities());

        if (!"mike".equals(authentication.getPrincipal())) {
            throw new BadCredentialsException("go away");
        }

        return auth;
    }

    public static void main(String args[]) {
        var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        var plainPw = args[0];
        var encryptedPw = encoder.encode(plainPw);
        System.out.println(String.format("'%s' encoded is: %s", plainPw, encryptedPw));
    }
}
