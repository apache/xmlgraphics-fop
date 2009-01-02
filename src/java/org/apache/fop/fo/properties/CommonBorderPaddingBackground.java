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
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.datatypes.URISpecification;
import org.apache.fop.events.ResourceEventProducer;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

/**
 * Stores all common border and padding properties.
 * See Sec. 7.7 of the XSL-FO Standard.
 */
public class CommonBorderPaddingBackground {

    /**
     *  cache holding all canonical instances
     *  (w/ absolute background-position-* and padding-*)
     */
    private static final PropertyCache cache = new PropertyCache(CommonBorderPaddingBackground.class);

    private int hash = -1;

    /**
     * The "background-attachment" property.
     */
    public final int backgroundAttachment;

    /**
     * The "background-color" property.
     */
    public final Color backgroundColor;

    /**
     * The "background-image" property.
     */
    public final String backgroundImage;

    /**
     * The "background-repeat" property.
     */
    public final int backgroundRepeat;

    /**
     * The "background-position-horizontal" property.
     */
    public final Length backgroundPositionHorizontal;

    /**
     * The "background-position-vertical" property.
     */
    public final Length backgroundPositionVertical;


    private ImageInfo backgroundImageInfo;


    /** the "before" edge */
    public static final int BEFORE = 0;
    /** the "after" edge */
    public static final int AFTER = 1;
    /** the "start" edge */
    public static final int START = 2;
    /** the "end" edge */
    public static final int END = 3;

    /**
     *
     */
    public static class BorderInfo {

        /** cache holding all canonical instances */
        private static final PropertyCache cache = new PropertyCache(BorderInfo.class);

        private int mStyle; // Enum for border style
        private Color mColor; // Border color
        private CondLengthProperty mWidth;

        private int hash = -1;

        /**
         * Hidden constructor
         */
        private BorderInfo(int style, CondLengthProperty width, Color color) {
            mStyle = style;
            mWidth = width;
            mColor = color;
        }

        /**
         * Returns a BorderInfo instance corresponding to the given values
         *
         * @param style the border-style
         * @param width the border-width
         * @param color the border-color
         * @return a cached BorderInfo instance
         */
        public static BorderInfo getInstance(int style, CondLengthProperty width, Color color) {
            return cache.fetch(new BorderInfo(style, width, color));
        }

        /**
         * @return  the border-style
         */
        public int getStyle() {
            return this.mStyle;
        }

        /**
         * @return the border-color
         */
        public Color getColor() {
            return this.mColor;
        }

        /**
         * @return the border-width
         */
        public CondLengthProperty getWidth() {
            return this.mWidth;
        }

        /**
         * Convenience method returning the border-width,
         * taking into account values of "none" and "hidden"
         *
         * @return  the retained border-width
         */
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

        /** {@inheritDoc} */
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj instanceof BorderInfo) {
                BorderInfo bi = (BorderInfo)obj;
                return (this.mColor == bi.mColor
                    && this.mStyle == bi.mStyle
                    && this.mWidth == bi.mWidth);
            }

