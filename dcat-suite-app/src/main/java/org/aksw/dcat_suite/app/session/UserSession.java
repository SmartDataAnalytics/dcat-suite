package org.aksw.dcat_suite.app.session;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

import javax.annotation.Resource;

import org.aksw.commons.util.string.StringUtils;
import org.aksw.dcat_suite.app.model.api.GroupMgr;
import org.aksw.dcat_suite.app.model.api.GroupMgrFactory;
import org.aksw.dcat_suite.app.model.api.SystemSpace;
import org.aksw.dcat_suite.app.model.api.UserSpace;
import org.aksw.jenax.model.foaf.domain.api.FoafOnlineAccount;
import org.aksw.jenax.model.foaf.domain.api.FoafPerson;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import com.google.common.collect.MutableClassToInstanceMap;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;

@Component
@SessionScope
public class UserSession implements Serializable {

    protected FoafOnlineAccount activeAccount;

    @Resource
    protected GroupMgrFactory gmf;

    protected MutableClassToInstanceMap<Object> resources = null;
    // protected GroupMgr groupMgr;

    protected SystemSpace systemSpace;
    protected UserSpace userSpace = null;

    protected UserSession(SystemSpace systemSpace) {
        this.systemSpace = systemSpace;
    }

    public SystemSpace getSystemSpace() {
        return systemSpace;
    }

    public UserSpace getUserSpace() {
        getUser();
        return userSpace;
    }

    public void initResources() {
        if (activeAccount != null && resources == null) {

            String email = Objects.requireNonNull(activeAccount.getOwner().getMbox());
            String str = StringUtils.urlEncode(email);

            resources = MutableClassToInstanceMap.create();
            resources.computeIfAbsent(GroupMgr.class, g -> gmf.create(str));


            String accountName = activeAccount.getAccountName();
            try {
                userSpace = systemSpace.getUserSpaceMgr().get(accountName);

                userSpace.createIfNotExists();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // getGroupMgr().get();
        }
    }

    public FoafOnlineAccount getUser() {
        activeAccount = activeAccount != null ? activeAccount : loadUser();

        // Check if the user is initialized
        if (resources == null) {
            initResources();
        }


        return activeAccount;
    }

    public GroupMgr getGroupMgr() {
        return resources.getInstance(GroupMgr.class);
    }

    protected FoafOnlineAccount loadUser() {
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


    public void logout() {
        String LOGOUT_SUCCESS_URL = "/";
        UI.getCurrent().getPage().setLocation(LOGOUT_SUCCESS_URL);
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(
                VaadinServletRequest.getCurrent().getHttpServletRequest(), null,
                null);

        resources.clear();
        activeAccount = null;
    }
}