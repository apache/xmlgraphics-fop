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

import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.area.Area;
import org.apache.fop.area.Trait;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonMarginBlock;

/**
 * This is a helper class used for setting common traits on areas.
 */
public class TraitSetter {

    /**
     * Sets border and padding traits on areas.
     * @param area area to set the traits on
     * @param bpProps border and padding properties
     */
    public static void setBorderPaddingTraits(Area area,
            CommonBorderAndPadding bpProps, boolean bNotFirst, boolean bNotLast) {
        int iBP;
        iBP = bpProps.getPadding(CommonBorderAndPadding.START, bNotFirst);
        if (iBP > 0) {
            //area.addTrait(new Trait(Trait.PADDING_START, new Integer(iBP)));
            area.addTrait(Trait.PADDING_START, new Integer(iBP));
        }
        iBP = bpProps.getPadding(CommonBorderAndPadding.END, bNotLast);
        if (iBP > 0) {
            //area.addTrait(new Trait(Trait.PADDING_END, new Integer(iBP)));
            area.addTrait(Trait.PADDING_END, new Integer(iBP));
        }
        iBP = bpProps.getPadding(CommonBorderAndPadding.BEFORE, false);
        if (iBP > 0) {
            // area.addTrait(new Trait(Trait.PADDING_BEFORE, new Integer(iBP)));
            area.addTrait(Trait.PADDING_BEFORE, new Integer(iBP));
        }
        iBP = bpProps.getPadding(CommonBorderAndPadding.AFTER, false);
        if (iBP > 0) {
            //area.addTrait(new Trait(Trait.PADDING_AFTER, new Integer(iBP)));
            area.addTrait(Trait.PADDING_AFTER, new Integer(iBP));
        }

        addBorderTrait(area, bpProps, bNotFirst,
                       CommonBorderAndPadding.START, Trait.BORDER_START);

        addBorderTrait(area, bpProps, bNotLast, CommonBorderAndPadding.END,
                       Trait.BORDER_END);

        addBorderTrait(area, bpProps, false, CommonBorderAndPadding.BEFORE,
                       Trait.BORDER_BEFORE);

        addBorderTrait(area, bpProps, false, CommonBorderAndPadding.AFTER,
                       Trait.BORDER_AFTER);
    }

    /**
     * Sets border traits on an area.
     * @param area area to set the traits on
     * @param bpProps border and padding properties
     */
    private static void addBorderTrait(Area area,
                                       CommonBorderAndPadding bpProps, 
                                       boolean bDiscard, int iSide,
                                       Object oTrait) {
        int iBP = bpProps.getBorderWidth(iSide, bDiscard);
        if (iBP > 0) {
            //     area.addTrait(new Trait(oTrait,
            //     new BorderProps(bpProps.getBorderStyle(iSide),
            //     iBP,
            //     bpProps.getBorderColor(iSide))));
            area.addTrait(oTrait,
                          new BorderProps(bpProps.getBorderStyle(iSide),
                                          iBP, bpProps.getBorderColor(iSide)));
        }
    }

    /**
     * Add borders to an area.
     * Layout managers that create areas with borders can use this to
     * add the borders to the area.
     * @param curBlock area to set the traits on
     * @param bordProps border properties
     */
    public static void addBorders(Area curBlock, CommonBorderAndPadding bordProps) {
        BorderProps bps = getBorderProps(bordProps, CommonBorderAndPadding.BEFORE);
        if (bps.width != 0) {
            curBlock.addTrait(Trait.BORDER_BEFORE, bps);
        }
        bps = getBorderProps(bordProps, CommonBorderAndPadding.AFTER);
        if (bps.width != 0) {
            curBlock.addTrait(Trait.BORDER_AFTER, bps);
        }
        bps = getBorderProps(bordProps, CommonBorderAndPadding.START);
        if (bps.width != 0) {
            curBlock.addTrait(Trait.BORDER_START, bps);
        }
        bps = getBorderProps(bordProps, CommonBorderAndPadding.END);
        if (bps.width != 0) {
            curBlock.addTrait(Trait.BORDER_END, bps);
        }
        
        int padding = bordProps.getPadding(CommonBorderAndPadding.START, false);
        if (padding != 0) {
            curBlock.addTrait(Trait.PADDING_START, new java.lang.Integer(padding));
        }
        
        padding = bordProps.getPadding(CommonBorderAndPadding.END, false);
        if (padding != 0) {
            curBlock.addTrait(Trait.PADDING_END, new java.lang.Integer(padding));
        }

        padding = bordProps.getPadding(CommonBorderAndPadding.BEFORE, false);
        if (padding != 0) {
            curBlock.addTrait(Trait.PADDING_BEFORE, new java.lang.Integer(padding));
        }
        
        padding = bordProps.getPadding(CommonBorderAndPadding.AFTER, false);
        if (padding != 0) {
            curBlock.addTrait(Trait.PADDING_AFTER, new java.lang.Integer(padding));
        }
    }

    private static BorderProps getBorderProps(CommonBorderAndPadding bordProps, int side) {
        BorderProps bps;
        bps = new BorderProps(bordProps.getBorderStyle(side),
                              bordProps.getBorderWidth(side, false),
                              bordProps.getBorderColor(side));
        return bps;
    }

    /**
     * Add background to an area.
     * Layout managers that create areas with a background can use this to
     * add the background to the area.
     * @param curBlock the current block
     * @param backProps the background properties
     */
    public static void addBackground(Area curBlock, CommonBackground backProps) {
        Trait.Background back = new Trait.Background();
        back.setColor(backProps.backColor);

        if (backProps.backImage != null) {
            back.setURL(backProps.backImage);
            back.setRepeat(backProps.backRepeat);
            if (backProps.backPosHorizontal != null) {
                back.setHoriz(backProps.backPosHorizontal.getValue());
            }
            if (backProps.backPosVertical != null) {
                back.setVertical(backProps.backPosVertical.getValue());
            }
        }

        if (back.getColor() != null || back.getURL() != null) {
            curBlock.addTrait(Trait.BACKGROUND, back);
        }
    }

    /**
     * Add space to a block area.
     * Layout managers that create block areas can use this to add space
     * outside of the border rectangle to the area.
     * @param curBlock the current block.
     * @param marginProps the margin properties.
     */
    public static void addMargins(Area curBlock,
                                  CommonBorderAndPadding bpProps, 
                                  CommonMarginBlock marginProps) {
        int spaceStart = marginProps.startIndent - 
                            bpProps.getBorderStartWidth(false) -
                            bpProps.getPaddingStart(false);
        if (spaceStart != 0) {
            curBlock.addTrait(Trait.SPACE_START, new Integer(spaceStart));
        }

        int spaceEnd = marginProps.endIndent -
                           bpProps.getBorderEndWidth(false) -
                           bpProps.getPaddingEnd(false);
        if (spaceEnd != 0) {
            curBlock.addTrait(Trait.SPACE_END, new Integer(spaceEnd));
        }
    }
}
