package org.themullers.library.web;

import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.themullers.library.Utils;
import org.themullers.library.auth.pwreset.BadPasswordResetTokenException;
import org.themullers.library.auth.pwreset.ExpiredPasswordResetTokenException;
import org.themullers.library.auth.pwreset.PasswordResetService;

import javax.mail.MessagingException;
import java.io.IOException;

@RestController
public class PasswordResetController {

    PasswordResetService pwReset;

    @Autowired
    public PasswordResetController(PasswordResetService pwReset) {
        this.pwReset = pwReset;
    }

    @GetMapping("/pw-reset")
    public ModelAndView displayPasswordResetForm(@RequestParam(value="msg", required=false) String msg) {
        var mv = new ModelAndView("password-reset");
        mv.addObject("msg", msg);
        return mv;
    }

    @PostMapping("/pw-reset")
    public ModelAndView processPasswordResetForm(@RequestParam("email") String email) throws TemplateException, MessagingException, IOException {

        // check to make sure the user provided something that looks like a valid email
        if (!Utils.isValidFormatEmail(email)) {
            var mv = new LibraryModelAndView("/password-reset");
            mv.addObject("msg", "Please enter a valid format email");
            return mv;
        }

        pwReset.sendPasswordResetEmail(email);
        return new ModelAndView("redirect:/pw-reset-email-sent");
    }

    @GetMapping("/pw-reset-email-sent")
    public ModelAndView checkEmailNotification() {
        return new ModelAndView("/check-email");
    }

    @GetMapping("/pw-reset-process-token")
    public ModelAndView processPasswordResetToken(@RequestParam("userid") int userId, @RequestParam("token") String token) {

        try {
            // generate a new password for the user
            var password = pwReset.processPasswordResetToken(token, userId);

            // display the confirmation page
            var mv = new LibraryModelAndView("/password-reset-complete");
            mv.addObject("password", password);
            return mv;

        }
        catch (BadPasswordResetTokenException x) {

            // determine the most meaningful error message
            var msg = "Your password reset failed (bad data).  Try again?";
            if (x instanceof ExpiredPasswordResetTokenException) {
                msg = String.format("You didn't click the link in your email within %d minutes.  Try again?", PasswordResetService.TOKEN_LIFESPAN_MINUTES);
            }

            // send the user back to the "mail me a link to reset my password" page
            var mv = new LibraryModelAndView("redirect:/pw-reset");
            mv.addObject("msg", msg);
            return mv;
        }
    }
}
