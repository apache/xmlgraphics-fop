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

import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.util.SoftMapCache;

import org.apache.fop.afp.AFPConstants;
import org.apache.fop.afp.AFPEventProducer;
import org.apache.fop.afp.util.AFPResourceAccessor;
import org.apache.fop.afp.util.StructuredFieldReader;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.Typeface;

/**
 * The CharacterSetBuilder is responsible building the a CharacterSet instance that holds
 * the font metric data.  The data is either read from disk and passed to a CharacterSet (*)
 * or a FopCharacterSet is instantiated that is composed of a Typeface instance configured
 * with this data.<br>
 * -*- For referenced fonts CharacterSetBuilder is responsible for reading the font attributes
 * from binary code page files and the character set metric files. In IBM font structure, a
 * code page maps each character of text to the characters in a character set.
 * Each character is translated into a code point. When the character is
 * printed, each code point is matched to a character ID on the code page
 * specified. The character ID is then matched to the image (raster pattern or
 * outline pattern) of the character in the character set specified. The image
 * in the character set is the image that is printed in the document. To be a
 * valid code page for a particular character set, all character IDs in the code
 * page must be included in that character set.<br>
 * This class will read the font information from the binary code page files and character
 * set metric files in order to determine the correct metrics to use when rendering the
 * formatted object.
 */
public abstract class CharacterSetBuilder {

    /**
     * Static logging instance
     */
    protected static final Log LOG = LogFactory.getLog(CharacterSetBuilder.class);

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
    private final Map<String, Map<String, String>> codePagesCache
            = Collections.synchronizedMap(new WeakHashMap<String, Map<String, String>>());

    /**
     * Cache of charactersets
     */
    private final SoftMapCache characterSetsCache = new SoftMapCache(true);

    /** Default constructor. */
    private CharacterSetBuilder() {
    }

    /**
     * Factory method for the single-byte implementation of AFPFontReader.
     * @return AFPFontReader
     */
    public static CharacterSetBuilder getSingleByteInstance() {
        return SingleByteLoader.getInstance();
    }

    /**
     * Factory method for the double-byte (CID Keyed font (Type 0)) implementation of AFPFontReader.
     * @return AFPFontReader
     */
    public static CharacterSetBuilder getDoubleByteInstance() {
        return DoubleByteLoader.getInstance();
    }


