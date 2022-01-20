package org.aksw.dcat_suite.cli.cmd.file;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.aksw.dcat.mgmt.api.DataProject;
import org.aksw.dcat.mgmt.vocab.DCATX;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.RDF;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Add a new data project to the current dcat repository.
 * A data project is identified by a (default) group id which is inherited by
 * its referenced datasets.
 *
 * Data projects hold a list of 'owned' datasets which allows for specific orders.
 *
 */
@Command(name = "add", separator = "=", description="Add a data projects to the local dcat repository", mixinStandardHelpOptions = true)
public class CmdDcatFileProjectAdd
    implements Callable<Integer>
{
    @Option(names= {"-g", "--groupId"}, arity = "1", required = true)
    public String groupId;

    @Override
    public Integer call() throws Exception {
        DcatRepoLocal repo = DcatRepoLocalUtils.findLocalRepo();

        Dataset repoDs = repo.getDataset();
        repoDs.begin(ReadWrite.WRITE);

        Model model = repoDs.getDefaultModel();
        List<DataProject> projects = DcatRepoLocalUtils.listDataProjects(model);

        // Check that the given group id is unique
        List<DataProject> conflicts = projects.stream()
            .filter(p -> Optional.ofNullable(p.getDefaultGroupId()).map(id -> id.equals(groupId)).orElse(false))
            .collect(Collectors.toList());

        if (!conflicts.isEmpty()) {
            throw new RuntimeException("There already exists a project with group id " + groupId + ": " + conflicts);
        }

        DataProject dataProject = model.createResource("#" + groupId).as(DataProject.class)
                .setDefaultGroupId(groupId);

        dataProject.addProperty(RDF.type, DCATX.DataProject);

        repoDs.commit();
        return 0;
    }
}
