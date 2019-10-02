package org.aksw.jena_sparql_api.utils.turtle;

import static org.apache.jena.riot.WebContent.contentTypeTurtle;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.LangBuilder;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFFormatVariant;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFWriterRegistry;
import org.apache.jena.riot.WriterGraphRIOT;
import org.apache.jena.riot.WriterGraphRIOTFactory;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.writer.TurtleShell;
import org.apache.jena.riot.writer.TurtleWriterBase;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sys.JenaSystem;

/**
 * A variant of the turtle writer that omits the base url statement.
 * Useful if the base URL corresponds to a directory
 * 
 * @author raven
 *
 */
class TurtleWriterOmitBaseUri
	extends TurtleWriterBase
{
    @Override
    protected void output(IndentedWriter iOut, Graph graph, PrefixMap prefixMap, String baseURI, Context context) {
        TurtleWriter$ w = new TurtleWriter$(iOut, prefixMap, baseURI, context) ;
        w.write(graph) ;
    }

    private static class TurtleWriter$ extends TurtleShell {
        public TurtleWriter$(IndentedWriter out, PrefixMap prefixMap, String baseURI, Context context) {
            super(out, prefixMap, baseURI, context) ;
        }

        private void write(Graph graph) {
//            writeBase(baseURI) ;
            writePrefixes(prefixMap) ;
            if ( !prefixMap.isEmpty() && !graph.isEmpty() )
                out.println() ;
            writeGraphTTL(graph) ;
        }
    }

	
}

public class TurtleNoBaseTest {
	

	
	
	public static void initTurtleWithoutBaseUri() {
	    final Lang TURTLE_NO_BASE = LangBuilder.create("ttl-nb", contentTypeTurtle + "+nb")
                //.addAltNames("TTL")
                //.addAltContentTypes(contentTypeTurtleAlt1, contentTypeTurtleAlt2)
                //.addFileExtensions("ttl")
                .build() ;		

		final RDFFormat TURTLE_PRETTY_NO_BASE  = new RDFFormat(Lang.TURTLE, new RDFFormatVariant("pretty-no-base"));
		
		WriterGraphRIOTFactory factory = new WriterGraphRIOTFactory() {
	        @Override
	        public WriterGraphRIOT create(RDFFormat serialization)
	        {
	            if ( Objects.equals(TURTLE_PRETTY_NO_BASE, serialization) ) {
	                return new TurtleWriterOmitBaseUri() ;
	        	} else {
	        		return null;
	        	}
	        }
		};
	    
	    RDFLanguages.register(TURTLE_NO_BASE);
		RDFWriterRegistry.register(TURTLE_NO_BASE, TURTLE_PRETTY_NO_BASE);
		RDFWriterRegistry.register(TURTLE_PRETTY_NO_BASE, factory);		
	}
	
	public static void main(String[] args) {

		
		
		Path p = Paths.get("/home/raven/.dcat/test2/c/c.ttl").normalize().toAbsolutePath();

		Model m = ModelFactory.createDefaultModel();
		String base = p.getParent().normalize().toUri().toString();

		RDFDataMgr.read(m, p.toString(), base, Lang.TURTLE);

		System.out.println("Base: " + base);
	
		// This does not work because the base URL is not part of the model,
		// and RDFDataMgr does not support supplying it to the writer
//		RDFDataMgr.write(System.out, m, TURTLE_NO_BASE);

		// This works
		org.apache.jena.rdf.model.RDFWriter writer = m.getWriter("ttl-nb");
		writer.write(m, System.out, base);
		
		
		System.out.println("done");
		
//		RDFDataMgr.read(m, p.toString(), base, Lang.TURTLE);
		
//		RDFWriter writer = RDFWriter.create().format(TURTLE_PRETTY_NO_BASE).source(m.getGraph()).build();

		//RDFWriterRegistry.
		//RDFWriter writer = new RDFW
		//RDFDataMgr.createGraphWriter(TURTLE_PRETTY_NO_BASE);
		
		//RDFDataMgr.write(System.out, m, TURTLE_PRETTY_NO_BASE);
		//RDFDataMgr.write(System.out, m, RDFFormat.TURTLE_PRETTY);
		
		
		//org.apache.jena.riot.RDFWriter.create().
		//new RDFWriterRIOT("foo").
//		WriterGraphRIOT writerGraph = new TurtleWriterWithoutBase(); //m.getWriter("ttl");
//		writer.write(m, System.out, base);
		//m.write(System.out, "ttl", base);
		// TODO Get rid of the @base ...

	}
	
}
