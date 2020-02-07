package org.aksw.jena_sparql_api.vocab;

/**
 * Constants for the W3C Data Catalog Vocabulary.
 *
 * @see <a href="https://www.w3.org/TR/vocab-dcat/">Data Catalog Vocabulary</a>
 */
public class DCAT {
//	private static final Model m = ModelFactory.createDefaultModel();
//
//"http://www.w3.org/ns/dcat#";
//	public static final Resource NAMESPACE = m.createResource(NS);
//	
//	/**
//	 * Returns the URI for this schema
//	 * @return URI
//	 */
//	public static String getURI() {
//		return NS;
//	}
//	
//	// Classes
//	public static final Resource Catalog = m.createResource(NS + "Catalog");
//	public static final Resource CatalogRecord = m.createResource(NS + "CatalogRecord");
//	public static final Resource Dataset = m.createResource(NS + "Dataset");
//	public static final Resource Distribution = m.createResource(NS + "Distribution");
//	
//	// Properties
//	public static final Property accessURL = ResourceFactory.createProperty(Strs.accessURL);
//	public static final Property byteSize = ResourceFactory.createProperty(Strs.byteSize);
//	public static final Property contactPoint = ResourceFactory.createProperty(Strs.contactPoint);
//	public static final Property dataset = ResourceFactory.createProperty(Strs.dataset);
//	public static final Property distribution = ResourceFactory.createProperty(Strs.distribution);
//	public static final Property downloadURL = ResourceFactory.createProperty(Strs.downloadURL);
//	public static final Property keyword = ResourceFactory.createProperty(Strs.keyword);	
//	public static final Property landingPage = ResourceFactory.createProperty(Strs.landingPage);
//	public static final Property mediaType = ResourceFactory.createProperty(Strs.mediaType);
//	public static final Property record = ResourceFactory.createProperty(Strs.record);
//	public static final Property theme = ResourceFactory.createProperty(Strs.theme);
//	public static final Property themeTaxonomy = ResourceFactory.createProperty(Strs.themeTaxonomy);
//	
	public static class Strs {
		public static final String NS = org.apache.jena.vocabulary.DCAT.NS; 

		// Classes
		public static final String Catalog = NS + "Catalog";
		public static final String CatalogRecord = NS + "CatalogRecord";
		public static final String Dataset = NS + "Dataset";
		public static final String Distribution = NS + "Distribution";
		
		// Properties
		public static final String accessURL = NS + "accessURL";
		public static final String byteSize = NS + "byteSize";
		public static final String contactPoint = NS + "contactPoint";
		public static final String dataset = NS + "dataset";
		public static final String distribution = NS + "distribution";
		public static final String downloadURL = NS + "downloadURL";
		public static final String keyword = NS + "keyword";	
		public static final String landingPage = NS + "landingPage";
		public static final String mediaType = NS + "mediaType";
		public static final String record = NS + "record";
		public static final String theme = NS + "theme";
		public static final String themeTaxonomy = NS + "themeTaxonomy";
	}
}
