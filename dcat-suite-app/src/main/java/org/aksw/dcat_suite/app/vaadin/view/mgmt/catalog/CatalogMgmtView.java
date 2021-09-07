package org.aksw.dcat_suite.app.vaadin.view.mgmt.catalog;

import org.aksw.dcat_suite.app.CrawlComponent;
import org.aksw.dcat_suite.app.MainView;
import org.aksw.dcat_suite.app.vaadin.layout.MClientMainLayout;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * A view for managing references to other data catalogs
 *
 * @author raven
 *
 */
@Route(value = "catalogs/:groupId*", layout = MClientMainLayout.class)
@PageTitle("Data Catalog Management")
public class CatalogMgmtView extends VerticalLayout {

    private static final long serialVersionUID = 1L;
    private VerticalLayout content;
    private MainView mainView;
    private CrawlComponent crawlComponent;

    public CatalogMgmtView () {
        mainView = new MainView();
        add(mainView);

        content = mainView.getContent();
        content.add( new H1( "Crawl Remote Open Data Portals" ) );

        mainView.getNameToButtons()
            .get("DCAT crawl")
            .addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        crawlComponent = new CrawlComponent();
        crawlComponent.addCrawl();
        content.add(this.crawlComponent);
    }

}