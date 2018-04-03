package org.aksw.dcat.jena.plugin;

import org.apache.jena.system.JenaSubsystemLifecycle;

public class InitJenaPluginDcat
	implements JenaSubsystemLifecycle {

	public void start() {
		JenaPluginDcat.init();
	}

	@Override
	public void stop() {
	}
}
