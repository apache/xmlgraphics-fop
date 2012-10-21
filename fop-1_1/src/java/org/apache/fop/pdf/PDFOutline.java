/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * <p>This represents a single Outline object in a PDF, including the root Outlines
 * object. Outlines provide the bookmark bar, usually rendered to the right of
 * a PDF document in user agents such as Acrobat Reader.</p>
 *
 * <p>This work was authored by Kelly A. Campbell.</p>
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

    // whether to show this outline item's child outline items
    private boolean openItem = false;

    /**
     * title to display for the bookmark entry
     */
    private String title;

    private String actionRef;

    /**
     * Create a PDF outline with the title and action.
     *
     * @param title the title of the outline entry (can only be null for root Outlines obj)
     * @param action the action for this outline
     * @param openItem indicator of whether child items are visible or not
     */
    public PDFOutline(String title, String action, boolean openItem) {
        super();
        subentries = new java.util.ArrayList();
        count = 0;
        parent = null;
        prev = null;
        next = null;
        first = null;
        last = null;
        this.title = title;
        actionRef = action;
        this.openItem = openItem;
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
            outline.prev
                = (PDFOutline)subentries.get(subentries.size() - 1);
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
     * {@inheritDoc}
     */
    protected byte[] toPDF() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(128);
        try {
            bout.write(encode("<<"));
            if (parent == null) {
                // root Outlines object
                if (first != null && last != null) {
                    bout.write(encode(" /First " + first.referencePDF() + "\n"));
                    bout.write(encode(" /Last " + last.referencePDF() + "\n"));
                    // no count... we start with the outline completely closed for now
                }
            } else {
                // subentry Outline item object
                bout.write(encode(" /Title "));
                bout.write(encodeText(this.title));
                bout.write(encode("\n"));
                bout.write(encode(" /Parent " + parent.referencePDF() + "\n"));
                if (prev != null) {
                    bout.write(encode(" /Prev " + prev.referencePDF() + "\n"));
                }
                if (next != null) {
                    bout.write(encode(" /Next " + next.referencePDF() + "\n"));
                }
                if (first != null && last != null) {
                    bout.write(encode(" /First " + first.referencePDF() + "\n"));
                    bout.write(encode(" /Last " + last.referencePDF() + "\n"));
                }
                if (count > 0) {
                    bout.write(encode(" /Count " + (openItem ? "" : "-")
                        + count + "\n"));
                }
                if (actionRef != null) {
                    bout.write(encode(" /A " + actionRef + "\n"));
                }
            }
            bout.write(encode(">>"));
        } catch (IOException ioe) {
            log.error("Ignored I/O exception", ioe);
        }
        return bout.toByteArray();
    }

}
