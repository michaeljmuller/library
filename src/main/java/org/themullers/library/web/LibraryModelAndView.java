package org.themullers.library.web;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.themullers.library.User;

public class LibraryModelAndView extends ModelAndView {
    public LibraryModelAndView(String view) {
        super(view);

        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof User user) {
            addObject("user", user);
        }
    }
}
