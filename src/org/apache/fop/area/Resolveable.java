/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
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

    /**
     * Check if this area has been resolved.
     *
     * @return true once this area is resolved
     */
    boolean isResolved();

    /**
     * Get the array of id references of this resolveable object.
     * If this object contains child resolveables that are
     * resolved through this then it should return the id's of
     * the child also.
     *
     * @return the id references for resolving this object
     */
    String[] getIDs();

    /**
     * This resolves reference with a list of pages.
     * The pages (PageViewport) contain the rectangle of the area.
     * @param id the id to resolve
     * @param pages the list of pages with the id area
     *              may be null if not found
     */
    void resolve(String id, List pages);
}
