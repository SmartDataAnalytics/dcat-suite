package org.aksw.dcat.ap.domain.api;

public interface ResourceLike {
	String getEntityUri();
	void setEntityUri(String uri);
	
//	public static void copy(DcatApAgent from, DcatApAgent to) {
//		to.setEntityUri(from.getEntityUri());
//	}
}