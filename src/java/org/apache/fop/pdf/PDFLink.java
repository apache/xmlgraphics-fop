/*
 * $Id: PDFLink.java,v 1.12 2003/03/07 08:25:47 jeremias Exp $
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
package org.apache.fop.pdf;

// Java
import java.awt.geom.Rectangle2D;

/**
 * class representing an /Annot object of /Subtype /Link
 */
public class PDFLink extends PDFObject {
    /**
     * Used to represent an external link.
     */
    public static final int EXTERNAL = 0;

    /**
     * Used to represent an internal link.
     */
    public static final int INTERNAL = 1;

    private float ulx;
    private float uly;
    private float brx;
    private float bry;
    private String color;
    private PDFAction action;

    /**
     * create objects associated with a link annotation (GoToR)
     *
     * @param r the rectangle of the link hotspot in absolute coordinates
     */
    public PDFLink(Rectangle2D r) {
        /* generic creation of PDF object */
        super();

        this.ulx = (float)r.getX();
        this.uly = (float)r.getY();
        this.brx = (float)(r.getX() + r.getWidth());
        this.bry = (float)(r.getY() + r.getHeight());
        this.color = "0 0 0";    // just for now

    }

    /**
     * Set the pdf action for this link.
     * @param action the pdf action that is activated for this link
     */
    public void setAction(PDFAction action) {
        this.action = action;
    }

    /**
     * @see org.apache.fop.pdf.PDFObject#toPDFString()
     */
    public String toPDFString() {
        String s = getObjectID()
                   + "<< /Type /Annot\n" + "/Subtype /Link\n" + "/Rect [ "
                   + (ulx) + " " + (uly) + " "
                   + (brx) + " " + (bry) + " ]\n" + "/C [ "
                   + this.color + " ]\n" + "/Border [ 0 0 0 ]\n" + "/A "
                   + this.action.getAction() + "\n" + "/H /I\n>>\nendobj\n";
        return s;
    }

    /*
     * example
     * 19 0 obj
     * <<
     * /Type /Annot
     * /Subtype /Link
     * /Rect [ 176.032 678.48412 228.73579 692.356 ]
     * /C [ 0.86491 0.03421 0.02591 ]
     * /Border [ 0 0 1 ]
     * /A 28 0 R
     * /H /I
     * >>
     * endobj
     */

    /**
     * Check if this equals another object.
     *
     * @param obj the object to compare
     * @return true if this equals other object
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof PDFLink)) {
            return false;
        }

        PDFLink link = (PDFLink)obj;

        if (!((link.ulx == ulx) && (link.uly == uly)
              && (link.brx == brx) && (link.bry == bry))) {
            return false;
        }

        if (!(link.color.equals(color)
             && link.action.getAction().equals(action.getAction()))) {
            return false;
        }

        return true;
    }
}

