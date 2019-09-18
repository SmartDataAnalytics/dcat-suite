package org.aksw.dcat_suite.metamodel;

import java.util.Set;

public interface Lang
	extends SkosEntity
{	
	String getPrefFileExtension();
	Set<String> getAltFileExtensions();
	
	String getPrefMimeType();
	Set<String> getAltMimeTypes();
}
