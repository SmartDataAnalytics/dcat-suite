package org.aksw.dcat.server.config;

import org.aksw.dcat_suite.server.conneg.torename.HttpMessageConverterModel;
import org.apache.jena.rdf.model.Model;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

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

		return new HttpMessageConverterModel();
	}
}
