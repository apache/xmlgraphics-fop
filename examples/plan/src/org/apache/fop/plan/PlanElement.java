/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.plan;

import java.awt.geom.Point2D;

import org.apache.fop.fo.FONode;
import org.apache.fop.apps.FOPException;

import org.w3c.dom.Document;
import org.xml.sax.Attributes;

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
     * @see org.apache.fop.fo.FONode#handleAttrs(Attributes)
     */
    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
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
     * @see org.apache.fop.fo.XMLObj#getDocument()
     */
    public Document getDocument() {
        convertToSVG();
        return doc;
    }

    /**
     * @see org.apache.fop.fo.XMLObj#getDocumentNamespace()
     */
    public String getDocumentNamespace() {
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

