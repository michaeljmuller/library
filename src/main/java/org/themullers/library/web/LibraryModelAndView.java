package org.themullers.library.web;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.themullers.library.User;
import org.themullers.library.auth.Security;

/**
 * Base class for ModelAndView objects returned by this application's controller methods.
 */
public class LibraryModelAndView extends ModelAndView {

    public LibraryModelAndView(String view) {
        super(view);

        // get information about the logged-in user
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var principal = authentication.getPrincipal();
        var isAdmin = authentication.getAuthorities().contains(Security.ADMIN_AUTHORITY);

        // add the user object
        if (principal instanceof User user) {
            addObject("user", user);
        }

        // add an indicator for whether the logged-in user is an administrator
        addObject("isAdmin", isAdmin);
    }
}
