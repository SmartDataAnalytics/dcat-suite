package org.aksw.dcat_suite.app.vaadin.view;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.rx.script.SparqlScriptProcessor;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.util.SparqlStmtUtils;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Given a set of files corresponding to sparql queries,
 * scan for mentioned variables
 *
 *
 *
 * @author raven
 *
 */
public class NewConjureWorkflowComponent
    extends FormLayout
{

    protected TextField tagField;
    protected TextField fileNameField;

    protected Map<Var, TextField> varToField = new HashMap<>();

    public NewConjureWorkflowComponent() {

    }


    public TextField getFileNameField() {
        return fileNameField;
    }

    public String getFileName() {
        return getFileNameField().getValue();
    }

    public static Collection<SparqlStmt> getSparqlStmts(Collection<Path> files) {
        SparqlScriptProcessor processor = SparqlScriptProcessor.createPlain(DefaultPrefixes.get(), null);
        processor.processPaths(files);
        return processor.getPlainSparqlStmts();
    }

    public Map<Var, Expr> getDefaultBindings() {
        Map<Var, Expr> result = varToField.entrySet().stream()
            .filter(e -> !e.getValue().getValue().isBlank())
            .collect(Collectors.toMap(
                e -> Var.alloc(e.getKey()),
                e -> ExprUtils.parse(e.getValue().getValue())
        ));

        return result;
    }

    public void refresh(Collection<SparqlStmt> sparqlStmts) {
        Set<String> rawNames = SparqlStmtUtils.getMentionedEnvVars(sparqlStmts);
        List<String> names = new ArrayList<>(rawNames);
        Collections.sort(names);


        removeAll();

        FormLayout formLayout = this; //new FormLayout();

        tagField = new TextField();
        fileNameField = new TextField();

        formLayout.addFormItem(tagField, "Tag");


        Map<String, TextField> nameToField = new HashMap<>();
        if (!names.isEmpty()) {

            for (String name : names) {
                TextField varField = new TextField();
                nameToField.put(name, varField);

                formLayout.addFormItem(varField, name);
            }
        }

        formLayout.addFormItem(fileNameField, "Output file");


        // add(formLayout);
    }
}
