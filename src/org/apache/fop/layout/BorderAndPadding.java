/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.CondLength;

public class BorderAndPadding implements Cloneable {

    public static final int BEFORE = 0;
    public static final int AFTER = 1;
    public static final int START = 2;
    public static final int END = 3;

    public static final int TOP = BEFORE;
    public static final int BOTTOM = AFTER;
    public static final int LEFT = START;
    public static final int RIGHT = END;

    private static class ResolvedCondLength implements Cloneable {
        int iLength;    // Resolved length value
        boolean bDiscard;

        ResolvedCondLength(CondLength length) {
            bDiscard = length.isDiscard();
            iLength = length.mvalue();
        }

	public Object clone() throws CloneNotSupportedException {
	    return super.clone();
	}

    }

    /**
     * Return a full copy of the BorderAndPadding information. This clones all
     * padding and border information.
     * @return The copy.
     */
    public Object clone() throws CloneNotSupportedException {
	BorderAndPadding bp = (BorderAndPadding) super.clone();
	bp.padding = (ResolvedCondLength[])padding.clone();
	bp.borderInfo = (BorderInfo[])borderInfo.clone();
	for (int i=0; i<padding.length; i++) {
	    if (padding[i] != null) {
		bp.padding[i]=(ResolvedCondLength)padding[i].clone();
	    }
	    if (borderInfo[i] != null) {
		bp.borderInfo[i]=(BorderInfo)borderInfo[i].clone();
	    }
	}
	return bp;
    }

    public static class BorderInfo implements Cloneable {
        private int mStyle;          // Enum for border style
        private ColorType mColor;    // Border color
        private ResolvedCondLength mWidth;

        BorderInfo(int style, CondLength width, ColorType color) {
            mStyle = style;
            mWidth = new ResolvedCondLength(width);
            mColor = color;
        }

	public Object clone() throws CloneNotSupportedException {
	    BorderInfo bi = (BorderInfo) super.clone();
	    bi.mWidth = (ResolvedCondLength)mWidth.clone();
	    // do we need to clone the Color too???
	    return bi;
	}
    }

    private BorderInfo[] borderInfo = new BorderInfo[4];
    private ResolvedCondLength[] padding = new ResolvedCondLength[4];

    public BorderAndPadding() {}

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

    public int getBorderLeftWidth(boolean bDiscard) {
        return getBorderWidth(LEFT, bDiscard);
    }

    public int getBorderRightWidth(boolean bDiscard) {
        return getBorderWidth(RIGHT, bDiscard);
    }

    public int getBorderTopWidth(boolean bDiscard) {
        return getBorderWidth(TOP, bDiscard);
    }

    public int getBorderBottomWidth(boolean bDiscard) {
        return getBorderWidth(BOTTOM, bDiscard);
    }

    public int getPaddingLeft(boolean bDiscard) {
        return getPadding(LEFT, bDiscard);
    }

    public int getPaddingRight(boolean bDiscard) {
        return getPadding(RIGHT, bDiscard);
    }

    public int getPaddingBottom(boolean bDiscard) {
        return getPadding(BOTTOM, bDiscard);
    }

    public int getPaddingTop(boolean bDiscard) {
        return getPadding(TOP, bDiscard);
    }


    public int getBorderWidth(int side, boolean bDiscard) {
        if ((borderInfo[side] == null)
                || (bDiscard && borderInfo[side].mWidth.bDiscard)) {
            return 0;
        } else
            return borderInfo[side].mWidth.iLength;
    }

    public ColorType getBorderColor(int side) {
        if (borderInfo[side] != null) {
            return borderInfo[side].mColor;
        } else
            return null;
    }

    public int getBorderStyle(int side) {
        if (borderInfo[side] != null) {
            return borderInfo[side].mStyle;
        } else
            return 0;
    }

    public int getPadding(int side, boolean bDiscard) {
        if ((padding[side] == null) || (bDiscard && padding[side].bDiscard)) {
            return 0;
        } else
            return padding[side].iLength;
    }

}
