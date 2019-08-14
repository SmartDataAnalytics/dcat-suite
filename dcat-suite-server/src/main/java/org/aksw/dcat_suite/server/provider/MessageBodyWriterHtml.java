package org.aksw.dcat_suite.server.provider;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;



@Provider
@Produces({MediaType.TEXT_HTML})
public class MessageBodyWriterHtml
	extends AbstractModelMessageReaderWriterProvider
{
	public MessageBodyWriterHtml() {
		super("HTML");
	}
}
