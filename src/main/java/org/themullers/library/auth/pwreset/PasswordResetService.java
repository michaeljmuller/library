package org.themullers.library.auth.pwreset;

import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;
import org.themullers.library.auth.PasswordGenerator;
import org.themullers.library.db.LibraryDAO;
import org.themullers.library.email.EmailSender;
import org.themullers.library.email.EmailTemplate;
import org.themullers.library.email.EmailTemplateProcessor;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

/**
 * Service to support administration of tokens that track users' requests to reset their passwords.
 */
@Service
public class PasswordResetService {

    public final static int TOKEN_LIFESPAN_MINUTES = 15;

    protected LibraryDAO dao;
    protected PasswordGenerator pwGenerator;
    protected EmailSender emailSender;
    protected EmailTemplateProcessor emailTemplateProcessor;
    protected String applicationBaseURL;

    @Autowired
    public PasswordResetService(LibraryDAO dao, PasswordGenerator pwGenerator, EmailSender emailSender, EmailTemplateProcessor emailTemplateProcessor, @Value("${application.base.url}") String applicationBaseURL) {
        this.dao = dao;
        this.pwGenerator = pwGenerator;
        this.emailSender = emailSender;
        this.emailTemplateProcessor = emailTemplateProcessor;
        this.applicationBaseURL = applicationBaseURL;
    }

    /**
     * Validate a password reset token and, if the token is valid, reset the user's password.
     * @param token  the token submitted with the password reset request
     * @param userId  the id of the user requesting the password reset
     * @return  the new password
     * @throws BadPasswordResetTokenException  thrown if the provided is not a valid token
     * @throws ExpiredPasswordResetTokenException  thrown if user has taken too long to use the token to reset their password
     */
    public String processPasswordResetToken(String token, int userId) throws BadPasswordResetTokenException, ExpiredPasswordResetTokenException {
        var tokenInfo = dao.fetchPasswordResetTokenForUser(userId);
        var user = dao.fetchUser(userId);

        // check for bad data
        if (tokenInfo == null || user == null || user.getId() != userId || !token.equals(tokenInfo.getToken())) {
            throw new BadPasswordResetTokenException();
        }

        // check for expired token
        var tokenTimeMillis = tokenInfo.getCreationTime().getTime();
        var tokenAgeMillis = System.currentTimeMillis() - tokenTimeMillis;
        var tokenLifespanMillis = TOKEN_LIFESPAN_MINUTES * 60000;
        if (tokenAgeMillis > tokenLifespanMillis) {
            throw new ExpiredPasswordResetTokenException();
        }

        // reset the user's password
        var password = pwGenerator.generate(10, true, false, true, false);
        var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        var encryptedPw = encoder.encode(password);
        dao.setPassword(userId, encryptedPw);

        // remove the token so it can't be reused
        dao.deletePasswordResetToken(user.getId());

        // display the confirmation page
        return password;
    }

    /**
     * Send an email to a user with a link that they can use to reset their password.
     * @param emailAddress  the user's email
     * @throws IOException  thrown if there is an error writing the email message
     * @throws TemplateException  thrown if there is an error creating the email message
     * @throws MessagingException  thrown if there is an error sending the email message
     */
    public void sendPasswordResetEmail(String emailAddress) throws IOException, TemplateException, MessagingException {

        // fetch the user account associated with this email
        var user = dao.fetchUser(emailAddress);
        if (user == null) {

            // if there is no user account associated with this email, just quietly do nothing
            return;
        }

        // generate a simple 6-character token
        var token = pwGenerator.generate(6, false, true, true, false);

        // save this password reset token
        dao.storePasswordResetToken(user.getId(), token);

        // calculate token issue time and expiration times
        var issueDate = new Date();
        var expirationDate = new Date(issueDate.getTime() + (TOKEN_LIFESPAN_MINUTES * 60000));

        // collect information needed to render the "reset password" email
        var model = new HashMap<String, Object>();
        model.put("user", user);
        model.put("token", token);
        model.put("tokenIssueDateTime", issueDate);
        model.put("tokenExpirationDateTime", expirationDate);
        model.put("tokenLifespan", TOKEN_LIFESPAN_MINUTES);
        model.put("applicationBaseURL", applicationBaseURL);

        // render and send the "reset password" email
        var email = emailTemplateProcessor.process(EmailTemplate.RESET_PASSWORD, model);
        email.setTo(emailAddress);
        emailSender.sendEmail(email);
    }
}
