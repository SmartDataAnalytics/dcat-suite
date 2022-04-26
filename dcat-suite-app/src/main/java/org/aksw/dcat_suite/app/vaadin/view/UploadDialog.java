package org.aksw.dcat_suite.app.vaadin.view;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.aksw.commons.io.util.FileUtils;
import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.engine.ExecutionUtils;
import org.aksw.jena_sparql_api.conjure.fluent.ConjureBuilderImpl;
import org.aksw.jena_sparql_api.conjure.fluent.QLib;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.Tabs.Orientation;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileData;
import com.vaadin.flow.component.upload.receivers.MultiFileBuffer;

public class UploadDialog
	extends Dialog
{
	protected Path fileRepoRootPath;
	
	protected Upload upload;
	
	protected Tab uploadTab;
	protected Tab sparqlTab;
	
	protected VerticalLayout content;
	
	
	// RDF Import: service url; which graphs; format; filename; stream

		
	public UploadDialog(Path fileRepoRootPath) {
		
		this.fileRepoRootPath = fileRepoRootPath;
		
		HorizontalLayout panel = new HorizontalLayout();
		
        Tabs tabs = new Tabs();
        tabs.setOrientation(Orientation.VERTICAL);
        tabs.setHeightFull();
        
        uploadTab = new Tab(
        	VaadinIcon.UPLOAD.create(),
			new Span("Upload")
			//createBadge("24")
		);

        sparqlTab = new Tab(
			VaadinIcon.DATABASE.create(),
			new Span("Sparql")
			//createBadge("439")
		);

        tabs.add(uploadTab, sparqlTab);

        content = new VerticalLayout();
        
        panel.add(tabs);
        panel.add(content);
        
        
        MultiFileBuffer receiver = new MultiFileBuffer();
        upload = new Upload(receiver);
        upload.setDropAllowed(true);
        upload.addSucceededListener(event -> {

            //Path fileRepoRootPath;
//	            try {
                // fileRepoRootPath = groupMgr.getBasePath();
                // Files.createDirectories(fileRepoRootPath);
//	            } catch (IOException e1) {
//	                throw new RuntimeException(e1);
//	            }


            String fileName = event.getFileName();

            FileData fileData = receiver.getFileData(fileName);
            File file = fileData.getFile();
            Path srcPath = file.toPath();
            Path tgtPath = fileRepoRootPath.resolve(fileName);

            try {
                FileUtils.moveAtomic(srcPath, tgtPath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Notification.show(String.format("Upload succeeded. Filename: '%s'", fileName));


            
            
        });
        upload.addFailedListener(event -> {
            Notification.show(String.format("Upload failed. Filename: %s", event.getFileName()));
        });
        
        add(panel);
        
    	tabs.addSelectedChangeListener(event -> setContent(event.getSelectedTab()));
    	setContent(tabs.getSelectedTab());		

        // add(upload);
	}
	
	public void setContent(Tab tab) {
		content.removeAll();
		
		if (tab.equals(uploadTab)) {
			content.add(upload);
		} else if (tab.equals(sparqlTab)) {
			content.add(new H3("sparql import"));
			
			FormLayout form = new FormLayout();
			TextField serviceUrlField = new TextField();			
			ComboBox<RDFFormat> formatBox = new ComboBox<>();
			TextField fileNameField = new TextField();

			Button saveBtn = new Button("Save");
			
			form.addFormItem(serviceUrlField, "Service URL");
			form.addFormItem(formatBox, "Format");
			form.addFormItem(fileNameField, "Filename");
			content.add(form);

			// RDFWriterRegistry.
			
			
			content.add(saveBtn);
			
			saveBtn.addClickListener(ev -> {
				String serviceUrl = serviceUrlField.getValue();
				
				String fileName = fileNameField.getValue();
				
				// FIXME Query jena's registry for RDFFormats
				// FIXME Support quads
				// How to support sorted ntriples - is this a post processing step or is it better modeled as some custom mime type?!
				
				// RDFConnection.connect(serviceUrl);
				Model model = ModelFactory.createDefaultModel();
				Op op = ConjureBuilderImpl.start()
					.fromDataRef(RdfDataRefSparqlEndpoint.create(model, serviceUrl))
					.construct(QLib.everything())
					.getOp();
				
				RdfDataPod dataPod = ExecutionUtils.executeJob(op);
				
				Model m = dataPod.getModel();
				try (OutputStream out = Files.newOutputStream(fileRepoRootPath.resolve(fileName))) {
					RDFDataMgr.write(out, m, RDFFormat.TRIG_BLOCKS);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				
				// 
			});
		}
	}

	public Upload getUpload() {
		return upload;
	}
}
