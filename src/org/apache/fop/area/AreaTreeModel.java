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
}
