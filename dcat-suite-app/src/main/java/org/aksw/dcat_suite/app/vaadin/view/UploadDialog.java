package org.aksw.dcat_suite.app.vaadin.view;

import java.io.File;
import java.nio.file.Path;

import org.aksw.commons.io.util.FileUtils;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileData;
import com.vaadin.flow.component.upload.receivers.MultiFileBuffer;

public class UploadDialog
	extends Dialog
{
	protected Upload upload;
	
	public UploadDialog(Path fileRepoRootPath) {
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
        
        
        add(upload);
	}

	public Upload getUpload() {
		return upload;
	}
}
