package org.aksw.dcat.jena.domain.api;

import java.util.Collection;
import java.util.Set;

import org.aksw.commons.accessors.CollectionFromConverter;
import org.apache.jena.rdf.model.Resource;

import com.google.common.base.Converter;

public interface DcatDistribution
	extends DcatEntity, DcatDistributionCore
{
	Set<Resource> getAccessURLs();
	Set<Resource> getDownloadURLs();
	
	// Assumes that getDownloadURLs returns a set view
	default void setDownloadURL(Resource r) {
		getDownloadURLs().clear();
		getDownloadURLs().add(r);
	}

	default void setAccessURL(Resource r) {
		getAccessURLs().clear();
		getAccessURLs().add(r);
	}
	
	default Collection<String> getAccessStrs() {
		Collection<String> result = new CollectionFromConverter<>(getAccessURLs(),
				Converter.from(getModel()::createResource, Resource::getURI));
		return result;
	}

	default Collection<String> getDownloadStrs() {
		Collection<String> result = new CollectionFromConverter<>(getDownloadURLs(),
				Converter.from(getModel()::createResource, Resource::getURI));
		return result;
	}

	
	
//	default SpdxChecksum getChecksum() {
//		return null;
//		//ResourceUtils.getProperty(this, Spdx.ge)
//	}
}
