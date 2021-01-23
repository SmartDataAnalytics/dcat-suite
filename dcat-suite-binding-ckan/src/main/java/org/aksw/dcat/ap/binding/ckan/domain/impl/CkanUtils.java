package org.aksw.dcat.ap.binding.ckan.domain.impl;

import java.util.Objects;
import java.util.function.Function;

import org.aksw.commons.accessors.PropertySource;
import org.aksw.commons.accessors.SingleValuedAccessor;
import org.aksw.commons.accessors.SingleValuedAccessorFromCollection;
import org.aksw.commons.accessors.SingleValuedAccessorFromPropertyOps;
import org.aksw.commons.beans.model.ConversionService;
import org.aksw.commons.beans.model.EntityModel;
import org.aksw.commons.beans.model.EntityOps;
import org.aksw.commons.beans.model.PropertyOps;
import org.aksw.commons.collections.ConvertingCollection;
import org.aksw.dcat.ap.binding.ckan.rdf_view.SetFromCkanExtras;
import org.aksw.dcat.ap.binding.jena.domain.impl.RdfDcatApAgentImpl;
import org.aksw.dcat.ap.domain.api.DcatApAgent;
import org.aksw.jena_sparql_api.mapper.impl.type.ConversionServiceSpringAdapter;
import org.aksw.jena_sparql_api.rdf.collections.ConverterFromObjectToLexicalFormViaRDFDatatype;
import org.aksw.jena_sparql_api.utils.model.SimpleImplementation;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.system.JenaSystem;
import org.springframework.context.support.ConversionServiceFactoryBean;

import com.google.common.base.Converter;

import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;

public class CkanUtils {
    public static void main(String[] args) {
        //mainJena();
        mainCkan();
    }

    public static void mainCkan() {
        //EntityOps.copy(sourceOps, targetOps, fromEntity, toEntity);

        CkanDataset d = new CkanDataset();

        PropertySource s = new PropertySourceCkanDataset(d);
        DcatApAgent v = new DcatApAgentFromPropertySource("extra:publisher_", s);
        //new DcatApCkanAgentView("extra:publisher_", s);

        // What we want to do:
        // Ask whether the dcat agent can also act as a foaf agent in the current context.
        // The context is "views over property sources". (note: a property source may be restricted in which properties it supports)
        // v.canAs(FoafAgent.class)
        // FoafAgent x = v.as(FoafAgent.class)
        // We might need to use a different naming in order to avoid clashes with jena
        // maybe v.viewAs(...) and canViewAs(...)

        // The DcatAgent would then need to somehow pass the prefix
        // it is referring to the new view instance
        // The prefix could be viewed as a path expression to a (json-like) attribute (i.e. literal, array, map)


        setValues(v);



        System.out.println(d.getExtrasAsHashMap());
    }

    public static void mainJena() {
        JenaSystem.init();
        BuiltinPersonalities.model.add(RdfDcatApAgentImpl.class, new SimpleImplementation(RdfDcatApAgentImpl::new));


        Model m = ModelFactory.createDefaultModel();
        RdfDcatApAgentImpl v = m.createResource().as(RdfDcatApAgentImpl.class);


//		v.addProperty(FOAF.homepage, RDF.HTML);
//		v.addProperty(FOAF.homepage, RDF.Property);


        setValues(v);

        RDFDataMgr.write(System.out, m, RDFFormat.TURTLE_PRETTY);
    }

