package org.aksw.dcat.jena.plugin;

import org.apache.jena.sys.JenaSubsystemLifecycle;

public class InitJenaPluginDcat
	implements JenaSubsystemLifecycle {

	public void start() {
		JenaPluginDcat.init();
	}

	@Override
	public void stop() {
	}
}
