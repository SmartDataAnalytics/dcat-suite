package org.aksw.ckan_deploy.core;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.aksw.commons.service.core.SimpleProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathTransformLbZip
	implements PathTransform
{
	private static final Logger logger = LoggerFactory.getLogger(PathTransformLbZip.class);
	
	public static void main(String[] args) throws Exception {
		
		PathTransformLbZip test = new PathTransformLbZip();
		System.out.println("Cmd exists? " + test.cmdExists());
		
		test.transform(Paths.get("/tmp/test.txt.bz2"), Paths.get("/tmp/hello.txt"));
	}
	
	public boolean cmdExists() {
		String[] cmd = {
			"/bin/sh",
			"-c",
			"lbzip2 --version"
		};

		ProcessBuilder processBuilder = new ProcessBuilder(cmd);

		boolean result;
		try {
			logger.trace("Checking availability of system command 'lbzip2'");
			Process p = SimpleProcessExecutor.wrap(processBuilder)
	            .setOutputSink(logger::trace)	
	            .execute();
	        
		
	        int exitValue = p.exitValue();
	        result = exitValue == 0;
		} catch(Exception e) {
			logger.debug("System command 'lbzip2' not available", e);
			result = false;
			
		}
        return result;
	}
	
	@Override
	public void transform(Path input, Path output) throws IOException, InterruptedException {

		String i = input.toString().replace("'", "\\'");
		String o = output.toString().replace("'", "\\'");
		String[] cmd = {
			"/bin/sh",
			"-c",
			"lbzip2 -cdk '" + i + "' > " + "'" + o + "'"
		};

		System.out.println(Arrays.asList(cmd));
		
		ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        SimpleProcessExecutor.wrap(processBuilder)
            .setOutputSink(logger::info) //System.out::println) //logger::debug)
            .execute();

	
	}
	
}
