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

}
