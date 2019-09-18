package org.aksw.dcat_suite.server.conneg;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Resource;

public class ResourceManager2 {
	protected Path basePath;
	
	// "_content" by default
	//protected Set<String> contentFolders = new LinkedHashSet<String>(Arrays.asList("_content"));
	
	public ResourceManager2(Path basePath) {
		super();
		this.basePath = basePath;
	}

	public Path getBasePath() {
		return basePath;
	}
	
	//protected PathGroup secondaryPaths;

	// Find a representation that matches the headers
	
	
	
//	Repls get(Path relPath) {
//		
//	}

	
	
	/**
	 * Yield all symbolic link resolutions that point to items below the
	 * configured roots
	 * 
	 * @param path
	 * @return
	 */
	public Collection<Path> readSymbolicLinkTransitive(Path path) throws Exception {
		
		Set<Path> seen = new LinkedHashSet<>();
		List<Path> cands = new ArrayList<>();
		
		// tentative result list, may be set to null
		Collection<Path> result = new ArrayList<>();
		
		while(Files.isSymbolicLink(path)) {
			if(seen.contains(path)) {
				throw new RuntimeException("Cyclic symbolic link detected: " + seen);
			}
			seen.add(path);
			path = Files.readSymbolicLink(path);
			if(!Files.exists(path)) {
				result = null;
				break;
			}
			
			toEntity(path);

			// Check if the symlink resolved to another resource folder
			// This is the case, if the parent is the CONTENT folder
			for(Path base : resourceBasePaths) {
				toEntity()
				Path folderName = path.getParent().getFileName().getFileName();
				if(path.startsWith(base))
			}
		}
		
		return result;
	}

	/**
	 * Attempt to map the given path to an entity.
	 * This requires the path to satisfy conditions that make it recognizable as one:
	 * - the prefix path must correspond to a known base folder for resources
	 * - the parent folder must be a _content folder
	 * 
	 * @param path
	 * @return
	 */
	public RdfEntity<Resource> pathToEntity(Path path) {
		path = path.toAbsolutePath().normalize();
		Path parent = path.getParent(); 
		String parentFolderName = parent.getFileName().toString();
		
		for(Path basePath : resourceBasePaths) {
			// TODO Make configurable
			if("_content".equals(parentFolderName) && path.startsWith(basePath)) {
				// Found an entity
				
				
			}
		}
		
	}
	
	public Collection<Path> resolveSecondaryPaths(Path path) {
		
		Collection<Path> secondaryPaths = readSymbolicLinkTransitive(path);
		
		// id/_content/file-1.nt
		//             file_1.nt.meta
		
		
		
		

		return null;
	}
	
	/**
	 * The content of a representation of a resource can be a link to another file,
	 * such as a previous download.
	 * 
	 * There are two reasonable natures of these links
	 * - direct symbolic links - e.g. resource/a/a.ttl -> resource/b/b.ttl
	 * - indirect symlinks via hash space - e.g. resource/a/a.ttl -> hash/foo/bar/data.ttl
	 *   with hash/foo/bar/reverse-links-> resource/b/b.ttl
	 * 
	 * In both cases, the essential point is, that there are 2 or more resources
	 * that share a common representation. So if for either resource conversions were created,
	 * they can be used to answer requests to the other.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param entityPath
	 * @return
	 */
	public Map<Path, RdfFileEntity> getSecondaryEntities(Path entityPath) {
		Path relPath = entityPath.relativize(basePath);

		
		Collection<Path> secondaryPaths = resolveSecondaryPaths(entityPath);
		
		
		// TODO Ensure the path is within the repo
//		if(relPath.st)
	
		//Path absEntityPath = basePath.resolve(other)
		return null;
	}
	
	
	public static void main(String[] args) {
		ResourceManager rm = new ResourceManager(Paths.get("/home/raven/.dcat/test2/"));
		
		//rm.getSecondaryEntities(Pa)
		Path p = rm.getBasePath().resolve("d").resolve("x.ttl");
		rm.resolveSecondaryPaths(p);
	}
}
