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

package org.apache.fop.render.extensions.prepress;

import java.awt.Rectangle;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xmlgraphics.util.QName;

import org.apache.fop.fo.extensions.ExtensionElementMapping;
import org.apache.fop.fo.properties.FixedLength;


/**
 * This class contains definition of page boundaries FOF's extension attributes for XSL-FO.
 * That is: bleedBox, trimBox and cropBox.
 * Also this class provides method to parse the possible values of these attributes
 * and to generate original size of bounded area.
 */
public final class PageBoundariesAttributes {

    /**
     * The extension attribute for calculating the PDF BleedBox area - specifies the bleed width
     */
    public static final QName EXT_BLEED
            = new QName(ExtensionElementMapping.URI, null, "bleed");

    /**
     * The extension attribute for the PDF CropBox area
     */
    public static final QName EXT_CROP_OFFSET
            = new QName(ExtensionElementMapping.URI, null, "crop-offset");

    /**
     * The extension attribute for the PDF CropBox area
     */
    public static final QName EXT_CROP_BOX
            = new QName(ExtensionElementMapping.URI, null, "crop-box");


    private static final Pattern SIZE_UNIT_PATTERN
            = Pattern.compile("^(-?\\d*\\.?\\d*)(px|in|cm|mm|pt|pc|mpt)$");

    /**
     * Utility classes should not have a public or default constructor.
     */
    private PageBoundariesAttributes() {
    }

    /**
     * The BleedBox is calculated by expanding the TrimBox by the bleed widths.
     *
     * @param trimBox the TrimBox rectangle
     * @param bleed   the given bleed widths
     * @return the calculated BleedBox rectangle
     */
    public static Rectangle getBleedBoxRectangle(Rectangle trimBox, String bleed) {
        return getRectagleUsingOffset(trimBox, bleed);
    }

    /**
     * The MediaBox is calculated by expanding the TrimBox by the crop offsets.
     *
     * @param trimBox     the TrimBox rectangle
     * @param cropOffsets the given crop offsets
     * @return the calculated MediaBox rectangle
     */
    public static Rectangle getMediaBoxRectangle(Rectangle trimBox, String cropOffsets) {
        return getRectagleUsingOffset(trimBox, cropOffsets);
    }

    /**
     * The crop box controls how Acrobat display the page or how the Java2DRenderer
     * sizes the output media. The PDF spec defines that the CropBox defaults to the MediaBox.
     * <p/>
     * The possible values of crop-box: (trim-box|bleed-box|media-box)
     * Default value: media-box
     *
     * @param trimBox  the TrimBox rectangle
     * @param bleedBox the BleedBox rectangle
     * @param mediaBox the MediaBox rectangle
     * @param value    the crop-box value
     * @return the calculated CropBox rectangle
     */
    public static Rectangle getCropBoxRectangle(final Rectangle trimBox, final Rectangle bleedBox,
                                                final Rectangle mediaBox, final String value) {
        final String err = "The crop-box has invalid value: {0}, "
                + "possible values of crop-box: (trim-box|bleed-box|media-box)";

        if ("trim-box".equals(value)) {
            return trimBox;
        } else if ("bleed-box".equals(value)) {
            return bleedBox;
        } else if ("media-box".equals(value) || value == null || "".equals(value)) {
            return mediaBox;
        } else {
            throw new IllegalArgumentException(MessageFormat.format(err, new Object[]{value}));
        }
    }

    /**
     * The crop box controls how Acrobat display the page or how the Java2DRenderer
     * sizes the output media. The PDF spec defines that the CropBox defaults to the  MediaBox
     * <p/>
     * The possible values of crop-box: (trim-box|bleed-box|media-box)
     * Default value: media-box
     *
     * @param trimBox    the TrimBox rectangle
     * @param bleed      the given bleed widths
     * @param cropOffset the given crop offsets
     * @param value      the crop-box value
     * @return the calculated CropBox rectangle
     */
    public static Rectangle getCropBoxRectangle(final Rectangle trimBox, final String bleed,
                                                final String cropOffset, final String value) {
        Rectangle bleedBox = getBleedBoxRectangle(trimBox, bleed);
        Rectangle mediaBox = getMediaBoxRectangle(trimBox, cropOffset);

        return getCropBoxRectangle(trimBox, bleedBox, mediaBox, value);
    }

    private static Rectangle getRectagleUsingOffset(Rectangle originalRect, String offset) {
        if (offset == null || "".equals(offset) || originalRect == null) {
            return originalRect;
        }

        String[] bleeds = offset.split(" ");
        int[] coords = new int[4]; // top, rigth, bottom, left
        if (bleeds.length == 1) {
            coords[0] = getLengthIntValue(bleeds[0]);
            coords[1] = coords[0];
            coords[2] = coords[0];
            coords[3] = coords[0];
        } else if (bleeds.length == 2) {
            coords[0] = getLengthIntValue(bleeds[0]);
            coords[2] = coords[0];
            coords[1] = getLengthIntValue(bleeds[1]);
            coords[3] = coords[1];
        } else if (bleeds.length == 3) {
            coords[0] = getLengthIntValue(bleeds[0]);
            coords[1] = getLengthIntValue(bleeds[1]);
            coords[3] = coords[1];
            coords[2] = getLengthIntValue(bleeds[2]);
        } else if (bleeds.length == 4) {
            coords[0] = getLengthIntValue(bleeds[0]);
            coords[1] = getLengthIntValue(bleeds[1]);
            coords[2] = getLengthIntValue(bleeds[2]);
            coords[3] = getLengthIntValue(bleeds[3]);
        }
        return new Rectangle((int) (originalRect.getX() - coords[3]),
                (int) (originalRect.getY() - coords[0]),
                (int) (originalRect.getWidth() + coords[3] + coords[1]),
                (int) (originalRect.getHeight() + coords[0] + coords[2]));
    }

    private static int getLengthIntValue(final String length) {
        final String err = "Incorrect length value: {0}";
        Matcher m = SIZE_UNIT_PATTERN.matcher(length);

        if (m.find()) {
            return FixedLength.getInstance(Double.parseDouble(m.group(1)),
                    m.group(2)).getLength().getValue();
        } else {
            throw new IllegalArgumentException(MessageFormat.format(err, new Object[]{length}));
        }
    }
}
