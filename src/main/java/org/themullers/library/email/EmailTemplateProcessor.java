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

@Component
public class EmailTemplateProcessor {

    Configuration freemarker;

    @Autowired
    public EmailTemplateProcessor(FreeMarkerConfigurer fmc) {
        this.freemarker = fmc.getConfiguration();
    }

    public Email process(EmailTemplate emailTemplate, Map<String, Object> model) throws IOException, TemplateException {
        var email = new Email();

        email.setSubject(processTemplateString(emailTemplate.getSubjectTemplate(), model));
        email.setBody(processTemplateFile(emailTemplate.getBodyTemplatePath(), model));
        email.setMimeType(emailTemplate.getMimeType());

        return email;
    }

    protected String processTemplateString(String templateString, Map<String, Object> model) throws IOException, TemplateException {

        var result = "";

        // create a string reader and writer in a try so they get auto-closed
        if (templateString != null) {
            try (var stringReader = new StringReader(templateString); var stringWriter = new StringWriter()) {

                // create a new freemarker template from the provided string and process the template
                var template = new Template("template for subject line of email", stringReader, freemarker);
                template.process(model, stringWriter);
                result = stringWriter.toString();
            }
        }

        return result;
    }

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
