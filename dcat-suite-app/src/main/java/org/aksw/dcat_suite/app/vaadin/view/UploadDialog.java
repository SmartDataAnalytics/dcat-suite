package org.aksw.dcat_suite.app.vaadin.view;

import java.io.File;
import java.nio.file.Path;

import org.aksw.commons.io.util.FileUtils;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.Tabs.Orientation;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileData;
import com.vaadin.flow.component.upload.receivers.MultiFileBuffer;

public class UploadDialog
	extends Dialog
{
	protected Upload upload;
	
	protected Tab uploadTab;
	protected Tab sparqlTab;
	
	protected VerticalLayout content;
	
	
	// RDF Import: service url; which graphs; format; filename; stream

		
	public UploadDialog(Path fileRepoRootPath) {
		
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
			content.add("sparql import");
		}
	}

	public Upload getUpload() {
		return upload;
	}
}
