package org.aksw.dcat_suite.app;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.aksw.dcat_suite.enrich.GTFSModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFFormat;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

public class EnrichComponent extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MainView view; 
	private DCATProvider provider; 
	private static final String DOWNLOAD = "/home/user/file.zip";
	private static final String TURTLE = "TURTLE";
	private static final String RDFXML = "RDF/XML";
	private static final String NTRIPLES = "NTRIPLES";


	public EnrichComponent(MainView view, DCATProvider provider)  {
		this.view = view; 
		this.provider = provider; 
	}
	
	public void addTransform() {
		Icon transformIcon = new Icon(VaadinIcon.CARET_SQUARE_RIGHT_O);
		Button clearButton = new Button("Clear");
		
		TextField namespace = new TextField();
		namespace.setLabel("Namespace");
		namespace.setPlaceholder("http://example.org/");
		
		TextField title = new TextField();
		title.setLabel("Title");
		title.setPlaceholder("Public Transport");
		
		TextField downloadURL = new TextField();
		downloadURL.setLabel("Download URL");
		downloadURL.setPlaceholder(DOWNLOAD);
		downloadURL.setVisible(false);
		
		Button button = new Button(new Icon(VaadinIcon.PLUS));
		button.addClickListener(clickevent -> { 
			downloadURL.setVisible(true);
		      
		});
		
		TextArea textArea = new TextArea();
		textArea.setMinWidth("900px");
	    HorizontalLayout layout = new HorizontalLayout();
	    layout.setVisible(false);
        transformIcon.addClickListener(clickevent -> { 
        GTFSModel model = null;
        try {
        		String titleValue = title.getValue() == null ? title.getValue() : title.getPlaceholder();
        		String namespaceValue = namespace.getValue() == null ? namespace.getValue() : namespace.getPlaceholder();
        		String downloadValue = downloadURL.getValue() == null ? downloadURL.getValue() : DOWNLOAD;
				model = this.provider.processEnrichGTFSWeb(view.getLatestServerPath(), titleValue, namespaceValue, downloadValue);
				String triplesText = getTriplesText(model,TURTLE); 
				textArea.setValue(triplesText);
				Anchor ttlAnchor = getAnchor(model, triplesText, TURTLE,"Download TURTLE");
				Anchor rdfAnchor = getAnchor(model, getTriplesText(model,RDFXML), RDFXML,"Download RDF/XML");
				Anchor ntAnchor = getAnchor(model, getTriplesText(model,NTRIPLES), NTRIPLES,"Download NTRIPLES");
		        layout.add(ttlAnchor,rdfAnchor,ntAnchor);
		        layout.setVisible(true);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Serialize to text and put it in a text block
			
		});
        clearButton.addClickListener(buttonEvent -> { 
        	textArea.clear(); 
        	layout.setVisible(false);
        	layout.removeAll();
        	downloadURL.setVisible(false); 
    	});
        add(namespace, title);
        add(button);
        add(downloadURL);
        add(transformIcon);
        add(textArea);
        add(clearButton); 
        add(layout);
 
	}
	

	private StreamResource getStreamResource(String triplesText, String format) {
		String fileSuffix =""; 
		switch(format){
        case TURTLE:
            fileSuffix=".ttl";
            break;
        case RDFXML:
        	fileSuffix=".rdf";
            break;
        case NTRIPLES:
        	fileSuffix=".nt";
            break; 
		}
		return new StreamResource("file"+fileSuffix, () -> {
				InputStream is = new ByteArrayInputStream(triplesText.getBytes(StandardCharsets.UTF_8));
				BufferedInputStream bis=new BufferedInputStream(is);
				return bis;
		});} 
	
	private Anchor getAnchor (GTFSModel model, String triplesText, String format, String name) {
		StreamResource streamResource=getStreamResource(triplesText, format);
        Anchor download = new Anchor(streamResource, "");
        download.getElement().setAttribute("download", true);
        Button button = new Button(format); 
        download.add(button);
        return download; 
	
	}

	private String getTriplesText(GTFSModel model, String format) 	{
		StringWriter out = new StringWriter();
		model.getModel().write(out, format);
		return out.toString();
	}
}
