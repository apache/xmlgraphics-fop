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

package org.apache.fop.area.inline;

import java.io.Serializable;
import java.util.List;

import org.apache.fop.area.Area;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.Trait;
import org.apache.fop.complexscripts.bidi.InlineRun;

/**
 * Inline Area
 * This area is for all inline areas that can be placed
 * in a line area.
 */
public class InlineArea extends Area {

    private static final long serialVersionUID = -8940066479810170980L;

    /**
     * this class stores information about potential adjustments
     * that can be used in order to re-compute adjustments when a
     * page-number or a page-number-citation is resolved
     */
    // @SuppressFBWarnings("SE_INNER_CLASS")
    protected class InlineAdjustingInfo implements Serializable {

        private static final long serialVersionUID = -5601387735459712149L;

        /** stretch of the inline area */
        protected int availableStretch;
        /** shrink of the inline area */
        protected int availableShrink;
        /** total adjustment (= ipd - width of fixed elements) */
        protected int adjustment;

        /**
         * Constructor
         *
         * @param stretch the available space for stretching
         * @param shrink the available space for shrinking
         * @param adj space adjustment type
         */
        protected InlineAdjustingInfo(int stretch, int shrink, int adj) {
            availableStretch = stretch;
            availableShrink = shrink;
            adjustment = adj;
        }

        /**
         * Apply the variation factor
         *
         * @param variationFactor the factor by which the adjustment is to be changed
         * @return the IPD increase
         */
        protected int applyVariationFactor(double variationFactor) {
            int oldAdjustment = adjustment;
            adjustment *= variationFactor;
            return adjustment - oldAdjustment;
        }
    }

    /**
     * offset position from before edge of parent area
     */
    protected int blockProgressionOffset;

    /**
     * parent area
     * it is needed in order to recompute adjust ratio and indents
     * when a page-number or a page-number-citation is resolved
     */
    private Area parentArea;

    /**
     * ipd variation of child areas: if this area has not already
     * been added and cannot notify its parent area, store the variation
     * and wait for the parent area to be set
     */
    private int storedIPDVariation;

    /**
     * The adjustment information object
     */
    protected InlineAdjustingInfo adjustingInfo;

    /**
     * Default constructor for inline area.
     */
    public InlineArea() {
        this (0, -1);
    }

    /**
     * Instantiate inline area.
     * @param blockProgressionOffset a block progression offset or zero
     * @param bidiLevel a resolved bidi level or -1
     */
    protected InlineArea(int blockProgressionOffset, int bidiLevel) {
        this.blockProgressionOffset = blockProgressionOffset;
        setBidiLevel(bidiLevel);
    }

    /**
     * @return the adjustment information object
     */
    public InlineAdjustingInfo getAdjustingInfo() {
        return adjustingInfo;
    }

    /**
     * Create a new adjustment information object
     * @param stretch the available space for stretching
     * @param shrink the available space for shrinking
     * @param adjustment space adjustment type
     */
    public void setAdjustingInfo(int stretch, int shrink, int adjustment) {
        adjustingInfo = new InlineAdjustingInfo(stretch, shrink, adjustment);
    }

    /**
     * Sets the adjustment information from an existing object
     * @param adjustingInfo the existing adjustment object
     */
    public void setAdjustingInfo(InlineAdjustingInfo adjustingInfo) {
        this.adjustingInfo = adjustingInfo;
    }

    /**
     * Modify the adjustment value in the adjustment information object
     * @param adjustment the new adjustment value
     */
    public void setAdjustment(int adjustment) {
        if (adjustingInfo != null) {
            adjustingInfo.adjustment = adjustment;
        }
    }

    /**
     * Increase the inline progression dimensions of this area.
     * This is used for inline parent areas that contain mulitple child areas.
     *
     * @param ipd the inline progression to increase by
     */
    public void increaseIPD(int ipd) {
        this.ipd += ipd;
    }

    /**
     * Set the block progression offset of this inline area.
     * This is used to set the offset of the inline area
     * which is relative to the before edge of the parent area.
     *
     * @param blockProgressionOffset the offset
     */
    public void setBlockProgressionOffset(int blockProgressionOffset) {
        this.blockProgressionOffset = blockProgressionOffset;
    }

    /**
     * Get the block progression offset of this inline area.
     * This returns the offset of the inline area
     * relative to the before edge of the parent area.
     *
     * @return the blockProgressionOffset
     */
    public int getBlockProgressionOffset() {
        return blockProgressionOffset;
    }

    /**
     * @param parentArea The parentArea to set.
     */
    public void setParentArea(Area parentArea) {
        this.parentArea = parentArea;
    }

