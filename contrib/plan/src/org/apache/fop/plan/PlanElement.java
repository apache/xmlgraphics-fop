/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.plan;

import java.awt.geom.Point2D;

import org.apache.fop.fo.FONode;
import org.apache.fop.apps.FOPException;

import org.w3c.dom.Document;
import org.xml.sax.Attributes;

public class PlanElement extends PlanObj {
    Document svgDoc = null;
    float width;
    float height;
    boolean converted;

    public PlanElement(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
        createBasicDocument();
    }

    public void convertToSVG() {
        try {
        if(!converted) {
            converted = true;
            PlanRenderer pr = new PlanRenderer();
            pr.setFontInfo("Helvetica", 12);
            svgDoc = pr.createSVGDocument(doc);
            width = pr.getWidth();
            height = pr.getHeight();

            doc = svgDoc;
        }
        } catch(Throwable t) {
            log.error("Could not convert Plan to SVG", t);
            width = 0;
            height = 0;
        }

    }

    public Document getDocument() {
        convertToSVG();
        return doc;
    }

    public String getDocumentNamespace() {
        if(svgDoc == null) {
            return PlanElementMapping.URI;
        }
        return "http://www.w3.org/2000/svg";
    }

    public Point2D getDimension(Point2D view) {
        convertToSVG();
        return new Point2D.Float(width, height);
    }
}

