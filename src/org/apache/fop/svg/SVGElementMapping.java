/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import java.util.HashMap;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTreeBuilder;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.apps.Driver;

import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.w3c.dom.DOMImplementation;

public class SVGElementMapping implements ElementMapping {
    private static HashMap foObjs = null;
    private static boolean batik = true;

    private static synchronized void setupSVG() {
        if (foObjs == null) {
            // this sets the parser that will be used
            // by default (SVGBrokenLinkProvider)
            // normally the user agent value is used
            XMLResourceDescriptor.setXMLParserClassName(
              Driver.getParserClassName());

            foObjs = new HashMap();
            foObjs.put("svg", new SE());
            foObjs.put(DEFAULT, new SVGMaker());
        }
    }

    public void addToBuilder(FOTreeBuilder builder) {
        if(batik) {
            try {
                setupSVG();
                String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
                builder.addMapping(svgNS, foObjs);
            } catch (Throwable t) {
                // if the classes are not available
                // the DISPLAY is not checked
                batik = false;
            }
        }
    }

    static class SVGMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new SVGObj(parent);
        }
    }

    static class SE extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new SVGElement(parent);
        }
    }
}
