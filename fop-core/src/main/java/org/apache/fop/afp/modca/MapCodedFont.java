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

package org.apache.fop.afp.modca;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import org.apache.fop.afp.AFPConstants;
import org.apache.fop.afp.fonts.AFPFont;
import org.apache.fop.afp.fonts.CharacterSet;
import org.apache.fop.afp.fonts.DoubleByteFont;
import org.apache.fop.afp.fonts.FontRuntimeException;
import org.apache.fop.afp.fonts.OutlineFont;
import org.apache.fop.afp.fonts.RasterFont;
import org.apache.fop.afp.util.BinaryUtils;

/**
 * The Map Coded Font structured field maps a unique coded font resource local
 * ID, which may be embedded one or more times within an object's data and
 * descriptor, to the identifier of a coded font resource object. Additionally,
 * the Map Coded Font structured field specifies a set of resource attributes
 * for the coded font.
 */
public class MapCodedFont extends AbstractStructuredObject {

    /** the collection of map coded fonts (maximum of 254) */
    private final List/*<FontDefinition>*/ fontList
        = new java.util.ArrayList/*<FontDefinition>*/();

    /**
     * Main constructor
     */
    public MapCodedFont() {
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] startData = new byte[9];
        copySF(startData, Type.MAP, Category.CODED_FONT);
        baos.write(startData);

