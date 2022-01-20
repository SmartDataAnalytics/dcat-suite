package org.aksw.dcat.repo.impl.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.ext.com.google.common.base.Objects;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCAT;

import com.google.common.collect.ComparisonChain;

@ResourceView
public interface SearchResult
	extends Resource
{
	@Iri("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
	Collection<Resource> getTypes();
	
// Boolean flag breaks with virtuoso - as a workaround we use a string
//	@Iri(DCATX.Strs.isLatestVersion)
//	Boolean isLatestVersion();

	@Iri(DCATX.Strs.versionTag)
	@IriType
	String getVersionTag();

	@Iri(DCATX.Strs.relatedDataset)
	Resource getRelatedDataset();

	@Iri("http://purl.org/dc/terms/identifier")
	String getIdentifier();
	
	default boolean isLatestVersion() {
		String versionTag = getVersionTag();
		return Objects.equal(versionTag, DCATX.Strs.latestVersion);
	}
	
	
	public static int getMinIndex(Collection<?> items, List<?> order) {
		// Get minimum index
		int result = items.stream()
				.mapToInt(t -> order.indexOf(t))
				.filter(v -> v >= 0)
				.min()
				.orElse(-1);
		return result;
	}

	public static int defaultCompare(SearchResult a, SearchResult b) {
		List<Resource> typeOrder = Arrays.asList(
				DCAT.Dataset,
				DCAT.Distribution,
				DCATX.DownloadURL);

		// Get minimum index
		int at = getMinIndex(a.getTypes(), typeOrder);
		int bt = getMinIndex(b.getTypes(), typeOrder);

		int result = ComparisonChain.start()
			.compare(at, bt)
			// Sort generic datasets first - those that have fewer types
			//.compareFalseFirst(a.getTypes().contains(VOID.Linkset), b.getTypes().contains(VOID.Linkset))
			.compare(a.getTypes().size(), b.getTypes().size())
			.result();
		
		return result;
	}
}
