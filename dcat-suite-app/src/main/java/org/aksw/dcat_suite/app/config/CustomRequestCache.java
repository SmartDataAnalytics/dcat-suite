package org.aksw.dcat_suite.app.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

public class CustomRequestCache extends HttpSessionRequestCache {

    @Override
    public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
        if (!VaadinSecurityUtils.isFrameworkInternalRequest(request)) {
            super.saveRequest(request, response);


        }
    }
}
