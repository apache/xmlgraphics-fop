/*
 * Copyright 2004-2005 The Apache Software Foundation.
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

package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

/**
 * Stores all common border and padding properties.
 * See Sec. 7.7 of the XSL-FO Standard.
 */
public class CommonBorderPaddingBackground implements Cloneable {
    /**
     * The "background-attachment" property.
     */
    public int backgroundAttachment;

    /**
     * The "background-color" property.
     */
    public ColorType backgroundColor;

    /**
     * The "background-image" property.
     */
    public String backgroundImage;

    /**
     * The "background-repeat" property.
     */
    public int backgroundRepeat;

    /**
     * The "background-position-horizontal" property.
     */
    public Length backgroundPositionHorizontal;

    /**
     * The "background-position-vertical" property.
     */
    public Length backgroundPositionVertical;
    
    public static final int BEFORE = 0;
    public static final int AFTER = 1;
    public static final int START = 2;
    public static final int END = 3;

    private static class BorderInfo implements Cloneable {
        private int mStyle; // Enum for border style
        private ColorType mColor; // Border color
        private CondLengthProperty mWidth;

        BorderInfo(int style, CondLengthProperty width, ColorType color) {
            mStyle = style;
            mWidth = width;
            mColor = color;
        }
    }

    private BorderInfo[] borderInfo = new BorderInfo[4];
    private CondLengthProperty[] padding = new CondLengthProperty[4];

    /**
     * Construct a CommonBorderPaddingBackground object.
     * @param pList The PropertyList to get properties from.
     */
    public CommonBorderPaddingBackground(PropertyList pList) throws PropertyException {
        backgroundAttachment = pList.get(Constants.PR_BACKGROUND_ATTACHMENT).getEnum();
        backgroundColor = pList.get(Constants.PR_BACKGROUND_COLOR).getColorType();
        if (backgroundColor.getAlpha() == 0) {
            backgroundColor = null;
        }

        backgroundImage = pList.get(Constants.PR_BACKGROUND_IMAGE).getString();
        if (backgroundImage == null || "none".equals(backgroundImage)) {
            backgroundImage = null;
        } else {
            backgroundRepeat = pList.get(Constants.PR_BACKGROUND_REPEAT).getEnum();
            backgroundPositionHorizontal = pList.get(Constants.PR_BACKGROUND_POSITION_HORIZONTAL).getLength();
            backgroundPositionVertical = pList.get(Constants.PR_BACKGROUND_POSITION_VERTICAL).getLength();
        }

        initBorderInfo(pList, BEFORE, 
                Constants.PR_BORDER_BEFORE_COLOR, 
                Constants.PR_BORDER_BEFORE_STYLE, 
                Constants.PR_BORDER_BEFORE_WIDTH, 
                Constants.PR_PADDING_BEFORE);
        initBorderInfo(pList, AFTER, 
                Constants.PR_BORDER_AFTER_COLOR, 
                Constants.PR_BORDER_AFTER_STYLE, 
                Constants.PR_BORDER_AFTER_WIDTH, 
                Constants.PR_PADDING_AFTER);
        initBorderInfo(pList, START, 
                Constants.PR_BORDER_START_COLOR, 
                Constants.PR_BORDER_START_STYLE, 
                Constants.PR_BORDER_START_WIDTH, 
                Constants.PR_PADDING_START);
        initBorderInfo(pList, END, 
                Constants.PR_BORDER_END_COLOR, 
                Constants.PR_BORDER_END_STYLE, 
                Constants.PR_BORDER_END_WIDTH, 
                Constants.PR_PADDING_END);

    }

    private void initBorderInfo(PropertyList pList, int side, 
            int colorProp, int styleProp, int widthProp, int paddingProp)
        throws PropertyException
    {
        padding[side] = pList.get(paddingProp).getCondLength();
        // If style = none, force width to 0, don't get Color (spec 7.7.20)
        int style = pList.get(styleProp).getEnum();
        if (style != Constants.EN_NONE) {
            borderInfo[side] = new BorderInfo(style, 
                    pList.get(widthProp).getCondLength(), 
                    pList.get(colorProp).getColorType());
        }
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
                || (borderInfo[side].mStyle == Constants.EN_NONE)
                || (bDiscard && borderInfo[side].mWidth.isDiscard())) {
            return 0;
        } else {
            return borderInfo[side].mWidth.getLengthValue();
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
            return Constants.EN_NONE;
        }
    }

    public int getPadding(int side, boolean bDiscard) {
        if ((padding[side] == null) || (bDiscard && padding[side].isDiscard())) {
            return 0;
        } else {
            return padding[side].getLengthValue();
        }
    }

    /**
     * Return all the border and padding width in the inline progression 
     * dimension.
     * @param bDiscard the discard flag.
     * @return all the padding and border width.
     */
    public int getIPPaddingAndBorder(boolean bDiscard) {
        return getPaddingStart(bDiscard) 
            + getPaddingEnd(bDiscard) 
            + getBorderStartWidth(bDiscard) 
            + getBorderEndWidth(bDiscard);        
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
