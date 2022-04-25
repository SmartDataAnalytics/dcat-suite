package org.aksw.dcat_suite.cli.cmd.file;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.aksw.dcat.jena.domain.api.MavenEntityCore;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "convert", description="Convert artifact id to path and file name", mixinStandardHelpOptions = true)
public class CmdDcatMvnConvert
    implements Callable<Integer>
{
    @Parameters
    public String mvnIdStr;

    @Option(names = { "-d", "--directories"})
    public boolean outputDirectories;

    @Option(names = { "-f", "--filename"})
    public boolean outputFilename;

    @Option(names = { "-s", "--separator"}, description = "Separator; defaults to ${DEFAULT-VALUE}", defaultValue = "/")
    public String separator;


    @Override
    public Integer call() throws Exception {

        MavenEntityCore mvnId = MavenEntityCore.parse(mvnIdStr);

        if (!outputDirectories && !outputFilename) {
            outputDirectories = true;
            outputFilename = true;
        }

        String pathStr = outputDirectories ? MavenEntityCore.toPath(mvnId) : null;
        String fileName = outputFilename ? MavenEntityCore.toFileName(mvnId) : null;

        String result = Arrays.asList(pathStr, fileName).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.joining(separator));

        System.out.println(result);

        return 0;
    }

}