    /**
     * @return Returns the parentArea.
     */
    public Area getParentArea() {
        return parentArea;
    }

    /**
     * Set the parent for the child area.
     *
     * {@inheritDoc}
     */
    @Override
    public void addChildArea(Area childArea) {
        super.addChildArea(childArea);
        if (childArea instanceof InlineArea) {
            ((InlineArea) childArea).setParentArea(this);
        }
    }

    /** @return true if the inline area is underlined. */
    public boolean hasUnderline() {
        return getTraitAsBoolean(Trait.UNDERLINE);
    }

    /** @return true if the inline area is overlined. */
    public boolean hasOverline() {
        return getTraitAsBoolean(Trait.OVERLINE);
    }

    /** @return true if the inline area has a line through. */
    public boolean hasLineThrough() {
        return getTraitAsBoolean(Trait.LINETHROUGH);
    }

    /** @return true if the inline area is blinking. */
    public boolean isBlinking() {
        return getTraitAsBoolean(Trait.BLINK);
    }

    /**
     * recursively apply the variation factor to all descendant areas
     * @param variationFactor the variation factor that must be applied to adjustments
     * @param lineStretch     the total stretch of the line
     * @param lineShrink      the total shrink of the line
     * @return true if there is an UnresolvedArea descendant
     */
    public boolean applyVariationFactor(double variationFactor,
                                        int lineStretch, int lineShrink) {
        // default behaviour: update the IPD and return false
        if (adjustingInfo != null) {
            setIPD(getIPD() + adjustingInfo.applyVariationFactor(variationFactor));
        }
        return false;
    }

    /**
     * Apply IPD variation.
     * @param ipdVariation the variation
     */
    public void handleIPDVariation(int ipdVariation) {
        if (log.isTraceEnabled()) {
            log.trace("Handling IPD variation for " + getClass().getSimpleName()
                    + ": increase by " + ipdVariation + " mpt.");
        }

        if (ipdVariation != 0) {
            increaseIPD(ipdVariation);
            notifyIPDVariation(ipdVariation);
        }
    }

    /**
     * notify the parent area about the ipd variation of this area
     * or of a descendant area
     * @param ipdVariation the difference between new and old ipd
     */
    protected void notifyIPDVariation(int ipdVariation) {
        Area parentArea = getParentArea();
        if (parentArea instanceof InlineArea) {
            ((InlineArea) parentArea).handleIPDVariation(ipdVariation);
        } else if (parentArea instanceof LineArea) {
            ((LineArea) parentArea).handleIPDVariation(ipdVariation);
        } else if (parentArea == null) {
            // parent area not yet set: store the variations
            storedIPDVariation += ipdVariation;
        }
    }

    /**
     * Returns the offset that this area would have if its offset and size were taking
     * children areas into account. The bpd of an inline area is taken from its nominal
     * font and doesn't depend on the bpds of its children elements. However, in the case
     * of a basic-link element we want the active area to cover all of the children
     * elements.
     *
     * @return the offset that this area would have if the before-edge of its
     * content-rectangle were coinciding with the <q>beforest</q> before-edge of its
     * children allocation-rectangles.
     * @see #getVirtualBPD()
     * @see BasicLinkArea
     */
    int getVirtualOffset() {
        return getBlockProgressionOffset();
    }

    /**
     * Returns the block-progression-dimension that this area would have if it were taking
     * its children elements into account. See {@linkplain #getVirtualOffset()}.
     *
     * @return the bpd
     */
    int getVirtualBPD() {
        return getBPD();
    }

    /**
     * Collection bidi inline runs.
     * @param runs current list of inline runs
     * @return modified list of inline runs, having appended new run
     */
    public List collectInlineRuns(List runs) {
        assert runs != null;
        runs.add(new InlineRun(this, new int[] {getBidiLevel()}));
        return runs;
    }

    /**
     * Determine if inline area IA is an ancestor inline area or same as this area.
     * @param ia inline area to test
     * @return true if specified inline area is an ancestor or same as this area
     */
    public boolean isAncestorOrSelf(InlineArea ia) {
        return (ia == this) || isAncestor(ia);
    }

    /**
     * Determine if inline area IA is an ancestor inline area of this area.
     * @param ia inline area to test
     * @return true if specified inline area is an ancestor of this area
     */
    public boolean isAncestor(InlineArea ia) {
        for (Area p = getParentArea(); p != null;) {
            if (p == ia) {
                return true;
            } else if (p instanceof InlineArea) {
                p = ((InlineArea) p).getParentArea();
            } else {
                p = null;
            }
        }
        return false;
    }

}
