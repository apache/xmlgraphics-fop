/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
 
package org.apache.fop.area;

import java.util.List;

/**
 * Resolvable Interface.
 * Classes that implement this can be resolved when
 * an id is added to the area tree.
 */
public interface Resolvable {

    /**
     * Check if this area has been resolved.
     *
     * @return true once this area is resolved
     */
    boolean isResolved();

    /**
     * Get the array of id references of this resolvable object.
     * If this object contains child resolvables that are
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
