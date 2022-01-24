package org.aksw.dcat_suite.app.async;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;


/**
 * Essentially a named CompletableFuture
 * 
 * @author raven
 *
 * @param <T>
 */
public class JobExecution<T> {
	protected String label;
	protected CompletableFuture<T> future;
	
	public JobExecution(String label, CompletableFuture<T> future) {
		super();
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}

	public <O> JobExecution<O> andThen(Function<? super T, O> next) {
		return new JobExecution<>(label, future.thenApply(next));
	}
}