    /**
     * Returns an InputStream to a given file path and filename
     *
     * @param accessor the resource accessor
     * @param uriStr the URI
     * @param eventProducer for handling AFP related events
     * @return an inputStream
     * @throws IOException in the event that an I/O exception of some sort has occurred
     */
    private InputStream openInputStream(AFPResourceAccessor accessor, String uriStr,
            AFPEventProducer eventProducer)
            throws IOException {
        URI uri;
        try {
            uri = InternalResourceResolver.cleanURI(uriStr.trim());
        } catch (URISyntaxException e) {
            throw new MalformedURLException("Invalid uri: " + uriStr + " (" + e.getMessage() + ")");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Opening " + uri);
        }
        return accessor.createInputStream(uri);
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
            LOG.error(ex.getMessage());
        }
    }

    /**
     * Load the font details and metrics into the CharacterSetMetric object, this will use the
     * actual afp code page and character set files to load the object with the necessary metrics.
     *
     * @param characterSetName name of the characterset
     * @param codePageName name of the code page file
     * @param encoding encoding name
     * @param accessor used to load codepage and characterset
     * @param eventProducer for handling AFP related events
     * @return CharacterSet object
     * @throws IOException if an I/O error occurs
     */
    public CharacterSet buildSBCS(String characterSetName, String codePageName, String encoding,
            AFPResourceAccessor accessor, AFPEventProducer eventProducer) throws IOException {
        return processFont(characterSetName, codePageName, encoding, CharacterSetType.SINGLE_BYTE,
                accessor, eventProducer);
    }

    /**
     * Load the font details and metrics into the CharacterSetMetric object, this will use the
     * actual afp code page and character set files to load the object with the necessary metrics.
     * This method is to be used for double byte character sets (DBCS).
     *
     * @param characterSetName name of the characterset
     * @param codePageName name of the code page file
     * @param encoding encoding name
     * @param charsetType the characterset type
     * @param accessor used to load codepage and characterset
     * @param eventProducer for handling AFP related events
     * @return CharacterSet object
     * @throws IOException if an I/O error occurs
     */
    public CharacterSet buildDBCS(String characterSetName, String codePageName, String encoding,
            CharacterSetType charsetType, AFPResourceAccessor accessor, AFPEventProducer eventProducer)
            throws IOException {
        return processFont(characterSetName, codePageName, encoding, charsetType, accessor,
                eventProducer);
    }

    /**
     * Load the font details and metrics into the CharacterSetMetric object, this will use the
     * actual afp code page and character set files to load the object with the necessary metrics.
     *
     * @param characterSetName the CharacterSetMetric object to populate
     * @param codePageName the name of the code page to use
     * @param encoding name of the encoding in use
     * @param typeface base14 font name
     * @param eventProducer for handling AFP related events
     * @return CharacterSet object
     * @throws IOException if an I/O error occurs
     */
    public CharacterSet build(String characterSetName, String codePageName, String encoding,
            Typeface typeface, AFPEventProducer eventProducer) throws IOException {
        return new FopCharacterSet(codePageName, encoding, characterSetName, typeface,
                eventProducer);
    }

    private CharacterSet processFont(String characterSetName, String codePageName, String encoding,
            CharacterSetType charsetType, AFPResourceAccessor accessor, AFPEventProducer eventProducer)
            throws IOException {
        // check for cached version of the characterset
        URI charSetURI = accessor.resolveURI(characterSetName);
        String cacheKey = charSetURI.toASCIIString() + "_" + characterSetName + "_" + codePageName;
        CharacterSet characterSet = (CharacterSet) characterSetsCache.get(cacheKey);
        if (characterSet != null) {
            return characterSet;
        }

        // characterset not in the cache, so recreating
        characterSet = new CharacterSet(codePageName, encoding, charsetType, characterSetName,
                accessor, eventProducer);

        InputStream inputStream = null;

        try {

            /**
             * Get the code page which contains the character mapping
             * information to map the unicode character id to the graphic
             * chracter global identifier.
             */
            Map<String, String> codePage;
            // TODO: This could have performance implications if several threads want to use the
            // codePagesCache to retrieve different codepages.
            synchronized (codePagesCache) {
                codePage = codePagesCache.get(codePageName);

                if (codePage == null) {
                    codePage = loadCodePage(codePageName, encoding, accessor, eventProducer);
                    codePagesCache.put(codePageName, codePage);
                }
            }

            inputStream = openInputStream(accessor, characterSetName, eventProducer);

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

                double metricNormalizationFactor;
                if (fontControl.isRelative()) {
                    metricNormalizationFactor = 1;
                } else {
                    int dpi = fontControl.getDpi();
                    metricNormalizationFactor = 1000.0d * 72000.0d
                        / fontDescriptor.getNominalFontSizeInMillipoints() / dpi;
                }
                ValueNormalizer normalizer = new ValueNormalizer(metricNormalizationFactor);
                //process D3AC89 Font Position
                processFontPosition(structuredFieldReader, characterSetOrientations, normalizer);
                //process D38C89 Font Index (per orientation)
                for (int i = 0; i < characterSetOrientations.length; i++) {
                    CharacterSetOrientation characterSetOrientation = characterSetOrientations[i];
                    processFontIndex(structuredFieldReader, characterSetOrientation, codePage, normalizer);
                    characterSet.addCharacterSetOrientation(characterSetOrientation);
                }
            } else {
                throw new IOException("Missing D3AE89 Font Control structured field.");
            }

        } finally {
            closeInputStream(inputStream);
        }
        characterSetsCache.put(cacheKey, characterSet);
        return characterSet;
    }

    private static class ValueNormalizer {

        private final double factor;

        public ValueNormalizer(double factor) {
            this.factor = factor;
        }

        public int normalize(int value) {
            return (int) Math.round(value *  factor);
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
     * @param eventProducer for handling AFP related events
     * @return a code page mapping (key: GCGID, value: Unicode character)
     * @throws IOException if an I/O exception of some sort has occurred.
     */
    protected Map<String, String> loadCodePage(String codePage, String encoding,
            AFPResourceAccessor accessor, AFPEventProducer eventProducer) throws IOException {

        // Create the HashMap to store code page information
        Map<String, String> codePages = new HashMap<String, String>();

        InputStream inputStream = null;
        try {
            inputStream = openInputStream(accessor, codePage.trim(), eventProducer);
        } catch (IOException e) {
            eventProducer.codePageNotFound(this, e);
            throw e;
        }
        try {
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
    private static FontDescriptor processFontDescriptor(
            StructuredFieldReader structuredFieldReader) throws IOException {

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
     * @return CharacterSetOrientation array
     * @throws IOException if an I/O exception of some sort has occurred.
     */
    private CharacterSetOrientation[] processFontOrientation(
        StructuredFieldReader structuredFieldReader) throws IOException {

        byte[] data = structuredFieldReader.getNext(FONT_ORIENTATION_SF);

        int position = 0;
        byte[] fnoData = new byte[26];

        List<CharacterSetOrientation> orientations = new ArrayList<CharacterSetOrientation>();

        // Read data, ignoring bytes 0 - 2
        for (int index = 3; index < data.length; index++) {
            // Build the font orientation record
            fnoData[position] = data[index];
            position++;

            if (position == 26) {
                position = 0;

                int orientation = determineOrientation(fnoData[2]);
                int spaceIncrement = getUBIN(fnoData, 8);
                int emIncrement = getUBIN(fnoData, 14);
                int nominalCharacterIncrement = getUBIN(fnoData, 20);

                orientations.add(new CharacterSetOrientation(orientation, spaceIncrement,
                        emIncrement, nominalCharacterIncrement));
            }
        }
        return orientations.toArray(EMPTY_CSO_ARRAY);
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
    private void processFontPosition(StructuredFieldReader structuredFieldReader,
        CharacterSetOrientation[] characterSetOrientations, ValueNormalizer normalizer)
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
                    int underscoreWidth = getUBIN(fpData, 17);
                    int underscorePosition = getSBIN(fpData, 20);
                    characterSetOrientation.setXHeight(normalizer.normalize(xHeight));
                    characterSetOrientation.setCapHeight(normalizer.normalize(capHeight));
                    characterSetOrientation.setAscender(normalizer.normalize(ascHeight));
                    characterSetOrientation.setDescender(normalizer.normalize(dscHeight));
                    characterSetOrientation.setUnderscoreWidth(normalizer.normalize(underscoreWidth));
                    characterSetOrientation.setUnderscorePosition(normalizer.normalize(underscorePosition));
                }
            } else if (position == 22) {
                position = 0;
                characterSetOrientationIndex++;
                fpData[position] = data[index];
            }
            position++;
        }

    }


    private void processFontIndex(StructuredFieldReader structuredFieldReader, CharacterSetOrientation cso,
            Map<String, String> codepage, ValueNormalizer normalizer)
            throws IOException {

        byte[] data = structuredFieldReader.getNext(FONT_INDEX_SF);

        int position = 0;

        byte[] gcgid = new byte[8];
        byte[] fiData = new byte[20];

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

                String idx = codepage.get(gcgiString);

                if (idx != null) {

                    char cidx = idx.charAt(0);
                    int width = getUBIN(fiData, 0);
                    int ascendHt = getSBIN(fiData, 2);
                    int descendDp = getSBIN(fiData, 4);
                    int a = getSBIN(fiData, 10);
                    int b = getUBIN(fiData, 12);
                    int c = getSBIN(fiData, 14);
                    int abc = a + b + c;
                    int diff = Math.abs(abc - width);
                    if (diff != 0 && width != 0) {
                        double diffPercent = 100 * diff / (double) width;
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
                    int normalizedWidth = normalizer.normalize(width);
                    int x0 = normalizer.normalize(a);
                    int y0 = normalizer.normalize(-descendDp);
                    int dx = normalizer.normalize(b);
                    int dy = normalizer.normalize(ascendHt + descendDp);
                    cso.setCharacterMetrics(cidx, normalizedWidth, new Rectangle(x0, y0, dx, dy));
                }
            }
        }

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

        private boolean isRelative;

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

    private static final class SingleByteLoader extends CharacterSetBuilder {

        private static final SingleByteLoader INSTANCE = new SingleByteLoader();

        private SingleByteLoader() {
            super();
        }

        private static SingleByteLoader getInstance() {
            return INSTANCE;
        }
    }

    /**
     * Double-byte (CID Keyed font (Type 0)) implementation of AFPFontReader.
     */
    private static final class DoubleByteLoader extends CharacterSetBuilder {

        private static final DoubleByteLoader INSTANCE = new DoubleByteLoader();

        private DoubleByteLoader() {
        }

        static DoubleByteLoader getInstance() {
            return INSTANCE;
        }

        @Override
        protected Map<String, String> loadCodePage(String codePage, String encoding,
                AFPResourceAccessor accessor, AFPEventProducer eventProducer) throws IOException {
            // Create the HashMap to store code page information
            Map<String, String> codePages = new HashMap<String, String>();
            InputStream inputStream = null;
            try {
                inputStream = super.openInputStream(accessor, codePage.trim(), eventProducer);
            } catch (IOException e) {
                eventProducer.codePageNotFound(this, e);
                throw e;
            }
            try {
                StructuredFieldReader structuredFieldReader = new StructuredFieldReader(inputStream);
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
                        } else {
                            position++;
                        }
                    }
                }
            } finally {
                super.closeInputStream(inputStream);
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
