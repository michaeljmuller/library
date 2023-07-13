package org.themullers.library.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
public class FirewallConfiguration {

    /**
     * Configure firewall to allow url-encoded slashes.
     *
     * This is to support URLs like this one:
     * https://myhost.com/tag/Award%20Winning%20%2F%20Nominated
     *
     * @return the firewall configuration object
     */
    @Bean
    public HttpFirewall configureFirewall() {
        var fw = new StrictHttpFirewall();
        fw.setAllowUrlEncodedSlash(true);
        return fw;
    }
}
