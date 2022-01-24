package org.aksw.dcat_suite.app.vaadin.view;

import org.aksw.dcat_suite.app.vaadin.layout.DmanMainLayout;
import org.aksw.dcat_suite.app.vaadin.layout.DmanRoutes;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = DmanRoutes.HOME, layout = DmanMainLayout.class)
@PageTitle("MClient Data Manager - Welcome")
public class DmanLandingPageView
    extends VerticalLayout
{
	public DmanLandingPageView() {
		add(new H1("Welcome"));
	}
}
