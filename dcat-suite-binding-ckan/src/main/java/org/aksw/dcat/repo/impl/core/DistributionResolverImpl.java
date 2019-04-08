package org.aksw.dcat.repo.impl.core;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.aksw.ckan_deploy.core.DcatDeployVirtuosoUtils;
import org.aksw.dcat.ckan.config.model.DcatResolverCkan;
import org.aksw.dcat.ckan.config.model.DcatResolverConfig;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.aksw.dcat.repo.api.DistributionResolver;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.tdb2.TDB2Factory;
import org.hobbit.core.service.docker.impl.docker_client.DockerServiceSystemDockerClient;

import com.spotify.docker.client.DockerClient;

import eu.trentorise.opendata.commons.internal.org.apache.commons.lang3.SystemUtils;
import virtuoso.jdbc4.VirtuosoDataSource;

/**
 * 
 * About distributions: I think they should be first class entities (regardless of the dataset)
 * So they will go under .dcat/repository/distributions/...
 * 
 * However, for each distribution, we can have
 * - symlinks to the containing datasets, e.g. _datasets/content{1,... n}
 * - a data folder for the files e.g. _data
 * 
 * 
 */









//interface DistributionResolver {
//	DatasetResolver getDatasetResolver();
//	
//}


//
public class DistributionResolverImpl
	implements DistributionResolver
{
	protected DatasetResolver datasetResolver;
	protected DcatDistribution dcatDistribution;

	transient protected CatalogResolver crf;

	
	public static void main(String[] args) throws Exception {
		Dataset ds = TDB2Factory.connectDataset(SystemUtils.USER_HOME + "/.dcat/db.tdb2");
		try(RDFConnection conn = RDFConnectionFactory.connect(ds)) {
			
			JenaPluginUtils.registerJenaResourceClasses(DcatResolverConfig.class);
			JenaPluginUtils.registerJenaResourceClasses(DcatResolverCkan.class);
			
			CatalogResolver cr = CatalogResolverUtils.createCatalogResolverDefault();
			
			String id = "org-linkedgeodata-osm-bremen-2018-04-04";
			DatasetResolver dr = cr.resolveDataset(id).blockingGet();

			String dist = dr.getDataset().getDistributions().iterator().next().getURI();
			Collection<DistributionResolver> drs = dr.resolveDistribution(dist).toList().blockingGet();
			System.out.println(drs);

			System.out.println("Result:");
			URL url = cr
				.resolveDownload("http://ckan.qrowd.aksw.org/dataset/8bbb915a-f476-4749-b441-5790b368c38b/resource/fb3fed1f-cc9a-4232-a876-b185d8e002c8/download/osm-bremen-2018-04-04-ways-amenity.sorted.nt.bz2")
				.blockingGet();
			
//			for(URL url : list) {
				System.out.println(url);
				
				DockerClient dockerClient = DockerServiceSystemDockerClient
						.create(true, Collections.emptyMap(), Collections.emptySet())
						.getDockerClient();
				
				VirtuosoDataSource dataSource = new VirtuosoDataSource();
				dataSource.setPassword("dba");
				dataSource.setUser("dba");
				dataSource.setPortNumber(1111);
				dataSource.setServerName("localhost");
				Connection conne = dataSource.getConnection();
				
				DcatDeployVirtuosoUtils.deploy(
						dr,
						null,
						dockerClient,
						"test",
						Paths.get("/tmp/"),
						Paths.get("/usr/local/virtuoso-opensource/var/lib/virtuoso/db/"),
						true,
						conne);
			//}
			
		} finally {
			ds.close();
		}
	}
	
	public DistributionResolverImpl(DatasetResolver datasetResolver, DcatDistribution dcatDistribution) {
		super();
		this.datasetResolver = datasetResolver;
		this.dcatDistribution = dcatDistribution;
		this.crf = datasetResolver.getCatalogResolver();
		//(CatalogResolverFilesystem)
	}

	@Override
	public DatasetResolver getDatasetResolver() {
		return datasetResolver;
	}

	@Override
	public DcatDistribution getDistribution() {
		return dcatDistribution;
	}

	@Override
	public InputStream open() throws Exception {
		InputStream result = open(Optional.ofNullable(dcatDistribution.getDownloadURL())
				.orElseThrow(() -> new RuntimeException("no download urls on distribution")));
		return result;
	}

	@Override
	public InputStream open(String url) throws Exception {
		// Ensure that the url is among the downloadURLs of the distributions
		
		boolean isValid = dcatDistribution.getDownloadURLs().contains(url);
		if(!isValid) {
			throw new RuntimeException("no such download url");
		}

//		List<URL> urls = crf.resolveDownload(url).blockingGet();
				

		return null;
	}

	@Override
	public Path getPath() {
		Path result;
		
		String datasetId = Optional.ofNullable(getDatasetResolver())
				.map(DatasetResolver::getDataset)
				.map(Resource::getURI)
				.orElse(null);
		
		String distributionId = this.getDistribution().getURI();
		DistributionResolver dr = crf.resolveDistribution(datasetId, distributionId)
				.blockingFirst();
//				.toList()
//				.blockingGet();
		//dr.get
		try {
			String downloadUrl = dr.getDistribution().getDownloadURL();
			URL url = crf.resolveDownload(downloadUrl).blockingGet();
			URI uri = url.toURI();
			result = Paths.get(uri);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
			
		//Path result = crf.resolveDistributionPath(distributionId);
		
		return result;
		//List<URL> urls = crf.resolveDownload(url).toList().blockingGet();
	}
}
//
//	protected DcatRepoConfig repoConfig;
//	protected String catalogFolderName;
//	protected String datasetFolderName;
//	
//	
//	public static void main(String[] args) {
//		System.out.println("yay");
//		CkanClient ckanClient = new CkanClient("http://ckan.qrowd.aksw.org", "");
//
//		//String id = "http://ckan.qrowd.aksw.org/dataset/8bbb915a-f476-4749-b441-5790b368c38b/resource/fb3fed1f-cc9a-4232-a876-b185d8e002c8/download/osm-bremen-2018-04-04-ways-amenity.sorted.nt.bz2";
//		//String id = "fb3fed1f-cc9a-4232-a876-b185d8e002c8";
//		String id = "http://dcat.linkedgeodata.org/distribution/osm-bremen-2018-04-04-ways-amenity";
//		CkanResource r = ckanClient.getResource(id);
//		System.out.println(r);
//		System.out.println(r.getPackageId());
//	}
//	
//	public Collection<URI> resolveDistribution(
//			Resource dcatDistribution,
//			Function<String, String> iriResolver) throws Exception {
//	
//		return null;
//	}
//	
//	
//}

