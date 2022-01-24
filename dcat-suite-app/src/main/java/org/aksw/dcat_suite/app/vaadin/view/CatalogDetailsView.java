package org.aksw.dcat_suite.app.vaadin.view;

import org.aksw.commons.util.string.StringUtils;
import org.apache.jena.sparql.engine.binding.Binding;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class CatalogDetailsView
	extends VerticalLayout
{
	protected Binding binding;
	
	protected Button browseBtn;
	
	public CatalogDetailsView() {
	}
	
	public void setBinding(Binding binding) {
		this.binding = binding;
		refresh();
	}
	

	public void refresh() {
		removeAll();
		browseBtn = new Button("Browse");
		browseBtn.addClickListener(ev -> UI.getCurrent().navigate(CatalogBrowseView.class, StringUtils.urlEncode(binding.get("Url").getURI())));
		
		add(browseBtn);
	}
}
