package org.aksw.dcat_suite.app.vaadin.view.system;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Adds a link that the user has to click to login.
 *
 * This view is marked with {@code @AnonymousAllowed} to allow all users access
 * to the login page.
 */
@Route("login")
@PageTitle("Login")
// @AnonymousAllowed
@PermitAll
public class LoginView extends VerticalLayout {

    /**
     * URL that Spring uses to connect to Google services
     */
    private static final String OAUTH_URL = "/oauth2/authorization/gitlab"; // "/oauth2/authorization/gitlab";

    public LoginView(@Autowired Environment env) {
        setPadding(true);
        setAlignItems(Alignment.CENTER);

        String clientkey = env.getProperty("spring.security.oauth2.client.registration.gitlab.client-secret");

        // Check that oauth keys are present
        if (clientkey == null || clientkey.isEmpty() || clientkey.length() < 32) {
            Paragraph text = new Paragraph("Could not find OAuth client key in application.properties. "
                    + "Please double-check the key and refer to the README.md file for instructions.");
            text.getStyle().set("padding-top", "100px");
            add(text);
        } else {
            Anchor loginLink = new Anchor(OAUTH_URL, "Login with Gitlab");
            // Set router-ignore attribute so that Vaadin router doesn't handle the login request
            loginLink.getElement().setAttribute("router-ignore", true);
            loginLink.getStyle().set("margin-top", "100px");
            add(loginLink);
        }
    }
}