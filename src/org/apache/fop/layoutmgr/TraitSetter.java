/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.area.Area;
import org.apache.fop.area.Trait;
import org.apache.fop.layout.BackgroundProps;

public class TraitSetter {

    public static void setBorderPaddingTraits(Area area,
            BorderAndPadding bpProps, boolean bNotFirst, boolean bNotLast) {
        int iBP;
        iBP = bpProps.getPadding(BorderAndPadding.START, bNotFirst);
        if (iBP > 0) {
            //area.addTrait(new Trait(Trait.PADDING_START, new Integer(iBP)));
            area.addTrait(Trait.PADDING_START, new Integer(iBP));
        }
        iBP = bpProps.getPadding(BorderAndPadding.END, bNotLast);
        if (iBP > 0) {
            //area.addTrait(new Trait(Trait.PADDING_END, new Integer(iBP)));
            area.addTrait(Trait.PADDING_END, new Integer(iBP));
        }
        iBP = bpProps.getPadding(BorderAndPadding.BEFORE, false);
        if (iBP > 0) {
            // area.addTrait(new Trait(Trait.PADDING_BEFORE, new Integer(iBP)));
            area.addTrait(Trait.PADDING_BEFORE, new Integer(iBP));
        }
        iBP = bpProps.getPadding(BorderAndPadding.AFTER, false);
        if (iBP > 0) {
            //area.addTrait(new Trait(Trait.PADDING_AFTER, new Integer(iBP)));
            area.addTrait(Trait.PADDING_AFTER, new Integer(iBP));
        }

        addBorderTrait(area, bpProps, bNotFirst,
                       BorderAndPadding.START, Trait.BORDER_START);

        addBorderTrait(area, bpProps, bNotLast, BorderAndPadding.END,
                       Trait.BORDER_END);

        addBorderTrait(area, bpProps, false, BorderAndPadding.BEFORE,
                       Trait.BORDER_BEFORE);

        addBorderTrait(area, bpProps, false, BorderAndPadding.AFTER,
                       Trait.BORDER_AFTER);
    }

    private static void addBorderTrait(Area area,
                                       BorderAndPadding bpProps, boolean bDiscard, int iSide,
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
     */
    public static void addBorders(Area curBlock, BorderAndPadding bordProps) {
        BorderProps bps = getBorderProps(bordProps, BorderAndPadding.TOP);
        if(bps.width != 0) {
            curBlock.addTrait(Trait.BORDER_BEFORE, bps);
        }
        bps = getBorderProps(bordProps, BorderAndPadding.BOTTOM);
        if(bps.width != 0) {
            curBlock.addTrait(Trait.BORDER_AFTER, bps);
        }
        bps = getBorderProps(bordProps, BorderAndPadding.LEFT);
        if(bps.width != 0) {
            curBlock.addTrait(Trait.BORDER_START, bps);
        }
        bps = getBorderProps(bordProps, BorderAndPadding.RIGHT);
        if(bps.width != 0) {
            curBlock.addTrait(Trait.BORDER_END, bps);
        }
    }

    private static BorderProps getBorderProps(BorderAndPadding bordProps, int side) {
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
     */
    public static void addBackground(Area curBlock, BackgroundProps backProps) {
        Trait.Background back = new Trait.Background();
        back.color = backProps.backColor;

        if(backProps.backImage != null) {
            back.url = backProps.backImage;
            back.repeat = backProps.backRepeat;
            if(backProps.backPosHorizontal != null) {
                back.horiz = backProps.backPosHorizontal.mvalue();
            }
            if(backProps.backPosVertical != null) {
                back.vertical = backProps.backPosVertical.mvalue();
            }
        }

        if(back.color != null || back.url != null) {
            curBlock.addTrait(Trait.BACKGROUND, back);
        }
    }
}
