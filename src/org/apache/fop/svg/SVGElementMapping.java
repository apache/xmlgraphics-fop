/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import java.util.Enumeration;
import java.util.HashMap;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FOTreeBuilder;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.apps.Driver;

import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.batik.dom.svg.SVGDOMImplementation;

public class SVGElementMapping implements ElementMapping {

    private static HashMap foObjs = null;

    public synchronized void addToBuilder(FOTreeBuilder builder) {
        try {
            if (foObjs == null) {
                // this sets the parser that will be used
                // by default (SVGBrokenLinkProvider)
                // normally the user agent value is used
                XMLResourceDescriptor.setXMLParserClassName(
                  Driver.getParserClassName());

                foObjs = new HashMap();
                foObjs.put("svg", new SE());
                foObjs.put("<default>", new SVGMaker());
            }

            String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
            builder.addMapping(svgNS, foObjs);
        } catch (Throwable t) {
            // if the classes are not available
        }
    }

    class SVGMaker extends ElementMapping.Maker {
        public FObj make(FObj parent) {
            return new SVGObj(parent);
        }
    }

    class SE extends ElementMapping.Maker {
        public FObj make(FObj parent) {
            return new SVGElement(parent);
        }
    }
}
