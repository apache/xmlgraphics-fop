/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.util.List;

/**
 * Resolveable Interface.
 * Classes that implement this can be resolved when
 * an id is added to the area tree.
 */
public interface Resolveable {

    public boolean isResolved();

    public String[] getIDs();

    /**
     * This resolves reference with a list of pages.
     * The pages (PageViewport) contain the rectangle of the area.
     * @param id the id to resolve
     * @param pages the list of pages with the id area
     *              may be null if not found
     */
    public void resolve(String id, List pages);
}
