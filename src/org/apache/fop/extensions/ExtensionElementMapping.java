/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.extensions;

import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.ExtensionPropertyMapping;
import org.apache.fop.fo.TreeBuilder;

import java.util.Enumeration;
import java.util.Hashtable;

public class ExtensionElementMapping implements ElementMapping {

    public static final String URI = "http://xml.apache.org/fop/extensions";

    public void addToBuilder(TreeBuilder builder) {
        builder.addMapping(URI, "outline", Outline.maker());
        builder.addMapping(URI, "label", Label.maker());


        builder.addPropertyList(ExtensionElementMapping.URI,
                                ExtensionPropertyMapping.getGenericMappings());
        /* Add any element mappings */
        for (Enumeration e = ExtensionPropertyMapping.getElementMappings();
                e.hasMoreElements(); ) {
            String elem = (String)e.nextElement();
            builder.addElementPropertyList(ExtensionElementMapping.URI, elem,
                                           ExtensionPropertyMapping.getElementMapping(elem));
        }
    }

}
