package org.themullers.library.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Service to send emails.
 */
@Component
public class EmailSender {

    public final String EMAIL_SENDER = "mike@themullers.org";

    protected String host;
    protected String port;
    protected String useAuthentication;
    protected String enableStartTLS;
    protected String username;
    protected String password;

    public void sendEmail(Email email) throws MessagingException {

        // configure
        var mailProps = new Properties();
        mailProps.put("mail.smtp.host", getHost());
        mailProps.put("mail.smtp.port", getPort());
        mailProps.put("mail.smtp.auth", getUseAuthentication());
        mailProps.put("mail.smtp.starttls.enable", getEnableStartTLS());

        // authenticate
        var session = Session.getInstance(mailProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(getUsername(), getPassword());
            }
        });

        // build the message object
        var message = new MimeMessage(session);
        message.setFrom(EMAIL_SENDER);
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email.getTo()));
        message.setSubject(email.getSubject());
        message.setContent(email.getBody(), email.getMimeType());

        // send the message
        Transport.send(message);
    }

    // ACCESSOR METHODS

    public String getHost() {
        return host;
    }

    @Value("${mail.smtp.host}")
    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    @Value("${mail.smtp.port}")
    public void setPort(String port) {
        this.port = port;
    }

    public String getUseAuthentication() {
        return useAuthentication;
    }

    @Value("${mail.smtp.auth}")
    public void setUseAuthentication(String useAuthentication) {
        this.useAuthentication = useAuthentication;
    }

    public String getEnableStartTLS() {
        return enableStartTLS;
    }

    @Value("${mail.smtp.starttls.enable}")
    public void setEnableStartTLS(String enableStartTLS) {
        this.enableStartTLS = enableStartTLS;
    }

    public String getUsername() {
        return username;
    }

    @Value("${mail.smtp.username}")
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    @Value("${mail.smtp.password}")
    public void setPassword(String password) {
        this.password = password;
    }
}
