package org.aksw.dcat_suite.clients;
import javax.annotation.Nullable;

import eu.trentorise.opendata.jackan.exceptions.*;
import eu.trentorise.opendata.jackan.model.CkanResponse;

public class DkanException extends JackanException {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Nullable
    private DkanClient dkanClient = null;
    @Nullable
    private CkanResponse ckanResponse = null;
    
    private static String makeMessage(String msg, @Nullable CkanResponse ckanResponse, @Nullable DkanClient client) {
        return msg + "  "
                + (ckanResponse != null ? ckanResponse + "  " : "")
                + (client != null ? client : "");
    }
          
    public DkanException(String msg, DkanClient client) {
        super(makeMessage(msg, null, client));        
        this.dkanClient = client;
    }    

	public DkanException(String msg, DkanClient client, Throwable ex) {
		this(msg, null, client, ex);
    }
	
	 public DkanException(String msg, CkanResponse ckanResponse, DkanClient client, Throwable ex) {
	        super(makeMessage(msg, ckanResponse, client), 
	                ex);
	        this.ckanResponse = ckanResponse;
	        this.dkanClient = client;         
	    }

	@Nullable
    public DkanClient getCkanClient() {
        return dkanClient;
    }
}
