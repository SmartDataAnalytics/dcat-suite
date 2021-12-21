package org.aksw.dcat.ap.plugin;

import org.aksw.dcat.ap.binding.jena.domain.impl.RdfDcatApAgent;
import org.aksw.dcat.ap.binding.jena.domain.impl.RdfDcatApDataset;
import org.aksw.dcat.ap.binding.jena.domain.impl.RdfDcatApDistributionImpl;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginDcatAp
    implements JenaSubsystemLifecycle {

    public void start() {
        JenaPluginDcatAp.init();
    }

    @Override
    public void stop() {
    }

    public static void init() {
        init(BuiltinPersonalities.model);
    }

    public static void init(Personality<RDFNode> p) {
        JenaPluginUtils.registerResourceClasses(
                RdfDcatApDataset.class,
                RdfDcatApDistributionImpl.class,
                RdfDcatApAgent.class);
    }
}
