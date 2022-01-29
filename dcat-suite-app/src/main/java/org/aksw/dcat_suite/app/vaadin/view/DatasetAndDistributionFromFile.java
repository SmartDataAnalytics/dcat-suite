package org.aksw.dcat_suite.app.vaadin.view;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.aksw.commons.util.string.FileName;
import org.aksw.commons.util.string.FileNameImpl;
import org.aksw.commons.util.string.FileNameUtils;
import org.aksw.dcat_suite.cli.cmd.file.DcatRepoLocalUtils;
import org.aksw.jena_sparql_api.conjure.utils.ContentTypeUtils;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;

public class DatasetAndDistributionFromFile
	extends FormLayout
{
	// protected DcatRepoLocal dcatRepo;
	protected Path path;
	
	protected ComboBox<String> datasetId;
	protected ComboBox<String> distributionId;
	protected ComboBox<String> version;
	
	
	public DatasetAndDistributionFromFile(Path path) {
		setResponsiveSteps(
				new ResponsiveStep("0", 1)
		);

		this.path = path;
		
		
		datasetId = new ComboBox<>();
		datasetId.setItems();
		datasetId.setAllowCustomValue(true);
		datasetId.addCustomValueSetListener(ev -> datasetId.setValue(ev.getDetail()));
		
		distributionId = new ComboBox<>();
		distributionId.setItems();
		distributionId.setAllowCustomValue(true);
		distributionId.addCustomValueSetListener(ev -> distributionId.setValue(ev.getDetail()));

		version = new ComboBox<>();
		version.setAllowCustomValue(true);
		version.setItems("1.0.0");
		version.addCustomValueSetListener(ev -> version.setValue(ev.getDetail()));

//		okBtn = new Button("Ok");
//		cancelBtn = new Button("Cancel");
//	
		addFormItem(datasetId, "Artifact");
		addFormItem(version, "Version");
		addFormItem(distributionId, "Classifier");
		
		setPath(path);
	}

	public String getVersion() {
		return version.getValue();
	}

	public String getDatasetId() {
		return datasetId.getValue();
	}
	
	public String getDistributionId() {
		return distributionId.getValue();
	}

		
	public void setPath(Path path) {
		this.path = path;
		
				
        RdfEntityInfo entityInfo = DcatRepoLocalUtils.probeFile(Path.of(""), path);
                
        // Derive the base name; remove encoding extensions but keep content type file extension
        FileName fileName = FileNameUtils.deriveFileName(path.getFileName().toString(), entityInfo);

        // Try to create canonical file extensions
        try {
        	// If the content type was not detected then try to cut off a file extension from the base name
        	String ct = entityInfo.getContentType();
        	if (ct != null) {
	        	String canonicalContentPart = ContentTypeUtils.toFileExtension(ct, false);
	        	fileName = FileNameImpl.create(fileName.getBaseName(), canonicalContentPart, fileName.getEncodingParts());
        	}
        } catch (Exception e) {
        	// Keep the non-canonical content part
        }
 
        List<String> canonicalEncodingParts = ContentTypeUtils.toFileExtensionParts(entityInfo.getContentEncodings());
        fileName = FileNameImpl.create(fileName.getBaseName(), fileName.getContentPart(), canonicalEncodingParts);
 
        String dateStr = "";
        try {
        	FileTime time = Files.getLastModifiedTime(path);
        	LocalDate date = LocalDate.ofInstant(time.toInstant(), ZoneId.systemDefault());
        	dateStr = date.toString();
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
        
        String distributionType = fileName.getExtension(false);
        
        datasetId.setValue(fileName.getBaseName());
        distributionId.setValue(distributionType);
    	version.setValue(dateStr);
	}
}
