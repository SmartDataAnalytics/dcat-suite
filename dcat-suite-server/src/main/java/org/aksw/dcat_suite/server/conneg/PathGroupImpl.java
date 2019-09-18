package org.aksw.dcat_suite.server.conneg;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PathGroupImpl
	implements PathGroup
{
	protected List<Path> basePaths;
	
	public PathGroupImpl() {
		this(new ArrayList<>());
	}
	
	public PathGroupImpl(List<Path> basePaths) {
		super();
		this.basePaths = basePaths;
	}
	
	public List<Path> getBasePaths() {
		return basePaths;
	}
	
	/**
	 * Find the first match where basePath + lookuPath exists 
	 * 
	 * @param suffix
	 * @return
	 */
	public Path findFirstExisting(Path lookupPath) {
		Path result = null;
		for(Path basePath : basePaths) {
			Path cand = basePath.resolve(lookupPath);
			if(Files.exists(cand)) {
				result = cand;
				break;
			}
		}
		return result;
	}
}
