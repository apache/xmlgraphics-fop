/*
 * $Id: PDFOutline.java,v 1.7 2003/03/07 08:25:47 jeremias Exp $
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

import java.util.List;

/**
 * This represents a single Outline object in a PDF, including the root Outlines
 * object. Outlines provide the bookmark bar, usually rendered to the right of
 * a PDF document in user agents such as Acrobat Reader
 *
 * @author Kelly A. Campbell
 *
 */
public class PDFOutline extends PDFObject {

    /**
     * list of sub-entries (outline objects)
     */
    private List subentries;

    /**
     * parent outline object. Root Outlines parent is null
     */
    private PDFOutline parent;

    private PDFOutline prev;
    private PDFOutline next;

    private PDFOutline first;
    private PDFOutline last;

    private int count;

    /**
     * title to display for the bookmark entry
     */
    private String title;

    private String actionRef;

    /**
     * Create a PDF outline with the title and action.
     *
     * @param number the object id number
     * @param title the title of the outline entry (can only be null for root Outlines obj)
     * @param action the action for this outline
     */
    public PDFOutline(int number, String title, String action) {
        super(number);
        subentries = new java.util.ArrayList();
        count = 0;
        parent = null;
        prev = null;
        next = null;
        first = null;
        last = null;
        this.title = title;
        actionRef = action;
    }

    /**
     * Set the title of this Outline object.
     *
     * @param t the title of the outline
     */
    public void setTitle(String t) {
        title = t;
    }

    /**
     * Add a sub element to this outline.
     *
     * @param outline a sub outline
     */
    public void addOutline(PDFOutline outline) {
        if (subentries.size() > 0) {
            outline.prev =
                (PDFOutline)subentries.get(subentries.size() - 1);
            outline.prev.next = outline;
        } else {
            first = outline;
        }

        subentries.add(outline);
        outline.parent = this;

        // note: count is not just the immediate children
        incrementCount();

        last = outline;
    }

    /**
     * Increment the number of subentries and descendants.
     */
    private void incrementCount() {
        // count is a total of our immediate subentries
        // and all descendent subentries
        count++;
        if (parent != null) {
            parent.incrementCount();
        }
    }

    /**
     * represent the object in PDF
     *
     * @return the PDF for this outline
     */
    protected byte[] toPDF() {
        StringBuffer result = new StringBuffer(this.number + " "
                                               + this.generation
                                               + " obj\n<<\n");
        if (parent == null) {
            // root Outlines object
            if (first != null && last != null) {
                result.append(" /First " + first.referencePDF() + "\n");
                result.append(" /Last " + last.referencePDF() + "\n");
                // no count... we start with the outline completely closed for now
            }
        } else {
            // subentry Outline object
            result.append(" /Title (" + escapeString(title) + ")\n");
            result.append(" /Parent " + parent.referencePDF() + "\n");
            if (first != null && last != null) {
                result.append(" /First " + first.referencePDF() + "\n");
                result.append(" /Last " + last.referencePDF() + "\n");
            }
            if (prev != null) {
                result.append(" /Prev " + prev.referencePDF() + "\n");
            }
            if (next != null) {
                result.append(" /Next " + next.referencePDF() + "\n");
            }
            if (count > 0) {
                result.append(" /Count -" + count + "\n");
            }

            if (actionRef != null) {
                result.append(" /A " + actionRef + "\n");
            }


        }
        result.append(">> endobj\n");
        return result.toString().getBytes();

    }

    /**
     * escape string (see 3.8.1 in PDF reference 2nd edition)
     */
    private String escapeString(String s) {
        StringBuffer result = new StringBuffer();
        if (s != null) {
            int l = s.length();

            // byte order marker (0xfeff)
            result.append("\\376\\377");

            for (int i = 0; i < l; i++) {
                char ch = s.charAt(i);
                int high = (ch & 0xff00) >>> 8;
                int low = ch & 0xff;
                result.append("\\");
                result.append(Integer.toOctalString(high));
                result.append("\\");
                result.append(Integer.toOctalString(low));
            }
        }

        return result.toString();
    }

}
