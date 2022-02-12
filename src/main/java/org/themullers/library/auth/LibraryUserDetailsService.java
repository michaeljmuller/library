package org.themullers.library.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.themullers.library.db.LibraryDAO;

/**
 * This is needed to make the "remember me" feature work; when I return
 * from an expired session, I don't need to log in again.
 */
@Service
public class LibraryUserDetailsService implements UserDetailsService {

    protected LibraryDAO dao;

    @Autowired
    public LibraryUserDetailsService(LibraryDAO dao) {
        this.dao = dao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new LibraryUserDetails(dao.fetchUser(username));
    }
}
