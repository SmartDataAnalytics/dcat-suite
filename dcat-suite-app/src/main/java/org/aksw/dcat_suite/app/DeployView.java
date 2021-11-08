package org.aksw.dcat_suite.app;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("deploy")
public class DeployView extends VerticalLayout {
	
	private static final long serialVersionUID = 1L; 
	private VerticalLayout content;
	private MainView mainView; 
	private DeployComponent deployComponent;

	public DeployView () {
		mainView = new MainView(); 
    	add(mainView);
    	
    	content = mainView.getContent();
    	content.add( new H1( "Deploy DCAT descriptions" ) );
    	
        mainView.getNameToButtons()
        	.get("DCAT deploy")
        	.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        
       deployComponent = new DeployComponent(mainView); 
       deployComponent.addDeploy();
       deployComponent.addAnnotate();
       content.add(this.deployComponent);
	}

}
