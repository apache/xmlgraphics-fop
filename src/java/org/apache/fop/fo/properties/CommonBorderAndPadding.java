/*
 * $Id$
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
package org.apache.fop.fo.properties;

import org.apache.fop.fo.Constants;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.CondLength;

/**
 * Stores all common border and padding properties.
 * See Sec. 7.7 of the XSL-FO Standard.
 */
public class CommonBorderAndPadding implements Cloneable {

    public static final int BEFORE = 0;
    public static final int AFTER = 1;
    public static final int START = 2;
    public static final int END = 3;

/*  TODO: need new definitions (below relations not always the same, 
    also unsure if direct access of absolute properties needed; 
    resolution of absolute & relative properties--Spec 5.3.1--can 
    possibly be done within this class alone)
    public static final int TOP = BEFORE;
    public static final int BOTTOM = AFTER;
    public static final int LEFT = START;
    public static final int RIGHT = END;
*/
    private static class ResolvedCondLength implements Cloneable {
        private int iLength; // Resolved length value
        private boolean bDiscard;

        public ResolvedCondLength(CondLength length) {
            bDiscard = length.isDiscard();
            iLength = length.getLengthValue();
        }

        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

    }

    /**
     * Return a full copy of the BorderAndPadding information. This clones all
     * padding and border information.
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException {
        CommonBorderAndPadding bp = (CommonBorderAndPadding) super.clone();
        bp.padding = (ResolvedCondLength[]) padding.clone();
        bp.borderInfo = (BorderInfo[]) borderInfo.clone();
        for (int i = 0; i < padding.length; i++) {
            if (padding[i] != null) {
                bp.padding[i] = (ResolvedCondLength) padding[i].clone();
            }
            if (borderInfo[i] != null) {
                bp.borderInfo[i] = (BorderInfo) borderInfo[i].clone();
            }
        }
        return bp;
    }

    public static class BorderInfo implements Cloneable {
        private int mStyle; // Enum for border style
        private ColorType mColor; // Border color
        private ResolvedCondLength mWidth;

        BorderInfo(int style, CondLength width, ColorType color) {
            mStyle = style;
            mWidth = new ResolvedCondLength(width);
            mColor = color;
        }

        public Object clone() throws CloneNotSupportedException {
            BorderInfo bi = (BorderInfo) super.clone();
            bi.mWidth = (ResolvedCondLength) mWidth.clone();
            // do we need to clone the Color too???
            return bi;
        }
    }

    private BorderInfo[] borderInfo = new BorderInfo[4];
    private ResolvedCondLength[] padding = new ResolvedCondLength[4];

    public void setBorder(int side, int style, CondLength width,
                          ColorType color) {
        borderInfo[side] = new BorderInfo(style, width, color);
    }

    public void setPadding(int side, CondLength width) {
        padding[side] = new ResolvedCondLength(width);
    }

    public void setPaddingLength(int side, int iLength) {
        padding[side].iLength = iLength;
    }

    public void setBorderLength(int side, int iLength) {
        borderInfo[side].mWidth.iLength = iLength;
    }

    public int getBorderStartWidth(boolean bDiscard) {
        return getBorderWidth(START, bDiscard);
    }

    public int getBorderEndWidth(boolean bDiscard) {
        return getBorderWidth(END, bDiscard);
    }

    public int getBorderBeforeWidth(boolean bDiscard) {
        return getBorderWidth(BEFORE, bDiscard);
    }

    public int getBorderAfterWidth(boolean bDiscard) {
        return getBorderWidth(AFTER, bDiscard);
    }

    public int getPaddingStart(boolean bDiscard) {
        return getPadding(START, bDiscard);
    }

    public int getPaddingEnd(boolean bDiscard) {
        return getPadding(END, bDiscard);
    }

    public int getPaddingBefore(boolean bDiscard) {
        return getPadding(BEFORE, bDiscard);
    }

    public int getPaddingAfter(boolean bDiscard) {
        return getPadding(AFTER, bDiscard);
    }

    public int getBorderWidth(int side, boolean bDiscard) {
        if ((borderInfo[side] == null)
                || (borderInfo[side].mStyle == Constants.NONE)
                || (bDiscard && borderInfo[side].mWidth.bDiscard)) {
            return 0;
        } else {
            return borderInfo[side].mWidth.iLength;
        }
    }

    public ColorType getBorderColor(int side) {
        if (borderInfo[side] != null) {
            return borderInfo[side].mColor;
        } else {
            return null;
        }
    }

    public int getBorderStyle(int side) {
        if (borderInfo[side] != null) {
            return borderInfo[side].mStyle;
        } else {
            return 0;
        }
    }

    public int getPadding(int side, boolean bDiscard) {
        if ((padding[side] == null) || (bDiscard && padding[side].bDiscard)) {
            return 0;
        } else {
            return padding[side].iLength;
        }
    }

    /**
     * Return all the border and padding height in the block progression 
     * dimension.
     * @param bDiscard the discard flag.
     * @return all the padding and border height.
     */
    public int getBPPaddingAndBorder(boolean bDiscard) {
        return getPaddingBefore(bDiscard) + getPaddingAfter(bDiscard) +
               getBorderBeforeWidth(bDiscard) + getBorderAfterWidth(bDiscard);        
    }

    public String toString() {
        return "CommonBordersAndPadding (Before, After, Start, End):\n" +
        "Borders: (" + getBorderBeforeWidth(false) + ", " + getBorderAfterWidth(false) + ", " +
        getBorderStartWidth(false) + ", " + getBorderEndWidth(false) + ")\n" +
        "Border Colors: (" + getBorderColor(BEFORE) + ", " + getBorderColor(AFTER) + ", " +
        getBorderColor(START) + ", " + getBorderColor(END) + ")\n" +
        "Padding: (" + getPaddingBefore(false) + ", " + getPaddingAfter(false) + ", " +
        getPaddingStart(false) + ", " + getPaddingEnd(false) + ")\n";
    }
}
