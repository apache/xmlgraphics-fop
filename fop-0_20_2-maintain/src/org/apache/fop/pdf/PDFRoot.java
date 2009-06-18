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
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * class representing a Root (/Catalog) object
 */
public class PDFRoot extends PDFObject {

    /**
     * the /Pages object that is root of the Pages hierarchy
     */
    protected PDFPages rootPages;

    /**
     * Root outline object
     */
    private PDFOutline _outline;

    /**
     * Collection of destinations
     */
    private Collection _destinations;

    /**
     * create a Root (/Catalog) object. NOTE: The PDFRoot
     * object must be created before the PDF document is
     * generated, but it is not assigned an object ID until
     * it is about to be written (immediately before the xref
     * table as part of the trsailer). (mark-fop@inomial.com)
     *
     * @param number the object's number
     */
    public PDFRoot(int number, PDFPages pages) {
        super(number);
        setRootPages(pages);
    }

    /**
     * Before the root is written to the document stream,
     * make sure it's object number is set. Package-private access
     * only; outsiders should not be fiddling with this stuff.
     */
    void setNumber(int number) {
        this.number = number;
    }

    /**
     * add a /Page object to the root /Pages object
     *
     * @param page the /Page object to add
     */
    public void addPage(PDFPage page) {
        this.rootPages.addPage(page);
    }

    /**
     * set the root /Pages object
     *
     * @param pages the /Pages object to set as root
     */
    public void setRootPages(PDFPages pages) {
        this.rootPages = pages;
    }

    public void setRootOutline(PDFOutline outline) {
        _outline = outline;
    }

    public PDFOutline getRootOutline() {
        return _outline;
    }

    public Collection getDestinations() {
        if (_destinations == null) {
            _destinations = new ArrayList();
        }
        return _destinations;
    }


    /**
     * represent the object as PDF.
     *
     * @throws IllegalStateException if the setNumber() method has
     * not been called.
     *
     * @return the PDF string
     */
    public byte[] toPDF()
    throws IllegalStateException {
        StringBuffer p = new StringBuffer(this.number + " " + this.generation
                                          + " obj\n<< /Type /Catalog\n/Pages "
                                          + this.rootPages.referencePDF()
                                          + "\n");
        if (_outline != null) {
            p.append(" /Outlines " + _outline.referencePDF() + "\n");
            p.append(" /PageMode /UseOutlines\n");

        }
        if (_destinations != null) {
           p.append(" /Names << /Dests << /Names [ ");
          for (Iterator i = _destinations.iterator(); i.hasNext(); ) {
              PDFDestination dest = (PDFDestination)i.next();
              p.append(dest.toPDF());
           }
           p.append(" ] >> >>\n");
        }
        p.append(" >>\nendobj\n");

        try {
            return p.toString().getBytes(PDFDocument.ENCODING);
        } catch (UnsupportedEncodingException ue) {
            return p.toString().getBytes();
        }
    }

}
