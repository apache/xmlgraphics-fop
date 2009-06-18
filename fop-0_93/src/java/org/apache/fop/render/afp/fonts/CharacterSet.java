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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.afp.modca.AFPConstants;
import org.apache.fop.render.afp.tools.StringUtils;

/**
 * The IBM Font Object Content Architecture (FOCA) supports presentation
 * of character shapes by defining their characteristics, which include
 * font description information for identifying the characters, font metric
 * information for positioning the characters, and character shape information
 * for presenting the character images.
 * <p/>
 * Presenting a graphic character on a presentation surface requires
 * information on the rotation and position of character on the physical
 * or logical page.
 * <p/>
 * This class proivdes font metric information for a particular font
 * as identified by the character set name. This information is obtained
 * directly from the AFP font files which must be installed in the path
 * specified in the afp-fonts xml definition file.
 * <p/>
 */
public class CharacterSet {

    /**
     * Static logging instance
     */
    protected static final Log log = LogFactory.getLog(CharacterSet.class.getName());

    /**
     * The code page to which the character set relates
     */
    protected String _codePage;

    /**
     * The encoding used for the code page
     */
    protected String _encoding;

    /**
     * The character set relating to the font
     */
    protected String _name;

    /**
     * The name of the character set as EBCIDIC bytes
     */
    private byte[] _nameBytes;

    /**
     * The path to the installed fonts
     */
    protected String _path;

    /**
     * Indicator as to whether to metrics have been loaded
     */
    private boolean _isMetricsLoaded = false;

    /**
     * The current orientation (currently only 0 is suppoted by FOP)
     */
    private String _currentOrientation = "0";

    /**
     * The collection of objects for each orientation
     */
    private HashMap _characterSetOrientations;

    /**
     * Constructor for the CharacterSetMetric object, the character set is used
     * to load the font information from the actual AFP font.
     * @param codePage the code page identifier
     * @param encoding the encoding of the font
     * @param name the character set name
     * @param path the path to the installed afp fonts
     */
    public CharacterSet(
        String codePage,
        String encoding,
        String name,
        String path) {

        if (name.length() > 8) {
            String msg = "Character set name must be a maximum of 8 characters " + name;
            log.error("Constructor:: " + msg);
            throw new IllegalArgumentException(msg);
        }

        if (name.length() < 8) {
            _name = StringUtils.rpad(name, ' ', 8);
        } else {
            _name = name;
        }

        try {

            _nameBytes = name.getBytes(AFPConstants.EBCIDIC_ENCODING);

        } catch (UnsupportedEncodingException usee) {

            _nameBytes = name.getBytes();
            log.warn(
                "Constructor:: UnsupportedEncodingException translating the name "
                + name);

        }

        _codePage = codePage;
        _encoding = encoding;
        _path = path;
        _characterSetOrientations = new HashMap(4);

    }

    /**
     * Add character set metric information for the different orientations
     * @param cso the metrics for the orientation
     */
    public void addCharacterSetOrientation(CharacterSetOrientation cso) {

        _characterSetOrientations.put(
            String.valueOf(cso.getOrientation()),
            cso);

    }

    /**
     * Ascender height is the distance from the character baseline to the
     * top of the character box. A negative ascender height signifies that
     * all of the graphic character is below the character baseline. For
     * a character rotation other than 0, ascender height loses its
     * meaning when the character is lying on its side or is upside down
     * with respect to normal viewing orientation. For the general case,
     * Ascender Height is the character�s most positive y-axis value.
     * For bounded character boxes, for a given character having an
     * ascender, ascender height and baseline offset are equal.
     * @return the ascender value in millipoints
     */
    public int getAscender() {
        load();
        return getCharacterSetOrientation().getAscender();
    }

    /**
     * Cap height is the average height of the uppercase characters in
     * a font. This value is specified by the designer of a font and is
     * usually the height of the uppercase M.
     * @return the cap height value in millipoints
     */
    public int getCapHeight() {
        load();
        return getCharacterSetOrientation().getCapHeight();
    }

    /**
     * Descender depth is the distance from the character baseline to
     * the bottom of a character box. A negative descender depth signifies
     * that all of the graphic character is above the character baseline.
     * @return the descender value in millipoints
     */
    public int getDescender() {
        load();
        return getCharacterSetOrientation().getDescender();
    }

    /**
     * The first character in the character set
     * @return the first character
     */
    public int getFirstChar() {
        load();
        return getCharacterSetOrientation().getFirstChar();
    }

    /**
     * The last character in the character set
     * @return the last character
     */
    public int getLastChar() {
        load();
        return getCharacterSetOrientation().getLastChar();
    }

    /**
     * @return the path where the font resources are installed
     */
    public String getPath() {
        return _path;
    }

    /**
     * Get the width (in 1/1000ths of a point size) of all characters
     * @return the widths of all characters
     */
    public int[] getWidths() {
        load();
        return getCharacterSetOrientation().getWidths();
    }

    /**
     * XHeight refers to the height of the lower case letters above the baseline.
     * @return the typical height of characters
     */
    public int getXHeight() {
        load();
        return getCharacterSetOrientation().getXHeight();
    }

    /**
     * Get the width (in 1/1000ths of a point size) of the character
     * identified by the parameter passed.
     * @param character the character from which the width will be calculated
     * @return the width of the character
     */
    public int width(int character) {
        load();
        return getCharacterSetOrientation().width(character);
    }

    /**
     * Lazy creation of the character metrics, the afp font file will only
     * be processed on a method call requiring the metric information.
     */
    private void load() {

        if (!_isMetricsLoaded) {

            AFPFontReader.loadCharacterSetMetric(this);
            _isMetricsLoaded = true;

        }

    }

    /**
     * Returns the AFP character set identifier
     * @return String
     */
    public String getName() {
        return _name;
    }

    /**
     * Returns the AFP character set identifier
     * @return byte[]
     */
    public byte[] getNameBytes() {
        return _nameBytes;
    }

    /**
     * Returns the AFP code page identifier
     * @return String
     */
    public String getCodePage() {
        return _codePage;
    }

    /**
     * Returns the AFP code page encoding
     * @return String
     */
    public String getEncoding() {
        return _encoding;
    }

    /**
     * Helper method to return the current CharacterSetOrientation, note
     * that FOP does not yet implement the "reference-orientation"
     * attribute therefore we always use the orientation zero degrees,
     * Other orientation information is captured for use by a future
     * implementation (whenever FOP implement the mechanism). This is also
     * the case for landscape prints which use an orientation of 270 degrees,
     * in 99.9% of cases the font metrics will be the same as the 0 degrees
     * therefore the implementation currely will always use 0 degrees.
     * @return characterSetOrentation The current orientation metrics.
     */
    private CharacterSetOrientation getCharacterSetOrientation() {

        CharacterSetOrientation c =
            (CharacterSetOrientation) _characterSetOrientations.get(
            _currentOrientation);
        return c;

    }

    /**
     * Map a Unicode character to a code point in the font.
     * The code tables are already converted to Unicode therefore
     * we can use the identity mapping.
     * @param c character to map
     * @return the mapped character
     */
    public char mapChar(char c) {
        return c;
    }

}
