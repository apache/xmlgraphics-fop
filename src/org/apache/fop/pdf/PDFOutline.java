/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import java.util.List;
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
    public PDFOutline(int number, String ti, String action) {
        super(number);
        subentries = new ArrayList();
        count = 0;
        parent = null;
        prev = null;
        next = null;
        first = null;
        last = null;
        title = ti;
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
