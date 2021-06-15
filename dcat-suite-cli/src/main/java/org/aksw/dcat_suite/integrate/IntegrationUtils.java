package org.aksw.dcat_suite.integrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.jena.graph.Node;

public abstract class IntegrationUtils {
	
	public static final HashMap<Node,List<Node>> createLinkMap(HashMap<Node,List<Node>> sourceToTarget, Node source, Node target) {
		
		if (sourceToTarget.containsKey(source)) {
			sourceToTarget.get(source).add(target);
    	}
    	else {
    		ArrayList<Node> linkList = new ArrayList<Node>(); 
    		linkList.add(target);
    		sourceToTarget.put(source, linkList);
    	}
		return sourceToTarget;
	}
	
	public static final String getValuesString(Object [] nodes) {
		 String nodesValuesString = "";
		    for (Object node : nodes ) {
			    	nodesValuesString = nodesValuesString + "<"+node.toString()+"> ";
		    }
		return nodesValuesString; 
	}
}
