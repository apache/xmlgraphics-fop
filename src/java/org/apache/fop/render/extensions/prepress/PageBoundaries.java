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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.text.MessageFormat;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xmlgraphics.util.QName;

import org.apache.fop.fo.extensions.ExtensionElementMapping;
import org.apache.fop.fo.properties.FixedLength;

/**
 * This class is used to calculate the effective boundaries of a page including special-purpose
 * boxes used in prepress. These are specified using extension attributes:
 * bleedBox, trimBox and cropBox. The semantics are further described on the website.
 */
public class PageBoundaries {

    /**
     * The extension attribute for calculating the PDF BleedBox area - specifies the bleed width.
     */
    public static final QName EXT_BLEED
            = new QName(ExtensionElementMapping.URI, null, "bleed");

    /**
     * The extension attribute for the PDF CropBox area.
     */
    public static final QName EXT_CROP_OFFSET
            = new QName(ExtensionElementMapping.URI, null, "crop-offset");

    /**
     * The extension attribute for the PDF CropBox area.
     */
    public static final QName EXT_CROP_BOX
            = new QName(ExtensionElementMapping.URI, null, "crop-box");


    private static final Pattern SIZE_UNIT_PATTERN
            = Pattern.compile("^(-?\\d*\\.?\\d*)(px|in|cm|mm|pt|pc|mpt)$");

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    private Rectangle trimBox;
    private Rectangle bleedBox;
    private Rectangle mediaBox;
    private Rectangle cropBox;

    /**
     * Creates a new instance.
     * @param pageSize the page size (in mpt) defined by the simple-page-master.
     * @param bleed the bleed value (raw value as given in the property value)
     * @param cropOffset the crop-offset value (raw value as given in the property value)
     * @param cropBoxSelector the crop-box, valid values: (trim-box|bleed-box|media-box)
     */
    public PageBoundaries(Dimension pageSize, String bleed, String cropOffset,
            String cropBoxSelector) {
        calculate(pageSize, bleed, cropOffset, cropBoxSelector);
    }

    /**
     * Creates a new instance.
     * @param pageSize the page size (in mpt) defined by the simple-page-master.
     * @param foreignAttributes the foreign attributes for the page
     *                  (used to extract the extension attribute values)
     */
    public PageBoundaries(Dimension pageSize, Map foreignAttributes) {
        String bleed = (String)foreignAttributes.get(EXT_BLEED);
        String cropOffset = (String)foreignAttributes.get(EXT_CROP_OFFSET);
        String cropBoxSelector = (String)foreignAttributes.get(EXT_CROP_BOX);
        calculate(pageSize, bleed, cropOffset, cropBoxSelector);
    }

    private void calculate(Dimension pageSize, String bleed, String cropOffset,
            String cropBoxSelector) {
        this.trimBox = new Rectangle(pageSize);
        this.bleedBox = getBleedBoxRectangle(this.trimBox, bleed);
        Rectangle cropMarksBox = getCropMarksAreaRectangle(trimBox, cropOffset);

        //MediaBox includes all of the following three rectangles
        this.mediaBox = new Rectangle();
        this.mediaBox.add(this.trimBox);
        this.mediaBox.add(this.bleedBox);
        this.mediaBox.add(cropMarksBox);

        if ("trim-box".equals(cropBoxSelector)) {
            this.cropBox = this.trimBox;
        } else if ("bleed-box".equals(cropBoxSelector)) {
            this.cropBox = this.bleedBox;
        } else if ("media-box".equals(cropBoxSelector)
                || cropBoxSelector == null
                || "".equals(cropBoxSelector)) {
            this.cropBox = this.mediaBox;
        } else {
            final String err = "The crop-box has invalid value: {0}, "
                + "possible values of crop-box: (trim-box|bleed-box|media-box)";
            throw new IllegalArgumentException(MessageFormat.format(err,
                    new Object[]{cropBoxSelector}));
        }
    }

    /**
     * Returns the trim box for the page. This is equal to the page size given in XSL-FO.
     * After production the printed media is trimmed to this rectangle.
     * @return the trim box
     */
    public Rectangle getTrimBox() {
        return this.trimBox;
    }

    /**
     * Returns the bleed box for the page.
     * @return the bleed box
     */
    public Rectangle getBleedBox() {
        return this.bleedBox;
    }

    /**
     * Returns the media box for the page.
     * @return the media box
     */
    public Rectangle getMediaBox() {
        return this.mediaBox;
    }

    /**
     * Returns the crop box for the page. The crop box is used by Adobe Acrobat to select which
     * parts of the document shall be displayed and it also defines the rectangle to which a
     * RIP will clip the document. For bitmap output, this defines the size of the bitmap.
     * @return the crop box
     */
    public Rectangle getCropBox() {
        return this.cropBox;
    }

    /**
     * The BleedBox is calculated by expanding the TrimBox by the bleed widths.
     *
     * @param trimBox the TrimBox rectangle
     * @param bleed   the given bleed widths
     * @return the calculated BleedBox rectangle
     */
    private static Rectangle getBleedBoxRectangle(Rectangle trimBox, String bleed) {
        return getRectangleUsingOffset(trimBox, bleed);
    }

    /**
     * The MediaBox is calculated by expanding the TrimBox by the crop offsets.
     *
     * @param trimBox     the TrimBox rectangle
     * @param cropOffsets the given crop offsets
     * @return the calculated MediaBox rectangle
     */
    private static Rectangle getCropMarksAreaRectangle(Rectangle trimBox, String cropOffsets) {
        return getRectangleUsingOffset(trimBox, cropOffsets);
    }

    private static Rectangle getRectangleUsingOffset(Rectangle originalRect, String offset) {
        if (offset == null || "".equals(offset) || originalRect == null) {
            return originalRect;
        }

        String[] offsets = WHITESPACE_PATTERN.split(offset);
        int[] coords = new int[4]; // top, right, bottom, left
        switch (offsets.length) {
        case 1:
            coords[0] = getLengthIntValue(offsets[0]);
            coords[1] = coords[0];
            coords[2] = coords[0];
            coords[3] = coords[0];
            break;
        case 2:
            coords[0] = getLengthIntValue(offsets[0]);
            coords[1] = getLengthIntValue(offsets[1]);
            coords[2] = coords[0];
            coords[3] = coords[1];
            break;
        case 3:
            coords[0] = getLengthIntValue(offsets[0]);
            coords[1] = getLengthIntValue(offsets[1]);
            coords[2] = getLengthIntValue(offsets[2]);
            coords[3] = coords[1];
            break;
        case 4:
            coords[0] = getLengthIntValue(offsets[0]);
            coords[1] = getLengthIntValue(offsets[1]);
            coords[2] = getLengthIntValue(offsets[2]);
            coords[3] = getLengthIntValue(offsets[3]);
            break;
        default:
            // TODO throw appropriate exception that can be caught by the event
            // notification mechanism
            throw new IllegalArgumentException("Too many arguments");
        }
        return new Rectangle(originalRect.x - coords[3],
                originalRect.y - coords[0],
                originalRect.width + coords[3] + coords[1],
                originalRect.height + coords[0] + coords[2]);
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
