package org.themullers.library.email;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * This service applies a model to an email template to generate an Email.
 * If the template is "hello ${recipient} thank you for buying ${product}",
 * and the model is [ recipient: mike, product: donuts ], the email body would
 * be "hello mike thank you for buying donuts".
 */
@Component
public class EmailTemplateProcessor {

    Configuration freemarker;

    @Autowired
    public EmailTemplateProcessor(FreeMarkerConfigurer fmc) {
        this.freemarker = fmc.getConfiguration();
    }

    /**
     * Generate an email from a template and a model.
     * @param emailTemplate  the email template
     * @param model  the model for the information to be substituted into the template
     * @return  an email
     * @throws IOException  thrown if an unexpected error occurs building the email
     * @throws TemplateException  thrown if an unexpected error occurs expanding the email template
     */
    public Email process(EmailTemplate emailTemplate, Map<String, Object> model) throws IOException, TemplateException {
        var email = new Email();

        email.setSubject(processTemplateString(emailTemplate.getSubjectTemplate(), model));
        email.setBody(processTemplateFile(emailTemplate.getBodyTemplatePath(), model));
        email.setMimeType(emailTemplate.getMimeType());

        return email;
    }

    /**
     * Process a template that is provided as a string literal.
     * @param templateString  the template
     * @param model  the model with values that should be applied into the template
     * @return  the string resulting from substituting the model values into the template
     * @throws IOException  thrown if an unexpected error occurs building the template
     * @throws TemplateException  thrown if an unexpected error occurs expanding the template
     */
    protected String processTemplateString(String templateString, Map<String, Object> model) throws IOException, TemplateException {

        var result = "";

        // create a string reader and writer in a try so they get auto-closed
        if (templateString != null) {
            try (var stringReader = new StringReader(templateString); var stringWriter = new StringWriter()) {

                // create a new freemarker template from the provided string and process the template
                var template = new Template("ad-hoc string template", stringReader, freemarker);
                template.process(model, stringWriter);
                result = stringWriter.toString();
            }
        }

        return result;
    }

    /**
     * Process a template that is provided in a file.
     * @param templatePath  the path to the template (relative to maven's src/main/resources)
     * @param model  the model with values that should be applied into the template
     * @return  the string resulting from substituting the model values into the template
     * @throws IOException  thrown if an unexpected error occurs building the template
     * @throws TemplateException  thrown if an unexpected error occurs expanding the template
     */
    protected String processTemplateFile(String templatePath, Map<String, Object> model) throws IOException, TemplateException {

        var result = "";

        // create a string writer in a try so it gets auto-closed
        if (templatePath != null) {
            try (var stringWriter = new StringWriter()) {

                // get the freemarker template at the indicated path
                var template = freemarker.getTemplate(templatePath);
                if (template != null) {

                    // process the template
                    template.process(model, stringWriter);
                    result = stringWriter.toString();
                }
            }
        }

        return result;
    }
}
