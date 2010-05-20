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
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.util.SoftMapCache;

import org.apache.fop.afp.AFPConstants;
import org.apache.fop.afp.util.ResourceAccessor;
import org.apache.fop.afp.util.StructuredFieldReader;
import org.apache.fop.fonts.Typeface;

/**
 * The CharacterSetBuilder is responsible building the a CharacterSet instance that holds
 *  the font metric data.  The data is either read from disk and passed to a CharacterSet (*)
 *  or a FopCharacterSet is instantiated that is composed of a Typeface instance configured
 *  with this data.<p/>
 * -*- For referenced fonts CharacterSetBuilder is responsible for reading the font attributes
 * from binary code page files and the character set metric files. In IBM font structure, a
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
 */
public class CharacterSetBuilder {

    /**
     * Static logging instance
     */
    protected static final Log LOG = LogFactory.getLog(CharacterSetBuilder.class);

    /**
     * Singleton reference
     */
    private static CharacterSetBuilder instance;

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
            = new WeakHashMap/*<String, Map<String, String>>*/();

    /**
     * Cache of charactersets
     */
    private final SoftMapCache characterSetsCache = new SoftMapCache(true);


    private CharacterSetBuilder() { }

    /**
     * Factory method for the single-byte implementation of AFPFontReader.
     * @return AFPFontReader
     */
    public static CharacterSetBuilder getInstance() {
        if (instance == null) {
            instance = new CharacterSetBuilder();
        }
        return instance;
    }

    /**
     * Factory method for the double-byte (CID Keyed font (Type 0)) implementation of AFPFontReader.
     * @return AFPFontReader
     */
    public static CharacterSetBuilder getDoubleByteInstance() {
        return new DoubleByteLoader();
    }


    /**
     * Returns an InputStream to a given file path and filename
     *
     * * @param accessor the resource accessor
     * @param filename the file name
     * @return an inputStream
     *
     * @throws IOException in the event that an I/O exception of some sort has occurred
     */
    protected InputStream openInputStream(ResourceAccessor accessor, String filename)
            throws IOException {
        URI uri;
        try {
            uri = new URI(filename.trim());
        } catch (URISyntaxException e) {
            throw new FileNotFoundException("Invalid filename: "
                    + filename + " (" + e.getMessage() + ")");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Opening " + uri);
        }
        InputStream inputStream = accessor.createInputStream(uri);
        return inputStream;
    }