    public static void setValues(DcatApAgent v) {
        //v.setEntityUri("http://example.org/resource/Foo");
        v.setName("Foo Inc.");
        v.setHomepage("http://foobar");
        v.setMbox("mailto:foo@bar.baz");
        v.setType("Foobar");
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

    /**
     * Reflection based
     *
     * @param obj
     * @param localName
     * @param clazz
     * @return
     */
    public static <S, T> SingleValuedAccessor<T> getSingleValuedAccessorViaReflection(S obj, String localName, Class<T> clazz) {
        Class<? extends S> entityClass = (Class<? extends S>)obj.getClass();
        Function<S, SingleValuedAccessor<T>> accessorSupplier = getSingleValuedAccessorSupplierViaReflection(entityClass, localName, clazz);
        SingleValuedAccessor<T> result = accessorSupplier.apply(obj);
        return result;
    }




    public static <T> SingleValuedAccessor<T> getSingleValuedAccessor(CkanDataset ckanDataset, String namespace, String localName, Class<T> clazz) {
        SingleValuedAccessor<T> result;
        if(namespace.equals("extra")) {
            // FIXME hack ... need a converter in general
            result = (SingleValuedAccessor<T>)new SingleValuedAccessorFromCollection<>(new SetFromCkanExtras(ckanDataset, localName));
        } else {
            return getSingleValuedAccessorViaReflection(ckanDataset, localName, clazz);
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



    public static <T> SingleValuedAccessor<T> getSingleValuedAccessor(CkanResource ckanResource, String namespace, String localName, Class<T> clazz) {
        SingleValuedAccessor<T> result;
        if(namespace.equals("extra")) {
            // FIXME hack ... need a converter in general
            result = null; //(SingleValuedAccessor<T>)new SingleValuedAccessorFromSet<>(new SetFromCkanExtras(ckanResource, localName));
        } else {
            return getSingleValuedAccessorViaReflection(ckanResource, localName, clazz);
        }

        return result;
    }

    public static <T> SingleValuedAccessor<T> getSingleValuedAccessor(CkanResource ckanResource, String property, Class<T> clazz) {
        String[] parts = property.split("\\:", 2);

        String namespace = parts.length == 2 ? parts[0] : "";
        String localName = parts.length == 2 ? parts[1] : parts[0];

        SingleValuedAccessor<T> result = getSingleValuedAccessor(ckanResource, namespace, localName, clazz);

        return result;
    }


    public static <T> Function<CkanResource, SingleValuedAccessor<T>> getSingleValuedAccessorSupplierResource(String property, Class<T> clazz) {
        Function<CkanResource, SingleValuedAccessor<T>> result;

        String[] parts = property.split("\\:", 2);

        String namespace = parts.length == 2 ? parts[0] : "";
        String localName = parts.length == 2 ? parts[1] : parts[0];

        if(namespace.equals("extra")) {
            // FIXME hack ... need a converter in general
            result = null; //(SingleValuedAccessor<T>)new SingleValuedAccessorFromSet<>(new SetFromCkanExtras(ckanResource, localName));
        } else {
            result = getSingleValuedAccessorSupplierViaReflection(CkanResource.class, localName, clazz);
        }

        return result;
    }

    public static <T> Function<CkanDataset, SingleValuedAccessor<T>> getSingleValuedAccessorSupplierDataset(String property, Class<T> clazz) {
        Function<CkanDataset, SingleValuedAccessor<T>> result;

        String[] parts = property.split("\\:", 2);

        String namespace = parts.length == 2 ? parts[0] : "";
        String localName = parts.length == 2 ? parts[1] : parts[0];

        if (namespace.equals("extra")) {
            // FIXME hack ... need a converter in general
            RDFDatatype dtype = TypeMapper.getInstance().getTypeByClass(clazz);
            Objects.requireNonNull(dtype, "No mapper for " + clazz);
            Converter<String, Object> stringToObject = new ConverterFromObjectToLexicalFormViaRDFDatatype(dtype).reverse();

            result = ckanDataset -> (SingleValuedAccessor<T>)new SingleValuedAccessorFromCollection<>(
                    new ConvertingCollection<>(
                            new SetFromCkanExtras(ckanDataset, localName),
                            stringToObject
                    ));
        } else {
            result = getSingleValuedAccessorSupplierViaReflection(CkanDataset.class, localName, clazz);
        }


        return result;
    }


    public static <S, T> Function<S, SingleValuedAccessor<T>> getSingleValuedAccessorSupplierViaReflection(Class<? extends S> entityClazz, String localName, Class<T> clazz) {
        Function<S, SingleValuedAccessor<T>> result;

        ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();
        bean.afterPropertiesSet();

        ConversionService conversionService = new ConversionServiceSpringAdapter(bean.getObject());
        EntityOps eops = EntityModel.createDefaultModel(entityClazz, conversionService);

        PropertyOps pops = eops.getProperty(localName);
        if(pops != null && pops.acceptsType(clazz)) {
            result = obj -> new SingleValuedAccessorFromPropertyOps<T>(pops, obj);
        } else {
            result = null;
        }

        return result;
    }


}
