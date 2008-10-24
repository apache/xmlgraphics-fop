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

package org.apache.fop.render.afp.fonts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.afp.tools.StructuredFieldReader;

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
    protected static final Log log = LogFactory.getLog("org.apache.fop.render.afp.fonts");

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
     * The conversion factor to millipoints for 240 dpi
     */
    private static final int FOP_100_DPI_FACTOR = 1;

    /**
     * The conversion factor to millipoints for 240 dpi
     */
    private static final int FOP_240_DPI_FACTOR = 300000;

    /**
     * The conversion factor to millipoints for 300 dpi
     */
    private static final int FOP_300_DPI_FACTOR = 240000;

    /**
     * The encoding to use to convert from EBCIDIC to ASCII
     */
    private static final String ASCII_ENCODING = "UTF8";

    /**
     * The collection of code pages
     */
    private final Map/*<String, Map<String, String>>*/ codePages
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
    private InputStream openInputStream(String path, String filename) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = AFPFontReader.class.getClassLoader();
        }

        URL url = classLoader.getResource(path);

        if (url == null) {
            try {
                File file = new File(path);
                url = file.toURI().toURL();
                if (url == null) {
                    String msg = "file not found " + filename + " in classpath: " + path;
                    log.error(msg);
                    throw new FileNotFoundException(msg);
                }
            } catch (MalformedURLException ex) {
                String msg = "file not found " + filename + " in classpath: " + path;
                log.error(msg);
                throw new FileNotFoundException(msg);
            }
        }

        File directory = new File(url.getPath());
        if (!directory.canRead()) {
            String msg = "Failed to read directory " + url.getPath();
            log.error(msg);
            throw new FileNotFoundException(msg);
        }

        final String filterpattern = filename.trim();
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(filterpattern);
            }
        };

        File[] files = directory.listFiles(filter);

        if (files.length < 1) {
            String msg = "file search for " + filename + " located "
                + files.length + " files";
            log.error(msg);
            throw new FileNotFoundException(msg);
        } else if (files.length > 1) {
            String msg = "file search for " + filename + " located "
                + files.length + " files";
            log.warn(msg);
        }

        InputStream inputStream = files[0].toURI().toURL().openStream();

        if (inputStream == null) {
            String msg = "AFPFontReader:: getInputStream():: file not found for " + filename;
            log.error(msg);
            throw new FileNotFoundException(msg);
        }

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
            String path = characterSet.getPath();

            Map/*<String,String>*/ codePage = (Map/*<String,String>*/)codePages.get(codePageId);

            if (codePage == null) {
                codePage = loadCodePage(codePageId, characterSet.getEncoding(), path);
                codePages.put(codePageId, codePage);
            }

            /**
             * Load the character set metric information, no need to cache this
             * information as it should be cached by the objects that wish to
             * load character set metric information.
             */
            final String characterSetName = characterSet.getName();

            inputStream = openInputStream(path, characterSetName);

            StructuredFieldReader structuredFieldReader = new StructuredFieldReader(inputStream);

            // Process D3A789 Font Control
            FontControl fontControl = processFontControl(structuredFieldReader);

            if (fontControl != null) {
                //process D3AE89 Font Orientation
                CharacterSetOrientation[] characterSetOrientations
                    = processFontOrientation(structuredFieldReader);

                int dpi = fontControl.getDpi();

                //process D3AC89 Font Position
                processFontPosition(structuredFieldReader, characterSetOrientations, dpi);

                //process D38C89 Font Index (per orientation)
                for (int i = 0; i < characterSetOrientations.length; i++) {
                    processFontIndex(structuredFieldReader,
                            characterSetOrientations[i], codePage, dpi);
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
     * @returns a code page mapping
     */
    private Map/*<String,String>*/ loadCodePage(String codePage, String encoding,
        String path) throws IOException {

        // Create the HashMap to store code page information
        Map/*<String,String>*/ codePages = new java.util.HashMap/*<String,String>*/();

        InputStream inputStream = null;
        try {
            inputStream = openInputStream(path, codePage.trim());

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
//                int value = charString.charAt(0);
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
     * Process the font control details using the structured field reader.
     *
     * @param structuredFieldReader
     *            the structured field reader
     */
    private FontControl processFontControl(StructuredFieldReader structuredFieldReader)
    throws IOException {

        byte[] fncData = structuredFieldReader.getNext(FONT_CONTROL_SF);

//        int position = 0;
        FontControl fontControl = null;
        if (fncData != null) {
            fontControl = new FontControl();

            if (fncData[7] == (byte) 0x02) {
                fontControl.setRelative(true);
            }

            int dpi = (((fncData[9] & 0xFF) << 8) + (fncData[10] & 0xFF)) / 10;

            fontControl.setDpi(dpi);
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
     */
    private void processFontPosition(StructuredFieldReader structuredFieldReader,
        CharacterSetOrientation[] characterSetOrientations, int dpi) throws IOException {

        byte[] data = structuredFieldReader.getNext(FONT_POSITION_SF);

        int position = 0;
        byte[] fpData = new byte[26];

        int characterSetOrientationIndex = 0;
        int fopFactor = 0;

        switch (dpi) {
            case 100:
                fopFactor = FOP_100_DPI_FACTOR;
                break;
            case 240:
                fopFactor = FOP_240_DPI_FACTOR;
                break;
            case 300:
                fopFactor = FOP_300_DPI_FACTOR;
                break;
            default:
                String msg = "Unsupported font resolution of " + dpi + " dpi.";
                log.error(msg);
                throw new IOException(msg);
        }

        // Read data, ignoring bytes 0 - 2
        for (int index = 3; index < data.length; index++) {
            if (position < 22) {
                // Build the font orientation record
                fpData[position] = data[index];
            } else if (position == 22) {

                position = 0;

                CharacterSetOrientation characterSetOrientation
                    = characterSetOrientations[characterSetOrientationIndex];

                int xHeight = ((fpData[2] & 0xFF) << 8) + (fpData[3] & 0xFF);
                int capHeight = ((fpData[4] & 0xFF) << 8) + (fpData[5] & 0xFF);
                int ascHeight = ((fpData[6] & 0xFF) << 8) + (fpData[7] & 0xFF);
                int dscHeight = ((fpData[8] & 0xFF) << 8) + (fpData[9] & 0xFF);

                dscHeight = dscHeight * -1;

                characterSetOrientation.setXHeight(xHeight * fopFactor);
                characterSetOrientation.setCapHeight(capHeight * fopFactor);
                characterSetOrientation.setAscender(ascHeight * fopFactor);
                characterSetOrientation.setDescender(dscHeight * fopFactor);

                characterSetOrientationIndex++;

                fpData[position] = data[index];

            }

            position++;
        }

    }

    /**
     * Process the font index details for the character set orientation.
     *
     * @param structuredFieldReader
     *            the structured field reader
     * @param cso
     *            the CharacterSetOrientation object to populate
     * @param codepage
     *            the map of code pages
     */
    private void processFontIndex(StructuredFieldReader structuredFieldReader,
        CharacterSetOrientation cso, Map/*<String,String>*/ codepage, int dpi)
        throws IOException {

        byte[] data = structuredFieldReader.getNext(FONT_INDEX_SF);

        int fopFactor = 0;

        switch (dpi) {
            case 100:
                fopFactor = FOP_100_DPI_FACTOR;
                break;
            case 240:
                fopFactor = FOP_240_DPI_FACTOR;
                break;
            case 300:
                fopFactor = FOP_300_DPI_FACTOR;
                break;
            default:
                String msg = "Unsupported font resolution of " + dpi + " dpi.";
                log.error(msg);
                throw new IOException(msg);
        }

        int position = 0;

        byte[] gcgid = new byte[8];
        byte[] fiData = new byte[20];

        int lowest = 255;
        int highest = 0;

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
                    int width = ((fiData[0] & 0xFF) << 8) + (fiData[1] & 0xFF);

                    if (cidx < lowest) {
                        lowest = cidx;
                    }

                    if (cidx > highest) {
                        highest = cidx;
                    }

                    int a = (width * fopFactor);

                    cso.setWidth(cidx, a);

                }

            }
        }

        cso.setFirstChar(lowest);
        cso.setLastChar(highest);

    }

    private class FontControl {

        private int dpi;

        private boolean isRelative = false;

        public int getDpi() {
            return dpi;
        }

        public void setDpi(int i) {
            dpi = i;
        }

        public boolean isRelative() {
            return isRelative;
        }

        public void setRelative(boolean b) {
            isRelative = b;
        }
    }

}
