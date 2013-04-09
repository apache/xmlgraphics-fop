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
 * Abstract base class for <a href="http://www.w3.org/TR/xsl/#fo_region-before">
 * <code>fo:region-before</code></a> and <a href="http://www.w3.org/TR/xsl/#fo_region-after">
 * <code>fo:region-after</code></a>.
 */
public abstract class RegionBA extends SideRegion {
    // The value of properties relevant for fo:region-[before|after].
    private int precedence;
    // End of property values

    /**
     * Create a RegionBA instance that is a child of the
     * given parent {@link FONode}.
     * @param parent    the {@link FONode} that is to be the parent
     */
    protected RegionBA(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        precedence = pList.get(PR_PRECEDENCE).getEnum();
    }

    /**
     * Get the value of the <code>precedence</code> property.
     * @return the "precedence" property
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
    protected void adjustIPD
        (Rectangle vpRefRect, WritingMode wm, PercentBaseContext siblingContext) {
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
        // [TBD] WRITING MODE ALERT
        if (offset > 0) {
            if (wm == WritingMode.LR_TB || wm == WritingMode.RL_TB) {
                vpRefRect.width -= offset;
            } else {
                vpRefRect.height -= offset;
            }
        }
    }
}

