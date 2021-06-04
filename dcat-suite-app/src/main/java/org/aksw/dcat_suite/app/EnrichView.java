package org.aksw.dcat_suite.app;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;

@Route("enrich")
public class EnrichView extends VerticalLayout {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private EnrichComponent enrichComponent; 
	private VerticalLayout content;
	
	private MainView mainView; 

	private final MemoryBuffer buffer;
	Upload upload = new Upload();
	
    public EnrichView(){
    	buffer = new MemoryBuffer();
    	upload = new Upload(buffer);
    	DCATProvider dcatProvider = new DCATProvider(); 
    	
    	
    	mainView = new MainView(); 
    	add(mainView);
    	
    	content = mainView.getContent();
    	content.add( new H1( "Generate DCAT descriptions for GTFS files" ) );
    	
        mainView.addUpload();
    	enrichComponent = new EnrichComponent(mainView, dcatProvider); 
    	enrichComponent.addTransform();
        content.add(this.enrichComponent);
        mainView.getNameToButtons()
        	.get("DCAT enrich")
        	.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
    }

    

}