package org.aksw.dcat_suite.app.vaadin.view;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat_suite.app.QACProvider;
import org.aksw.dcat_suite.app.StatusCodes;
import org.aksw.dcat_suite.app.gtfs.DetectorGtfs;
import org.aksw.dcat_suite.cli.cmd.file.DcatRepoLocal;
import org.aksw.jena_sparql_api.conjure.job.api.JobInstance;
import org.aksw.jenax.arq.dataset.api.DatasetOneNg;
import org.aksw.jenax.model.prov.Activity;
import org.aksw.jenax.model.prov.Entity;
import org.apache.http.client.ClientProtocolException;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.system.Txn;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.RDF;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;


public class BrowseRepoComponent
	extends FileBrowserComponent
{
    protected DcatRepoLocal dcatRepo;
    
    protected QACProvider gtfsValidator;
    

	public BrowseRepoComponent(DcatRepoLocal dcatRepo, QACProvider gtfsValidator) {
		super(dcatRepo.getBasePath());
		this.dcatRepo = dcatRepo;
		this.gtfsValidator = gtfsValidator;
	}

	public void validateGtfs(QACProvider validationProvider, Path basePath, Path relInPath, Path relOutPath)
			throws ClientProtocolException, URISyntaxException, IOException
	{	
		Instant activityStartedAtTime = Instant.now();
        validationProvider.startJob(basePath.resolve(relInPath).toAbsolutePath().toString());

        String currentStatus = validationProvider.getStatus();
        while (currentStatus.equals(StatusCodes.NEW) ||
            currentStatus.equals(StatusCodes.UPLOADED) ||
            currentStatus.equals(StatusCodes.PROCESSING)) {
            currentStatus = validationProvider.getStatus();

        }
        if (currentStatus.equals(StatusCodes.READY)) {
            String validationResult = validationProvider.getResult();
            Files.write(basePath.resolve(relOutPath), validationResult.getBytes());
        }
        
		Instant activityEndedAtTime = Instant.now();

        Entity entity = createGtfsValidateDialogX(relInPath.toString(), relOutPath.toString());
        
        Activity activity = entity.getQualifiedDerivations().iterator().next().getOrSetHadActivity();
        activity
        	.setStartedAtTime(activityStartedAtTime)
        	.setEndedAtTime(activityEndedAtTime);

//        activity
//        	.setStartedAtTime(activityStartedAtTime)
//        	.setEndedAtTime(activityEndedAtTime);
       
        Dataset dataset = dcatRepo.getDataset();
        Txn.executeWrite(dataset, () -> {
        	DatasetOneNg m = GraphEntityUtils.getOrCreateModel(dataset, entity, EntityAnnotators.Provenance);
        	
	        // Model m = dcatRepo.getDataset().getNamedModel(entity);
	        m.getModel().add(entity.getModel());
        });        
        updateFileSearch();
	}
	
	public Entity createGtfsValidateDialogX(String relInPath, String relOutPath) {
		Model m = ModelFactory.createDefaultModel();
		Entity result = createProvenanceData(
				m.createResource(relInPath),
				m.createResource("urn:gftsValidator").as(JobInstance.class),
				m.createResource(relOutPath));
		return result;
	}

	public void validateGtfs(QACProvider validationProvider, Path basePath, Path relInPath) {
		Path gtfsInPath = basePath.resolve(relInPath);
		String filename = gtfsInPath.getFileName().toString();
		Path relOutPath = relInPath.resolveSibling(filename + ".report.ttl");

		try {
			validateGtfs(validationProvider, basePath, relInPath, relOutPath);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public int addExtraOptions(GridContextMenu<Path> contextMenu, Path relPath) {
		int numOptions = 0;
		
        Path absPath = path.resolve(relPath);

        Dialog importGtfsDialog = new Dialog();
        Button closeBtn = new Button("Close", ev -> importGtfsDialog.close());
        importGtfsDialog.add("Import GTFS...");
        importGtfsDialog.add(closeBtn);

        boolean isGtfs = InvokeUtils.tryCall(() -> DetectorGtfs.isGtfs(absPath)).orElse(false);
        if (isGtfs) {
            contextMenu.addItem("Import GTFS...", ev -> {
                importGtfsDialog.open();
            });
            ++numOptions;

        
            contextMenu.addItem("Validate GTFS...", ev -> validateGtfs(gtfsValidator, path, relPath));
            ++numOptions;

        }
        

        // View (RDF) Metadata
        contextMenu.addItem("View ...", ev -> {
        	Dialog dlg = new Dialog();
        	// DatasetAndDistributionFromFile content = new DatasetAndDistributionFromFile(absPath);
        	VerticalLayout content = new FileDetailsDialog(path, relPath, dcatRepo.getDataset());
        	content.setSizeFull();
        	dlg.setSizeFull();
        	dlg.add(content);
        	dlg.open();
        });
        ++numOptions;

        
        // Create dataset / distribution
        contextMenu.addItem("Create Dataset...", ev -> {
        	Dialog dlg = new Dialog();
        	DatasetAndDistributionFromFile content = new DatasetAndDistributionFromFile(absPath);
        	dlg.add(content);
        	
        	Button okBtn = new Button("Create");
        	okBtn.addClickListener(ev2 -> {
        		Dataset dataset = dcatRepo.getDataset();
        		        		
        		// String datasetIri = "#" + content.getDatasetId() + ":" + content.getVersion();
        		// String distIri = datasetIri + ":" + content.getDistributionId();

        		Txn.executeWrite(dataset, () -> {
            		DatasetOneNg dong = GraphEntityUtils.getOrCreateModel(dataset,
            				NodeFactory.createLiteral(content.getDatasetId()),
            				NodeFactory.createLiteral(content.getVersion()),
            				NodeFactory.createLiteral(content.getDistributionId())
            		);
            		dong.getSelfResource().as(DcatDistribution.class)
            			.setDownloadUrl(relPath.toString());


//        			Model model = dataset.getNamedModel(datasetIri);
//        			model.createResource(datasetIri)
//        				.addProperty(RDF.type, DCAT.Dataset)
//        				.as(DcatDataset.class)
//        				.addNewDistribution(distIri)
//        				.setDownloadUrl(relPath.toString());
        		});
        		
        	});
        	dlg.add(okBtn);
        	
        	dlg.open();
        	
        });
        ++numOptions;

        return numOptions;
	}

	public void refresh() {
		
	}
	
//
//	public void configureFileGridContextMenu() {
//		GridContextMenu<Path> contextMenu = fileGrid.addContextMenu();
//		
//        contextMenu.setDynamicContentHandler(relPath -> {
//        	contextMenu.removeAll();
//        	Path ap = activePath.get();
//        	Path p = ap == null ? path : path.resolve(ap);
////            Path fileRepoRootPath = groupMgr.getBasePath();
//            Path absPath = p.resolve(relPath);
//
//            contextMenu.addItem("Actions for " + relPath.toString()).setEnabled(false);
//            contextMenu.add(new Hr());
//
//            int numOptions = 0;
//
//            numOptions += addExtraOptions(contextMenu, p, relPath);
//            
//            // Delete action
//            
//            contextMenu.addItem("Delete", ev -> {
//                ConfirmDialog dialog = VaadinDialogUtils.confirmDialog("Confirm delete",
//                        "You are about to delete: " + relPath,
//                        "Delete", x -> {
//                            InvokeUtils.invoke(() -> Files.delete(absPath), e -> {
//                                // TODO Show a notification if delete failed
//                            });
//                            updateFileSearch();
//                        }, "Cancel", x -> {});
//                //dialog.setConfirmButtonTheme("error primary");
//                dialog.open();
//            });
//            ++numOptions;
//
//            
//            // Create dataset / distribution
//            contextMenu.addItem("Create Dataset...", ev -> {
//            	Dialog dlg = new Dialog();
//            	DatasetAndDistributionFromFile content = new DatasetAndDistributionFromFile(absPath);
//            	dlg.add(content);
//            	dlg.open();
//            	
//            });
//            ++numOptions;
//            
//            
//            if (numOptions == 0) {
//                contextMenu.addItem("(no actions available)").setEnabled(false);
//            }
//
//            return true;
//        });
//
//	}
}

