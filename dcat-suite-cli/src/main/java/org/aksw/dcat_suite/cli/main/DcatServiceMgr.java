package org.aksw.dcat_suite.cli.main;

import java.util.Collections;

import org.hobbit.core.service.docker.impl.docker_client.DockerServiceDockerClient;
import org.hobbit.core.service.docker.impl.docker_client.DockerServiceSystemDockerClient;

import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.exceptions.DockerCertificateException;

/**
 * This class just holds work in progress code.
 * 
 * We need:
 * - Some interface to abstract simple service management.
 * - VendorSpecific Driver implementations
 * - A system to describe docker images with additional RDF metadata, such as
 *   "tenforce/virtuoso: endpoint at /sparql, db folder /usr/lib/whatever
 * 
 * Nice to have
 * - Some additional interface to abstract service configuration, such as memory or update suppor for sparql endpoints 
 * 
 * 
 * 
 * 
 * @author raven
 *
 */
public class DcatServiceMgr {
	public static void createService() throws DockerCertificateException {
		DockerServiceSystemDockerClient dss = DockerServiceSystemDockerClient.create(true, Collections.emptyMap(), Collections.emptySet());
		//DockerClient dockerClient = dss.getDockerClient();
		
		//conn = DatasetFactory.wrap(model);
		
		//logger.info("Attempting to starting a virtuoso from docker");
		DockerServiceDockerClient dsCore = dss.create("tenforce/virtuoso", ImmutableMap.<String, String>builder()
				.put("SPARQL_UPDATE", "true")
				.put("DEFAULT_GRAPH", "http://www.example.org/")
				.put("VIRT_Parameters_NumberOfBuffers", "170000")
				.put("VIRT_Parameters_MaxDirtyBuffers", "130000")
				.put("VIRT_Parameters_MaxVectorSize", "1000000000")
				.put("VIRT_SPARQL_ResultSetMaxRows", "1000000000")
				.put("VIRT_SPARQL_MaxQueryCostEstimationTime", "0")
				.put("VIRT_SPARQL_MaxQueryExecutionTime", "600")
				.build());
						
//		DockerService ds = ComponentUtils.wrapSparqlServiceWithHealthCheck(dsCore, 8890);

		dsCore.startAsync().awaitRunning();

		
	}
}
