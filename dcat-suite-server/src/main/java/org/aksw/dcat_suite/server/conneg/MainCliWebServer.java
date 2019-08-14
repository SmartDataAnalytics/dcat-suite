package org.aksw.dcat_suite.server.conneg;

import org.apache.jena.query.ARQ;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainCliWebServer {
	public static void main(String[] args) throws Exception {
		ARQ.enableBlankNodeResultLabels();
		ARQ.setFalse(ARQ.constantBNodeLabels);

		SpringApplication.run(MainCliWebServer.class, args);
	}
}
