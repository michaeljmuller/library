package org.themullers.library.auth;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class Security extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {

        // configure access to the various sections of the web site
        http.authorizeRequests(conf -> {

            // you don't need to be logged in to get to the login or "forgot pw/pw reset" pages
            conf.antMatchers("/pw*", "/login*").permitAll();

            // everything else requires you to be logged in
            conf.anyRequest().authenticated();
        });

        // configure sign-in
        http.formLogin(conf -> {

            // use our custom login page
            conf.loginPage("/login");

            // indicate which page should be displayed after login
            conf.defaultSuccessUrl("/", true);

            // allow everyone to access the login page
            conf.permitAll();
        });

        // configure logout
        http.logout().logoutSuccessUrl("/login?logout");
    }
}
