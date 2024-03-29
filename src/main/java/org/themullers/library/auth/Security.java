package org.themullers.library.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Use the Spring Security framework to configure which users
 * can access which pages.
 */
@EnableWebSecurity
public class Security extends WebSecurityConfigurerAdapter {

    public final static GrantedAuthority ADMIN_AUTHORITY = new SimpleGrantedAuthority("ROLE_admin");

    LibraryUserDetailsService userDetailsService;

    @Autowired
    public Security(LibraryUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {

        // configure access to the various sections of the web site
        http.authorizeRequests(conf -> {

            // you don't need to be logged in to get to the login or "forgot pw/pw reset" pages
            conf.antMatchers("/pw*", "/login*").permitAll();

            // also allow everyone access to the static resources
            conf.antMatchers("/styles/**", "/images/**").permitAll();

            // free access to the amazon data collection endpoint

            /*

            commented out because disabling CSRF for the amazon endpoints seems to disable it for all endpoints.
            maybe try http.csrf.ignoringAntMatchers() as indicated here: https://stackoverflow.com/a/34994299

            try {
                conf.antMatchers(HttpMethod.POST, "/api/amazon").permitAll().and().csrf().disable();
                conf.antMatchers("/api/amazon").permitAll().and().csrf().disable();
            }
            catch (Exception x) {
                x.printStackTrace(System.err);
            }
             */

            // require an administrator to access these pages (and REST API endpoints)
            conf.antMatchers("/admin/**", "/api/**").hasAnyRole("admin");

            // everything else requires you to be logged in
            conf.anyRequest().authenticated();
        });

        // configure sign-in
        http.formLogin(conf -> {

            // use our custom login page
            conf.loginPage("/login");

            // indicate which page should be displayed after login
            conf.defaultSuccessUrl("/", false);

            // allow everyone to access the login page
            conf.permitAll();
        });

        // remember users when they come back to the site
        http.rememberMe(conf -> {
            conf.alwaysRemember(true);
            conf.key("library-remember-key");
        });
    }

    @Override
    public void configure(AuthenticationManagerBuilder builder) throws Exception {
        builder.userDetailsService(userDetailsService);
    }
}
