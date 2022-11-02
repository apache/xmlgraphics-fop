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

package org.apache.fop.layoutmgr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.traits.MinOptMax;

/**
 * A block-stacking layout manager for an FO that supports spaces, border and padding.
 */
public abstract class SpacedBorderedPaddedBlockLayoutManager extends BlockStackingLayoutManager
        implements ConditionalElementListener {

    private static final Log LOG = LogFactory.getLog(BlockLayoutManager.class);

    protected MinOptMax effSpaceBefore;

    protected MinOptMax effSpaceAfter;

    protected boolean discardBorderBefore;
    protected boolean discardBorderAfter;
    protected boolean discardPaddingBefore;
    protected boolean discardPaddingAfter;

    public SpacedBorderedPaddedBlockLayoutManager(FObj node) {
        super(node);
    }

    public void notifySpace(RelSide side, MinOptMax effectiveLength) {
        if (RelSide.BEFORE == side) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(this + ": Space " + side + ", "
                        + this.effSpaceBefore + "-> " + effectiveLength);
            }
            this.effSpaceBefore = effectiveLength;
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug(this + ": Space " + side + ", "
                        + this.effSpaceAfter + "-> " + effectiveLength);
            }
            this.effSpaceAfter = effectiveLength;
        }
    }

    public void notifyBorder(RelSide side, MinOptMax effectiveLength) {
        if (effectiveLength == null) {
            if (RelSide.BEFORE == side) {
                this.discardBorderBefore = true;
            } else {
                this.discardBorderAfter = true;
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(this + ": Border " + side + " -> " + effectiveLength);
        }
    }

    public void notifyPadding(RelSide side, MinOptMax effectiveLength) {
        if (effectiveLength == null) {
            if (RelSide.BEFORE == side) {
                this.discardPaddingBefore = true;
            } else {
                this.discardPaddingAfter = true;
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(this + ": Padding " + side + " -> " + effectiveLength);
        }
    }

    @Override
    public int getBaselineOffset() {
        int baselineOffset = super.getBaselineOffset();
        if (effSpaceBefore != null) {
            baselineOffset += effSpaceBefore.getOpt();
        }
        if (!discardBorderBefore) {
            baselineOffset += getCommonBorderPaddingBackground().getBorderBeforeWidth(false);
        }
        if (!discardPaddingBefore) {
            baselineOffset += getCommonBorderPaddingBackground().getPaddingBefore(false, this);
        }
        return baselineOffset;
    }

    /**
     * Returns the {@link CommonBorderPaddingBackground} instance from the FO handled by this layout manager.
     */
    protected abstract CommonBorderPaddingBackground getCommonBorderPaddingBackground();

}
