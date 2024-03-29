package org.aksw.dcat_suite.app.vaadin.view;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collection.observable.ObservableValue;
import org.aksw.commons.collection.observable.ObservableValueImpl;
import org.aksw.jena_sparql_api.conjure.fluent.JobUtils;
import org.aksw.jena_sparql_api.conjure.job.api.Job;
import org.aksw.jena_sparql_api.conjure.job.api.JobInstance;
import org.aksw.jenax.model.prov.Activity;
import org.aksw.jenax.model.prov.Entity;
import org.aksw.jenax.model.prov.QualifiedDerivation;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.claspina.confirmdialog.ConfirmDialog;

import com.github.jsonldjava.shaded.com.google.common.io.MoreFiles;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
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
		fileGrid.setSelectionMode(SelectionMode.MULTI);
		fileGrid.setDetailsVisibleOnClick(true);
		
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

        
        // This button does not really belong here;
        // The FileBrowser should have an extensible getToolBar() method
        Button generateWorkflowBtn = new Button(VaadinIcon.WRENCH.create());
        generateWorkflowBtn.addClickListener(ev -> {
        	List<Path> paths = toAbsPaths(fileGrid.getSelectedItems());
        	Collection<SparqlStmt> sparqlStmts = NewConjureWorkflowComponent.getSparqlStmts(paths);
        	
        	Dialog dlg = new Dialog();
        	NewConjureWorkflowComponent content = new NewConjureWorkflowComponent();
        	content.refresh(sparqlStmts);
        	dlg.add(content);

        	
        	Button okBtn = new Button("Generate");
        	okBtn.addClickListener(ev2 -> {        		
        		
        		Set<String> optionalArgs = new HashSet<>();
        		Map<Var, Expr> bindingMap = content.getDefaultBindings();
        		
        		Job job = JobUtils.fromSparqlStmts(sparqlStmts, optionalArgs, bindingMap);
        		
        		Path outFile = activeAbsPath().resolve(content.getFileName());
        		try (OutputStream out = Files.newOutputStream(outFile)) {
        			RDFDataMgr.write(out, job.getModel(), RDFFormat.TURTLE_BLOCKS);
        		} catch (IOException e) {
        			throw new RuntimeException(e);
				}
        		dlg.close();
        	});
        	dlg.add(okBtn);
        	
        	dlg.open();
        });

        
        Button moveBtn = new Button(VaadinIcon.FILE_O.create());
        moveBtn.setEnabled(false);
        
		fileGrid.addSelectionListener(ev -> {
			Set<Path> pathTips = ev.getAllSelectedItems();

			moveBtn.setEnabled(!pathTips.isEmpty());
			
		});

        moveBtn.addClickListener(ev -> {
        	VerticalLayout content = new VerticalLayout();
        	Button newFolderBtn = new Button(VaadinIcon.FOLDER_ADD.create());
        	content.add(newFolderBtn);

        	Grid<Path> grid = createFolderGrid(path);

        	newFolderBtn.addClickListener(ev2 -> {
        		Path ap = grid.getSelectionModel().getFirstSelectedItem().orElse(null);
            	Path p = ap == null ? path : path.resolve(ap);

            	VaadinDialogUtils.confirmInputDialog("Create Folder", "Name", "Create", name -> {
            		try {
            			Files.createDirectory(p.resolve(name));
            			grid.getDataProvider().refreshAll();
                		folderGrid.getDataProvider().refreshAll();
            		} catch (Exception e) {
            			throw new RuntimeException(e);
            		}
    			}, "Cancel", ev3 -> {}).open();
        	});
        	
        	
        	content.add("Select a new current folder");
        	content.add(grid);

        	VaadinDialogUtils.confirmDialog("Move Files", content, "Move", ev2 -> {
        		Path item = grid.getSelectionModel().getFirstSelectedItem().orElse(null);
        		if (item != null) {
                	Path tgt = path.resolve(item);

        			for (Path src : fileGrid.getSelectedItems()) {
        				// TODO resolve src
        				System.out.println("Moving " + src + " to " + tgt);
        			}
        		}
        		
        	}, "Abort", ev3 -> {}).open();
        });

        

        folderGrid.setMinWidth(30, Unit.EM);
        folderGrid.setMinHeight(30, Unit.EM);
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
        hl.add(changeFolderBtn, showUploadDlgBtn, moveBtn, generateWorkflowBtn, searchField);

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
	
	
	/** Convert the paths of the file grid to abs paths - allows for opening them*/
	public List<Path> toAbsPaths(Collection<Path> tipPaths) {
		List<Path> result = tipPaths.stream().map(this::tipPathToAbsPath).collect(Collectors.toList());
		return result;
	}

	public Path activeAbsPath() {
		Path activeAbsPath = path.resolve(activePath.getOrDefault(path));
		return activeAbsPath;
	}

	/** Return the absolute path for a path in the file grid */
	public Path tipPathToAbsPath(Path tipPath) {
		Path absPath = activeAbsPath().resolve(tipPath);
		return absPath;
	}

	/** Return the path relative to the repo root for a path in the file grid */
	public Path tipPathtoRelPath(Path tipPath) {
		Path absPath = tipPathToAbsPath(tipPath);
		Path relPath = path.relativize(absPath);
		return relPath;
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
    	
    	folderGrid.getDataProvider().refreshAll();

        updateFileSearch((TreeDataProvider<Path>)fileGrid.getDataProvider(), p, searchField.getValue(), recursiveSearchCb.getValue());
    }


    
    public TreeGrid<Path> createFolderGrid(Path fileRepoRootPath) {
    	TreeGrid<Path> folderGrid = createCoreFolderGrid(fileRepoRootPath);
    	
        folderGrid.setSelectionMode(SelectionMode.SINGLE);
        
        GridContextMenu<Path> folderContextMenu = folderGrid.addContextMenu();
        folderContextMenu.setDynamicContentHandler(tipPath -> {
        	folderContextMenu.removeAll();
//        	Path ap = activePath.get();
//        	Path p = ap == null ? path : path.resolve(ap);
////            Path fileRepoRootPath = groupMgr.getBasePath();
//            Path absPath = p.resolve(tipPath);
//            Path relPath = path.relativize(absPath);
			Path absPath = path.resolve(activePath.getOrDefault(path)).resolve(tipPath);
			Path relPath = path.relativize(absPath);

			folderContextMenu.addItem("Actions for " + relPath.toString()).setEnabled(false);
			folderContextMenu.add(new Hr());

            int numOptions = 0;
            
            // Delete action
            
            folderContextMenu.addItem("Delete", ev -> {
                ConfirmDialog dialog = VaadinDialogUtils.confirmDialog("Confirm delete",
                        "You are about to RECURSIVELY delete: " + relPath,
                        "Delete", x -> {
                            InvokeUtils.invoke(() -> MoreFiles.deleteRecursively(absPath), e -> {
                                // TODO Show a notification if delete failed
                            });
                            updateFileSearch();
                        }, "Cancel", x -> {});
                //dialog.setConfirmButtonTheme("error primary");
                dialog.open();
            });
            ++numOptions;
            
            
            if (numOptions == 0) {
                folderContextMenu.addItem("(no actions available)").setEnabled(false);
            }

            return true;
        });
        
        return folderGrid;
    }
    
    public static TreeGrid<Path> createCoreFolderGrid(Path fileRepoRootPath) {
    	
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
