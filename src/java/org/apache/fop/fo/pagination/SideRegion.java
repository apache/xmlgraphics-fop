/*
 * Copyright 2005 The Apache Software Foundation.
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

package org.apache.fop.fo.pagination;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;

/**
 * Common base class for side regions (before, after, start, end).
 */
public abstract class SideRegion extends Region {

    private Length extent;
    
    /** @see org.apache.fop.fo.FONode#FONode(FONode) */
    protected SideRegion(FONode parent) {
        super(parent);
    }

    /** @see org.apache.fop.fo.FObj#bind(PropertyList) */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        extent = pList.get(PR_EXTENT).getLength();
    }
    
    /** @return the "extent" property. */
    public Length getExtent() {
        return extent;
    }
    
}
