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
import java.util.HashMap;
import java.util.Iterator;

public class ExtensionElementMapping implements ElementMapping {

    public static final String URI = "http://xml.apache.org/fop/extensions";

    private static HashMap foObjs = null;

    public synchronized void addToBuilder(TreeBuilder builder) {
        if(foObjs == null) {
            foObjs = new HashMap();    
            foObjs.put("outline", Outline.maker());
            foObjs.put("label", Label.maker());
        }
        builder.addMapping(URI, foObjs);


        builder.addPropertyList(ExtensionElementMapping.URI,
                                ExtensionPropertyMapping.getGenericMappings());
        /* Add any element mappings */
        for (Iterator iter = ExtensionPropertyMapping.getElementMappings().iterator();
                iter.hasNext(); ) {
            String elem = (String)iter.next();
            builder.addElementPropertyList(ExtensionElementMapping.URI, elem,
                                           ExtensionPropertyMapping.getElementMapping(elem));
        }
    }

}
