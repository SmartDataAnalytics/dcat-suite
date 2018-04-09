package org.aksw.dcat.ap.ckan;

import org.aksw.dcat.ap.jena.domain.api.DcatApAgent;


// Use the versions based on FoafAgentAccessor
@Deprecated
public class DcatApCkanAgentView
	implements DcatApAgent
{
	/** e.g. extra:publisher_ */
	protected String prefix;

	protected PropertySource source;
	
	
	
	public DcatApCkanAgentView(String prefix, PropertySource source) {
		super();
		this.prefix = prefix;
		this.source = source;
	}
	
	@Override
	public String getEntityUri() {
		return source.getProperty(prefix + "uri", String.class).get();
	}

	@Override
	public void setEntityUri(String uri) {
		source.getProperty(prefix + "uri", String.class).set(uri);
	}


	@Override
	public String getName() {
		return source.getProperty(prefix + "name", String.class).get();
	}

	@Override
	public void setName(String name) {
		source.getProperty(prefix + "name", String.class).set(name);		
	}

	@Override
	public String getMbox() {
		return source.getProperty(prefix + "email", String.class).get();
	}

	@Override
	public void setMbox(String mbox) {
		source.getProperty(prefix + "email", String.class).set(mbox);
	}

	@Override
	public String getHomepage() {
		return source.getProperty(prefix + "url", String.class).get();
	}

	@Override
	public void setHomepage(String homepage) {
		source.getProperty(prefix + "url", String.class).set(homepage);
	}

	@Override
	public String getType() {
		return source.getProperty(prefix + "type", String.class).get();
	}

	@Override
	public void setType(String resource) {
		 source.getProperty(prefix + "type", String.class).set(resource);
	}

}
