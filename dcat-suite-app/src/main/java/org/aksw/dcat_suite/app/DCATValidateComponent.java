package org.aksw.dcat_suite.app;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;

//@CssImport("./styles/grid-style.css")
@CssImport(value = "./styles/grid-style.css", themeFor = "vaadin-grid")
public class DCATValidateComponent extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MainView view; 
	private DCATProvider provider; 
	
	public DCATValidateComponent(MainView view)  {
		this.view = view; 
		this.provider = new DCATProvider(); 
		
	}

	public void addDCATValidate() throws ClientProtocolException, URISyntaxException, IOException {

        Button validateButton = new Button("Validate your DCAT", VaadinIcon.CHECK_SQUARE.create());
        Grid<ResultItem> grid = new Grid<>(ResultItem.class);
        validateButton.addClickListener(clickevent -> {   
        	List<ResultItem> resultItems = null;
        	Model model = RDFDataMgr.loadModel(view.getLatestServerPath());
        	StringWriter out = new StringWriter();
    		//System.out.println(model.write(out, "RDF/XML").toString());
    		model.write(out, "TURTLE");
    		String modelText = out.toString(); 
    		try {
				//this.provider.getTestReport(model.write(out, "TURTLE").toString());
    			resultItems = this.provider.getTestReport(modelText);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
    		grid.setWidth("900px");
    		grid.setItems(resultItems);
    		grid.getColumns()
            .forEach(grid::removeColumn);
			grid.addColumn(new ComponentRenderer<>(row -> {
				Label label = new Label(row.getSeverity());
				return label;
			})).setHeader("Severity").setWidth("200px").setFlexGrow(0);  
			
			grid.addColumn(new ComponentRenderer<>(row -> {
				Label label = new Label(row.getMessage());
				return label;
			})).setHeader("Message").setWidth("700px").setFlexGrow(0);
    		
			grid.setClassNameGenerator(row -> {
				if (row.getSeverity().equals("Warning")) {
					return "line-warning";
				}
				else if (row.getSeverity().equals("Error")) {
					return "line-error";
				}
				return "";
			});
    		view.getContent().add(grid); 
        	//TODO: add DCAT validation functionality
        	// Determine file type, if it is not .nt .ttl or RDF/XML, print error
        	//implement standard validation DCAT-AP 2.1 
        });
        view.getContent().add(validateButton); 
        

	}
}
