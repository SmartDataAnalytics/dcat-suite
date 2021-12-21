package org.aksw.dcat.ap.domain.api;

/**
 * DCAT AP defines these fields for publishers which are considered to be of type foaf:Agent.
 *
 *
 *
 * @author raven Apr 9, 2018
 *
 */
public interface DcatApAgent
//	extends ResourceLike
{
    String getName();
    DcatApAgent setName(String name);

    String getMbox();
    DcatApAgent setMbox(String mbox);

    String getHomepage();
    DcatApAgent setHomepage(String homepage);

    String getType();
    DcatApAgent setType(String resource);

//	public static void copy(DcatApAgent from, DcatApAgent to) {
//		ResourceLike.copy(from, to);
//
//		to.setName(from.getName());
//		to.setMbox(from.getMbox());
//		to.setHomepage(from.getHomepage());
//		to.setType(from.getType());
//	}
}
