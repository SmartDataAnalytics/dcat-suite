package org.aksw.dcat_suite.server.provider;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

import org.apache.jena.riot.WebContent;

@Provider
@Produces({WebContent.contentTypeTurtle, WebContent.contentTypeTurtleAlt1, WebContent.contentTypeTurtleAlt1})
public class MessageBodyWriterTurtle
	extends AbstractModelMessageReaderWriterProvider
{
	public MessageBodyWriterTurtle() {
		super("TURTLE");
	}
}