/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import org.apache.fop.traits.WritingMode;

/**
 * Abstract base class for <a href="http://www.w3.org/TR/xsl/#fo_region-start">
 * <code>fo:region-start</code></a> and <a href="http://www.w3.org/TR/xsl/#fo_region-end">
 * <code>fo:region-end</code></a>.
 */
public abstract class RegionSE extends SideRegion {
    // The value of properties relevant for fo:region-[start|end].
    // End of property values

    /**
     * Create a RegionSE instance that is a child of the
     * given parent {@link FONode}.
     * @param parent    the {@link FONode} that is to be the parent
     */
    protected RegionSE(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
    }

    /**
     * Adjust the viewport reference rectangle for a region as a function
     * of precedence.
     * If  before and after have precedence = true, the start and end
     * regions only go to the limits of their extents, otherwise
     * they extend in the BPD to the page reference rectangle
     * diminish by extend of start and end if present.
     * @param vpRefRect viewport reference rectangle
     * @param wm writing mode
     * @param siblingContext the context to use to resolve extent on siblings
     */
    protected void adjustIPD
        ( Rectangle vpRefRect, WritingMode wm, PercentBaseContext siblingContext ) {
        int offset = 0;
        RegionBefore before = (RegionBefore) getSiblingRegion(FO_REGION_BEFORE);
        if (before != null && before.getPrecedence() == EN_TRUE) {
            offset = before.getExtent().getValue(siblingContext);
            vpRefRect.translate(0, offset);
        }
        RegionAfter after = (RegionAfter) getSiblingRegion(FO_REGION_AFTER);
        if (after != null && after.getPrecedence() == EN_TRUE) {
            offset += after.getExtent().getValue(siblingContext);
        }
        // [TBD] WRITING MODE ALERT
        if (offset > 0) {
            if (wm == WritingMode.LR_TB || wm == WritingMode.RL_TB) {
                vpRefRect.height -= offset;
            } else {
                vpRefRect.width -= offset;
            }
        }
    }
}

