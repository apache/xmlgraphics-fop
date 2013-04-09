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

import java.awt.Rectangle;

import org.apache.fop.datatypes.FODimension;
import org.apache.fop.datatypes.LengthBase;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;

/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_region-before">
 * <code>fo:region-before</code></a> object.
 */
public class RegionBefore extends RegionBA {

    /**
     * Create a RegionBefore instance that is a child of the
     * given parent {@link FONode}.
     * @param parent    the {@link FONode} that is to be the parent
     */
    public RegionBefore(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    protected String getDefaultRegionName() {
        return "xsl-region-before";
    }

    /** {@inheritDoc} */
    public Rectangle getViewportRectangle (FODimension reldims) {
        /* Special rules apply to resolving extent as values are resolved relative
         * to the page size and reference orientation.
         */
        PercentBaseContext pageWidthContext = getPageWidthContext(LengthBase.CUSTOM_BASE);
        PercentBaseContext pageHeightContext = getPageHeightContext(LengthBase.CUSTOM_BASE);
        PercentBaseContext neighbourContext;
        Rectangle vpRect;
        // [TBD] WRITING MODE ALERT
        switch (getWritingMode().getEnumValue()) {
        case Constants.EN_TB_LR:
        case Constants.EN_TB_RL:
            neighbourContext = pageHeightContext;
            vpRect = new Rectangle(0, 0, getExtent().getValue(pageWidthContext), reldims.ipd);
            break;
        case Constants.EN_LR_TB:
        case Constants.EN_RL_TB:
        default:
            neighbourContext = pageWidthContext;
            vpRect = new Rectangle(0, 0, reldims.ipd, getExtent().getValue(pageHeightContext));
            break;
        }
        if (getPrecedence() == EN_FALSE) {
            adjustIPD(vpRect, layoutMaster.getWritingMode(), neighbourContext);
        }
        return vpRect;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "region-before";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_REGION_BEFORE}
     */
    public int getNameId() {
        return FO_REGION_BEFORE;
    }
}

