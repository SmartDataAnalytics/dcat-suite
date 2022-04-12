package org.aksw.dcat_suite.cli.cmd.file;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public class Xpp3DomUtils {
    public static Xpp3Dom addEntryAsChild(Xpp3Dom parent, String key, String value) {
        if (value != null) {
            Xpp3Dom child = new Xpp3Dom(key);
            child.setValue(value);
            parent.addChild(child);
        }
        return parent;
    }
}
