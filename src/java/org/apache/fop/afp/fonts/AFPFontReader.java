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

package org.apache.fop.afp.fonts;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.afp.AFPConstants;
import org.apache.fop.afp.util.ResourceAccessor;
import org.apache.fop.afp.util.StructuredFieldReader;

/**
 * The AFPFontReader is responsible for reading the font attributes from binary
 * code page files and the character set metric files. In IBM font structure, a
 * code page maps each character of text to the characters in a character set.
 * Each character is translated into a code point. When the character is
 * printed, each code point is matched to a character ID on the code page
 * specified. The character ID is then matched to the image (raster pattern or
 * outline pattern) of the character in the character set specified. The image
 * in the character set is the image that is printed in the document. To be a
 * valid code page for a particular character set, all character IDs in the code
 * page must be included in that character set. <p/>This class will read the
 * font information from the binary code page files and character set metric
 * files in order to determine the correct metrics to use when rendering the
 * formatted object. <p/>
 *
 * @author <a href="mailto:pete@townsend.uk.com">Pete Townsend </a>
 */
public final class AFPFontReader {

    /**
     * Static logging instance
     */
    protected static final Log log = LogFactory.getLog(AFPFontReader.class);

    /**
     * Template used to convert lists to arrays.
     */
    private static final CharacterSetOrientation[] EMPTY_CSO_ARRAY = new CharacterSetOrientation[0];

    /** Codepage MO:DCA structured field. */
    private static final byte[] CODEPAGE_SF = new byte[] {
        (byte) 0xD3, (byte) 0xA8, (byte) 0x87};

    /** Character table MO:DCA structured field. */
    private static final byte[] CHARACTER_TABLE_SF = new byte[] {
        (byte) 0xD3, (byte) 0x8C, (byte) 0x87};

    /** Font descriptor MO:DCA structured field. */
    private static final byte[] FONT_DESCRIPTOR_SF = new byte[] {
        (byte) 0xD3, (byte) 0xA6, (byte) 0x89 };

    /** Font control MO:DCA structured field. */
    private static final byte[] FONT_CONTROL_SF = new byte[] {
        (byte) 0xD3, (byte) 0xA7, (byte) 0x89 };

    /** Font orientation MO:DCA structured field. */
    private static final byte[] FONT_ORIENTATION_SF = new byte[] {
        (byte) 0xD3, (byte) 0xAE, (byte) 0x89 };

    /** Font position MO:DCA structured field. */
    private static final byte[] FONT_POSITION_SF = new byte[] {
        (byte) 0xD3, (byte) 0xAC, (byte) 0x89 };

    /** Font index MO:DCA structured field. */
    private static final byte[] FONT_INDEX_SF = new byte[] {
        (byte) 0xD3, (byte) 0x8C, (byte) 0x89 };

    /**
     * The collection of code pages
     */
    private final Map/*<String, Map<String, String>>*/ codePagesCache
        = new java.util.HashMap/*<String, Map<String, String>>*/();

    /**
     * Returns an InputStream to a given file path and filename
     *
     * @param path the file path
     * @param filename the file name
     * @return an inputStream
     *
     * @throws IOException in the event that an I/O exception of some sort has occurred
     */
    private InputStream openInputStream(ResourceAccessor accessor, String filename)
            throws IOException {
        URI uri;
        try {
            uri = new URI(filename.trim());
        } catch (URISyntaxException e) {
            throw new FileNotFoundException("Invalid filename: "
                    + filename + " (" + e.getMessage() + ")");
        }
        InputStream inputStream = accessor.createInputStream(uri);
        return inputStream;
    }

