package org.aksw.rdf_view;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainTestDcatRepo {
	public static void main(String[] args) throws IOException {
		Path link = Paths.get("/tmp/test.link");
		Path target = Paths.get("/tmp/pavel.trig");
		Files.createSymbolicLink(link, target);
	}
}
