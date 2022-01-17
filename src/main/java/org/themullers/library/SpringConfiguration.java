package org.themullers.library;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.ajp.AjpNioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfiguration {
    /**
     * Configure the embedded tomcat server so that it listens for the AJP protocol
     * on port 8009.  Don't require a secret.  This is less secure, but I could not
     * get AJP secrets working with Apache 2.4.51.  The firewall should protect the
     * application.
     *
     * @return an embeded tomcat server
     */
    @Bean
    public ServletWebServerFactory servletContainer() {

        // create an AJP connector on the default port (8192)
        Connector ajpConnector = new Connector("AJP/1.3");
        ajpConnector.setPort(8009);
        ajpConnector.setSecure(false);
        ajpConnector.setScheme("http");
        ajpConnector.setAllowTrace(false);

        // configure the AJP secret
        var protocol = (AjpNioProtocol) ajpConnector.getProtocolHandler();
        protocol.setSecretRequired(false);

        // add the AJP connector to the default web server
        var tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(ajpConnector);
        return tomcat;
    }
}
