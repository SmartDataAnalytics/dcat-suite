package org.aksw.dcat_suite.app.config;

import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import com.vaadin.flow.server.ServletHelper;
import com.vaadin.flow.shared.ApplicationConstants;

/**
 * Configures Spring Security, doing the following:
 * <li>Bypass security checks for static resources,</li>
 * <li>Restrict access to the application, allowing only logged in users,</li>
 * <li>Set up the login form,</li>
 */
@EnableWebSecurity
@Configuration
public class ConfigDmanSecurity extends WebSecurityConfigurerAdapter {

    private static final String LOGIN_URL = "/login";
    private static final String LOGOUT_URL = "/logout";
    private static final String LOGOUT_SUCCESS_URL = "/";

    /**
     * Registers our UserDetailsService and the password encoder to be used on
     * login attempts.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http

            // Allow all flow internal requests.
            .authorizeRequests().requestMatchers(ConfigDmanSecurity::isFrameworkInternalRequest).permitAll()

            // Restrict access to our application.
            .and().authorizeRequests().anyRequest().authenticated()

            // Not using Spring CSRF here to be able to use plain HTML for the login page
            .and().csrf().disable()

            // Configure logout
            .logout().logoutUrl(LOGOUT_URL).logoutSuccessUrl(LOGOUT_SUCCESS_URL)

            // Configure the login page.
            .and().oauth2Login().loginPage(LOGIN_URL).permitAll();
        // @formatter:on
    }

    /**
     * Allows access to static resources, bypassing Spring Security.
     */
    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers(
                // client-side JS code
                "/VAADIN/**",

                // the standard favicon URI
                "/favicon.ico",

                // web application manifest
                "/manifest.webmanifest", "/sw.js", "/offline-page.html",

                // icons and images
                "/icons/**", "/images/**");
    }

    /**
     * Tests if the request is an internal framework request. The test consists
     * of checking if the request parameter is present and if its value is
     * consistent with any of the request types know.
     *
     * @param request
     *            {@link HttpServletRequest}
     * @return true if is an internal framework request. False otherwise.
     */
    static boolean isFrameworkInternalRequest(HttpServletRequest request) {
        final String parameterValue = request
                .getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER);
        return parameterValue != null
                && Stream.of(ServletHelper.RequestType.values()).anyMatch(
                        r -> r.getIdentifier().equals(parameterValue));
    }
}