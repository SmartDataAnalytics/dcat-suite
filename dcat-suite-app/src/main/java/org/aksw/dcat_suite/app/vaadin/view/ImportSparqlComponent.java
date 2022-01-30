package org.aksw.dcat_suite.app.vaadin.view;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;

public class ImportSparqlComponent
	extends FormLayout
{
	TextField serviceUrl;
	TextField graphNamePattern;
	ComboBox<String> format;
	TextField fileName;	

	public ImportSparqlComponent() {
		
	}
}
