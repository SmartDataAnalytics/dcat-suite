package org.aksw.dcat_suite.binding.ckan.jena.plugin;

import org.aksw.dcat.ckan.config.model.DcatResolverCkan;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginDcatSuiteCkanBinding
	implements JenaSubsystemLifecycle
{
	@Override
	public void start() {
		init();
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}
	
	public static void init() {
		JenaPluginUtils.registerJenaResourceClassesUsingPackageScan(
				DcatResolverCkan.class.getPackage().getName(),
				BuiltinPersonalities.model,
				PrefixMapping.Extended);
	}
}
