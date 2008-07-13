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
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.afp.exceptions.FontRuntimeException;
import org.apache.fop.render.afp.modca.AFPConstants;
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
    private HashMap codePages = new HashMap();

    /**
     * Load the font details and metrics into the CharacterSetMetric object,
     * this will use the actual afp code page and character set files to load
     * the object with the necessary metrics.
     *
     * @param characterSet the CharacterSetMetric object to populate
     */
    public void loadCharacterSetMetric(CharacterSet characterSet) {

        InputStream inputStream = null;

        try {

            /**
             * Get the code page which contains the character mapping
             * information to map the unicode character id to the graphic
             * chracter global identifier.
             */
            String cp = new String(characterSet.getCodePage());
            String path = characterSet.getPath();

            HashMap codepage = (HashMap) codePages.get(cp);

            if (codepage == null) {
                codepage = loadCodePage(cp, characterSet.getEncoding(), path);
                codePages.put(cp, codepage);
            }

            /**
             * Load the character set metric information, no need to cache this
             * information as it should be cached by the objects that wish to
             * load character set metric information.
             */
            final String characterset = characterSet.getName();

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
                        String msg = "CharacterSet file not found for "
                            + characterset + " in classpath: " + path;
                        log.error(msg);
                        throw new FileNotFoundException(msg);
                    }
                } catch (MalformedURLException ex) {
                    String msg = "CharacterSet file not found for "
                        + characterset + " in classpath: " + path;
                    log.error(msg);
                    throw new FileNotFoundException(msg);
                }

            }

            File directory = new File(url.getPath());

            final String filterpattern = characterset.trim();
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.startsWith(filterpattern);
                }
            };

            File[] csfont = directory.listFiles(filter);
            if (csfont.length < 1) {
                String msg = "CharacterSet file search for " + characterset
                    + " located " + csfont.length + " files";
                log.error(msg);
                throw new FileNotFoundException(msg);
            } else if (csfont.length > 1) {
                String msg = "CharacterSet file search for " + characterset
                    + " located " + csfont.length + " files";
                log.warn(msg);
            }

            inputStream = csfont[0].toURI().toURL().openStream();
            if (inputStream == null) {
                String msg = "Failed to open character set resource "
                    + characterset;
                log.error(msg);
                throw new FileNotFoundException(msg);
            }

            StructuredFieldReader sfr = new StructuredFieldReader(inputStream);

            // Process D3A789 Font Control
            FontControl fnc = processFontControl(sfr);

            //process D3AE89 Font Orientation
            CharacterSetOrientation[] csoArray = processFontOrientation(sfr);

            //process D3AC89 Font Position
            processFontPosition(sfr, csoArray, fnc.getDpi());

            //process D38C89 Font Index (per orientation)
            for (int i = 0; i < csoArray.length; i++) {
                processFontIndex(sfr, csoArray[i], codepage, fnc.getDpi());
                characterSet.addCharacterSetOrientation(csoArray[i]);
            }

        } catch (Exception ex) {
            throw new FontRuntimeException(
                "Failed to load the character set metrics for code page "
                + characterSet.getCodePage(), ex);
        } finally {
            try {
                inputStream.close();
            } catch (Exception ex) {
                // Ignore
            }
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
     */
    private static HashMap loadCodePage(String codePage, String encoding,
        String path) throws IOException {

        // Create the HashMap to store code page information
        HashMap codepages = new HashMap();

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
                    String msg = "CodePage file not found for " + codePage
                        + " in classpath: " + path;
                    log.error(msg);
                    throw new FileNotFoundException(msg);
                }
            } catch (MalformedURLException ex) {
                String msg = "CodePage file not found for " + codePage
                    + " in classpath: " + path;
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

        final String filterpattern = codePage.trim();
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(filterpattern);
            }
        };

        File[] codepage = directory.listFiles(filter);

        if (codepage.length < 1) {
            String msg = "CodePage file search for " + codePage + " located "
                + codepage.length + " files";
            log.error(msg);
            throw new FileNotFoundException(msg);
        } else if (codepage.length > 1) {
            String msg = "CodePage file search for " + codePage + " located "
                + codepage.length + " files";
            log.warn(msg);
        }

        InputStream is = codepage[0].toURI().toURL().openStream();

        if (is == null) {
            String msg = "AFPFontReader:: loadCodePage(String):: code page file not found for "
                + codePage;
            log.error(msg);
            throw new FileNotFoundException(msg);
        }

        StructuredFieldReader sfr = new StructuredFieldReader(is);
        byte[] data = sfr.getNext(CHARACTER_TABLE_SF);

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
                codepages.put(gcgiString, charString);
            } else {
                position++;
            }
        }

        try {
            is.close();
        } catch (Exception ex) {
            // Ignore
        }

        return codepages;

    }

    /**
     * Process the font control details using the structured field reader.
     *
     * @param sfr
     *            the structured field reader
     */
    private static FontControl processFontControl(StructuredFieldReader sfr)
    throws IOException {

        byte[] fncData = sfr.getNext(FONT_CONTROL_SF);

//        int position = 0;

        FontControl fontControl = new AFPFontReader().new FontControl();

        if (fncData[7] == (byte) 0x02) {
            fontControl.setRelative(true);
        }

        int dpi = (((fncData[9] & 0xFF) << 8) + (fncData[10] & 0xFF)) / 10;

        fontControl.setDpi(dpi);

        return fontControl;

    }

    /**
     * Process the font orientation details from using the structured field
     * reader.
     *
     * @param sfr
     *            the structured field reader
     */
    private static CharacterSetOrientation[] processFontOrientation(
        StructuredFieldReader sfr) throws IOException {

        byte[] data = sfr.getNext(FONT_ORIENTATION_SF);

        int position = 0;
        byte[] fnoData = new byte[26];

        ArrayList orientations = new ArrayList();

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
     * @param sfr
     *            the structured field reader
     * @param csoArray
     *            the array of CharacterSetOrientation objects
     */
    private static void processFontPosition(StructuredFieldReader sfr,
        CharacterSetOrientation[] csoArray, int dpi) throws IOException {

        byte[] data = sfr.getNext(FONT_POSITION_SF);

        int position = 0;
        byte[] fpData = new byte[26];

        int csoIndex = 0;
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

                CharacterSetOrientation cso = csoArray[csoIndex];

                int xHeight = ((fpData[2] & 0xFF) << 8) + (fpData[3] & 0xFF);
                int capHeight = ((fpData[4] & 0xFF) << 8) + (fpData[5] & 0xFF);
                int ascHeight = ((fpData[6] & 0xFF) << 8) + (fpData[7] & 0xFF);
                int dscHeight = ((fpData[8] & 0xFF) << 8) + (fpData[9] & 0xFF);

                dscHeight = dscHeight * -1;

                cso.setXHeight(xHeight * fopFactor);
                cso.setCapHeight(capHeight * fopFactor);
                cso.setAscender(ascHeight * fopFactor);
                cso.setDescender(dscHeight * fopFactor);

                csoIndex++;

                fpData[position] = data[index];

            }

            position++;
        }

    }

    /**
     * Process the font index details for the character set orientation.
     *
     * @param sfr
     *            the structured field reader
     * @param cso
     *            the CharacterSetOrientation object to populate
     * @param codepage
     *            the map of code pages
     */
    private static void processFontIndex(StructuredFieldReader sfr,
        CharacterSetOrientation cso, HashMap codepage, int dpi)
        throws IOException {

        byte[] data = sfr.getNext(FONT_INDEX_SF);

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
                gcgid[position] = (byte) data[index];
                position++;
            } else if (position < 27) {
                fiData[position - 8] = (byte) data[index];
                position++;
            } else if (position == 27) {

                fiData[position - 8] = (byte) data[index];

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
