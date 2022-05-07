package org.aksw.dcat_suite.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.shared.Registration;

public class MainView extends VerticalLayout {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private HorizontalLayout header;
    private VerticalLayout navBar;
    private VerticalLayout content;
    private HorizontalLayout center;
    private HorizontalLayout footer;
    private final MemoryBuffer buffer;
    private Upload upload;
    private String enrichFile;
    private Map<String,Button> nameToButtons;
    MenuBar menuBar;


    public MainView () {
        buffer = new MemoryBuffer();
        upload = new MyUpload();
        upload = new MyUpload(buffer);
        // Instantiate layouts
        header = new HorizontalLayout();
        navBar = new VerticalLayout();
        content = new VerticalLayout();
        center = new HorizontalLayout();
        footer = new HorizontalLayout();

        nameToButtons = new HashMap<>();
        createMenuItems();
        header.add(menuBar);

        // Configure layouts
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        header.setWidth("100%");
        header.setPadding(true);
        center.setWidth("100%");
        navBar.setWidth("200px");
        content.setWidth("100%");
        footer.setWidth("100%");
        footer.setPadding(true);

        // compose layout
          center.add(navBar, content);
        center.setFlexGrow(1, navBar);
        add(header, center, footer);
        expand(center);
    }

    public void addUpload() {
        upload.setMinWidth("300px");
        upload.setUploadButton(new Button("Upload your file", VaadinIcon.UPLOAD.create()));
        upload.setMaxFiles(1);
        upload.setDropAllowed(false);
        upload.addSucceededListener(event -> {
            Notification.show(String.format("Upload succeeded. Filename: '%s'", event.getFileName()));
            ServletContext sCtx = getServletContext();
            String dirName = sCtx.getRealPath("upload");
            File dir = new File(dirName);
            if (!dir.exists()) dir.mkdirs();
            File longPath = new File(event.getFileName());
            String filename = longPath.getName();
            File file = new File(dir, filename);

            try {
                FileOutputStream fos = new FileOutputStream(file);
                IOUtils.copy(buffer.getInputStream(),fos);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.enrichFile = file.getAbsoluteFile().getAbsolutePath();

        });
        upload.addFailedListener(event -> {
            Notification.show(String.format("Upload failed. Filename: %s", event.getFileName()));
        });
        upload.getElement()
        .addEventListener(
                "file-remove",
                event -> {
                  this.enrichFile="";
                });
        content.add(upload);

    }



    private ServletContext getServletContext() {

        final ServletContext sCtx = VaadinServlet.getCurrent().getServletContext();;
        return sCtx;
    }

    private void createMenuItems () {
        menuBar = new MenuBar();
        Anchor homeAnchor = createMenuAnchor("HOME","");
        Anchor enrichAnchor = createMenuAnchor("DCAT enrich","/enrich");
        Anchor crawlAnchor = createMenuAnchor("DCAT crawl","/crawl");
        Anchor deployAnchor = createMenuAnchor("DCAT deploy","/deploy");
        Anchor validateAnchor = createMenuAnchor("DCAT validate","/validate");

        Stream.of(homeAnchor, enrichAnchor, crawlAnchor, deployAnchor, validateAnchor)
                .forEach(menuBar::addItem);
    }

    private Anchor createMenuAnchor (String label, String link) {
        Anchor anchor = new Anchor();
        Button button = new Button(label);
        anchor.setHref(link);
        anchor.add(button);
        nameToButtons.put(label, button);
        return anchor;
    }

    public String getLatestServerPath () {
        return this.enrichFile;
    }

    public VerticalLayout getContent() {
        return this.content;
    }

    public HorizontalLayout getHeader() {
        return this.header;
    }

    public Map<String, Button> getNameToButtons () {
        return nameToButtons;
    }

    class MyUpload extends Upload {
        /**
         *
         */
        public MyUpload() {
            super();
        }

        public MyUpload(Receiver receiver) {
            super(receiver);
        }

        private static final long serialVersionUID = 1L;

        Registration addFileRemoveListener(ComponentEventListener<FileRemoveEvent> listener) {
            return super.addListener(FileRemoveEvent.class, listener);
        }
    }

    @DomEvent("file-remove")
    public static class FileRemoveEvent extends ComponentEvent<Upload> {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public FileRemoveEvent(Upload source, boolean fromClient) {
            super(source, fromClient);
        }
    }

}
