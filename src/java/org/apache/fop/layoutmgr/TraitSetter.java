/*
 * $Id: TraitSetter.java,v 1.6 2003/03/05 20:38:26 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.layoutmgr;

import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.area.Area;
import org.apache.fop.area.Trait;
import org.apache.fop.layout.BackgroundProps;

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

    /**
     * Sets border traits on an area.
     * @param area area to set the traits on
     * @param bpProps border and padding properties
     */
    private static void addBorderTrait(Area area,
                                       BorderAndPadding bpProps, 
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
    public static void addBorders(Area curBlock, BorderAndPadding bordProps) {
        BorderProps bps = getBorderProps(bordProps, BorderAndPadding.TOP);
        if (bps.width != 0) {
            curBlock.addTrait(Trait.BORDER_BEFORE, bps);
        }
        bps = getBorderProps(bordProps, BorderAndPadding.BOTTOM);
        if (bps.width != 0) {
            curBlock.addTrait(Trait.BORDER_AFTER, bps);
        }
        bps = getBorderProps(bordProps, BorderAndPadding.LEFT);
        if (bps.width != 0) {
            curBlock.addTrait(Trait.BORDER_START, bps);
        }
        bps = getBorderProps(bordProps, BorderAndPadding.RIGHT);
        if (bps.width != 0) {
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
     * @param curBlock the current block
     * @param backProps the background properties
     */
    public static void addBackground(Area curBlock, BackgroundProps backProps) {
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
}
