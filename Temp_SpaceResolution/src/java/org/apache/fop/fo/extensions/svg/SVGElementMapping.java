/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fo.extensions.svg;

import java.util.HashMap;
import javax.xml.parsers.SAXParserFactory;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.ElementMapping;

import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.batik.dom.svg.SVGDOMImplementation;

/**
 * Setup the SVG element mapping.
 * This adds the svg element mappings used to create the objects
 * that create the SVG Document.
 */
public class SVGElementMapping extends ElementMapping {
    public static String URI = SVGDOMImplementation.SVG_NAMESPACE_URI;
    private boolean batik = true;

    public SVGElementMapping() {
        namespaceURI = URI;
    }

    /**
     * Returns the fully qualified classname of an XML parser for
     * Batik classes that apparently need it (error messages, perhaps)
     * @return an XML parser classname
     */
    private final String getAParserClassName() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            return factory.newSAXParser().getXMLReader().getClass().getName();
        } catch (Exception e) {
            return null;
        }
    }

    protected void initialize() {
        if (foObjs == null && batik == true) {
            // this sets the parser that will be used
            // by default (SVGBrokenLinkProvider)
            // normally the user agent value is used
            try {
                XMLResourceDescriptor.setXMLParserClassName(
                  getAParserClassName());

                foObjs = new HashMap();
                foObjs.put("svg", new SE());
                foObjs.put(DEFAULT, new SVGMaker());
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
