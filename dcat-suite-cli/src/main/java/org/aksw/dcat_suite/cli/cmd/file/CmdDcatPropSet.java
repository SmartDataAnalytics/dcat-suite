package org.aksw.dcat_suite.cli.cmd.file;

import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.dcat.jena.conf.api.DcatRepoConfig;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.system.Txn;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * Set properties of a local repository.
 *
 * e.g. dcat set groupId org.example.mygroup
 *
 */
@Command(name = "set", separator = "=", description="Set properties of a local repository", mixinStandardHelpOptions = true)
public class CmdDcatPropSet
    implements Callable<Integer>
{
    @Parameters(arity = "2")
    public List<String> keyAndValue;

    @Override
    public Integer call() throws Exception {
        DcatRepoLocal repo = DcatRepoLocalUtils.findLocalRepo();
        Dataset conf = repo.getConfig();

        String key = keyAndValue.get(0);
        String value = keyAndValue.get(1);

        Txn.executeWrite(conf, () -> {
            DcatRepoConfig c = repo.getConfigResource(conf).as(DcatRepoConfig.class);
            c.getProperties().put(key, value);
        });

        return 0;
    }
}
