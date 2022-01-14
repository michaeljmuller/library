package org.themullers.library.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.themullers.library.User;

import java.util.Collection;
import java.util.LinkedList;

public class LibraryUserDetails extends User implements UserDetails {

    public LibraryUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        var authorities = new LinkedList<GrantedAuthority>();
        if ("mike@themullers.org".equals(email)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_admin"));
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
