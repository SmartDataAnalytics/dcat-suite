package org.aksw.dcat.repo.impl.core;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import org.aksw.dcat.ckan.config.model.DcatResolverCkan;
import org.aksw.dcat.ckan.config.model.DcatResolverConfig;
import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.impl.cache.CatalogResolverCaching;
import org.aksw.dcat.repo.impl.ckan.CatalogResolverCkan;
import org.aksw.dcat.repo.impl.fs.CatalogResolverFilesystem;
import org.aksw.dcat.repo.impl.fs.CatalogResolverMulti;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;

import eu.trentorise.opendata.commons.internal.org.apache.commons.lang3.SystemUtils;
import eu.trentorise.opendata.jackan.CkanClient;

public class CatalogResolverUtils {
	public static CatalogResolver createCatalogResolverDefault() {
		Path dcatPath = Paths.get(SystemUtils.USER_HOME).resolve(".dcat");
		CatalogResolver result = createCatalogResolverDefault(dcatPath);
		return result;
	}
	
	public static CatalogResolver wrapWithDiskCache(CatalogResolver coreCatalogResolver) {
		CatalogResolver result = new CatalogResolverCaching(
				CatalogResolverFilesystem.createDefault(),
				coreCatalogResolver);

		return result;
	}
	
	/**
	 * 
	 * dcatPath is e.g. ~/.dcat
	 * 
	 * @param dcatPath
	 * @return
	 */
	public static CatalogResolver createCatalogResolverDefault(Path dcatPath) {
		//Model configModel = ModelFactory.createDefaultModel();
		String configUrl = dcatPath.resolve("settings.ttl").toUri().toString();
		Model configModel = RDFDataMgr.loadModel(configUrl);
		List<DcatResolverConfig> configs = configModel
				.listResourcesWithProperty(ResourceFactory.createProperty("http://www.example.org/resolvers"))
				.mapWith(r -> r.as(DcatResolverConfig.class))
				.toList();

		CatalogResolverMulti coreResolver = new CatalogResolverMulti();
		
		for(DcatResolverConfig config : configs) {
			Collection<DcatResolverCkan> resolvers = config.resolvers(DcatResolverCkan.class);
			for(DcatResolverCkan ckanResolverSpec : resolvers) {
				System.out.println("Got: " + ckanResolverSpec.getApiKey());	
				System.out.println("Got: " + ckanResolverSpec.getUrl());
				
				
				String ckanApiUrl = ckanResolverSpec.getUrl();
				String ckanApiKey = ckanResolverSpec.getApiKey();
				
//				CkanClient ckanClient = new CkanClient("http://ckan.qrowd.aksw.org", "25b91078-fbc6-4b3a-93c5-acfce414bbeb");
				CkanClient ckanClient = new CkanClient(ckanApiUrl, ckanApiKey);
				CatalogResolver ckanResolver = new CatalogResolverCkan(ckanClient);
				coreResolver.getResolvers().add(ckanResolver);
			}			
		}
		
		
			
		
		//String id = "http://ckan.qrowd.aksw.org/dataset/8bbb915a-f476-4749-b441-5790b368c38b/resource/fb3fed1f-cc9a-4232-a876-b185d8e002c8/download/osm-bremen-2018-04-04-ways-amenity.sorted.nt.bz2";
		//String id = "fb3fed1f-cc9a-4232-a876-b185d8e002c8";
		//String id = "http://dcat.linkedgeodata.org/distribution/osm-bremen-2018-04-04-ways-amenity";
		
		CatalogResolver result = wrapWithDiskCache(coreResolver);

		return result;
	}
}
