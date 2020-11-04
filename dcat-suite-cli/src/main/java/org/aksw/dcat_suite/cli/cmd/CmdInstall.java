package org.aksw.dcat_suite.cli.cmd;

import java.util.concurrent.Callable;
import java.util.function.Function;

import org.aksw.ckan_deploy.core.DcatCkanRdfUtils;
import org.aksw.ckan_deploy.core.DcatInstallUtils;
import org.aksw.ckan_deploy.core.DcatRepository;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.utils.DcatUtils;
import org.aksw.dcat_suite.cli.main.MainCliDcatSuite;
import org.apache.jena.rdf.model.Model;

import com.beust.jcommander.Parameter;

import picocli.CommandLine.Command;

@Command(name = "install", description = "Download datasets to local repository based on DCAT information")
public class CmdInstall
    implements Callable<Integer>
{
    @Parameter(description = "A DCAT file")
    protected String file;

    @Override
    public Integer call() throws Exception {
        CmdInstall cmInstall = this;

        String dcatSource = cmInstall.file;

        Model dcatModel = DcatCkanRdfUtils.createModelWithNormalizedDcatFragment(cmInstall.file);
        Function<String, String> iriResolver = MainCliDcatSuite.createIriResolver(dcatSource);
        DcatRepository dcatRepository = MainCliDcatSuite.createDcatRepository();

        //for(dcatModel.listSubjects(null, DCAT.distribution, null)

        for(DcatDataset dcatDataset : DcatUtils.listDcatDatasets(dcatModel)) {
            DcatInstallUtils.install(dcatRepository, dcatDataset, iriResolver, false);
        }
        return 0;
    }


}