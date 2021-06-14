package org.aksw.dcat_suite.app;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("crawl")
public class CrawlView extends VerticalLayout {
	
	private static final long serialVersionUID = 1L; 
	private VerticalLayout content;
	private MainView mainView; 
	private CrawlComponent crawlComponent;

	public CrawlView () {
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