    /**
     * Closes the inputstream
     *
     * @param inputStream the inputstream to close
     */
    protected void closeInputStream(InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception ex) {
            // Lets log at least!
            LOG.error(ex.getMessage());
        }
    }

    /**
     * Load the font details and metrics into the CharacterSetMetric object,
     * this will use the actual afp code page and character set files to load
     * the object with the necessary metrics.
     * @param characterSetName name of the characterset
     * @param codePageName name of the code page file
     * @param encoding encoding name
     * @param accessor used to load codepage and characterset
     * @return CharacterSet object
     * @throws IOException if an I/O error occurs
     */
    public CharacterSet build(String characterSetName, String codePageName,
            String encoding, ResourceAccessor accessor) throws IOException {

        // check for cached version of the characterset
        String descriptor = characterSetName + "_" + encoding + "_" + codePageName;
        CharacterSet characterSet = (CharacterSet)characterSetsCache.get(descriptor);

        if (characterSet != null) {
            return characterSet;
        }

        // characterset not in the cache, so recreating
        characterSet = new CharacterSet(
                codePageName, encoding, characterSetName, accessor);

        InputStream inputStream = null;

        try {

            /**
             * Get the code page which contains the character mapping
             * information to map the unicode character id to the graphic
             * chracter global identifier.
             */

            Map/*<String,String>*/ codePage
                = (Map/*<String,String>*/)codePagesCache.get(codePageName);

            if (codePage == null) {
                codePage = loadCodePage(codePageName, encoding, accessor);
                codePagesCache.put(codePageName, codePage);
            }

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
                throw new IOException("Missing D3AE89 Font Control structured field.");
            }

        } finally {
            closeInputStream(inputStream);
        }
        characterSetsCache.put(descriptor, characterSet);
        return characterSet;

    }

    /**
     * Load the font details and metrics into the CharacterSetMetric object,
     * this will use the actual afp code page and character set files to load
     * the object with the necessary metrics.
     *
     * @param characterSetName the CharacterSetMetric object to populate
     * @param codePageName the name of the code page to use
     * @param encoding name of the encoding in use
     * @param typeface base14 font name
     * @return CharacterSet object
     */
    public CharacterSet build(String characterSetName, String codePageName,
            String encoding, Typeface typeface) {
       return new FopCharacterSet(codePageName, encoding, characterSetName, typeface);
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
     * @return a code page mapping (key: GCGID, value: Unicode character)
     * @throws IOException if an I/O exception of some sort has occurred.
     */
    protected Map/*<String,String>*/ loadCodePage(String codePage, String encoding,
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
                    //Use the 8-bit char index to find the Unicode character using the Java encoding
                    //given in the configuration. If the code page and the Java encoding don't
                    //match, a wrong Unicode character will be associated with the AFP GCGID.
                    //Idea: we could use IBM's GCGID to Unicode map and build code pages ourselves.
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
     * @throws IOException if an I/O exception of some sort has occurred.
     */
    protected static FontDescriptor processFontDescriptor(
                StructuredFieldReader structuredFieldReader)
    throws IOException {

        byte[] fndData = structuredFieldReader.getNext(FONT_DESCRIPTOR_SF);
        return new FontDescriptor(fndData);
    }

    /**
     * Process the font control details using the structured field reader.
     *
     * @param structuredFieldReader
     *            the structured field reader
     * @return the FontControl
     * @throws IOException if an I/O exception of some sort has occurred.
     */
    protected FontControl processFontControl(StructuredFieldReader structuredFieldReader)
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
     * @return CharacterSetOrientation array
     * @throws IOException if an I/O exception of some sort has occurred.
     */
    protected CharacterSetOrientation[] processFontOrientation(
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

                int orientation = determineOrientation(fnoData[2]);
                //  Space Increment
                int space = ((fnoData[8] & 0xFF ) << 8) + (fnoData[9] & 0xFF);
                //  Em-Space Increment
                int em = ((fnoData[14] & 0xFF ) << 8) + (fnoData[15] & 0xFF);

                CharacterSetOrientation cso = new CharacterSetOrientation(orientation);
                cso.setSpaceIncrement(space);
                cso.setEmSpaceIncrement(em);
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
     * @throws IOException if an I/O exception of some sort has occurred.
     */
    protected void processFontPosition(StructuredFieldReader structuredFieldReader,
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
     * @throws IOException if an I/O exception of some sort has occurred.
     */
    protected void processFontIndex(StructuredFieldReader structuredFieldReader,
        CharacterSetOrientation cso, Map/*<String,String>*/ codepage,
        double metricNormalizationFactor)
        throws IOException {

        byte[] data = structuredFieldReader.getNext(FONT_INDEX_SF);

        int position = 0;

        byte[] gcgid = new byte[8];
        byte[] fiData = new byte[20];

        char lowest = 255;
        char highest = 0;
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

                    char cidx = idx.charAt(0);
                    int width = getUBIN(fiData, 0);
                    int a = getSBIN(fiData, 10);
                    int b = getUBIN(fiData, 12);
                    int c = getSBIN(fiData, 14);
                    int abc = a + b + c;
                    int diff = Math.abs(abc - width);
                    if (diff != 0 && width != 0) {
                        double diffPercent = 100 * diff / (double)width;
                        if (diffPercent > 2) {
                            if (LOG.isTraceEnabled()) {
                                LOG.trace(gcgiString + ": "
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

        if (LOG.isDebugEnabled() && firstABCMismatch != null) {
            //Debug level because it usually is no problem.
            LOG.debug("Font has metrics inconsitencies where A+B+C doesn't equal the"
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

    /**
     * Double-byte (CID Keyed font (Type 0)) implementation of AFPFontReader.
     */
    private static class DoubleByteLoader extends CharacterSetBuilder {

        protected Map/*<String,String>*/ loadCodePage(String codePage, String encoding,
                ResourceAccessor accessor) throws IOException {

            // Create the HashMap to store code page information
            Map/*<String,String>*/ codePages = new java.util.HashMap/*<String,String>*/();

            InputStream inputStream = null;
            try {
                inputStream = openInputStream(accessor, codePage.trim());

                StructuredFieldReader structuredFieldReader
                    = new StructuredFieldReader(inputStream);
                byte[] data;
                while ((data = structuredFieldReader.getNext(CHARACTER_TABLE_SF)) != null) {
                    int position = 0;

                    byte[] gcgiBytes = new byte[8];
                    byte[] charBytes = new byte[2];
                    // Read data, ignoring bytes 0 - 2
                    for (int index = 3; index < data.length; index++) {

                        if (position < 8) {
                            // Build the graphic character global identifier key
                            gcgiBytes[position] = data[index];
                            position++;
                        } else if (position == 9) {
                            // Set the character
                            charBytes[0] = data[index];
                            position++;
                        } else if (position == 10) {
                            position = 0;
                            // Set the character
                            charBytes[1] = data[index];

                            String gcgiString = new String(gcgiBytes,
                                    AFPConstants.EBCIDIC_ENCODING);
                            String charString = new String(charBytes, encoding);
                            codePages.put(gcgiString, charString);

                        }
                        else {
                            position++;
                        }
                    }
                }
            } finally {
                closeInputStream(inputStream);
            }

            return codePages;
        }

    }

    private static int determineOrientation(byte orientation) {
        int degrees = 0;

        switch (orientation) {
        case 0x00:
            degrees = 0;
            break;
        case 0x2D:
            degrees = 90;
            break;
        case 0x5A:
            degrees = 180;
            break;
        case (byte) 0x87:
            degrees = 270;
            break;
        default:
            throw new IllegalStateException("Invalid orientation: " + orientation);
        }
        return degrees;
    }
}
