/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
 
package org.apache.fop.plan;

import java.awt.geom.Point2D;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;

import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

/**
 * This class defines the plan element.
 */
public class PlanElement extends PlanObj {

    private Document svgDoc = null;
    private float width;
    private float height;
    private boolean converted;

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public PlanElement(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FONode#processNode
     */
    public void processNode(String elementName, Locator locator, 
                            Attributes attlist, PropertyList propertyList)
        throws FOPException {
        super.processNode(elementName, locator, attlist, propertyList);
        createBasicDocument();
    }

    /**
     * Converts the element to SVG.
     */
    public void convertToSVG() {
        try {
            if (!converted) {
                converted = true;
                PlanRenderer pr = new PlanRenderer();
                pr.setFontInfo("Helvetica", 12);
                svgDoc = pr.createSVGDocument(doc);
                width = pr.getWidth();
                height = pr.getHeight();
    
                doc = svgDoc;
            }
        } catch (Throwable t) {
            getLogger().error("Could not convert Plan to SVG", t);
            width = 0;
            height = 0;
        }

    }

    /**
     * @see org.apache.fop.fo.XMLObj#getDOMDocument()
     */
    public Document getDOMDocument() {
        convertToSVG();
        return doc;
    }

    /** @see org.apache.fop.fo.FONode#getNamespaceURI() */
    public String getNamespaceURI() {
        if (svgDoc == null) {
            return PlanElementMapping.NAMESPACE;
        }
        return "http://www.w3.org/2000/svg";
    }

    /**
     * @see org.apache.fop.fo.XMLObj#getDimension(Point2D)
     */
    public Point2D getDimension(Point2D view) {
        convertToSVG();
        return new Point2D.Float(width, height);
    }
}

