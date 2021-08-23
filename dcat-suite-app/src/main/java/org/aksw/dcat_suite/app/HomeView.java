package org.aksw.dcat_suite.app;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

	@Route("")
	public class HomeView extends VerticalLayout {
		
		private static final long serialVersionUID = 1L; 
		private VerticalLayout content;
		private MainView mainView; 

		public HomeView () {
			mainView = new MainView(); 
	    	add(mainView);
	    	
	    	content = mainView.getContent();
	    	content.add( new H1( "MCLIENT Demonstrator" ) );
	    	
	        mainView.getNameToButtons()
	        	.get("HOME")
	        	.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
	        
	        
		}

	}



