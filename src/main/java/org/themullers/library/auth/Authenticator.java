package org.themullers.library.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;
import org.themullers.library.db.LibraryDAO;

import java.util.LinkedList;

/**
 * Work within the Spring Security framework to authenticate login requests.
 */
@Service
public class Authenticator implements AuthenticationManager {

    public static final String BAD_PW = "Unknown user or password";

    LibraryDAO dao;

    @Autowired
    public Authenticator(LibraryDAO dao) {
        this.dao = dao;
    }

    /**
     * Authenticate a login request.
     * @param authentication  the credentials provided at login
     * @return  an authentication token
     * @throws AuthenticationException  if the credentials provided are invalid
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var email = authentication.getPrincipal();
        var password = authentication.getCredentials();
        var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        // get info about the user whose email address matches the one entered in the login dialog
        var user = dao.fetchUser(email.toString());

        // if we can't find a user with a matching password, reject the login
        if (user == null) {
            throw new BadCredentialsException(BAD_PW);
        }

        // if the passwords don't match, reject the login
        if (!encoder.matches(password.toString(), user.getPassword())) {
            throw new BadCredentialsException(BAD_PW);
        }

        // if the user is me, grant administrative authority
        var authorities = new LinkedList<GrantedAuthority>();
        if ("mike@themullers.org".equals(email)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_admin"));
        }

        // build an authentication token for use
        var authToken = new UsernamePasswordAuthenticationToken(email, password, authorities);
        authToken.setDetails(user);
        return authToken;
    }
}
