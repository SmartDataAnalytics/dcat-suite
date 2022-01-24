package org.aksw.dcat_suite.app.vaadin.layout;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;

/** Source:https://cookbook.vaadin.com/tabs-with-routes/a */
public class RouteTabs extends Tabs implements BeforeEnterObserver {
    private final Map<RouterLink, Tab> routerLinkTabMap = new HashMap<>();

    public void add(RouterLink ...routerLinks) {
    	for (RouterLink routerLink : routerLinks) {
	        routerLink.setHighlightCondition(HighlightConditions.sameLocation());
	        routerLink.setHighlightAction(
	            (link, shouldHighlight) -> {
	                if (shouldHighlight) setSelectedTab(routerLinkTabMap.get(routerLink));
	            }
	        );
	        routerLinkTabMap.put(routerLink, new Tab(routerLink));
	        add(routerLinkTabMap.get(routerLink));
    	}
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // In case no tabs will match
        setSelectedTab(null);
    }
}