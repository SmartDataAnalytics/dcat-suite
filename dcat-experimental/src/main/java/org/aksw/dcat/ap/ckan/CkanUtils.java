package org.aksw.dcat.ap.ckan;

import org.aksw.dcat.ap.jena.domain.api.DcatApAgent;
import org.aksw.dcat.ap.jena.domain.impl.DcatApAgentAccessor;
import org.aksw.dcat.ap.jena.domain.impl.DcatApAgentAsResource;
import org.aksw.dcat.ap.jena.domain.impl.DcatApAgentFromPropertySource;
import org.aksw.dcat.util.view.SetFromCkanExtras;
import org.aksw.dcat.util.view.SingleValuedAccessor;
import org.aksw.dcat.util.view.SingleValuedAccessorFromSet;
import org.aksw.jena_sparql_api.beans.model.EntityModel;
import org.aksw.jena_sparql_api.beans.model.EntityOps;
import org.aksw.jena_sparql_api.beans.model.PropertyOps;
import org.aksw.jena_sparql_api.utils.model.SimpleImplementation;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.system.JenaSystem;
import org.apache.jena.vocabulary.RDF;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;

import eu.trentorise.opendata.jackan.model.CkanDataset;

public class CkanUtils {
	public static void main(String[] args) {
		JenaSystem.init();
		BuiltinPersonalities.model.add(DcatApAgentAsResource.class, new SimpleImplementation(DcatApAgentAsResource::new));

		
		Model m = ModelFactory.createDefaultModel();
		DcatApAgentAsResource v = m.createResource().as(DcatApAgentAsResource.class);

//		v.addProperty(FOAF.homepage, RDF.HTML);
//		v.addProperty(FOAF.homepage, RDF.Property);
		
		
		setValues(v);
		
		RDFDataMgr.write(System.out, m, RDFFormat.TURTLE_PRETTY);
	}
	
	public static void setValues(DcatApAgent v) {
		v.setEntityUri("http://example.org/resource/Foo");
		v.setName("Foo Inc.");
		v.setHomepage("http://foobar");
		v.setMbox("mailto:foo@bar.baz");
		v.setType("Foobar");
	}
	
	public static void createCkanPublisher() {
		CkanDataset d = new CkanDataset();
		
		PropertySource s = new PropertySourceCkanDataset(d);
		DcatApAgent v = new DcatApAgentFromPropertySource("extra:publisher_", s);
		//new DcatApCkanAgentView("extra:publisher_", s);
		
		setValues(v);
		
		System.out.println(d.getExtrasAsHashMap());
	}
	
	
	/** Some property accessor tests */
	public static void main2(String[] args) {
		CkanDataset d = new CkanDataset();
		
		SingleValuedAccessor<String> a1 = getSingleValuedAccessor(d, "name", String.class);
		a1.set("Hello");

		System.out.println(d.getName());

		
		SingleValuedAccessor<String> a2 = getSingleValuedAccessor(d, "extra:publisher_name", String.class);
		a2.set("World");
		
		System.out.println(d.getExtrasAsHashMap());

		
		SingleValuedAccessor<String> a3 = getSingleValuedAccessor(d, "invalidName", String.class);
		if(a3 != null) {
			a3.set("Hell");
		}
		
		SingleValuedAccessor<Integer> a4 = getSingleValuedAccessor(d, "name", Integer.class);
		if(a4 != null) {
			a4.set(4);
		}

	}
	
	public static <T> SingleValuedAccessor<T> getSingleValuedAccessor(CkanDataset ckanDataset, String namespace, String localName, Class<?> clazz) {
		SingleValuedAccessor<T> result;
		if(namespace.equals("extra")) {
			// FIXME hack ... need a converter in general
			result = (SingleValuedAccessor<T>)new SingleValuedAccessorFromSet<>(new SetFromCkanExtras(ckanDataset, localName));
		} else {

	        ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();
	        bean.afterPropertiesSet();

	        ConversionService conversionService = bean.getObject();
			EntityOps eops = EntityModel.createDefaultModel(CkanDataset.class, conversionService);

			PropertyOps pops = eops.getProperty(localName);
			if(pops != null && pops.acceptsType(clazz)) {
				result = new SingleValuedAccessorFromPropertyOps<T>(pops, ckanDataset);
			} else {
				result = null;
			}
		}
		
		return result;
	}

	//@Override
	public static <T> SingleValuedAccessor<T> getSingleValuedAccessor(CkanDataset ckanDataset, String property, Class<T> clazz) {
		String[] parts = property.split("\\:", 2);

		String namespace = parts.length == 2 ? parts[0] : "";
		String localName = parts.length == 2 ? parts[1] : parts[0];

		SingleValuedAccessor<T> result = getSingleValuedAccessor(ckanDataset, namespace, localName, clazz);
	
		return result;
	}
}
