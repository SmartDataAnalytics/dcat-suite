package org.aksw.dcat_suite.cli.cmd.file;

import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.maven.shared.invoker.InvocationRequest;

public class InvocationRequestUtils {
    /** Add a collection value property by separating the strings of items with a given separator */
    public static InvocationRequest setProperty(InvocationRequest request, String key, String separator, Collection<String> items) {
        String str = items == null ? null : items.stream().collect(Collectors.joining(separator));
        setProperty(request, key, str);

        return request;
    }

    /** Add a key value pair to the invocation request. Creates and sets a the properties object if needed. */
    public static InvocationRequest setProperty(InvocationRequest request, String key, String value) {
        Properties props = request.getProperties();
        if (props == null) {
            props = new Properties();
            request.setProperties(props);
        }

        if (value == null) {
            props.remove(key);
        } else {
            props.put(key, value);
        }
        return request;
    }
}
