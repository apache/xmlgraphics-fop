/*
 * $Id: PDFGoToRemote.java,v 1.7 2003/03/07 08:25:47 jeremias Exp $
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

/**
 * class representing a /GoToR object.
 */
public class PDFGoToRemote extends PDFAction {

    /**
     * the file specification
     */
    private PDFFileSpec pdfFileSpec;
    private int pageReference = 0;
    private String destination = null;

    /**
     * create an GoToR object.
     *
     * @param pdfFileSpec the fileSpec associated with the action
     */
    public PDFGoToRemote(PDFFileSpec pdfFileSpec) {
        /* generic creation of object */
        super();

        this.pdfFileSpec = pdfFileSpec;
    }

    /**
     * create an GoToR object.
     *
     * @param pdfFileSpec the fileSpec associated with the action
     * @param page a page reference within the remote document
     */
    public PDFGoToRemote(PDFFileSpec pdfFileSpec, int page) {
        /* generic creation of object */
        super();

        this.pdfFileSpec = pdfFileSpec;
        this.pageReference = page;
    }

    /**
     * create an GoToR object.
     *
     * @param pdfFileSpec the fileSpec associated with the action
     * @param dest a named destination within the remote document
     */
    public PDFGoToRemote(PDFFileSpec pdfFileSpec, String dest) {
        /* generic creation of object */
        super();

        this.pdfFileSpec = pdfFileSpec;
        this.destination = dest;
    }

    /**
     * return the action string which will reference this object
     *
     * @return the action String
     */
    public String getAction() {
        return this.referencePDF();
    }

    /**
     * @see org.apache.fop.pdf.PDFObject#toPDFString()
     */
    public String toPDFString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append(getObjectID());
        sb.append("<<\n/S /GoToR\n/F " + pdfFileSpec.referencePDF() + "\n");

        if (destination != null) {
            sb.append("/D (" + this.destination + ")");
        } else {
            sb.append("/D [ " + this.pageReference + " /XYZ null null null ]");
        }

        sb.append(" \n>>\nendobj\n");

        return sb.toString();
    }


    /*
     * example
     * 28 0 obj
     * <<
     * /S /GoToR
     * /F 29 0 R
     * /D [ 0 /XYZ -6 797 null ]
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

        if (obj == null || !(obj instanceof PDFGoToRemote)) {
            return false;
        }

        PDFGoToRemote remote = (PDFGoToRemote)obj;

        if (!remote.pdfFileSpec.referencePDF().equals(pdfFileSpec.referencePDF())) {
            return false;
        }

        if (destination != null) {
            if (!destination.equals(remote.destination)) {
                return false;
            }
        } else {
            if (pageReference != remote.pageReference) {
                return false;
            }
        }

        return true;
    }
}

