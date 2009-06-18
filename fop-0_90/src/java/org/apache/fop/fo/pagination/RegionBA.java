/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

// Java
import java.awt.Rectangle;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;

/**
 * Abstract base class for fo:region-before and fo:region-after.
 */
public abstract class RegionBA extends SideRegion {
    // The value of properties relevant for fo:region-[before|after].
    private int precedence;
    // End of property values
    
    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    protected RegionBA(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        precedence = pList.get(PR_PRECEDENCE).getEnum();
    }

    /**
     * @return the "precedence" property.
     */
    public int getPrecedence() {
        return precedence;
    }

    /**
     * Adjust the viewport reference rectangle for a region as a function
     * of precedence.
     * If precedence is false on a before or after region, its
     * inline-progression-dimension is limited by the extent of the start
     * and end regions if they are present.
     * @param vpRefRect viewport reference rectangle
     * @param wm writing mode
     * @param siblingContext the context to use to resolve extent on siblings
     */
    protected void adjustIPD(Rectangle vpRefRect, int wm, PercentBaseContext siblingContext) {
        int offset = 0;
        RegionStart start = (RegionStart) getSiblingRegion(FO_REGION_START);
        if (start != null) {
            offset = start.getExtent().getValue(siblingContext);
            vpRefRect.translate(offset, 0);  // move (x, y) units
        }
        RegionEnd end = (RegionEnd) getSiblingRegion(FO_REGION_END);
        if (end != null) {
            offset += end.getExtent().getValue(siblingContext);
        }
        if (offset > 0) {
            if (wm == EN_LR_TB || wm == EN_RL_TB) {
                vpRefRect.width -= offset;
            } else {
                vpRefRect.height -= offset;
            }
        }
    }
}