        Iterator iter = fontList.iterator();
        while (iter.hasNext()) {
            FontDefinition fd = (FontDefinition) iter.next();

            // Start of repeating groups (occurs 1 to 254)
            baos.write(0x00);

            if (fd.scale == 0) {
                // Raster Font
                baos.write(0x22); // Length of 34
            } else {
                // Outline Font
                baos.write(0x3A); // Length of 58
            }

            // Font Character Set Name Reference
            baos.write(0x0C); //TODO Relax requirement for 8 chars in the name
            baos.write(0x02);
            baos.write((byte) 0x86);
            baos.write(0x00);
            baos.write(fd.characterSet);

            // Font Code Page Name Reference
            baos.write(0x0C); //TODO Relax requirement for 8 chars in the name
            baos.write(0x02);
            baos.write((byte) 0x85);
            baos.write(0x00);
            baos.write(fd.codePage);

            //TODO idea: for CIDKeyed fonts, maybe hint at Unicode encoding with X'50' triplet
            //to allow font substitution.

            // Character Rotation
            baos.write(0x04);
            baos.write(0x26);
            baos.write(fd.orientation);
            baos.write(0x00);

            // Resource Local Identifier
            baos.write(0x04);
            baos.write(0x24);
            baos.write(0x05);
            baos.write(fd.fontReferenceKey);

            if (fd.scale != 0) {
                // Outline Font (triplet '1F')
                baos.write(0x14);
                baos.write(0x1F);
                baos.write(0x00);
                baos.write(0x00);

                baos.write(BinaryUtils.convert(fd.scale, 2)); // Height
                baos.write(new byte[] {0x00, 0x00}); // Width

                baos.write(new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00});

                baos.write(0x60);

                // Outline Font (triplet '5D')
                baos.write(0x04);
                baos.write(0x5D);
                baos.write(BinaryUtils.convert(fd.scale, 2));
            }
        }

        byte[] data = baos.toByteArray();

        // Set the total record length
        byte[] rl1 = BinaryUtils.convert(data.length - 1, 2);
        data[1] = rl1[0];
        data[2] = rl1[1];

        os.write(data);
    }

    /**
     * Add a font definition on the the map coded font object.
     *
     * @param fontReference
     *            the font number used as the resource identifier
     * @param font
     *            the font
     * @param size
     *            the size of the font
     * @param orientation
     *            the orientation of the font
     * @throws MaximumSizeExceededException if the maximum number of fonts have been exceeded
     */
    public void addFont(int fontReference, AFPFont font, int size, int orientation)
        throws MaximumSizeExceededException {

        FontDefinition fontDefinition = new FontDefinition();

        fontDefinition.fontReferenceKey = BinaryUtils.convert(fontReference)[0];

        switch (orientation) {
            case 90:
                fontDefinition.orientation = 0x2D;
                break;
            case 180:
                fontDefinition.orientation = 0x5A;
                break;
            case 270:
                fontDefinition.orientation = (byte) 0x87;
                break;
            default:
                fontDefinition.orientation = 0x00;
                break;
        }

        try {
            if (font instanceof RasterFont) {
                RasterFont raster = (RasterFont) font;
                CharacterSet cs = raster.getCharacterSet(size);
                if (cs == null) {
                    String msg = "Character set not found for font "
                        + font.getFontName() + " with point size " + size;
                    LOG.error(msg);
                    throw new FontRuntimeException(msg);
                }

                fontDefinition.characterSet = cs.getNameBytes();

                if (fontDefinition.characterSet.length != 8) {
                    throw new IllegalArgumentException("The character set "
                        + new String(fontDefinition.characterSet,
                        AFPConstants.EBCIDIC_ENCODING)
                        + " must have a fixed length of 8 characters.");
                }

                fontDefinition.codePage = cs.getCodePage().getBytes(
                    AFPConstants.EBCIDIC_ENCODING);

                if (fontDefinition.codePage.length != 8) {
                    throw new IllegalArgumentException("The code page "
                        + new String(fontDefinition.codePage,
                        AFPConstants.EBCIDIC_ENCODING)
                        + " must have a fixed length of 8 characters.");
                }

            } else if (font instanceof OutlineFont) {
                OutlineFont outline = (OutlineFont) font;
                CharacterSet cs = outline.getCharacterSet();
                fontDefinition.characterSet = cs.getNameBytes();

                // There are approximately 72 points to 1 inch or 20 1440ths per point.

                fontDefinition.scale = 20 * size / 1000;

                fontDefinition.codePage = cs.getCodePage().getBytes(
                    AFPConstants.EBCIDIC_ENCODING);

                if (fontDefinition.codePage.length != 8) {
                    throw new IllegalArgumentException("The code page "
                        + new String(fontDefinition.codePage,
                        AFPConstants.EBCIDIC_ENCODING)
                        + " must have a fixed length of 8 characters.");
                }
            }  else if (font instanceof DoubleByteFont) {
                DoubleByteFont outline = (DoubleByteFont) font;
                CharacterSet cs = outline.getCharacterSet();
                fontDefinition.characterSet = cs.getNameBytes();

                // There are approximately 72 points to 1 inch or 20 1440ths per point.

                fontDefinition.scale = 20 * size / 1000;

                fontDefinition.codePage = cs.getCodePage().getBytes(
                    AFPConstants.EBCIDIC_ENCODING);

                //TODO Relax requirement for 8 characters
                if (fontDefinition.codePage.length != 8) {
                    throw new IllegalArgumentException("The code page "
                        + new String(fontDefinition.codePage,
                        AFPConstants.EBCIDIC_ENCODING)
                        + " must have a fixed length of 8 characters.");
                }
            } else {
                String msg = "Font of type " + font.getClass().getName()
                    + " not recognized.";
                LOG.error(msg);
                throw new FontRuntimeException(msg);
            }

            if (fontList.size() > 253) {
                // Throw an exception if the size is exceeded
                throw new MaximumSizeExceededException();
            } else {
                fontList.add(fontDefinition);
            }

        } catch (UnsupportedEncodingException ex) {
            throw new FontRuntimeException("Failed to create font "
                + " due to a UnsupportedEncodingException", ex);
        }
    }


    /**
     * Private utility class used as a container for font attributes
     */
    private static final class FontDefinition {

        private FontDefinition() {
        }

        /**
         * The code page of the font
         */
        private byte[] codePage;

        /**
         * The character set of the font
         */
        private byte[] characterSet;

        /**
         * The font reference key
         */
        private byte fontReferenceKey;

        /**
         * The orientation of the font
         */
        private byte orientation;

        /**
         * The scale (only specified for outline fonts)
         */
        private int scale;
    }

}
