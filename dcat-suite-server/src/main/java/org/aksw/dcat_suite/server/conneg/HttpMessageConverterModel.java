package org.aksw.dcat_suite.server.conneg;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

/**
 * Generic HttpMessageConverter for Jena Models using Jena's
 * global RDFLanguages registry.
 * 
 * Note, that no extra implementation is necessary for Jena Graph,
 * because any Jena Graph can be wrapped as a Model using ModelFactory.createModelForGraph(...)
 * 
 * @author raven
 *
 */
public class HttpMessageConverterModel
	implements HttpMessageConverter<Model> {

	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		boolean result = Model.class.isAssignableFrom(clazz) &&
				(mediaType == null || getSupportedMediaTypes().stream().anyMatch(mt -> mt.includes(mediaType)));

//		System.out.println("canRead: " + result);
		return result;
	}

	@Override
	public boolean canWrite(Class<?> clazz, MediaType mediaType) {
//		System.out.println(mediaType + " ---> " + getSupportedMediaTypes());
		boolean result = Model.class.isAssignableFrom(clazz) &&
				(mediaType == null || getSupportedMediaTypes().stream().anyMatch(mt -> mt.includes(mediaType)));

//		System.out.println("canWrite: " + result);
		return result;
	}

	@Override
	public List<MediaType> getSupportedMediaTypes() {
		return HttpMessageConverterModel.supportedMediaTypes();
	}
	
	
	public static List<MediaType> supportedMediaTypes() {
		List<MediaType> types = RDFLanguages.getRegisteredLanguages().stream()
				// Models can surely be served using based languages
				// TODO but what about quad based formats? I guess its fine to serve a quad based dataset
				// with only a default graph
				//.filter(RDFLanguages::isTriples)
				.flatMap(lang -> Stream.concat(
						Stream.of(lang.getContentType().getContentTypeStr()),
						lang.getAltContentTypes().stream()))
				.map(MediaType::valueOf)
				.collect(Collectors.toList());
		return types;
//		System.out.println("Supported media types requested");
		//RDFDataMgr.createReader(lang)
		//return Arrays.asList(MediaType.valueOf(WebContent.contentTypeTextPlain), MediaType.valueOf(WebContent.contentTypeNTriples));
	}

	@Override
	public Model read(Class<? extends Model> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		Model result = ModelFactory.createDefaultModel();
		MediaType ct = inputMessage.getHeaders().getContentType();

		if(ct == null) {
			ct = MediaType.valueOf(WebContent.contentTypeTurtle);
//			throw new RuntimeException("guessing format not supported");
		}
		
		String ctType = ct.toString();
		Lang lang = RDFLanguages.nameToLang(ctType);
		RDFDataMgr.read(result, inputMessage.getBody(), lang);
		return result;
	}

	@Override
	public void write(Model t, MediaType contentType, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException
	{
		String ctType = contentType.toString();
		Lang lang = RDFLanguages.nameToLang(ctType);

		
		HttpHeaders headers = outputMessage.getHeaders();
		headers.setContentType(contentType);
		//headers.setVary(Arrays.asList("Accept,Accept-Encoding"));
		headers.set("Variants", "Accept;text/plain,Accept-Encoding;gzip;br");
//		headers.add(HttpHeaders.CONTENT_ENCODING, this.coding);
//		headers.add(HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING);

		RDFDataMgr.write(outputMessage.getBody(), t, lang);
	}
	
};