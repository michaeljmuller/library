package org.themullers.library.web;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.themullers.library.User;
import org.themullers.library.auth.Security;

public class LibraryModelAndView extends ModelAndView {
    public LibraryModelAndView(String view) {
        super(view);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var principal = authentication.getPrincipal();
        var isAdmin = authentication.getAuthorities().contains(Security.ADMIN_AUTHORITY);

        if (principal instanceof User user) {
            addObject("user", user);
        }

        addObject("isAdmin", isAdmin);
    }
}
