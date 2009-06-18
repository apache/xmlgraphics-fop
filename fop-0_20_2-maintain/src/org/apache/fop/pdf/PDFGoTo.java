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
package org.apache.fop.pdf;

// Java
import java.io.UnsupportedEncodingException;

/**
 * class representing a /GoTo object.
 *
 */
public class PDFGoTo extends PDFAction {

    /**
     * the pageReference
     */
    protected String pageReference;
    protected String destination = null;
    protected float xPosition = 0, yPosition = 0;

    /**
     * create a /GoTo object.
     *
     * @param number the object's number
     * @param pageReference the pageReference represented by this object
     */
    public PDFGoTo(int number, String pageReference) {

        /* generic creation of object */
        super(number);

        this.pageReference = pageReference;
    }


    /**
     * Sets page reference after object has been created
     *
     * @param pageReference
     * the new page reference to use
     */
    public void setPageReference(String pageReference) {
        this.pageReference = pageReference;
    }

    public String getPageReference() { 
      return pageReference;
    }

    /**
     * Sets the Y position to jump to
     *
     * @param yPosition y position
     */
    public void setYPosition(int yPosition) {
        this.yPosition = (yPosition / 1000f);
    }

    public void setDestination(String dest) {
        destination = dest;
    }

    /**
     * Sets the x Position to jump to
     *
     * @param xPosition x position
     */
    public void setXPosition(int xPosition) {
        this.xPosition = (xPosition / 1000f);
    }

    public String getAction() {
        return referencePDF();
    }


    /**
     * represent the object in PDF
     *
     * @return the PDF string
     */
    public byte[] toPDF() {
        if(destination == null) {
            destination = "/D ["
                              + this.pageReference + " /XYZ " + xPosition
                              + " " + yPosition + " null]\n";
        } else {
            destination = "/D ["
                              + this.pageReference + " " + destination + "]\n";
        }
        String p = new String(this.number + " " + this.generation
                              + " obj\n<<\n/S /GoTo\n" + destination
                              + ">>\nendobj\n");
                              
        try {
            return p.getBytes(PDFDocument.ENCODING);
        } catch (UnsupportedEncodingException ue) {
            return p.getBytes();
        }       
    }

    /*
     * example
     * 29 0 obj
     * <<
     * /S /GoTo
     * /D [23 0 R /FitH 600]
     * >>
     * endobj
     */
}
