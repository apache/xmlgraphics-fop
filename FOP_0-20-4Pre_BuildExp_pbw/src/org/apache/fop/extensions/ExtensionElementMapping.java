/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.extensions;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.FOTreeBuilder;

import java.util.HashMap;

public class ExtensionElementMapping implements ElementMapping {

    public static final String URI = "http://xml.apache.org/fop/extensions";

    private static HashMap foObjs = null;

    private static synchronized void setupExt() {
        if(foObjs == null) {
            foObjs = new HashMap();
            foObjs.put("bookmarks", new B());
            foObjs.put("outline", new O());
            foObjs.put("label", new L());
        }
    }

    public void addToBuilder(FOTreeBuilder builder) {
        if(foObjs == null) {
            setupExt();
        }
        builder.addMapping(URI, foObjs);
    }

    static class B extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new Bookmarks(parent);
        }
    }

    static class O extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new Outline(parent);
        }
    }

    static class L extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new Label(parent);
        }
    }
}
