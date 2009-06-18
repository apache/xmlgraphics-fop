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
import java.util.ArrayList;


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
    private ArrayList _subentries;

    /**
     * parent outline object. Root Outlines parent is null
     */
    private PDFOutline _parent;

    private PDFOutline _prev;
    private PDFOutline _next;

    private PDFOutline _first;
    private PDFOutline _last;

    private int _count;


    /**
     * title to display for the bookmark entry
     */
    private String _title;

    String _actionRef;



    /**
     * @param number the object id number
     * @param title the title of the outline entry (can only be null for root Outlines obj)
     * @param page the page which this outline refers to.
     */
    public PDFOutline(int number, String title, String action) {
        super(number);
        _subentries = new ArrayList();
        _count = 0;
        _parent = null;
        _prev = null;
        _next = null;
        _first = null;
        _last = null;
        _title = title;
        _actionRef = action;


    }

    public void setTitle(String title) {
        _title = title;
    }

    /**
     * Add a sub element to this outline
     */
    public void addOutline(PDFOutline outline) {
        if (_subentries.size() > 0) {
            outline._prev =
                (PDFOutline)_subentries.get(_subentries.size() - 1);
            outline._prev._next = outline;
        } else {
            _first = outline;
        }

        _subentries.add(outline);
        outline._parent = this;

        incrementCount();    // note: count is not just the immediate children

        _last = outline;

    }

    private void incrementCount() {
        // count is a total of our immediate subentries and all descendent subentries
        _count++;
        if (_parent != null) {
            _parent.incrementCount();
        }
    }


    /**
     * represent the object in PDF
     */
    protected byte[] toPDF() {
        StringBuffer result = new StringBuffer(this.number + " "
                                               + this.generation
                                               + " obj\n<<\n");
        if (_parent == null) {
            // root Outlines object
            if (_first != null && _last != null) {
                result.append(" /First " + _first.referencePDF() + "\n");
                result.append(" /Last " + _last.referencePDF() + "\n");
                // no count... we start with the outline completely closed for now
            }
        } else {
            // subentry Outline object
            result.append(" /Title (" + escapeString(_title) + ")\n");
            result.append(" /Parent " + _parent.referencePDF() + "\n");
            if (_first != null && _last != null) {
                result.append(" /First " + _first.referencePDF() + "\n");
                result.append(" /Last " + _last.referencePDF() + "\n");
            }
            if (_prev != null) {
                result.append(" /Prev " + _prev.referencePDF() + "\n");
            }
            if (_next != null) {
                result.append(" /Next " + _next.referencePDF() + "\n");
            }
            if (_count > 0) {
                result.append(" /Count -" + _count + "\n");
            }

            if (_actionRef != null) {
                result.append(" /A " + _actionRef + "\n");
            }


        }
        result.append(">> endobj\n");

        try {
            return result.toString().getBytes(PDFDocument.ENCODING);
        } catch (UnsupportedEncodingException ue) {
            return result.toString().getBytes();
        }
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
