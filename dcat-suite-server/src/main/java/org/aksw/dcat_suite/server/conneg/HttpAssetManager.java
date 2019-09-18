package org.aksw.dcat_suite.server.conneg;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;

interface HttpAssetManager {
	/**
	 * Retrieve an HttpEntity from the asset mangaer
	 * 
	 * @author raven
	 *
	 */
	CompletableFuture<HttpEntity> get(HttpRequest request);
	CompletableFuture<?> put(String baseName, Supplier<? extends HttpEntity> supplier);	
}