package org.aksw.dcat_suite.app.vaadin.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;

public class ToggleTab
	extends VerticalLayout
{
	protected Button toggleBtn;
	protected Component content;
	
	public ToggleTab(String text, Component tabContent) {
		toggleBtn = new Button(text);
		content = tabContent;
		
		toggleBtn.addClickListener(ev -> {
			Style style = content.getElement().getStyle();
			String value = style.get("display");
			String newValue = "none".equals(value) ? null : "none";
			style.set("display", newValue);
		});
		// content.setVisible(false);
		
		add(toggleBtn);
		add(content);
	}
}
