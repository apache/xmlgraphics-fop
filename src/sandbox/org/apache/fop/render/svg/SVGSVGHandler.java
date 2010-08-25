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

package org.apache.fop.render.svg;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.XMLHandler;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGSVGElement;

/** The svg:svg element handler. */
public class SVGSVGHandler implements XMLHandler, SVGRendererContextConstants {

    /** {@inheritDoc} */
    public void handleXML(RendererContext context,
                org.w3c.dom.Document doc, String ns) throws Exception {
        if (getNamespace().equals(ns)) {
            if (!(doc instanceof SVGDocument)) {
                DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
                doc = DOMUtilities.deepCloneDocument(doc, impl);
            }
            SVGSVGElement svg = ((SVGDocument) doc).getRootElement();
            SVGDocument targetDoc = (SVGDocument)context.getProperty(SVG_DOCUMENT);
            SVGElement currentPageG = (SVGElement)context.getProperty(SVG_PAGE_G);
            Element view = targetDoc.createElementNS(getNamespace(), "svg");
            Node newsvg = targetDoc.importNode(svg, true);
            //view.setAttributeNS(null, "viewBox", "0 0 ");
            int xpos = ((Integer)context.getProperty(XPOS)).intValue();
            int ypos = ((Integer)context.getProperty(YPOS)).intValue();
            view.setAttributeNS(null, "x", "" + xpos / 1000f);
            view.setAttributeNS(null, "y", "" + ypos / 1000f);

            // this fixes a problem where the xmlns is repeated sometimes
            Element ele = (Element) newsvg;
            ele.setAttributeNS(XMLSupport.XMLNS_NAMESPACE_URI, "xmlns",
                               getNamespace());
            if (ele.hasAttributeNS(null, "xmlns")) {
                ele.removeAttributeNS(null, "xmlns");
            }

            view.appendChild(newsvg);
            currentPageG.appendChild(view);
        }
    }


    /** {@inheritDoc} */
    public boolean supportsRenderer(Renderer renderer) {
        return (renderer instanceof SVGRenderer);
    }

    /** {@inheritDoc} */
    public String getNamespace() {
        return SVGRenderer.MIME_TYPE;
    }


}
