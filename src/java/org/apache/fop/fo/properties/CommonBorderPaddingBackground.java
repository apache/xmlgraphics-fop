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

package org.apache.fop.fo.properties;

import java.awt.Color;

import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.datatypes.URISpecification;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

/**
 * Stores all common border and padding properties.
 * See Sec. 7.7 of the XSL-FO Standard.
 */
public class CommonBorderPaddingBackground {
    /**
     * The "background-attachment" property.
     */
    public int backgroundAttachment;

    /**
     * The "background-color" property.
     */
    public Color backgroundColor;

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


    private ImageInfo backgroundImageInfo;


    /** the "before" edge */
    public static final int BEFORE = 0;
    /** the "after" edge */
    public static final int AFTER = 1;
    /** the "start" edge */
    public static final int START = 2;
    /** the "end" edge */
    public static final int END = 3;

    public static class BorderInfo {
        private int mStyle; // Enum for border style
        private Color mColor; // Border color
        private CondLengthProperty mWidth;

        BorderInfo(int style, CondLengthProperty width, Color color) {
            mStyle = style;
            mWidth = width;
            mColor = color;
        }

        public int getStyle() {
            return this.mStyle;
        }

        public Color getColor() {
            return this.mColor;
        }

        public CondLengthProperty getWidth() {
            return this.mWidth;
        }

        public int getRetainedWidth() {
            if ((mStyle == Constants.EN_NONE)
                    || (mStyle == Constants.EN_HIDDEN)) {
                return 0;
            } else {
                return mWidth.getLengthValue();
            }
        }

        /** {@inheritDoc} */
        public String toString() {
            StringBuffer sb = new StringBuffer("BorderInfo");
            sb.append(" {");
            sb.append(mStyle);
            sb.append(", ");
            sb.append(mColor);
            sb.append(", ");
            sb.append(mWidth);
            sb.append("}");
            return sb.toString();
        }
    }

    /**
     * A border info with style none. Used as a singleton, in the collapsing-border model,
     * for elements which don't specify any border on some of their sides.
     */
    private static BorderInfo defaultBorderInfo;

    /**
     * A conditional length of value 0. Returned by the
     * {@link CommonBorderPaddingBackground#getBorderInfo(int)} method when the
     * corresponding border isn't specified, to avoid to callers painful checks for null.
     */
    private static class ConditionalNullLength extends CondLengthProperty {

        /** {@inheritDoc} */
        public Property getComponent(int cmpId) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        public Property getConditionality() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        public Length getLength() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        public Property getLengthComponent() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        public int getLengthValue() {
            return 0;
        }

        /** {@inheritDoc} */
        public int getLengthValue(PercentBaseContext context) {
            return 0;
        }

        /** {@inheritDoc} */
        public boolean isDiscard() {
            return true;
        }

        /** {@inheritDoc} */
        public void setComponent(int cmpId, Property cmpnValue, boolean isDefault) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        public String toString() {
            return "CondLength[0mpt, discard]";
        }
    }

    /**
     * Returns a default BorderInfo of style none.
     * 
     * @return a BorderInfo instance with style set to {@link Constants#EN_NONE}
     */
    public static synchronized BorderInfo getDefaultBorderInfo() {
        if (defaultBorderInfo == null) {
            /* It is enough to set color to null, as it should never be consulted */
            defaultBorderInfo = new BorderInfo(Constants.EN_NONE,
                    new ConditionalNullLength(), null);
        }
        return defaultBorderInfo;
    }

    private BorderInfo[] borderInfo = new BorderInfo[4];
    private CondLengthProperty[] padding = new CondLengthProperty[4];

    /**
     * Construct a CommonBorderPaddingBackground object.
     */
    public CommonBorderPaddingBackground() {
    }

