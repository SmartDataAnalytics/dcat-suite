package org.aksw.dcat_suite.server.conneg;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.dcat.repo.impl.fs.CatalogResolverFilesystem;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;


interface ResourceSourceFile {
	Resource getResource(Path path);
}


class ResourceSourceFileImpl
	implements ResourceSourceFile 
{
	@Override
	public Resource getResource(Path path) {
		String fileName = path.getFileName().toString();
		RdfEntityInfo result = ContentTypeUtils.deriveHeadersFromFileExtension(fileName);
		return result;
	}
}

class ResourceUtils {
	public static Resource merge(Collection<Resource> resources) {
		Model m = ModelFactory.createDefaultModel();
		Resource result = m.createResource();
		
		for(Resource r : resources) {
			Model n = r.getModel();
			StmtIterator it = n.listStatements();
			while(it.hasNext()) {
				Statement stmt = it.next();

				RDFNode s = stmt.getSubject();
				Property p = stmt.getPredicate();
				RDFNode o = stmt.getObject();
				
				if(s.equals(r)) { s = result; }
				if(o.equals(r)) { o = result; }
				
				// Should never be not a resource here
//				if(s.isResource()) {
				m.add(s.asResource(), p, o);
//				}
			}
		}
		
		return result;
	}
}


/**
 * ResourceManager for a single folder; does not manager multiple repositories
 * 
 * @author raven
 *
 */
public class ResourceStoreImpl
	implements ResourceStore
{
	protected Path basePath;
	
	protected String CONTENT = "_content";
	
	protected ResourceSourceFile resourceSource;
	protected PathAnnotatorRdf pathAnnotator;
	protected Function<String, Path> uriToRelPath;
		
	public ResourceStoreImpl(Path basePath) {
		super();
		this.basePath = basePath;
		this.uriToRelPath = CatalogResolverFilesystem::resolvePath;
		
		this.pathAnnotator = new PathAnnotatorRdfImpl();
		this.resourceSource = new ResourceSourceFileImpl();
	}

	
	@Override
	public Path getAbsolutePath() {
		return basePath;
	}
	
	public Resource getInfo(Path absPath) {
		Resource r1 = resourceSource.getResource(absPath);		
		Resource r2 = pathAnnotator.getRecord(absPath);
		
		Resource result = ResourceUtils.merge(Arrays.asList(r1, r2));
		
		return result;
	}
	
	public Path fullPath(String uri) {
		Path relPath = uriToRelPath.apply(uri);		
		Path result = basePath.resolve(relPath);

		return result;
	}
	
	public RdfHttpResourceFile get(String uri) {
		Path fullPath = fullPath(uri);
		RdfFileResourceImpl result = new RdfFileResourceImpl(this, fullPath);
		return result;
	}
	
	public RdfHttpEntityFile pathToEntity(Path absEntityPath) {
		String fileName = absEntityPath.getFileName().toString();
		
		Path parent = absEntityPath.getParent();
		String parentFileName = parent.getFileName().toString();
		
		RdfHttpEntityFile result;
		if(parentFileName.equals(CONTENT)) {
			Path resRelFolder = basePath.relativize(parent);
			
			Path entityRelFolder = parent.relativize(absEntityPath);
			//Path resFolder = parent.getParent();
			RdfFileResourceImpl res = new RdfFileResourceImpl(this, resRelFolder);
			result = new RdfHttpEntityFileImpl(res, entityRelFolder);	
		} else {
			result = null;
		}
		
		//Resource cachedInfo = pathAnnotator.getRecord(path);
		
//		RdfEntityInfo info = ContentTypeUtils.deriveHeadersFromFileExtension(fileName);

		return result;	
	}
	
	
//	EntitySpace getEntitySpace(Path basePath) {
//		
//	}

	public RdfHttpResourceFile getResource(String uri) {
		Path path = uriToRelPath.apply(uri);
		
		path = path.resolve(CONTENT);
		
		RdfHttpResourceFile result = new RdfFileResourceImpl(this, path);
		return result;
	}
	
	public Collection<RdfHttpEntityFile> listEntities(Path relContentFolder) {
		//Path contentFolder = basePath.resolve(CONTENT);
		
		Path contentFolder = basePath.resolve(relContentFolder);
		
		List<RdfHttpEntityFile> result;
		
		try {
			result = (!Files.exists(contentFolder)
					? Collections.<Path>emptyList().stream()
					: Files.list(contentFolder))
						.filter(file -> pathAnnotator.isAnnotationFor(file).isEmpty())
						.map(this::pathToEntity)
						.collect(Collectors.toList());			
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	@Override
	public boolean contains(Path path) {
		path = path.toAbsolutePath();
		
		boolean result = path.startsWith(basePath);
		return result;
	}

	@Override
	public Resource getInfo(Path path, String layer) {
		Resource result = contains(path)
			? pathAnnotator.getRecord(path)
			: null;

		return result;
	}

	@Override
	public RdfHttpEntityFile allocateEntity(String uri, Resource description) {
		Path relPath = uriToRelPath.apply(uri);
		
		RdfHttpEntityFile result = allocateEntity(relPath, description);
		return result;
	}

	public RdfHttpResourceFile pathToResource(Path baseRelPath) {
		// TODO Validate the path
		RdfHttpResourceFile result = new RdfFileResourceImpl(this, baseRelPath);

		return result;
	}
	
	public RdfHttpEntityFile allocateEntity(Path baseRelPath, Resource _info) {
		
		RdfEntityInfo info = _info.as(RdfEntityInfo.class);

		String suffix = ContentTypeUtils.toFileExtension(info);
		pathToResource(baseRelPath);
		Path finalRelPath = Paths.get("data" + suffix); 

		
		RdfHttpResourceFile res = pathToResource(baseRelPath);
		
		
		RdfHttpEntityFile result = new RdfHttpEntityFileImpl(res, finalRelPath);
		
		return result;
	}

}
