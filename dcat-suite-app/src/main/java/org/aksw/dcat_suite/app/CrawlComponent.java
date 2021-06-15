package org.aksw.dcat_suite.app;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;

import org.aksw.dcat_suite.enrich.GTFSModel;
import org.apache.jena.rdf.model.Model;
import org.json.simple.parser.ParseException;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

public class CrawlComponent extends VerticalLayout {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private CrawlProvider crawlProvider; 
	
	public CrawlComponent () {
		crawlProvider = new CrawlProvider();
	}

	public void addCrawl() {
		
		
		Select<String> select = new Select<>();
		select.setLabel("Portal Type");
		select.setItems("ckan", "dkan");
		
		TextField url = new TextField();
		url.setLabel("URL");
		url.setPlaceholder("https://geo.muelheim-ruhr.de/");
		
		TextField namespace= new TextField();
		namespace.setLabel("Namespace");
		namespace.setPlaceholder("http://example.org/");
		
		TextArea textArea = new TextArea();
		textArea.setMinWidth("900px");
		
		Icon crawlIcon = new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT);
		crawlIcon.addClickListener(clickevent -> { 

			String urlValue = AppUtils.getTextValue(url);
			String namespaceValue = AppUtils.getTextValue(namespace);
			Model model = null;
			try {
				model = crawlProvider.crawl(urlValue, namespaceValue, select);
			} catch (IOException | ParseException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
	    
			String triplesText = getTriplesText(model,"TURTLE"); 
			textArea.setValue(triplesText); 

				// Serialize to text and put it in a text block
				
		});
		
		add(select);
		add(url);
		add(namespace);
		add(crawlIcon);
		add(textArea);
		
	}
	
	private String getTriplesText(Model model, String format) 	{
		StringWriter out = new StringWriter();
		model.write(out, format);
		return out.toString();
	}
	
	
	}


