/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources."
 */

package org.apache.fop.svg;

import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.SVGPropertyMapping;

import java.util.Enumeration;

public class SVGPropertyListMapping implements PropertyListMapping {

    public void addToBuilder(TreeBuilder builder) {

        String uri = "http://www.w3.org/2000/svg";
        builder.addPropertyList(uri,
                                SVGPropertyMapping.getGenericMappings());
        /* Add any element mappings */
        for (Enumeration e = SVGPropertyMapping.getElementMappings();
                e.hasMoreElements();) {
            String elem = (String) e.nextElement();
            builder.addElementPropertyList(uri, elem,
                                           SVGPropertyMapping.getElementMapping(elem));
        }
    }
}
