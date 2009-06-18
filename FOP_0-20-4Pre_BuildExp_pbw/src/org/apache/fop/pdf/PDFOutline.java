/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import java.util.Vector;


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
    private Vector _subentries;

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
        _subentries = new Vector();
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
                (PDFOutline)_subentries.elementAt(_subentries.size() - 1);
            outline._prev._next = outline;
        } else {
            _first = outline;
        }

        _subentries.addElement(outline);
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
