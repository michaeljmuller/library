package org.themullers.library.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.themullers.library.db.LibraryDAO;

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
