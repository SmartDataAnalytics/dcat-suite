package org.aksw.dcat.ap.binding.ckan.rdf_view;

import java.util.Objects;
import java.util.function.Function;

import org.aksw.commons.accessors.AccessorSupplierFactory;
import org.aksw.commons.accessors.AccessorSupplierFactoryDelegate;
import org.aksw.commons.accessors.SingleValuedAccessor;
import org.aksw.commons.accessors.SingleValuedAccessorFromCollection;
import org.aksw.commons.collections.ConvertingCollection;
import org.aksw.jena_sparql_api.rdf.collections.ConverterFromObjectToLexicalFormViaRDFDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;

import com.google.common.base.Converter;

import eu.trentorise.opendata.jackan.model.CkanDataset;

public class AccessorSupplierCkanDataset
    extends AccessorSupplierFactoryDelegate<CkanDataset>
{
    public AccessorSupplierCkanDataset(AccessorSupplierFactory<CkanDataset> delegate) {
        super(delegate);
    }

    @Override
    public <T> Function<CkanDataset, ? extends SingleValuedAccessor<T>> createAccessor(String name, Class<T> clazz) {
        Function<CkanDataset, ? extends SingleValuedAccessor<T>> result;

        String[] parts = name.split("\\:", 2);

        String namespace = parts.length == 2 ? parts[0] : "";
        String localName = parts.length == 2 ? parts[1] : parts[0];

        if(namespace.equals("extra")) {
            RDFDatatype dtype = TypeMapper.getInstance().getTypeByClass(clazz);
            Objects.requireNonNull(dtype, "No mapper for " + clazz);
            Converter<String, Object> stringToObject = new ConverterFromObjectToLexicalFormViaRDFDatatype(dtype).reverse();

            result = ckanDataset -> (SingleValuedAccessor<T>)new SingleValuedAccessorFromCollection<>(
                    new ConvertingCollection<>(
                            new SetFromCkanExtras(ckanDataset, localName),
                            stringToObject
                    ));

//            result = ckanDataset -> (SingleValuedAccessor<T>)new SingleValuedAccessorFromCollection<>(new SetFromCkanExtras(ckanDataset, localName));
        } else {
            result = delegate.createAccessor(localName, clazz);
        }

        return result;
    }
}