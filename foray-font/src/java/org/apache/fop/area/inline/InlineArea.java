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

import org.apache.fop.area.Area;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.Trait;

/**
 * Inline Area
 * This area is for all inline areas that can be placed
 * in a line area.
 */
public class InlineArea extends Area {
    
    /**
     * this class stores information about potential adjustments
     * that can be used in order to re-compute adjustments when a
     * page-number or a page-number-citation is resolved
     */
    protected class InlineAdjustingInfo {
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
    }
    
    /**
     * offset position from before edge of parent area
     */
    protected int offset = 0;
    
    /**
     * parent area
     * it is needed in order to recompute adjust ratio and indents
     * when a page-number or a page-number-citation is resolved
     */
    private Area parentArea = null;
    
    /**
     * ipd variation of child areas: if this area has not already
     * been added and cannot notify its parent area, store the variation
     * and wait for the parent area to be set
     */
    private int storedIPDVariation = 0;

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
     * Set the offset of this inline area.
     * This is used to set the offset of the inline area
     * which is relative to the before edge of the parent area.
     *
     * @param offset the offset
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Get the offset of this inline area.
     * This returns the offset of the inline area
     * which is relative to the before edge of the parent area.
     *
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * @param parentArea The parentArea to set.
     */
    public void setParentArea(Area parentArea) {
        this.parentArea = parentArea;
        // notify the parent area about ipd variations
        if (storedIPDVariation > 0) {
            notifyIPDVariation(storedIPDVariation);
            storedIPDVariation = 0;
        }
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
     * @see org.apache.fop.area.Area#addChildArea(Area)
     */
    public void addChildArea(Area childArea) {
        super.addChildArea(childArea);
        if (childArea instanceof InlineArea) {
            ((InlineArea) childArea).setParentArea(this);
        }
    }
    
    /**
     *@return true if the inline area is underlined.
     */
    public boolean hasUnderline() {
        return getBooleanTrait(Trait.UNDERLINE);
    }

    /** @return true if the inline area is overlined. */
    public boolean hasOverline() {
        return getBooleanTrait(Trait.OVERLINE);
    }
    
    /** @return true if the inline area has a line through. */
    public boolean hasLineThrough() {
        return getBooleanTrait(Trait.LINETHROUGH);
    }
    
    /** @return true if the inline area is blinking. */
    public boolean isBlinking() {
        return getBooleanTrait(Trait.BLINK);
    }
    
    /**
     * set the ipd and notify the parent area about the variation;
     * this happens when a page-number or a page-number-citation
     * is resolved to its actual value
     * @param newIPD the new ipd of the area
     */
    public void updateIPD(int newIPD) {
        // default behaviour: do nothing
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
        // default behaviour: simply return false
        return false;
    }
    
    /**
     * notify the parent area about the ipd variation of this area
     * or of a descendant area
     * @param ipdVariation the difference between new and old ipd
     */
    protected void notifyIPDVariation(int ipdVariation) {
        if (getParentArea() instanceof InlineArea) {
            ((InlineArea) getParentArea()).notifyIPDVariation(ipdVariation);
        } else if (getParentArea() instanceof LineArea) {
            ((LineArea) getParentArea()).handleIPDVariation(ipdVariation);
        } else if (getParentArea() == null) {
            // parent area not yet set: store the variations
            storedIPDVariation += ipdVariation;
        }
    }
}

