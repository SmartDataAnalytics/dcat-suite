package org.aksw.dcat_suite.app.vaadin.view;

import javax.annotation.security.PermitAll;

import org.aksw.dcat_suite.app.vaadin.layout.DmanMainLayout;
import org.aksw.dcat_suite.app.vaadin.layout.DmanRoutes;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = DmanRoutes.HISTORY, layout = DmanMainLayout.class)
@PageTitle("History")
@PermitAll
public class HistoryView
    extends VerticalLayout
{
    public HistoryView() {
        add(new H1("History"));
    }
}
