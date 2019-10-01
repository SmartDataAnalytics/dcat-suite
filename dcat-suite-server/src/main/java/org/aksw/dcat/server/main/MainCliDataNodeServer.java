package org.aksw.dcat.server.main;

import org.aksw.dcat.server.config.ServerConfig;
import org.aksw.dcat.server.controller.ControllerLookup;
import org.apache.jena.query.ARQ;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackageClasses = {ControllerLookup.class, ServerConfig.class})
public class MainCliDataNodeServer {
	public static void main(String[] args) throws Exception {
		ARQ.enableBlankNodeResultLabels();
		ARQ.setFalse(ARQ.constantBNodeLabels);
		
		SpringApplication.run(MainCliDataNodeServer.class, args);
	}
}
