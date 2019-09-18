package org.aksw.dcat_suite.server.conneg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

import com.google.common.net.MediaType;

public class HttpHeaderUtils {
	public static float qValueOf(HeaderElement h) {
		float result = Optional.ofNullable(h.getParameterByName("q"))
						.map(NameValuePair::getValue)
						.map(Float::parseFloat)
						.orElse(1.0f);
		return result;
	}

	public static Stream<Header> streamHeaders(Header[] headers) {
		Stream<Header> result = headers == null ? Stream.empty() :
			Arrays.asList(headers).stream();
		
		return result;
	}

	public static Stream<HeaderElement> getElements(Header[] headers) {
		Stream<HeaderElement> result = streamHeaders(headers)
				.flatMap(h -> Arrays.asList(h.getElements()).stream());
		
		return result;
	}

	public static Stream<HeaderElement> getElements(Header[] headers, String name) {
		Stream<HeaderElement> result = streamHeaders(headers)
				.filter(Objects::nonNull)
				.filter(h -> h.getName().equalsIgnoreCase(name))
				.flatMap(h -> Arrays.asList(h.getElements()).stream());
		
		return result;
	}

	public static Map<String, Float> getOrderedValues(Header[] headers, String name) {
		Map<String, Float> result = getElements(headers, name)
			.collect(Collectors.toMap(e -> e.getName(), e -> qValueOf(e)));
		return result;
	}
	
	
	
	public static String getValue(Header[] headers, String name) {
		List<String> contentTypes = getValues(headers, name);
		if(contentTypes.size() != 1) {
			throw new RuntimeException("Exactly one content type expected, got: " + contentTypes);
		}

		return contentTypes.get(0);
	}
	
	public static List<String> getValues(Header header, String name) {
		List<String> result = getValues(new Header[] { header }, name);
		return result;
	}

	public static List<String> getValues(Header[] headers, String name) {
		List<String> result = getElements(headers, name)					
			.map(HeaderElement::getName)
			.collect(Collectors.toList());

		return result;
	}

	public static RdfEntityInfo copyMetaData(HttpEntity src, RdfEntityInfo tgt) {
		tgt = tgt != null
				? tgt
				: ModelFactory.createDefaultModel().createResource().as(RdfEntityInfo.class);
		
		List<String> encodings = getValues(src.getContentEncoding(), HttpHeaders.CONTENT_ENCODING);
		String ct = getValue(new Header[] { src.getContentType() }, HttpHeaders.CONTENT_TYPE);
		
		tgt.setContentType(ct);
		tgt.setContentEncodings(encodings);
		tgt.setContentLength(src.getContentLength());
		
		return tgt;
	}
	


	// Bridge between rdf model and apache http components
	public static Header[] toHeaders(RdfEntityInfo info) {
		Header[] result = new Header[] {
			// TODO Add charset argument to content type header if info.getCharset() is non null
			new BasicHeader(HttpHeaders.CONTENT_TYPE, info.getContentType()),
			new BasicHeader(HttpHeaders.CONTENT_ENCODING, info.getEncodingsAsHttpHeader())
		};
		
		return result;
	}
	
	// TODO Move to some jena http utils
	
	
	
	public static List<MediaType> supportedMediaTypes() {
		return supportedMediaTypes(RDFLanguages.getRegisteredLanguages());
	}
	
	public static List<MediaType> supportedMediaTypes(Collection<Lang> langs) {
		List<MediaType> types = langs.stream()
				// Models can surely be served using based languages
				// TODO but what about quad based formats? I guess its fine to serve a quad based dataset
				// with only a default graph
				//.filter(RDFLanguages::isTriples)
				.flatMap(lang -> Stream.concat(
						Stream.of(lang.getContentType().getContentType()),
						lang.getAltContentTypes().stream()))
				.map(MediaType::parse)
				.collect(Collectors.toList());
		return types;
	}
	
}
