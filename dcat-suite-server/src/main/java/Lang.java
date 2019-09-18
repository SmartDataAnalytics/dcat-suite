

import java.util.Set;

public interface Lang {
	String getLabel();
	Set<String> getAltLabels();
	
	String getPrefFileExtension();
	Set<String> getAltFileExtensions();
	
	String getPrefMimeType();
	Set<String> getAltMimeTypes();
}
