/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

/**
 * Area tree extension interface.
 * This interface is used by area tree extensions that are handled
 * by the renderer.
 * When this extension is handled by the area tree it is rendered
 * according to the three possibilities, IMMEDIATELY, AFTER_PAGE
 * or END_OF_DOC.
 */
public interface TreeExt {
    /**
     * Render this extension immediately when
     * being handled by the area tree.
     */
    public static final int IMMEDIATELY = 0;

    /**
     * Render this extension after the next page is rendered
     * or prepared when being handled by the area tree.
     */
    public static final int AFTER_PAGE = 1;

    /**
     * Render this extension at the end of the document once
     * all pages have been fully rendered.
     */
    public static final int END_OF_DOC = 2;

    /**
     * Check if this tree extension is also resolveable so that
     * the area tree can do id reference resolution when the
     * extension is added to the area tree.
     *
     * @return true if this also implements resolveable
     */
    boolean isResolveable();

    /**
     * Get the mime type for the document that this area tree
     * extension applies.
     *
     * @return the mime type of the document where this applies
     */
    String getMimeType();

    /**
     * Get the name of this extension.
     *
     * @return the name of this extension
     */
    String getName();
}
