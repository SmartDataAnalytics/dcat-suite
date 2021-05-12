package org.aksw.dcat_suite.enrich;

import java.io.File;
import java.io.IOException;

import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;

public class GTFSFile {
	
	private GtfsDaoImpl store;
	
	public GTFSFile (String gtfsFile) throws IOException {
		GtfsReader reader = new GtfsReader();
		reader.setInputLocation(new File(gtfsFile));
	    store = new GtfsDaoImpl();
	    reader.setEntityStore(store);
	    reader.run();
	 
	}
	
	public GtfsDaoImpl getStore() {
		return store;
	}
}
