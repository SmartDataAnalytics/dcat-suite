package org.aksw.dcat.repo.impl.fs;

import java.io.InputStream;
import java.util.Optional;

import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.aksw.dcat.repo.api.DistributionResolver;
import org.aksw.dcat.repo.impl.cache.CatalogResolverCaching;
import org.aksw.dcat.repo.impl.ckan.CatalogResolverCkan;

import eu.trentorise.opendata.jackan.CkanClient;
import io.reactivex.Maybe;

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
public class DistributionResolverFilesystem
	implements DistributionResolver
{
	protected CatalogResolverFilesystem crf;
	
	public static void main(String[] args) throws Exception {
		System.out.println("yay");
		CkanClient ckanClient = new CkanClient("http://ckan.qrowd.aksw.org", "25b91078-fbc6-4b3a-93c5-acfce414bbeb");
			
		
		//String id = "http://ckan.qrowd.aksw.org/dataset/8bbb915a-f476-4749-b441-5790b368c38b/resource/fb3fed1f-cc9a-4232-a876-b185d8e002c8/download/osm-bremen-2018-04-04-ways-amenity.sorted.nt.bz2";
		//String id = "fb3fed1f-cc9a-4232-a876-b185d8e002c8";
		//String id = "http://dcat.linkedgeodata.org/distribution/osm-bremen-2018-04-04-ways-amenity";
		
		String id = "org-linkedgeodata-osm-bremen-2018-04-04";
		CatalogResolver cr = new CatalogResolverCaching(
				CatalogResolverFilesystem.createDefault(),
				new CatalogResolverCkan(ckanClient, "http://foo"));
		
		DatasetResolver dr = cr.resolveDataset(id).blockingGet();
		
		String dist = dr.getDataset().getDistributions().iterator().next().getURI();
		dr.resolveDistribution(dist);
		System.out.println(dr);
	}

	protected DatasetResolver datasetResolver;
	protected DcatDistribution dcatDistribution;
	
	public DistributionResolverFilesystem(DatasetResolver datasetResolver, DcatDistribution dcatDistribution) {
		super();
		this.datasetResolver = datasetResolver;
		this.dcatDistribution = dcatDistribution;
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
		
		

		return null;
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

