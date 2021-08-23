package org.aksw.dcat_suite.app;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("validate")
public class DCATValidateView extends VerticalLayout {
	
	private static final long serialVersionUID = 1L; 
	private VerticalLayout content;
	private MainView mainView; 
	private DCATValidateComponent validateComponent; 

	public DCATValidateView () throws ClientProtocolException, URISyntaxException, IOException {
		mainView = new MainView(); 
		validateComponent = new DCATValidateComponent(mainView); 
    	add(mainView);
    	
    	content = mainView.getContent();
    	content.add( new H1( "Validate DCAT Files" ) );
    	
        mainView.getNameToButtons()
        	.get("DCAT validate")
        	.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        
        mainView.addUpload();
        validateComponent.addDCATValidate(); 
        content.add(validateComponent);
      
	}

}
