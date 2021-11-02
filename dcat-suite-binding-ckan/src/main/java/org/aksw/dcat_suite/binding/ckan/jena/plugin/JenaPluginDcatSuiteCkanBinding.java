package org.aksw.dcat_suite.binding.ckan.jena.plugin;

import org.aksw.ckan.domain.CkanEntity;
import org.aksw.dcat.ckan.config.model.DcatResolverCkan;
import org.aksw.dcat.ckan.config.model.DcatResolverConfig;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
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
    }

    public static void init() {
//		JenaPluginUtils.scan(
//				DcatResolverCkan.class.getPackage().getName(),
//				BuiltinPersonalities.model,
//				PrefixMapping.Extended);
        JenaPluginUtils.registerResourceClasses(
                DcatResolverCkan.class, DcatResolverConfig.class, CkanEntity.class);
    }
}
