/*
 * $Id: PDFPage.java,v 1.21 2003/03/07 08:25:47 jeremias Exp $
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
 * class representing a /Page object.
 *
 * There is one of these for every page in a PDF document. The object
 * specifies the dimensions of the page and references a /Resources
 * object, a contents stream and the page's parent in the page
 * hierarchy.
 *
 * Modified by Mark Lillywhite, mark-fop@inomial.com. The Parent
 * object was being referred to by reference, but all that we
 * ever used from the Parent was it's PDF object ID, and according
 * to the memory profile this was causing OOM issues. So, we store
 * only the object ID of the parent, rather than the parent itself.
 */
public class PDFPage extends PDFResourceContext {

    /**
     * the page's parent, a PDF reference object
     */
    protected String parent;

    /**
     * the contents stream
     */
    protected PDFStream contents;

    /**
     * the width of the page in points
     */
    protected int pagewidth;

    /**
     * the height of the page in points
     */
    protected int pageheight;

    /**
     * Duration to display page
     */
    protected int duration = -1;

    /**
     * Transition dictionary
     */
    protected TransitionDictionary trDictionary = null;

    /**
     * create a /Page object
     *
     * @param doc the PDF document holding this page
     * @param number the object's number
     * @param resources the /Resources object
     * @param contents the content stream
     * @param pagewidth the page's width in points
     * @param pageheight the page's height in points
     */
    public PDFPage(PDFDocument doc, int number, PDFResources resources, PDFStream contents,
                   int pagewidth, int pageheight) {

        /* generic creation of object */
        super(number, doc, resources);

        /* set fields using parameters */
        this.contents = contents;
        this.pagewidth = pagewidth;
        this.pageheight = pageheight;
    }

    /**
     * create a /Page object
     *
     * @param doc the PDF document holding this page
     * @param number the object's number
     * @param resources the /Resources object
     * @param pagewidth the page's width in points
     * @param pageheight the page's height in points
     */
    public PDFPage(PDFDocument doc, int number, PDFResources resources,
                   int pagewidth, int pageheight) {

        /* generic creation of object */
        super(number, doc, resources);

        /* set fields using parameters */
        this.pagewidth = pagewidth;
        this.pageheight = pageheight;
    }

    /**
     * set this page contents
     *
     * @param contents the contents of the page
     */
    public void setContents(PDFStream contents) {
        this.contents = contents;
    }

    /**
     * set this page's parent
     *
     * @param parent the /Pages object that is this page's parent
     */
    public void setParent(PDFPages parent) {
        this.parent = parent.referencePDF();
    }

    /**
     * Set the transition dictionary and duration.
     * This sets the duration of the page and the transition
     * dictionary used when going to the next page.
     *
     * @param dur the duration in seconds
     * @param tr the transition dictionary
     */
    public void setTransition(int dur, TransitionDictionary tr) {
        duration = dur;
        trDictionary = tr;
    }

    /**
     * represent this object as PDF
     *
     * @return the PDF string
     */
    public byte[] toPDF() {
        StringBuffer sb = new StringBuffer();

        sb = sb.append(this.number + " " + this.generation + " obj\n"
                       + "<< /Type /Page\n" + "/Parent "
                       + this.parent + "\n"
                       + "/MediaBox [ 0 0 " + this.pagewidth + " "
                       + this.pageheight + " ]\n" + "/Resources "
                       + this.resources.referencePDF() + "\n" + "/Contents "
                       + this.contents.referencePDF() + "\n");
        if (this.annotList != null) {
            sb = sb.append("/Annots " + this.annotList.referencePDF() + "\n");
        }
        if (this.duration != -1) {
            sb = sb.append("/Dur " + this.duration + "\n");
        }
        if (this.trDictionary != null) {
            sb = sb.append("/Trans << " + this.trDictionary.getDictionary() + " >>\n");
        }

        sb = sb.append(">>\nendobj\n");
        return sb.toString().getBytes();
    }

}
