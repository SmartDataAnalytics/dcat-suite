package org.aksw.dcat_suite.app;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.aksw.dcat_suite.enrich.GTFSModel;
import org.apache.http.client.ClientProtocolException;
import org.apache.jena.rdf.model.Model;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.StreamResource;

public class EnrichComponent extends VerticalLayout {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MainView view; 
	private GTFSProvider gtfsProvider; 
	private static final String DOWNLOAD = "";
	private static final String TURTLE = "TURTLE";
	private static final String RDFXML = "RDF/XML";
	private static final String NTRIPLES = "NTRIPLES";
    private QACProvider validationProvider;

	public EnrichComponent(MainView view)  {
		this.view = view; 
		this.gtfsProvider = new GTFSProvider(); 
		this.validationProvider = new QACProvider();
	}
	
	public void addGTFSValidate() throws ClientProtocolException, URISyntaxException, IOException {
        Button validateButton = new Button("Validate your GTFS zip", VaadinIcon.CHECK_SQUARE.create());
        Button validateClear = new Button("Clear"); 
        TextArea validateArea = new TextArea(); 
        validateArea.setMinWidth("900px");
        HorizontalLayout validateLayout = new HorizontalLayout();
	    validateLayout.setVisible(false);
        validateButton.addClickListener(clickevent -> {
            try {
                this.validationProvider.startJob(view.getLatestServerPath());
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                String currentStatus = this.validationProvider.getStatus();
                while (currentStatus.equals(StatusCodes.NEW) ||
                    currentStatus.equals(StatusCodes.UPLOADED) ||
                    currentStatus.equals(StatusCodes.PROCESSING)) {
                    currentStatus = this.validationProvider.getStatus();

                }
                if (currentStatus.equals(StatusCodes.READY)) {
                    String validationResult = this.validationProvider.getResult();
                    validateArea.setValue(validationResult);
                    Anchor validationAnchor = getAnchor(validationResult, TURTLE,"Download Validation Report");
                    validateLayout.add(validationAnchor); 
                    validateLayout.setVisible(true);
                }
            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
            }
            
            validateClear.addClickListener(buttonEvent -> { 
            	validateArea.clear(); 
            	validateLayout.setVisible(false);
            	validateLayout.removeAll();
        	});
        });
        add(validateButton);
        add(validateArea);
        add(validateLayout);
        add(validateClear);

    }

	public void addTransform() {
		Button dcatIcon = new Button("Generate DCAT");
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
        dcatIcon.addClickListener(clickevent -> { 
        GTFSModel model = null;
        try {
        		String titleValue = AppUtils.getTextValue(title);
        		String namespaceValue = AppUtils.getTextValue(namespace);
				model = this.gtfsProvider.processEnrichGTFSWeb(view.getLatestServerPath(), titleValue, namespaceValue, downloadURL.getValue());
				String triplesText = getTriplesText(model.getModel(),TURTLE); 
				textArea.setValue(triplesText);
				Anchor ttlAnchor = getAnchor(triplesText, TURTLE,"Download TURTLE");
				Anchor rdfAnchor = getAnchor(getTriplesText(model.getModel(),RDFXML), RDFXML,"Download RDF/XML");
				Anchor ntAnchor = getAnchor(getTriplesText(model.getModel(),NTRIPLES), NTRIPLES,"Download NTRIPLES");
		        layout.add(ttlAnchor,rdfAnchor,ntAnchor);
		        layout.setVisible(true);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
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
        add(dcatIcon);
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
	
	private Anchor getAnchor (String triplesText, String format, String name) {
		StreamResource streamResource=getStreamResource(triplesText, format);
        Anchor download = new Anchor(streamResource, "");
        download.getElement().setAttribute("download", true);
        Button button = new Button(format); 
        download.add(button);
        return download; 
	
	}

	private String getTriplesText(Model model, String format) 	{
		StringWriter out = new StringWriter();
		model.write(out, format);
		return out.toString();
	}
}
