package org.aksw.dcat_suite.server.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFWriterI;


public class AbstractModelMessageReaderWriterProvider
//	extends AbstractMessageReaderWriterProvider<Model>
    implements MessageBodyWriter<Model>
{
    private final String lang;

    public AbstractModelMessageReaderWriterProvider(String lang) {
		System.out.println("YAAAY");

        this.lang = lang;
    }

    /*
    @Override
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return Model.class == type;
    }

    @Override
    public Model readFrom(Class<Model> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {

        Model result = ModelFactory.createDefaultModel();

        result.read(entityStream, null, lang);

        return result;
    }
    */

    //private static RDFWriter htmlWriter = new RDFWriterHtml();

    public static RDFWriterI getWriter(String format)
    {
        // FIXME The Jena writer needs to be configurable (e.g. css path)
        // Probably use ApplicationContext for that
        /*
        if("HTML".equalsIgnoreCase(format)) ;
            return htmlWriter;
        }
        */



        RDFWriterI writer = ModelFactory.createDefaultModel().getWriter(format);

        return writer;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return Model.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(Model t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException,
            WebApplicationException {

    	System.out.println("Writing with lang " + lang);

        t.write(entityStream, lang);
    }

    @Override
    public long getSize(Model t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return -1;
    }
}
