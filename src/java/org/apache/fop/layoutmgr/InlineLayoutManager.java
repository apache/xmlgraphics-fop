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

package org.apache.fop.layoutmgr;


import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.SpaceProperty;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;

/**
 * LayoutManager for objects which stack children in the inline direction,
 * such as Inline or Line
 */
public class InlineLayoutManager extends InlineStackingLayoutManager {
    private Inline fobj;

    private CommonMarginInline inlineProps = null;
    private CommonBorderPaddingBackground borderProps = null;

    /**
     * Create an inline layout manager.
     * This is used for fo's that create areas that
     * contain inline areas.
     *
     * @param node the formatting object that creates the area
     */
    public InlineLayoutManager(Inline node) {
        super(node);
        fobj = node;
    }
    
    /**
     * @see org.apache.fop.layoutmgr.AbstractLayoutManager#initProperties()
     */
    protected void initProperties() {
        inlineProps = fobj.getCommonMarginInline();
        borderProps = fobj.getCommonBorderPaddingBackground();
        int iPad = borderProps.getPadding(CommonBorderPaddingBackground.BEFORE, false);
        iPad += borderProps.getBorderWidth(CommonBorderPaddingBackground.BEFORE,
                                             false);
        iPad += borderProps.getPadding(CommonBorderPaddingBackground.AFTER, false);
        iPad += borderProps.getBorderWidth(CommonBorderPaddingBackground.AFTER, false);
        extraBPD = new MinOptMax(iPad);
    }

    protected MinOptMax getExtraIPD(boolean bNotFirst, boolean bNotLast) {
        int iBP = borderProps.getPadding(CommonBorderPaddingBackground.START,
                                           bNotFirst);
        iBP += borderProps.getBorderWidth(CommonBorderPaddingBackground.START,
                                            bNotFirst);
        iBP += borderProps.getPadding(CommonBorderPaddingBackground.END, bNotLast);
        iBP += borderProps.getBorderWidth(CommonBorderPaddingBackground.END, bNotLast);
        return new MinOptMax(iBP);
    }


    protected boolean hasLeadingFence(boolean bNotFirst) {
        int iBP = borderProps.getPadding(CommonBorderPaddingBackground.START,
                                           bNotFirst);
        iBP += borderProps.getBorderWidth(CommonBorderPaddingBackground.START,
                                            bNotFirst);
        return (iBP > 0);
    }

    protected boolean hasTrailingFence(boolean bNotLast) {
        int iBP = borderProps.getPadding(CommonBorderPaddingBackground.END, bNotLast);
        iBP += borderProps.getBorderWidth(CommonBorderPaddingBackground.END, bNotLast);
        return (iBP > 0);
    }

    protected SpaceProperty getSpaceStart() {
        return inlineProps.spaceStart;
    }
    protected SpaceProperty getSpaceEnd() {
        return inlineProps.spaceEnd;
    }
    

    /**
     * Return value indicating whether the next area to be generated could
     * start a new line. This should only be called in the "START" condition
     * if a previous inline BP couldn't end the line.
     * Return true if any space-start, border-start or padding-start, else
     * propagate to first child LM
     */
    public boolean canBreakBefore(LayoutContext context) {
        if (new SpaceVal(inlineProps.spaceStart).getSpace().min > 0 || hasLeadingFence(false)) {
            return true;
        }
        return super.canBreakBefore(context);
    }
    
    protected void setTraits(boolean bNotFirst, boolean bNotLast) {
        
        // Add border and padding to current area and set flags (FIRST, LAST ...)
        TraitSetter.setBorderPaddingTraits(getCurrentArea(),
                                           borderProps, bNotFirst, bNotLast);

        if (borderProps != null) {
            TraitSetter.addBorders(getCurrentArea(), borderProps);
            TraitSetter.addBackground(getCurrentArea(), borderProps);
        }
    }

}

