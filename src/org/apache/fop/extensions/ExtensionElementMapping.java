/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.extensions;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.FOTreeBuilder;

import java.util.HashMap;

/**
 * Element mapping for the pdf bookmark extension.
 * This sets up the mapping for the classes that handle the
 * pdf bookmark extension.
 */
public class ExtensionElementMapping implements ElementMapping {
    /**
     * The pdf bookmark extension uri
     */
    public static final String URI = "http://xml.apache.org/fop/extensions";

    // the mappings are only setup once and resued after that
    private static HashMap foObjs = null;

    private static synchronized void setupExt() {
        if (foObjs == null) {
            foObjs = new HashMap();
            foObjs.put("bookmarks", new B());
            foObjs.put("outline", new O());
            foObjs.put("label", new L());
        }
    }

    /**
     * Add the mappings to the fo tree builder.
     *
     * @param builder the fo tree builder to add the mappings
     */
    public void addToBuilder(FOTreeBuilder builder) {
        if (foObjs == null) {
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
