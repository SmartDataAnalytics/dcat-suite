package org.aksw.dcat.ap.jena.domain.api;

/**
 * 
 * This is akin to Jena's polymorphism architecture on RDFNode
 * 
 * @author raven Apr 9, 2018
 *
 * @param <M>
 */
public interface View
	extends Polymorphic
{
	/**
	 * The backing entity of the view
	 * May be null
	 * @return
	 */
	Object getEntity();
	
	/**
	 * Optional context information associated with the entity and the view
	 *  
	 * @return
	 */
	Object getContext();

	//@Override
	<T extends View> boolean canAs(Class<T> view);

	//@Override
	<T extends View> T as(Class<T> view);	
}
