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

    public static final int TOP = 0;
    public static final int RIGHT = 1;
    public static final int BOTTOM = 2;
    public static final int LEFT = 3;
    //ResolvedCondLength is long, mask wiht 0x100000000 != 0 is bDiscard
    //	mask wiht 0xFFFFFFFF is iLength
    static final long bDiscard_MASK = 0x100000000L;
    static final long iLength_MASK =  0x0FFFFFFFFL;
    private static final long new_ResolvedCondLength(CondLength length) {
        return (length.isDiscard()?bDiscard_MASK:0) + length.mvalue();
    }

    /**
        * Return a full copy of the BorderAndPadding information. This clones all
        * padding and border information.
        * @return The copy.
        */
    public Object clone() throws CloneNotSupportedException {
	BorderAndPadding bp = (BorderAndPadding) super.clone();
        bp.padding = new long[ padding.length]; //
        System.arraycopy( padding, 0, bp.padding, 0, padding.length);
	bp.borderInfo = (BorderInfo[])borderInfo.clone();
        for (int i=0; i<borderInfo.length; i++) {
	    if (borderInfo[i] != null) {
		bp.borderInfo[i]=(BorderInfo)borderInfo[i].clone();
	    }
	}
	return bp;
    }

    public static class BorderInfo implements Cloneable {
        private int mStyle;          // Enum for border style
        private ColorType mColor;    // Border color
        private long mWidth;

        BorderInfo(int style, CondLength width, ColorType color) {
            mStyle = style;
            mWidth = new_ResolvedCondLength(width);
            mColor = color;
        }
	public Object clone() throws CloneNotSupportedException {
	    BorderInfo bi = (BorderInfo) super.clone();
            bi.mWidth = mWidth;
	    // do we need to clone the Color too???
	    return bi;
	}
    }

    private BorderInfo[] borderInfo = new BorderInfo[4];
    private long[] padding = new long[4];//

    public BorderAndPadding() {}

    public void setBorder(int side, int style, CondLength width,
                          ColorType color) {
        borderInfo[side] = new BorderInfo(style, width, color);
    }

    public void setPadding(int side, CondLength width) {
        padding[ side] = new_ResolvedCondLength(width);
    }

    public void setPaddingLength(int side, int iLength) {
        padding[side] = iLength + (padding[side] & bDiscard_MASK);
    }

    public void setBorderLength(int side, int iLength) {
        borderInfo[side].mWidth = iLength + (borderInfo[side].mWidth & bDiscard_MASK);
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


    private int getBorderWidth(int side, boolean bDiscard) {
        if ((borderInfo[side] == null)
                || (bDiscard &&
                    ((borderInfo[side].mWidth&bDiscard_MASK) != 0L)
                   )) {
            return 0;
        } else {
            return (int) (borderInfo[side].mWidth&iLength_MASK);
        }
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

    private int getPadding(int side, boolean bDiscard) {
        return (int)(padding[side]&iLength_MASK);
    }

}
