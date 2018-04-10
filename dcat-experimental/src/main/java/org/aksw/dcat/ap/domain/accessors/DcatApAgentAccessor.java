package org.aksw.dcat.ap.domain.accessors;

import org.aksw.dcat.ap.domain.api.DcatApAgent;
import org.aksw.dcat.util.view.SingleValuedAccessor;

/**
 * Default implementations that delegate getters / setters to accessor-supplying methods 
 * 
 * @author raven Apr 9, 2018
 *
 */
public interface DcatApAgentAccessor
	extends ResourceLikeAccessor, DcatApAgent
{
	SingleValuedAccessor<String> name();
	SingleValuedAccessor<String> mbox();
	SingleValuedAccessor<String> homepage();
	SingleValuedAccessor<String> type();		


	default String getName() { return name().get(); }
	default void setName(String name) { name().set(name); }

	default String getMbox() { return mbox().get(); }
	default void setMbox(String name) { mbox().set(name); }

	default String getHomepage() { return homepage().get(); }
	default void setHomepage(String name) { homepage().set(name); }

	default String getType() { return type().get(); }
	default void setType(String name) { type().set(name); }
}