            return false;
        }

        /** {@inheritDoc} */
        public int hashCode() {
            if (this.hash == -1) {
                int hash = 17;
                hash = 37 * hash + (mColor == null ? 0 : mColor.hashCode());
                hash = 37 * hash + mStyle;
                hash = 37 * hash + (mWidth == null ? 0 : mWidth.hashCode());
                this.hash = hash;
            }
            return this.hash;
        }
    }

    /**
     * A border info with style "none". Used as a singleton, in the collapsing-border model,
     * for elements which don't specify any border on some of their sides.
     */
    private static final BorderInfo defaultBorderInfo
            = BorderInfo.getInstance(Constants.EN_NONE, new ConditionalNullLength(), null);

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
    public static BorderInfo getDefaultBorderInfo() {
        return defaultBorderInfo;
    }

    private BorderInfo[] borderInfo = new BorderInfo[4];
    private CondLengthProperty[] padding = new CondLengthProperty[4];

    /**
     * Construct a CommonBorderPaddingBackground object.
     *
     * @param pList The PropertyList to get properties from.
     * @throws PropertyException if there's an error while binding the properties
     */
    private CommonBorderPaddingBackground(PropertyList pList) throws PropertyException {

        backgroundAttachment = pList.get(Constants.PR_BACKGROUND_ATTACHMENT).getEnum();

        Color bc = pList.get(Constants.PR_BACKGROUND_COLOR).getColor(
                                        pList.getFObj().getUserAgent());
        if (bc.getAlpha() == 0) {
            backgroundColor = null;
        } else {
            backgroundColor = bc;
        }

        String img = pList.get(Constants.PR_BACKGROUND_IMAGE).getString();
        if (img == null || "none".equals(img)) {
            backgroundImage = "";
            backgroundRepeat = -1;
            backgroundPositionHorizontal = null;
            backgroundPositionVertical = null;
        } else {
            backgroundImage = img;
            backgroundRepeat = pList.get(Constants.PR_BACKGROUND_REPEAT).getEnum();
            backgroundPositionHorizontal = pList.get(
                    Constants.PR_BACKGROUND_POSITION_HORIZONTAL).getLength();
            backgroundPositionVertical = pList.get(
                    Constants.PR_BACKGROUND_POSITION_VERTICAL).getLength();
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

    /**
     * Obtain a CommonBorderPaddingBackground instance based on the
     * related property valus in the given {@link PropertyList}
     *
     * @param pList the {@link PropertyList} to use
     * @return a CommonBorderPaddingBackground instance (cached if possible)
     * @throws PropertyException in case of an error
     */
    public static CommonBorderPaddingBackground getInstance(PropertyList pList)
        throws PropertyException {

        CommonBorderPaddingBackground newInstance
                = new CommonBorderPaddingBackground(pList);
        CommonBorderPaddingBackground cachedInstance = null;
        /* if padding-* and background-position-* resolve to absolute lengths
         * the whole instance can be cached */
        if ((newInstance.padding[BEFORE] == null || newInstance.padding[BEFORE].getLength().isAbsolute())
                && (newInstance.padding[AFTER] == null || newInstance.padding[AFTER].getLength().isAbsolute())
                && (newInstance.padding[START] == null || newInstance.padding[START].getLength().isAbsolute())
                && (newInstance.padding[END] == null || newInstance.padding[END].getLength().isAbsolute())
                && (newInstance.backgroundPositionHorizontal == null || newInstance.backgroundPositionHorizontal.isAbsolute())
                && (newInstance.backgroundPositionVertical == null || newInstance.backgroundPositionVertical.isAbsolute())) {
            cachedInstance = cache.fetch(newInstance);
        }

        /* for non-cached, or not-yet-cached instances, preload the image */
        if ((cachedInstance == null
                || cachedInstance == newInstance)
                && !("".equals(newInstance.backgroundImage))) {
            //Additional processing: preload image
            String uri = URISpecification.getURL(newInstance.backgroundImage);
            FObj fobj = pList.getFObj();
            FOUserAgent userAgent = pList.getFObj().getUserAgent();
            ImageManager manager = userAgent.getFactory().getImageManager();
            ImageSessionContext sessionContext = userAgent.getImageSessionContext();
            ImageInfo info;
            try {
                info = manager.getImageInfo(uri, sessionContext);
                newInstance.backgroundImageInfo = info;
            } catch (ImageException e) {
                ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                        fobj.getUserAgent().getEventBroadcaster());
                eventProducer.imageError(fobj, uri, e, fobj.getLocator());
            } catch (FileNotFoundException fnfe) {
                ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                        fobj.getUserAgent().getEventBroadcaster());
                eventProducer.imageNotFound(fobj, uri, fnfe, fobj.getLocator());
            } catch (IOException ioe) {
                ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                        fobj.getUserAgent().getEventBroadcaster());
                eventProducer.imageIOError(fobj, uri, ioe, fobj.getLocator());
            }
        }

        return (cachedInstance != null ? cachedInstance : newInstance);
    }

    private void initBorderInfo(PropertyList pList, int side,
                    int colorProp, int styleProp, int widthProp, int paddingProp)
                throws PropertyException {

        padding[side] = pList.get(paddingProp).getCondLength();
        // If style = none, force width to 0, don't get Color (spec 7.7.20)
        int style = pList.get(styleProp).getEnum();
        if (style != Constants.EN_NONE) {
            FOUserAgent ua = pList.getFObj().getUserAgent();
            setBorderInfo(BorderInfo.getInstance(style,
                pList.get(widthProp).getCondLength(),
                pList.get(colorProp).getColor(ua)), side);
        }

    }

    /**
     * Sets a border.
     * @param info the border information
     * @param side the side to apply the info to
     */
    private void setBorderInfo(BorderInfo info, int side) {
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
     * @return the background image info object, null if there is
     *     no background image.
     */
    public ImageInfo getImageInfo() {
        return this.backgroundImageInfo;
    }

    /**
     * @param discard indicates whether the .conditionality component should be
     * considered (start of a reference-area)
     * @return the width of the start-border, taking into account the specified conditionality
     */
    public int getBorderStartWidth(boolean discard) {
        return getBorderWidth(START, discard);
    }

    /**
     * @param discard indicates whether the .conditionality component should be
     * considered (end of a reference-area)
     * @return the width of the end-border, taking into account the specified conditionality
     */
    public int getBorderEndWidth(boolean discard) {
        return getBorderWidth(END, discard);
    }

    /**
     * @param discard indicates whether the .conditionality component should be
     * considered (start of a reference-area)
     * @return the width of the before-border, taking into account the specified conditionality
     */
    public int getBorderBeforeWidth(boolean discard) {
        return getBorderWidth(BEFORE, discard);
    }

    /**
     * @param discard indicates whether the .conditionality component should be
     * considered (end of a reference-area)
     * @return the width of the after-border, taking into account the specified conditionality
     */
    public int getBorderAfterWidth(boolean discard) {
        return getBorderWidth(AFTER, discard);
    }

    /**
     * @param discard indicates whether the .conditionality component should be
     * considered (start of a reference-area)
     * @param context the context to evaluate percentage values
     * @return the width of the start-padding, taking into account the specified conditionality
     */
    public int getPaddingStart(boolean discard, PercentBaseContext context) {
        return getPadding(START, discard, context);
    }

    /**
     * @param discard indicates whether the .conditionality component should be
     * considered (start of a reference-area)
     * @param context the context to evaluate percentage values
     * @return the width of the end-padding, taking into account the specified conditionality
     */
    public int getPaddingEnd(boolean discard, PercentBaseContext context) {
        return getPadding(END, discard, context);
    }

    /**
     * @param discard indicates whether the .conditionality component should be
     * considered (start of a reference-area)
     * @param context the context to evaluate percentage values
     * @return the width of the before-padding, taking into account the specified conditionality
     */
    public int getPaddingBefore(boolean discard, PercentBaseContext context) {
        return getPadding(BEFORE, discard, context);
    }

    /**
     * @param discard indicates whether the .conditionality component should be
     * considered (start of a reference-area)
     * @param context the context to evaluate percentage values
     * @return the width of the after-padding, taking into account the specified conditionality
     */
    public int getPaddingAfter(boolean discard, PercentBaseContext context) {
        return getPadding(AFTER, discard, context);
    }

    /**
     * @param discard indicates whether the .conditionality component should be
     * considered (end of a reference-area)
     * @return the width of the start-border, taking into account the specified conditionality
     */
    public int getBorderWidth(int side, boolean discard) {
        if ((borderInfo[side] == null)
                || (borderInfo[side].mStyle == Constants.EN_NONE)
                || (borderInfo[side].mStyle == Constants.EN_HIDDEN)
                || (discard && borderInfo[side].mWidth.isDiscard())) {
            return 0;
        } else {
            return borderInfo[side].mWidth.getLengthValue();
        }
    }

    /**
     * The border-color for the given side
     *
     * @param side one of {@link #BEFORE}, {@link #AFTER}, {@link #START}, {@link #END}
     * @return  the border-color for the given side
     */
    public Color getBorderColor(int side) {
        if (borderInfo[side] != null) {
            return borderInfo[side].getColor();
        } else {
            return null;
        }
    }

    /**
     * The border-style for the given side
     *
     * @param side one of {@link #BEFORE}, {@link #AFTER}, {@link #START}, {@link #END}
     * @return  the border-style for the given side
     */
    public int getBorderStyle(int side) {
        if (borderInfo[side] != null) {
            return borderInfo[side].mStyle;
        } else {
            return Constants.EN_NONE;
        }
    }

    /**
     * Return the padding for the given side, taking into account
     * the conditionality and evaluating any percentages in the given
     * context.
     *
     * @param side  one of {@link #BEFORE}, {@link #AFTER}, {@link #START}, {@link #END}
     * @param discard   true if the conditionality component should be considered
     * @param context   the context for percentage-resolution
     * @return  the computed padding for the given side
     */
    public int getPadding(int side, boolean discard, PercentBaseContext context) {
        if ((padding[side] == null) || (discard && padding[side].isDiscard())) {
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
     * @param discard the discard flag.
     * @param context for percentage evaluation.
     * @return all the padding and border width.
     */
    public int getIPPaddingAndBorder(boolean discard, PercentBaseContext context) {
        return getPaddingStart(discard, context)
            + getPaddingEnd(discard, context)
            + getBorderStartWidth(discard)
            + getBorderEndWidth(discard);
    }

    /**
     * Return all the border and padding height in the block progression
     * dimension.
     * @param discard the discard flag.
     * @param context for percentage evaluation
     * @return all the padding and border height.
     */
    public int getBPPaddingAndBorder(boolean discard, PercentBaseContext context) {
        return getPaddingBefore(discard, context) + getPaddingAfter(discard, context)
               + getBorderBeforeWidth(discard) + getBorderAfterWidth(discard);
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

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof CommonBorderPaddingBackground) {
            CommonBorderPaddingBackground cbpb = (CommonBorderPaddingBackground)obj;
            return (this.backgroundAttachment == cbpb.backgroundAttachment
                && this.backgroundColor == cbpb.backgroundColor
                && this.backgroundImage.equals(cbpb.backgroundImage)
                && this.backgroundPositionHorizontal == cbpb.backgroundPositionHorizontal
                && this.backgroundPositionVertical == cbpb.backgroundPositionVertical
                && this.backgroundRepeat == cbpb.backgroundRepeat
                && this.borderInfo[BEFORE] == cbpb.borderInfo[BEFORE]
                && this.borderInfo[AFTER] == cbpb.borderInfo[AFTER]
                && this.borderInfo[START] == cbpb.borderInfo[START]
                && this.borderInfo[END] == cbpb.borderInfo[END]
                && this.padding[BEFORE] == cbpb.padding[BEFORE]
                && this.padding[AFTER] == cbpb.padding[AFTER]
                && this.padding[START] == cbpb.padding[START]
                && this.padding[END] == cbpb.padding[END]);
        }

        return false;
    }

    /** {@inheritDoc} */
    public int hashCode() {
        if (this.hash == -1) {
            int hash = 17;
            hash = 37 * hash + backgroundAttachment;
            hash = 37 * hash + (backgroundColor == null ? 0 : backgroundColor.hashCode());
            hash = 37 * hash + (backgroundImage == null ? 0 : backgroundImage.hashCode());
            hash = 37 * hash + (backgroundPositionHorizontal == null ? 0 : backgroundPositionHorizontal.hashCode());
            hash = 37 * hash + (backgroundPositionVertical == null ? 0 : backgroundPositionVertical.hashCode());
            hash = 37 * hash + backgroundRepeat;
            hash = 37 * hash + (borderInfo[BEFORE] == null ? 0 : borderInfo[BEFORE].hashCode());
            hash = 37 * hash + (borderInfo[AFTER] == null ? 0 : borderInfo[AFTER].hashCode());
            hash = 37 * hash + (borderInfo[START] == null ? 0 : borderInfo[START].hashCode());
            hash = 37 * hash + (borderInfo[END] == null ? 0 : borderInfo[END].hashCode());
            hash = 37 * hash + (padding[BEFORE] == null ? 0 : padding[BEFORE].hashCode());
            hash = 37 * hash + (padding[AFTER] == null ? 0 : padding[AFTER].hashCode());
            hash = 37 * hash + (padding[START] == null ? 0 : padding[START].hashCode());
            hash = 37 * hash + (padding[END] == null ? 0 : padding[END].hashCode());
            this.hash = hash;
        }

        return this.hash;
    }
}
