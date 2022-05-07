package org.aksw.dcat_suite.app.session;

import java.io.Serializable;

import org.aksw.jenax.model.foaf.domain.api.FoafOnlineAccount;
import org.aksw.jenax.model.foaf.domain.api.FoafPerson;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
public class UserSession implements Serializable {

    public FoafOnlineAccount getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticatedPrincipal principal = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();

        Model model = ModelFactory.createDefaultModel();

        FoafPerson agent = model.createResource().as(FoafPerson.class)
            .setFamilyName(principal.getAttribute("family_name"))
            .setMbox(principal.getAttribute("email"))
            .setName(principal.getAttribute("username"))
            .setDepiction(principal.getAttribute("avatar_url"))
            .asFoafPerson()
            ;

        FoafOnlineAccount result = model.createResource().as(FoafOnlineAccount.class)
            .setAccountName(principal.getAttribute("username"))

            // Is this a good fit for web_url? Value may be e.g. https://gitlab.com/Aklakan
            .setAccountServiceHomepage(principal.getAttribute("web_url"))
            ;

        result.setOwner(agent);

        return result;
    }

    public boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null;
    }
}