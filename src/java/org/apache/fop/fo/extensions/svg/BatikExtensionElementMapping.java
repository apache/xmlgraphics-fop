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

import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.fop.apps.FOFileHandler;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.FONode;

/**
 * This Element Mapping is for Batik SVG Extension elements
 * of the http://xml.apache.org/batik/ext namespace.
 */
public class BatikExtensionElementMapping extends ElementMapping {
    private boolean batikAvail = true;

    public BatikExtensionElementMapping() {
        namespaceURI = "http://xml.apache.org/batik/ext";
    }

    protected void initialize() {
        if (foObjs == null && batikAvail == true) {
            // this sets the parser that will be used
            // by default (SVGBrokenLinkProvider)
            // normally the user agent value is used
            try {
                XMLResourceDescriptor.setXMLParserClassName(
                  FOFileHandler.getParserClassName());

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
