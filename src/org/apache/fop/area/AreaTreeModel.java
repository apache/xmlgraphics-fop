/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

/**
 * This is the model for the area tree object.
 * The model implementation can handle the page sequence,
 * page and extensions.
 * The mathods to acces the page viewports can only
 * assume the PageViewport is valid as it remains for
 * the life of the area tree model.
 */
public abstract class AreaTreeModel {
    /**
     * Start a page sequence on this model.
     * @param title the title of the new page sequence
     */
    public abstract void startPageSequence(Title title);

    /**
     * Add a page to this moel.
     * @param page the page to add to the model.
     */
    public abstract void addPage(PageViewport page);

    /**
     * Add an extension to this model.
     * @param ext the extension to add
     * @param when when the extension should be handled
     */
    public abstract void addExtension(TreeExt ext, int when);

    /**
     * Signal the end of the document for any processing.
     */
    public abstract void endDocument();

    /**
     * Get the page sequence count.
     * @return the number of page sequences in the document.
     */
    public abstract int getPageSequenceCount();

    /**
     * Get the title for a page sequence.
     * @param count the page sequence count
     * @return the title of the page sequence
     */
    public abstract Title getTitle(int count);

    /**
     * Get the page count.
     * @param seq the page sequence to count.
     * @return returns the number of pages in a page sequence
     */
    public abstract int getPageCount(int seq);

    /**
     * Get the page for a position in the document.
     * @param seq the page sequence number
     * @param count the page count in the sequence
     * @return the PageViewport for the particular page
     */
    public abstract PageViewport getPage(int seq, int count);

}
