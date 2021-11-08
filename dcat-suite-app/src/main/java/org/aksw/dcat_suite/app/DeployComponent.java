package org.aksw.dcat_suite.app;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.Lumo;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCAT;

public class DeployComponent extends VerticalLayout {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MainView view; 
	private SpotlightProvider spotlightProvider;
	private Resource datasetResource; 
	private static final String DBPEDIA_NS = "http://de.dbpedia.org/resource/";
	private static final String DBPEDIA_PREFIX = "dbpedia-de-";
	
	public DeployComponent (MainView view) {
		this.view = view;
		this.spotlightProvider = new SpotlightProvider();
	}
	
	public void addAnnotate() {
		Button annotateButton = new Button("Annotate");
		annotateButton.addClickListener(clickevent -> { 
			Model model = RDFDataMgr.loadModel(view.getLatestServerPath()) ;
			StmtIterator iter = model.listStatements();

			// print out the predicate, subject and object of each statement
			String title = ""; 
			while (iter.hasNext()) {
			    Statement stmt = iter.nextStatement();
			    Property predicate = stmt.getPredicate(); // get the predicate
			    if (predicate.getLocalName().equals("title")) {
			    	datasetResource = stmt.getSubject();
			    	title = stmt.getObject().asLiteral().getString();
			    	break;
			    }
			    
			}
			try {
				Map<String,String> map = spotlightProvider.getAnnotations(title.replace("-", " "));
				for (String key : map.keySet()) {
					String buttonValue=key.replace(DBPEDIA_NS, DBPEDIA_PREFIX);
					Button tagButton = new Button(buttonValue); 
					tagButton.setThemeName(Lumo.DARK);
					tagButton.addClickListener(tagevent -> { 
						model.add(datasetResource, DCAT.keyword, buttonValue);
						StringWriter out = new StringWriter();
						model.write(out, "TURTLE");
						System.out.println(out.toString());
						remove(tagButton);
					});
					add(tagButton);
				}
			} catch (URISyntaxException | IOException e) {
				e.printStackTrace();
			}
		});
		add(annotateButton);
	}

	public void addDeploy() {
		
		TextField url = new TextField();
		url.setLabel("CKAN URL");
		url.setPlaceholder("http://ckan.qrowd.aksw.org");
		
		TextField apiKey= new TextField();
		apiKey.setLabel("API Key");
		
		TextField group= new TextField();
		group.setLabel("CKAN group");
		group.setPlaceholder("mclient");

		add(url);
		add(apiKey);
		add(group);
		view.addUpload();
		
		Button deployButton = new Button("Deploy to CKAN");
		
		deployButton.addClickListener(clickevent -> { 
	        try {
	        	// Copy file to target/dcat on the server
	        	Path p = Paths.get(view.getLatestServerPath());
	        	String filename = p.getFileName().toString();
	        	File src = new File(view.getLatestServerPath());
	        	File dest = new File(Paths.get(p.getParent().toString(),"target/dcat",filename).toString());
	        	FileUtils.copyFile(src, dest);
	        	// Get placeholder values
	        	String urlValue = AppUtils.getTextValue(url);
	        	String groupValue = AppUtils.getTextValue(group);
	        	DeployProvider.deploy(urlValue, apiKey.getValue(), dest.toString(), true, false, groupValue);
			} catch (IOException e) {
				e.printStackTrace();
			}
				
			});
		add(deployButton);
		
	}
}


