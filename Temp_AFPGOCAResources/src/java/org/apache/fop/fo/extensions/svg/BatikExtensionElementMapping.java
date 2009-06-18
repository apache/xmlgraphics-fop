/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.FONode;
import org.w3c.dom.DOMImplementation;

/**
 * This Element Mapping is for Batik SVG Extension elements
 * of the http://xml.apache.org/batik/ext namespace.
 */
public class BatikExtensionElementMapping extends ElementMapping {

    /** Namespace URI for Batik extension elements */
    public static final String URI = "http://xml.apache.org/batik/ext";

    private boolean batikAvail = true;

    /** Main constructor. */
    public BatikExtensionElementMapping() {
        namespaceURI = URI;
    }

    /** {@inheritDoc} */
    public DOMImplementation getDOMImplementation() {
        return null; //no DOMImplementation necessary here
    }

    /**
     * Returns the fully qualified classname of an XML parser for
     * Batik classes that apparently need it (error messages, perhaps)
     * @return an XML parser classname
     */
    private final String getAParserClassName() {
        try {
            //TODO Remove when Batik uses JAXP instead of SAX directly.
            SAXParserFactory factory = SAXParserFactory.newInstance();
            return factory.newSAXParser().getXMLReader().getClass().getName();
        } catch (Exception e) {
            return null;
        }
    }

    protected void initialize() {
        if (foObjs == null && batikAvail) {
            // this sets the parser that will be used
            // by default (SVGBrokenLinkProvider)
            // normally the user agent value is used
            try {
                XMLResourceDescriptor.setXMLParserClassName(
                  getAParserClassName());

                foObjs = new HashMap();
                foObjs.put("batik", new SE());
                foObjs.put(DEFAULT, new SVGMaker());
            } catch (Throwable t) {
                // if the classes are not available
                // the DISPLAY is not checked
                batikAvail = false;
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
