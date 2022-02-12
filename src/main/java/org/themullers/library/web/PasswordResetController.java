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

/**
 * Handles requests from the user related to resetting a forgotten password.
 *
 * The process flow goes like this:  User clicks "I forgot my pw" link on the login page,
 * system displays form where they can enter their email.  User submits email, system
 * sends them an email and displays the "check your email" page.  User clicks a link
 * in the email, system resets the password and displays a page saying "make a note
 * of your new password and then use it to log in".
 */
@RestController
public class PasswordResetController {

    PasswordResetService pwReset;

    @Autowired
    public PasswordResetController(PasswordResetService pwReset) {
        this.pwReset = pwReset;
    }

    /**
     * Prompts the user to enter their email so we can send them their password.
     * @param msg  an optional message to be displayed on top of the page
     * @return  a view object containing the template that should be used to render the "book details" page
     */
    @GetMapping("/pw-reset")
    public ModelAndView displayPasswordResetForm(@RequestParam(value="msg", required=false) String msg) {
        var mv = new ModelAndView("password-reset");
        mv.addObject("msg", msg);
        return mv;
    }

    /**
     * Process the submitted "I forgot my email" form.
     * If the user enters a valid email address, they are redirected to the "password
     * reset email has been sent" page.  Otherwise, this page is rendered again
     * with an error message on top.
     * @param email  the email address that was entered in the form
     * @return  a redirect or the view for this page
     * @throws TemplateException  if an unexpected error occurs processing the template for the email
     * @throws MessagingException  if an unexpected email error occurs sending the email
     * @throws IOException  if an unexpected IO error occurs sending the email
     */
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

    /**
     * Renders a page informing the user that we've sent them an email
     * with a link they can use to reset their email.
     * @return  a view object containing the template that should be used to render the "check your email" page
     */
    @GetMapping("/pw-reset-email-sent")
    public ModelAndView checkEmailNotification() {
        return new ModelAndView("/check-email");
    }

    /**
     * This page is rendered when the user clicks a link in their email to reset
     * their password; it generates a new password and displays that password to the user.
     * @param userId  the id of the user for whom we should generate a new password
     * @param token  a token that we sent to the user's email
     * @return  a view object containing the template that should be used to render the "your email has been reset" page
     */
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
