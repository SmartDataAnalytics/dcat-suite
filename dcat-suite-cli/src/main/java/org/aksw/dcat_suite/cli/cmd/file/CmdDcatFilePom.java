package org.aksw.dcat_suite.cli.cmd.file;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Callable;

import org.aksw.commons.util.string.Envsubst;
import org.apache.commons.io.IOUtils;

import picocli.CommandLine.Command;

@Command(name = "pom", separator = "=", description="Prepare a maven project from the dataset description", mixinStandardHelpOptions = true)
public class CmdDcatFilePom
    implements Callable<Integer>
{
    protected String buildDirName = "target";


    @Override
    public Integer call() throws Exception {
        Path pwd = Path.of("").toAbsolutePath();


        Path buildAbsPath = pwd.resolve(buildDirName);

        String str;
        try (InputStream in = CmdDcatFilePom.class.getClassLoader().getResourceAsStream("dcat.pom.xml.template")) {
            Objects.requireNonNull(in);
            str = IOUtils.toString(in, StandardCharsets.UTF_8);
        }

        // Envsubst.envsubst(str, );

        return 0;
    }

}
