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

package org.apache.fop.fo.pagination;

// Java
import java.awt.Rectangle;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTreeVisitor;

/**
 * Abstract base class for fo:region-start and fo:region-end.
 */
public abstract class RegionSE extends RegionBASE {

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    protected RegionSE(FONode parent) {
        super(parent);
    }

    /**
     * Adjust the viewport reference rectangle for a region as a function
     * of precedence.
     * If  before and after have precedence = true, the start and end
     * regions only go to the limits of their extents, otherwise
     * they extend in the BPD to the page reference rectangle
     * diminish by extend of start and end if present.
     * @param refRect reference rectangle
     * @param wm writing mode
     */
    protected void adjustIPD(Rectangle refRect, int wm) {
        int offset = 0;
        Region before = getSiblingRegion(Region.BEFORE);
        if (before != null && before.getPrecedence()) {
            offset = before.getExtent();
            refRect.translate(0, offset);
        }
        Region after = getSiblingRegion(Region.AFTER);
        if (after != null && after.getPrecedence()) {
            offset += after.getExtent();
        }
        if (offset > 0) {
            if (wm == WritingMode.LR_TB || wm == WritingMode.RL_TB) {
                refRect.height -= offset;
            } else {
                refRect.width -= offset;
            }
        }
    }

    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveRegionSE(this);
    }

}