    /**
     * Construct a CommonBorderPaddingBackground object.
     * 
     * @param pList The PropertyList to get properties from.
     * @throws PropertyException if there's an error while binding the properties
     */
    public CommonBorderPaddingBackground(PropertyList pList) throws PropertyException {

        backgroundAttachment = pList.get(Constants.PR_BACKGROUND_ATTACHMENT).getEnum();
        backgroundColor = pList.get(Constants.PR_BACKGROUND_COLOR).getColor(
                                        pList.getFObj().getUserAgent());
        if (backgroundColor.getAlpha() == 0) {
            backgroundColor = null;
        }

        backgroundImage = pList.get(Constants.PR_BACKGROUND_IMAGE).getString();
        if (backgroundImage == null || "none".equals(backgroundImage)) {
            backgroundImage = null;
        } else {
            backgroundRepeat = pList.get(Constants.PR_BACKGROUND_REPEAT).getEnum();
            backgroundPositionHorizontal = pList.get(
                    Constants.PR_BACKGROUND_POSITION_HORIZONTAL).getLength();
            backgroundPositionVertical = pList.get(
                    Constants.PR_BACKGROUND_POSITION_VERTICAL).getLength();

            //Additional processing: preload image
            String uri = URISpecification.getURL(backgroundImage);
            FOUserAgent userAgent = pList.getFObj().getUserAgent();
            ImageManager manager = userAgent.getFactory().getImageManager();
            ImageSessionContext sessionContext = userAgent.getImageSessionContext();
            ImageInfo info;
            try {
                info = manager.getImageInfo(uri, sessionContext);
                this.backgroundImageInfo = info;
            } catch (Exception e) {
                Property.log.error("Background image not available: " + uri);
            }
            //TODO Report to caller so he can decide to throw an exception
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
                throws PropertyException {
        padding[side] = pList.get(paddingProp).getCondLength();
        // If style = none, force width to 0, don't get Color (spec 7.7.20)
        int style = pList.get(styleProp).getEnum();
        if (style != Constants.EN_NONE) {
            FOUserAgent ua = pList.getFObj().getUserAgent();
            setBorderInfo(new BorderInfo(style,
                pList.get(widthProp).getCondLength(),
                pList.get(colorProp).getColor(ua)), side);
        }
    }

    /**
     * Sets a border.
     * @param info the border information
     * @param side the side to apply the info to
     */
    public void setBorderInfo(BorderInfo info, int side) {
        this.borderInfo[side] = info;
    }

    /**
     * @param side the side to retrieve
     * @return the border info for a side
     */
    public BorderInfo getBorderInfo(int side) {
        if (this.borderInfo[side] == null) {
            return getDefaultBorderInfo();
        } else {
            return this.borderInfo[side];
        }
    }

    /**
     * Set padding.
     * @param source the padding info to copy from
     */
    public void setPadding(CommonBorderPaddingBackground source) {
        this.padding = source.padding;
    }

    /**
     * @return the background image info object, null if there is
     *     no background image.
     */
    public ImageInfo getImageInfo() {
        return this.backgroundImageInfo;
    }

    /**
     * @param bDiscard indicates whether the .conditionality component should be
     * considered (start of a reference-area)
     */
    public int getBorderStartWidth(boolean bDiscard) {
        return getBorderWidth(START, bDiscard);
    }

    /**
     * @param bDiscard indicates whether the .conditionality component should be
     * considered (end of a reference-area)
     */
    public int getBorderEndWidth(boolean bDiscard) {
        return getBorderWidth(END, bDiscard);
    }

    /**
     * @param bDiscard indicates whether the .conditionality component should be
     * considered (start of a reference-area)
     */
    public int getBorderBeforeWidth(boolean bDiscard) {
        return getBorderWidth(BEFORE, bDiscard);
    }

    /**
     * @param bDiscard indicates whether the .conditionality component should be
     * considered (end of a reference-area)
     */
    public int getBorderAfterWidth(boolean bDiscard) {
        return getBorderWidth(AFTER, bDiscard);
    }

    public int getPaddingStart(boolean bDiscard, PercentBaseContext context) {
        return getPadding(START, bDiscard, context);
    }

    public int getPaddingEnd(boolean bDiscard, PercentBaseContext context) {
        return getPadding(END, bDiscard, context);
    }

    public int getPaddingBefore(boolean bDiscard, PercentBaseContext context) {
        return getPadding(BEFORE, bDiscard, context);
    }

    public int getPaddingAfter(boolean bDiscard, PercentBaseContext context) {
        return getPadding(AFTER, bDiscard, context);
    }

    public int getBorderWidth(int side, boolean bDiscard) {
        if ((borderInfo[side] == null)
                || (borderInfo[side].mStyle == Constants.EN_NONE)
                || (borderInfo[side].mStyle == Constants.EN_HIDDEN)
                || (bDiscard && borderInfo[side].mWidth.isDiscard())) {
            return 0;
        } else {
            return borderInfo[side].mWidth.getLengthValue();
        }
    }

    public Color getBorderColor(int side) {
        if (borderInfo[side] != null) {
            return borderInfo[side].getColor();
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

    public int getPadding(int side, boolean bDiscard, PercentBaseContext context) {
        if ((padding[side] == null) || (bDiscard && padding[side].isDiscard())) {
            return 0;
        } else {
            return padding[side].getLengthValue(context);
        }
    }

    /**
     * Returns the CondLengthProperty for the padding on one side.
     * @param side the side
     * @return the requested CondLengthProperty
     */
    public CondLengthProperty getPaddingLengthProperty(int side) {
        return padding[side];
    }

    /**
     * Return all the border and padding width in the inline progression
     * dimension.
     * @param bDiscard the discard flag.
     * @param context for percentage evaluation.
     * @return all the padding and border width.
     */
    public int getIPPaddingAndBorder(boolean bDiscard, PercentBaseContext context) {
        return getPaddingStart(bDiscard, context)
            + getPaddingEnd(bDiscard, context)
            + getBorderStartWidth(bDiscard)
            + getBorderEndWidth(bDiscard);
    }

    /**
     * Return all the border and padding height in the block progression
     * dimension.
     * @param bDiscard the discard flag.
     * @param context for percentage evaluation
     * @return all the padding and border height.
     */
    public int getBPPaddingAndBorder(boolean bDiscard, PercentBaseContext context) {
        return getPaddingBefore(bDiscard, context) + getPaddingAfter(bDiscard, context)
               + getBorderBeforeWidth(bDiscard) + getBorderAfterWidth(bDiscard);
    }

    /** {@inheritDoc} */
    public String toString() {
        return "CommonBordersAndPadding (Before, After, Start, End):\n"
            + "Borders: (" + getBorderBeforeWidth(false) + ", " + getBorderAfterWidth(false) + ", "
            + getBorderStartWidth(false) + ", " + getBorderEndWidth(false) + ")\n"
            + "Border Colors: (" + getBorderColor(BEFORE) + ", " + getBorderColor(AFTER) + ", "
            + getBorderColor(START) + ", " + getBorderColor(END) + ")\n"
            + "Padding: (" + getPaddingBefore(false, null) + ", " + getPaddingAfter(false, null)
            + ", " + getPaddingStart(false, null) + ", " + getPaddingEnd(false, null) + ")\n";
    }

    /**
     * @return true if there is any kind of background to be painted
     */
    public boolean hasBackground() {
        return ((backgroundColor != null || getImageInfo() != null));
    }

    /** @return true if border is non-zero. */
    public boolean hasBorder() {
        return ((getBorderBeforeWidth(false) + getBorderAfterWidth(false)
                + getBorderStartWidth(false) + getBorderEndWidth(false)) > 0);
    }

    /** 
     * @param context for percentage based evaluation.
     * @return true if padding is non-zero.
     */
    public boolean hasPadding(PercentBaseContext context) {
        return ((getPaddingBefore(false, context) + getPaddingAfter(false, context)
                + getPaddingStart(false, context) + getPaddingEnd(false, context)) > 0);
    }

    /** @return true if there are any borders defined. */
    public boolean hasBorderInfo() {
        return (borderInfo[BEFORE] != null || borderInfo[AFTER] != null
                || borderInfo[START] != null || borderInfo[END] != null);
    }
}
