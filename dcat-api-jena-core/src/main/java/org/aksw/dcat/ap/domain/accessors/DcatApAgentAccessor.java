package org.aksw.dcat.ap.domain.accessors;

import org.aksw.commons.accessors.SingleValuedAccessor;
import org.aksw.dcat.ap.domain.api.DcatApAgent;

/**
 * Default implementations that delegate getters / setters to accessor-supplying methods
 *
 * @author raven Apr 9, 2018
 *
 */
public interface DcatApAgentAccessor
    extends DcatApAgent
{
    SingleValuedAccessor<String> name();
    SingleValuedAccessor<String> mbox();
    SingleValuedAccessor<String> homepage();
    SingleValuedAccessor<String> type();


    default String getName() { return name().get(); }
    default DcatApAgentAccessor setName(String name) { name().set(name); return this; }

    default String getMbox() { return mbox().get(); }
    default DcatApAgentAccessor setMbox(String name) { mbox().set(name); return this; }

    default String getHomepage() { return homepage().get(); }
    default DcatApAgentAccessor setHomepage(String name) { homepage().set(name); return this; }

    default String getType() { return type().get(); }
    default DcatApAgentAccessor setType(String name) { type().set(name); return this; }
}