    /**
     * Closes the inputstream
     *
     * @param inputStream the inputstream to close
     */
    private void closeInputStream(InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception ex) {
            // Lets log at least!
            log.error(ex.getMessage());
        }
    }

    /**
     * Load the font details and metrics into the CharacterSetMetric object,
     * this will use the actual afp code page and character set files to load
     * the object with the necessary metrics.
     *
     * @param characterSet the CharacterSetMetric object to populate
     * @throws IOException if an I/O exception of some sort has occurred.
     */
    public void loadCharacterSetMetric(CharacterSet characterSet) throws IOException {

        InputStream inputStream = null;

        try {

            /**
             * Get the code page which contains the character mapping
             * information to map the unicode character id to the graphic
             * chracter global identifier.
             */
            String codePageId = new String(characterSet.getCodePage());
            ResourceAccessor accessor = characterSet.getResourceAccessor();

            Map/*<String,String>*/ codePage
                = (Map/*<String,String>*/)codePagesCache.get(codePageId);

            if (codePage == null) {
                codePage = loadCodePage(codePageId, characterSet.getEncoding(), accessor);
                codePagesCache.put(codePageId, codePage);
            }

            /**
             * Load the character set metric information, no need to cache this
             * information as it should be cached by the objects that wish to
             * load character set metric information.
             */
            final String characterSetName = characterSet.getName();

            inputStream = openInputStream(accessor, characterSetName);

            StructuredFieldReader structuredFieldReader = new StructuredFieldReader(inputStream);

            // Process D3A689 Font Descriptor
            FontDescriptor fontDescriptor = processFontDescriptor(structuredFieldReader);
            characterSet.setNominalVerticalSize(fontDescriptor.getNominalFontSizeInMillipoints());

            // Process D3A789 Font Control
            FontControl fontControl = processFontControl(structuredFieldReader);

            if (fontControl != null) {
                //process D3AE89 Font Orientation
                CharacterSetOrientation[] characterSetOrientations
                    = processFontOrientation(structuredFieldReader);

                int metricNormalizationFactor;
                if (fontControl.isRelative()) {
                    metricNormalizationFactor = 1;
                } else {
                    int dpi = fontControl.getDpi();
                    metricNormalizationFactor = 1000 * 72000
                        / fontDescriptor.getNominalFontSizeInMillipoints() / dpi;
                }

                //process D3AC89 Font Position
                processFontPosition(structuredFieldReader, characterSetOrientations,
                        metricNormalizationFactor);

                //process D38C89 Font Index (per orientation)
                for (int i = 0; i < characterSetOrientations.length; i++) {
                    processFontIndex(structuredFieldReader,
                            characterSetOrientations[i], codePage, metricNormalizationFactor);
                    characterSet.addCharacterSetOrientation(characterSetOrientations[i]);
                }
            } else {
                throw new IOException(
                        "Failed to read font control structured field in character set "
                        + characterSetName);
            }

        } finally {
            closeInputStream(inputStream);
        }

    }

    /**
     * Load the code page information from the appropriate file. The file name
     * to load is determined by the code page name and the file extension 'CDP'.
     *
     * @param codePage
     *            the code page identifier
     * @param encoding
     *            the encoding to use for the character decoding
     * @param accessor the resource accessor
     * @returns a code page mapping
     */
    private Map/*<String,String>*/ loadCodePage(String codePage, String encoding,
        ResourceAccessor accessor) throws IOException {

        // Create the HashMap to store code page information
        Map/*<String,String>*/ codePages = new java.util.HashMap/*<String,String>*/();

        InputStream inputStream = null;
        try {
            inputStream = openInputStream(accessor, codePage.trim());

            StructuredFieldReader structuredFieldReader = new StructuredFieldReader(inputStream);
            byte[] data = structuredFieldReader.getNext(CHARACTER_TABLE_SF);

            int position = 0;
            byte[] gcgiBytes = new byte[8];
            byte[] charBytes = new byte[1];

            // Read data, ignoring bytes 0 - 2
            for (int index = 3; index < data.length; index++) {
                if (position < 8) {
                    // Build the graphic character global identifier key
                    gcgiBytes[position] = data[index];
                    position++;
                } else if (position == 9) {
                    position = 0;
                    // Set the character
                    charBytes[0] = data[index];
                    String gcgiString = new String(gcgiBytes,
                            AFPConstants.EBCIDIC_ENCODING);
                    String charString = new String(charBytes, encoding);
                    codePages.put(gcgiString, charString);
                } else {
                    position++;
                }
            }
        } finally {
            closeInputStream(inputStream);
        }

        return codePages;
    }

    /**
     * Process the font descriptor details using the structured field reader.
     *
     * @param structuredFieldReader the structured field reader
     * @return a class representing the font descriptor
     */
    private static FontDescriptor processFontDescriptor(StructuredFieldReader structuredFieldReader)
    throws IOException {

        byte[] fndData = structuredFieldReader.getNext(FONT_DESCRIPTOR_SF);
        return new FontDescriptor(fndData);
    }

    /**
     * Process the font control details using the structured field reader.
     *
     * @param structuredFieldReader
     *            the structured field reader
     */
    private FontControl processFontControl(StructuredFieldReader structuredFieldReader)
    throws IOException {

        byte[] fncData = structuredFieldReader.getNext(FONT_CONTROL_SF);

        FontControl fontControl = null;
        if (fncData != null) {
            fontControl = new FontControl();

            if (fncData[7] == (byte) 0x02) {
                fontControl.setRelative(true);
            }
            int metricResolution = getUBIN(fncData, 9);
            if (metricResolution == 1000) {
                //Special case: 1000 units per em (rather than dpi)
                fontControl.setUnitsPerEm(1000);
            } else {
                fontControl.setDpi(metricResolution / 10);
            }
        }
        return fontControl;
    }

    /**
     * Process the font orientation details from using the structured field
     * reader.
     *
     * @param structuredFieldReader
     *            the structured field reader
     */
    private CharacterSetOrientation[] processFontOrientation(
        StructuredFieldReader structuredFieldReader) throws IOException {

        byte[] data = structuredFieldReader.getNext(FONT_ORIENTATION_SF);

        int position = 0;
        byte[] fnoData = new byte[26];

        List orientations = new java.util.ArrayList();

        // Read data, ignoring bytes 0 - 2
        for (int index = 3; index < data.length; index++) {
            // Build the font orientation record
            fnoData[position] = data[index];
            position++;

            if (position == 26) {

                position = 0;

                int orientation = 0;

                switch (fnoData[2]) {
                    case 0x00:
                        orientation = 0;
                        break;
                    case 0x2D:
                        orientation = 90;
                        break;
                    case 0x5A:
                        orientation = 180;
                        break;
                    case (byte) 0x87:
                        orientation = 270;
                        break;
                    default:
                        System.out.println("ERROR: Oriantation");
                }

                CharacterSetOrientation cso = new CharacterSetOrientation(
                    orientation);
                orientations.add(cso);

            }
        }

        return (CharacterSetOrientation[]) orientations
            .toArray(EMPTY_CSO_ARRAY);
    }

    /**
     * Populate the CharacterSetOrientation object in the suplied array with the
     * font position details using the supplied structured field reader.
     *
     * @param structuredFieldReader
     *            the structured field reader
     * @param characterSetOrientations
     *            the array of CharacterSetOrientation objects
     * @param metricNormalizationFactor factor to apply to the metrics to get normalized
     *                  font metric values
     */
    private void processFontPosition(StructuredFieldReader structuredFieldReader,
        CharacterSetOrientation[] characterSetOrientations, double metricNormalizationFactor)
            throws IOException {

        byte[] data = structuredFieldReader.getNext(FONT_POSITION_SF);

        int position = 0;
        byte[] fpData = new byte[26];

        int characterSetOrientationIndex = 0;

        // Read data, ignoring bytes 0 - 2
        for (int index = 3; index < data.length; index++) {
            if (position < 22) {
                // Build the font orientation record
                fpData[position] = data[index];
                if (position == 9) {
                    CharacterSetOrientation characterSetOrientation
                            = characterSetOrientations[characterSetOrientationIndex];

                    int xHeight = getSBIN(fpData, 2);
                    int capHeight = getSBIN(fpData, 4);
                    int ascHeight = getSBIN(fpData, 6);
                    int dscHeight = getSBIN(fpData, 8);

                    dscHeight = dscHeight * -1;

                    characterSetOrientation.setXHeight(
                            (int)Math.round(xHeight * metricNormalizationFactor));
                    characterSetOrientation.setCapHeight(
                            (int)Math.round(capHeight * metricNormalizationFactor));
                    characterSetOrientation.setAscender(
                            (int)Math.round(ascHeight * metricNormalizationFactor));
                    characterSetOrientation.setDescender(
                            (int)Math.round(dscHeight * metricNormalizationFactor));
                }
            } else if (position == 22) {
                position = 0;
                characterSetOrientationIndex++;
                fpData[position] = data[index];
            }

            position++;
        }

    }

    /**
     * Process the font index details for the character set orientation.
     *
     * @param structuredFieldReader the structured field reader
     * @param cso the CharacterSetOrientation object to populate
     * @param codepage the map of code pages
     * @param metricNormalizationFactor factor to apply to the metrics to get normalized
     *                  font metric values
     */
    private void processFontIndex(StructuredFieldReader structuredFieldReader,
        CharacterSetOrientation cso, Map/*<String,String>*/ codepage,
        double metricNormalizationFactor)
        throws IOException {

        byte[] data = structuredFieldReader.getNext(FONT_INDEX_SF);

        int position = 0;

        byte[] gcgid = new byte[8];
        byte[] fiData = new byte[20];

        int lowest = 255;
        int highest = 0;
        String firstABCMismatch = null;

        // Read data, ignoring bytes 0 - 2
        for (int index = 3; index < data.length; index++) {
            if (position < 8) {
                gcgid[position] = data[index];
                position++;
            } else if (position < 27) {
                fiData[position - 8] = data[index];
                position++;
            } else if (position == 27) {

                fiData[position - 8] = data[index];

                position = 0;

                String gcgiString = new String(gcgid, AFPConstants.EBCIDIC_ENCODING);

                String idx = (String) codepage.get(gcgiString);

                if (idx != null) {

                    int cidx = idx.charAt(0);
                    int width = getUBIN(fiData, 0);
                    int a = getSBIN(fiData, 10);
                    int b = getUBIN(fiData, 12);
                    int c = getSBIN(fiData, 14);
                    int abc = a + b + c;
                    int diff = Math.abs(abc - width);
                    if (diff != 0 && width != 0) {
                        double diffPercent = 100 * diff / (double)width;
                        //if difference > 2%
                        if (diffPercent > 2) {
                            if (log.isTraceEnabled()) {
                                log.trace(gcgiString + ": "
                                        + a + " + " + b + " + " + c + " = " + (a + b + c)
                                        + " but found: " + width);
                            }
                            if (firstABCMismatch == null) {
                                firstABCMismatch = gcgiString;
                            }
                        }
                    }

                    if (cidx < lowest) {
                        lowest = cidx;
                    }

                    if (cidx > highest) {
                        highest = cidx;
                    }

                    int normalizedWidth = (int)Math.round(width * metricNormalizationFactor);

                    cso.setWidth(cidx, normalizedWidth);

                }

            }
        }

        cso.setFirstChar(lowest);
        cso.setLastChar(highest);

        if (log.isDebugEnabled() && firstABCMismatch != null) {
            //Debug level because it usually is no problem.
            log.debug("Font has metrics inconsitencies where A+B+C doesn't equal the"
                    + " character increment. The first such character found: "
                    + firstABCMismatch);
        }
    }

    private static int getUBIN(byte[] data, int start) {
        return ((data[start] & 0xFF) << 8) + (data[start + 1] & 0xFF);
    }

    private static int getSBIN(byte[] data, int start) {
        int ubin = ((data[start] & 0xFF) << 8) + (data[start + 1] & 0xFF);
        if ((ubin & 0x8000) != 0) {
            //extend sign
            return ubin | 0xFFFF0000;
        } else {
            return ubin;
        }
    }

    private class FontControl {

        private int dpi;
        private int unitsPerEm;

        private boolean isRelative = false;

        public int getDpi() {
            return dpi;
        }

        public void setDpi(int i) {
            dpi = i;
        }

        public int getUnitsPerEm() {
            return this.unitsPerEm;
        }

        public void setUnitsPerEm(int value) {
            this.unitsPerEm = value;
        }

        public boolean isRelative() {
            return isRelative;
        }

        public void setRelative(boolean b) {
            isRelative = b;
        }
    }

    private static class FontDescriptor {

        private byte[] data;

        public FontDescriptor(byte[] data) {
            this.data = data;
        }

        public int getNominalFontSizeInMillipoints() {
            int nominalFontSize = 100 * getUBIN(data, 39);
            return nominalFontSize;
        }
    }

}
