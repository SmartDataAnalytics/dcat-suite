package org.aksw.dcat_suite.app;

import com.vaadin.flow.component.textfield.TextField;

public final class AppUtils {
	
	public static String getTextValue (TextField textfield) {
		String value = ""; 
		if ( textfield.isEmpty() ) {
			value = textfield.getPlaceholder(); 
		}
		else { 
			value = textfield.getValue(); 
		}
		return value; 
	}

}
