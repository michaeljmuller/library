package org.themullers.library.auth.pwreset;

import java.util.Date;

/**
 * When the user requests to reset their password, the system sends a randomly
 * generated token to their email.  The user needs to submit that token back to
 * the system to reset their password to prove it's really them (or at least someone
 * with access to their email).
 */
public class PasswordResetToken {
    protected int userId;  // the user to whom the token was issued
    protected String token;  // the token
    protected Date creationTime;  // the time when the token was issued

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }
}
