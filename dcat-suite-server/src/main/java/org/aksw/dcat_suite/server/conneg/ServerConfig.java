package org.aksw.dcat_suite.server.conneg;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.WebContent;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

@Configuration
//@ComponentScan("org.aksw.dcat_suite.server.conneg")
//@ComponentScan(basePackageClasses = MessageBodyWriterHtml.class)
public class ServerConfig {
	@Bean
	public WebServerFactoryCustomizer<TomcatServletWebServerFactory> containerCustomizer() {
		System.out.println("Found: " + this.getClass());
		return new EmbeddedTomcatCustomizer();
	}

	public static class EmbeddedTomcatCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
		@Override
		public void customize(TomcatServletWebServerFactory factory) {
			factory.addConnectorCustomizers((TomcatConnectorCustomizer) connector -> {
				connector.setAttribute("relaxedPathChars", "<>[\\]^`{|}");
				connector.setAttribute("relaxedQueryChars", "<>[\\]^`{|}");
			});
		}
	}
	
	// Variants: Accept-Encoding;gzip;br, Accept-Language;en ;fr
	// Variant header: https://httpwg.org/http-extensions/draft-ietf-httpbis-variants.html
	//https://spring.io/blog/2013/05/11/content-negotiation-using-spring-mvc
//	https://www.logicbig.com/tutorials/spring-framework/spring-web-mvc/spring-mvc-custom-converter.html
	// https://stackoverflow.com/questions/44168781/how-can-i-register-a-custom-httpmessageconverter-to-deal-with-an-invalid-content
	// https://www.baeldung.com/spring-httpmessageconverter-rest
	@Bean
	public HttpMessageConverter<Model> createNTriplesHttpMessageConverter() {
//	    MarshallingHttpMessageConverter xmlConverter = new MarshallingHttpMessageConverter();

		return new HttpMessageConverter<Model>() {

			@Override
			public boolean canRead(Class<?> clazz, MediaType mediaType) {
//				System.out.println("canRead: " + clazz + " - " + mediaType);
				return true;
			}

			@Override
			public boolean canWrite(Class<?> clazz, MediaType mediaType) {
//				System.out.println("canWrite: " + clazz + " - " + mediaType);
				return Model.class.isAssignableFrom(clazz);
			}

			@Override
			public List<MediaType> getSupportedMediaTypes() {
//				System.out.println("Supported media types requested");
				return Arrays.asList(MediaType.valueOf(WebContent.contentTypeTextPlain), MediaType.valueOf(WebContent.contentTypeNTriples));
			}

			@Override
			public Model read(Class<? extends Model> clazz, HttpInputMessage inputMessage)
					throws IOException, HttpMessageNotReadableException {
				Model result = ModelFactory.createDefaultModel();
				RDFDataMgr.read(result, inputMessage.getBody(), Lang.NTRIPLES);
				return result;
			}

			@Override
			public void write(Model t, MediaType contentType, HttpOutputMessage outputMessage)
					throws IOException, HttpMessageNotWritableException
			{
				HttpHeaders headers = outputMessage.getHeaders();
				headers.setContentType(contentType);
				//headers.setVary(Arrays.asList("Accept,Accept-Encoding"));
				headers.set("Variants", "Accept;text/plain,Accept-Encoding;gzip;br");
				
		        RDFDataMgr.write(outputMessage.getBody(), t, RDFFormat.NTRIPLES);
			}
			
		};
	}
}
