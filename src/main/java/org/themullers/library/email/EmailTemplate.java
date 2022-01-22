package org.themullers.library.email;

public class EmailTemplate {

    public static final EmailTemplate RESET_PASSWORD = new EmailTemplate("Password Reset for Mike's eBook Library", "email/forgot-password-email.ftl", "text/html");

    protected String subjectTemplate;
    protected String bodyTemplatePath;
    protected String mimeType = "text/plain";

    public EmailTemplate(String subjectTemplate, String bodyTemplatePath) {
        this.subjectTemplate = subjectTemplate;
        this.bodyTemplatePath = bodyTemplatePath;
    }

    public EmailTemplate(String subjectTemplate, String bodyTemplatePath, String mimeType) {
        this(subjectTemplate, bodyTemplatePath);
        this.mimeType = mimeType;
    }

    // ACCESSOR METHODS

    public String getSubjectTemplate() {
        return subjectTemplate;
    }

    public void setSubjectTemplate(String subjectTemplate) {
        this.subjectTemplate = subjectTemplate;
    }

    public String getBodyTemplatePath() {
        return bodyTemplatePath;
    }

    public void setBodyTemplatePath(String bodyTemplatePath) {
        this.bodyTemplatePath = bodyTemplatePath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
