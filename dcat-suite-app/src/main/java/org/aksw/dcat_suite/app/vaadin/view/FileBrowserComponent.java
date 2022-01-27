package org.aksw.dcat_suite.app.vaadin.view;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collection.observable.ObservableValue;
import org.aksw.commons.collection.observable.ObservableValueImpl;
import org.aksw.jena_sparql_api.conjure.job.api.JobInstance;
import org.aksw.jenax.model.prov.Activity;
import org.aksw.jenax.model.prov.Entity;
import org.aksw.jenax.model.prov.QualifiedDerivation;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.claspina.confirmdialog.ConfirmDialog;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;


public class FileBrowserComponent
	extends VerticalLayout
{
	protected TreeGrid<Path> folderGrid;
	protected TreeGrid<Path> fileGrid;
	protected TextField searchField;
	protected Checkbox recursiveSearchCb;
	protected Button showUploadDlgBtn;
	
	protected Button changeFolderBtn;
	
	protected Path path;
	
    /** The active path within the file repository; used e.g. for uploads and searches */
    protected ObservableValue<Path> activePath;

	public FileBrowserComponent(Path path) {
		
		// setWidthFull();
		// setHeight("500px");
		
		// path = Path.of("/tmp/dman");
		this.path = path;
		
	    TreeData<Path> treeData = new TreeData<>();
	    TreeDataProvider<Path> treeDataProvider = new TreeDataProvider<>(treeData);

		activePath = ObservableValueImpl.create(path);
        activePath.addValueChangeListener(ev -> {
            // searchField.setLabel("Search " + pathToString2(path).apply(ev.getNewValue()));
        	updateFileSearch();
        });

        recursiveSearchCb = new Checkbox("recursive");
        recursiveSearchCb.addValueChangeListener(ev -> updateFileSearch());
        recursiveSearchCb.setValue(false);
        
	    folderGrid = createFolderGrid(path);
		fileGrid = createFileGrid(treeDataProvider, path);
		
		configureFileGridContextMenu();
		
        searchField = new TextField();
        searchField.setMinWidth(20, Unit.EM);
        // searchField.setWidthFull();
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setSuffixComponent(recursiveSearchCb);

        
        // searchField.setWidthFull();
        searchField.setValueChangeMode(ValueChangeMode.LAZY);

        showUploadDlgBtn = new Button(VaadinIcon.UPLOAD.create());
        showUploadDlgBtn.addClickListener(ev -> {
        	UploadDialog dlg = new UploadDialog(activePath.get());
        	// TODO Analyze the uploaded file
        	dlg.getUpload().addFinishedListener(ev2 -> updateFileSearch());
//        	dlg.getUpload().addSucceededListener(ev2 -> {
//        		// ev2.get
//        	});
        	dlg.open();
        });
        

        folderGrid.setMinWidth(30, Unit.EM);
        folderGrid.setMinHeight(30, Unit.EM);
        folderGrid.setSelectionMode(SelectionMode.SINGLE);
        folderGrid.addSelectionListener(ev -> {
            activePath.set(ev.getFirstSelectedItem().orElse(null));
        });

        searchField.addValueChangeListener(ev -> updateFileSearch());
        searchField.setValue("");


        changeFolderBtn = new Button("Change folder...");
        changeFolderBtn.addClickListener(ev -> {
        	Dialog dlg = new Dialog();
        	Button btn = new Button("Close");

        	Button newFolderBtn = new Button(VaadinIcon.FOLDER_ADD.create());
        	dlg.add(newFolderBtn);
        	newFolderBtn.addClickListener(ev2 -> {
        		Path ap = activePath.get();
        			VaadinDialogUtils.confirmInputDialog("Create Folder", "Name", "Create", name -> {
                		try {
                			Files.createDirectory(ap.resolve(name));
                    		folderGrid.getDataProvider().refreshAll();
                		} catch (Exception e) {
                			throw new RuntimeException(e);
                		}
        			}, "Cancel", null).open();        		
        	});
        	
        	dlg.add("Select a new current folder");
        	dlg.add(folderGrid, btn);
        	btn.addClickListener(ev2 -> dlg.close());
        	
        	dlg.open();
        });

        FlexLayout hl = new FlexLayout();
        hl.setFlexWrap(FlexWrap.WRAP);
        hl.setWidthFull();
        hl.add(changeFolderBtn, showUploadDlgBtn, searchField);

        add(hl, fileGrid);
        
        updateFileSearch();
        
//        VerticalLayout left = new VerticalLayout();        
//        left.add(showUploadDlgBtn, folderGrid);
//
////        SlideTab sliderPanel = new SlideTabBuilder(left)
////      		  .caption("White Slider")
////      		  .mode(SlideMode.LEFT)
////      		  .tabPosition(SlideTabPosition.MIDDLE)
////      		  // .style()
////      		  .build();
//
//        ToggleTab sliderPanel = new ToggleTab("Folders", left);
//        
//        VerticalLayout right = new VerticalLayout();
//        right.setWidthFull();
//        right.add(searchField, fileGrid);
//        
////        SplitLayout hl = new SplitLayout(left, right);
////        hl.setOrientation(Orientation.HORIZONTAL);
//        HorizontalLayout hl = new HorizontalLayout();
//        hl.setWidthFull();
//        hl.add(sliderPanel, right);
////        hl.setFlexGrow(1.0, left);
////        hl.setFlexGrow(2.0, right);
//        
//        add(hl);

	}

	
	public void configureFileGridContextMenu() {
		GridContextMenu<Path> contextMenu = fileGrid.addContextMenu();
		
        contextMenu.setDynamicContentHandler(tipPath -> {
        	contextMenu.removeAll();
//        	Path ap = activePath.get();
//        	Path p = ap == null ? path : path.resolve(ap);
////            Path fileRepoRootPath = groupMgr.getBasePath();
//            Path absPath = p.resolve(tipPath);
//            Path relPath = path.relativize(absPath);
			Path absPath = path.resolve(activePath.getOrDefault(path)).resolve(tipPath);
			Path relPath = path.relativize(absPath);

            contextMenu.addItem("Actions for " + relPath.toString()).setEnabled(false);
            contextMenu.add(new Hr());

            int numOptions = 0;

            numOptions += addExtraOptions(contextMenu, relPath);
            
            // Delete action
            
            contextMenu.addItem("Delete", ev -> {
                ConfirmDialog dialog = VaadinDialogUtils.confirmDialog("Confirm delete",
                        "You are about to delete: " + relPath,
                        "Delete", x -> {
                            InvokeUtils.invoke(() -> Files.delete(absPath), e -> {
                                // TODO Show a notification if delete failed
                            });
                            updateFileSearch();
                        }, "Cancel", x -> {});
                //dialog.setConfirmButtonTheme("error primary");
                dialog.open();
            });
            ++numOptions;
            
            
            if (numOptions == 0) {
                contextMenu.addItem("(no actions available)").setEnabled(false);
            }

            return true;
        });

	}

	public int addExtraOptions(GridContextMenu<Path> contextMenu, Path relPath) {
		return 0;
	}
	
    public static Entity createProvenanceData(
            Resource inputEntity,
            JobInstance jobInstance,
            Resource outputEntity) {

        Model outModel = outputEntity.getModel();
        outModel.add(jobInstance.getModel());
        Entity derivedEntity = outputEntity.as(Entity.class);

        QualifiedDerivation qd = derivedEntity.addNewQualifiedDerivation();
        qd.setEntity(inputEntity);
        Activity activity = qd.getOrSetHadActivity();

        activity.setHadPlan(jobInstance);
        // Plan plan = activity.getOrSetHadPlan();
        // JobInstance ji = plan.as(JobInstance.class);

        // FIXME Get the job iris from the list; requires a bit of refactoring
//        String jobIri = transformFileRelPaths.get(0).getURI();
//        NodeRef jobRef = NodeRef.createForFile(outModel, jobIri, null, null);
//        ji.setJobRef(jobRef);

        return derivedEntity;
    }

//	public Dialog createGtfsValidateDialog(Path basePath, Path relInPath) {
//		Path gtfsInPath = basePath.resolve(relInPath);
//		String filename = gtfsInPath.getFileName().toString();
//		Path relOutPath = relInPath.resolveSibling(filename + ".report.ttl");
//		Path gtfsOutPath = basePath.resolve(relOutPath);
//	
//		Model m = ModelFactory.createDefaultModel();
//		Entity result = createProvenanceData(
//				m.createResource(relInPath.toString()),
//				m.createResource("urn:gftsValidator").as(JobInstance.class),
//				m.createResource(relOutPath.toString()));
//		return result;
//	}


	
    
    public void updateFileSearch() {
    	Path ap = activePath.get();
    	Path p = ap == null ? path : path.resolve(ap);

    	changeFolderBtn.setText(pathToString2(path).apply(ap));

        updateFileSearch((TreeDataProvider<Path>)fileGrid.getDataProvider(), p, searchField.getValue(), recursiveSearchCb.getValue());
    }


    
    public static TreeGrid<Path> createFolderGrid(Path fileRepoRootPath) {
    	
        HierarchicalConfigurableFilterDataProvider<Path, Void, String> folderDataProvider =
                HierarchicalDataProviderForPath.createForFolderStructure(fileRepoRootPath).withConfigurableFilter();

    	Function<Path, String> pathToString = pathToString(fileRepoRootPath);
    	
        TreeGrid<Path> folderTreeGrid = new TreeGrid<>();
	    //folderTreeGrid.setSizeFull();

	    Column<?> hierarchyColumn = folderTreeGrid.addHierarchyColumn(path -> {
            // System.out.println(path);
            // return "" + Optional.ofNullable(path).map(Path::getFileName).map(Object::toString).orElse("");
            return pathToString.apply(path);
            // return path.toString();
        });
	    // folderTreeGrid.addColumn(p -> "");
        hierarchyColumn.setResizable(true);
        hierarchyColumn.setFrozen(true);
    	
        folderTreeGrid.setDataProvider(folderDataProvider);

        
        folderTreeGrid.addItemClickListener(ev -> {
        	Path item = ev.getItem();
        	if (!folderTreeGrid.isExpanded(item)) {
        		folderTreeGrid.expand(item);
        	}
    	});
        
        return folderTreeGrid;
    }

    public static Function<Path, String> pathToString(Path fileRepoRootPath) {
        return path -> path == null ? "(null)" : (fileRepoRootPath.equals(path) ? "/" : path.getFileName().toString());
    }

    public static Function<Path, String> pathToString2(Path fileRepoRootPath) {
        return path -> path == null ? "(null)" : (fileRepoRootPath.equals(path) ? "/" : fileRepoRootPath.relativize(path).toString());
    }


    public static TreeGrid<Path> createFileGrid(TreeDataProvider<Path> treeDataProvider, Path fileRepoRootPath) {
	
	    TreeGrid<Path> fileGrid = new TreeGrid<>(treeDataProvider);
	    //fileGrid.setSizeFull();
        Column<?> hierarchyColumn = fileGrid.addHierarchyColumn(path -> {
            // System.out.println(path);
            // return "" + Optional.ofNullable(path).map(Path::getFileName).map(Object::toString).orElse("");
            return Objects.toString(path); //fileRepoRootPath.relativize(path));
            // return path.toString();
        });
        hierarchyColumn.setResizable(true);
        hierarchyColumn.setFrozen(true);
 	    
	    return fileGrid;
    }
    
    
    
    public static void updateFileSearch(TreeDataProvider<Path> treeDataProvider, Path fileRepoRootPath, String value, boolean recursive) {

        TreeData<Path> treeData = treeDataProvider.getTreeData();

        String str = Optional.ofNullable(value).orElse("");
        String v = str.isBlank() ? "*" : str;


        // PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:" + v);

        Pattern pattern = Pattern.compile(buildPattern(v), Pattern.CASE_INSENSITIVE);
        // Pattern.compile(v, Pattern.LITERAL);

        PathMatcher pathMatcher = path -> pattern.matcher(path.toString()).find();
        // dataProvider.setFilter(v);


        try {
            TreeData<Path> td = new TreeData<>();

            Stream<Path> stream = fileRepoRootPath == null || !Files.exists(fileRepoRootPath)
                    ? Stream.empty()
                    : recursive
                    	? Files.walk(fileRepoRootPath)
                    	: Files.list(fileRepoRootPath);
                    		
            List<Path> matches = stream
            		.filter(Files::isRegularFile)
            		.filter(pathMatcher::matches)
            		.map(fileRepoRootPath::relativize)
            		.collect(Collectors.toList());

            // List<Path> matches = FileUtils.listPaths(repoRootPath, v);

            for (Path path : matches) {
                // add(path, repoRootPath, td);
                td.addItem(null, path);
            }

            TreeDataSynchronizer.sync(td, treeData);
            treeDataProvider.refreshAll();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void add(Path path, Path basePath, TreeData<Path> treeData) {
        if (!treeData.contains(path)) {
            Path parent = path.getParent();

            if (parent != null && parent.startsWith(basePath)) {
                Path effParent = parent.equals(basePath) ? null : parent;

                add(effParent, path, treeData);

                treeData.addItem(effParent, path);
            }

        }
    }
    
    public static String buildPattern(String str) {
        StringBuilder sb = new StringBuilder();

        String quotedAsterisk = Pattern.quote("*");

        boolean escaped = false;
        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);

            if (c == '*') {
                if (!escaped) {
                    escaped = true;
                } else {
                    sb.append(quotedAsterisk);
                    escaped = false;
                }
            } else {
                if (escaped) {
                    sb.append(".*");
                    escaped = false;
                }
                sb.append(Pattern.quote(Character.toString(c)));
            }
        }

        if (escaped) {
            sb.append(".*");
            // escaped = false;
        }


        return sb.toString();
    }

}
