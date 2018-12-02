package org.aksw.dcat.repo.impl.fs;

/**
 * The idea is to have a builder for uniform ProgressTrackers backed by push or pull strategies
 * - Supplier<STATUS> poller - poll function to yield the current progress
 * - Flowable<STATUS> statusUpdateFlow - backend pushes update events - once the flow completes the task is assumed to be done
 * 
 * 
 * 
 * @author Claus Stadler, Dec 2, 2018
 *
 * @param <T>
 */
interface ProgressTrackerBuilder<T> {
	
}