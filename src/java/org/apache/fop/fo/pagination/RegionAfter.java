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
import org.apache.fop.datatypes.FODimension;

/**
 * The fo:region-after element.
 */
public class RegionAfter extends RegionBA {

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public RegionAfter(FONode parent) {
        super(parent, Region.AFTER_CODE);
    }

    /**
     * @see org.apache.fop.fo.pagination.Region#getViewportRectangle(FODimension)
     */
    public Rectangle getViewportRectangle (FODimension reldims) {
        // Depends on extent, precedence ans writing mode
        Rectangle vpRect;
        if (this.wm == WritingMode.LR_TB || this.wm == WritingMode.RL_TB) {
            vpRect = new Rectangle(0, reldims.bpd - getExtent(), reldims.ipd, getExtent());
        } else {
            vpRect = new Rectangle(0, reldims.bpd - getExtent(), getExtent(), reldims.ipd);
        }
        if (getPrecedence() == false) {
            adjustIPD(vpRect, this.wm);
        }
        return vpRect;
    }

    /**
     * @see org.apache.fop.fo.pagination.Region#getDefaultRegionName()
     */
    protected String getDefaultRegionName() {
        return "xsl-region-after";
    }

    /**
     * @see org.apache.fop.fo.pagination.Region#getRegionClassCode()
     */
    public int getRegionClassCode() {
        return Region.AFTER_CODE;
    }

    public String getName() {
        return "fo:region-after";
    }
}

