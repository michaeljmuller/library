package org.themullers.library.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.themullers.library.User;

import java.util.Collection;
import java.util.LinkedList;

/**
 * This class implementing UserDetails is necessary for use with the
 * LibraryUserDetailsService, which in turn is necessary to support
 * the "remember me" feature that keeps you from having to log in
 * if you visit the site again from the the same browser even after a while.
 */
public class LibraryUserDetails extends User implements UserDetails {

    public LibraryUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
    }

    /**
     * Get the authorities granted to this user.
     * Specifically, if the user is me, grant "admin" authority.
     * TODO: resolve duplicate logic shared with Authenticator.authenticate()
     * @return
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        var authorities = new LinkedList<GrantedAuthority>();
        if ("mike@themullers.org".equals(email)) {
            authorities.add(Security.ADMIN_AUTHORITY);
        }
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
