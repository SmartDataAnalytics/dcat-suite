package org.aksw.dcat_suite.app;

public class ResultItem implements Cloneable {
	private String severity;
    private String message;
    
    public ResultItem() {
    	
    }
    
    public ResultItem(String severity, String message) {
    	super(); 
    	this.severity = severity; 
    	this.message = message; 
    }
    
    public String getSeverity () {
    	return this.severity; 
    }
    
    public String getMessage () {
    	return this.message; 
    }
	
}