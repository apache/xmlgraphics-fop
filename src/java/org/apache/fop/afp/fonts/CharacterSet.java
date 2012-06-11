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

import java.io.UnsupportedEncodingException;
import java.nio.charset.CharacterCodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.afp.AFPConstants;
import org.apache.fop.afp.AFPEventProducer;
import org.apache.fop.afp.fonts.CharactersetEncoder.EncodedChars;
import org.apache.fop.afp.util.AFPResourceAccessor;
import org.apache.fop.afp.util.StringUtils;

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

    /** Static logging instance */
    protected static final Log LOG = LogFactory.getLog(CharacterSet.class.getName());

    /** default codepage */
    public static final String DEFAULT_CODEPAGE = "T1V10500";

    /** default encoding */
    public static final String DEFAULT_ENCODING = "Cp500";

    private static final int MAX_NAME_LEN = 8;


    /** The code page to which the character set relates */
    protected final String codePage;

    /** The encoding used for the code page */
    protected final String encoding;

    /** The characterset encoder corresponding to this encoding */
    private final CharactersetEncoder encoder;

    /** The character set relating to the font */
    protected final String name;

    /** The path to the installed fonts */
    private final AFPResourceAccessor accessor;

    /** The current orientation (currently only 0 is supported by FOP) */
    private final String currentOrientation = "0";

    /** The collection of objects for each orientation */
    private final Map<String, CharacterSetOrientation> characterSetOrientations;

    /** The nominal vertical size (in millipoints) for bitmap fonts. 0 for outline fonts. */
    private int nominalVerticalSize;

    /**
     * Constructor for the CharacterSetMetric object, the character set is used to load the font
     * information from the actual AFP font.
     *
     * @param codePage the code page identifier
     * @param encoding the encoding of the font
     * @param charsetType the type of the characterset
     * @param name the character set name
     * @param accessor the resource accessor to load resource with
     * @param eventProducer for handling AFP related events
     */
    CharacterSet(String codePage, String encoding, CharacterSetType charsetType, String name,
            AFPResourceAccessor accessor, AFPEventProducer eventProducer) {
        if (name.length() > MAX_NAME_LEN) {
            String msg = "Character set name '" + name + "' must be a maximum of "
                + MAX_NAME_LEN + " characters";
            eventProducer.characterSetNameInvalid(this, msg);
            throw new IllegalArgumentException(msg);
        }

        if (name.length() < MAX_NAME_LEN) {
            this.name = StringUtils.rpad(name, ' ', MAX_NAME_LEN);
        } else {
            this.name = name;
        }
        this.codePage = codePage;
        this.encoding = encoding;
        this.encoder = CharactersetEncoder.newInstance(encoding, charsetType);
        this.accessor = accessor;

        this.characterSetOrientations = new HashMap<String, CharacterSetOrientation>(4);
    }

    /**
     * Add character set metric information for the different orientations
     *
     * @param cso the metrics for the orientation
     */
    public void addCharacterSetOrientation(CharacterSetOrientation cso) {
        characterSetOrientations.put(String.valueOf(cso.getOrientation()), cso);
    }

    /**
     * Sets the nominal vertical size of the font in the case of bitmap fonts.
     * @param nominalVerticalSize the nominal vertical size (in millipoints)
     */
    public void setNominalVerticalSize(int nominalVerticalSize) {
        this.nominalVerticalSize = nominalVerticalSize;
    }

    /**
     * Returns the nominal vertical size of the font in the case of bitmap fonts. For outline fonts,
     * zero is returned, because these are scalable fonts.
     * @return the nominal vertical size (in millipoints) for bitmap fonts, or 0 for outline fonts.
     */
    public int getNominalVerticalSize() {
        return this.nominalVerticalSize;
    }

    /**
     * Ascender height is the distance from the character baseline to the
     * top of the character box. A negative ascender height signifies that
     * all of the graphic character is below the character baseline. For
     * a character rotation other than 0, ascender height loses its
     * meaning when the character is lying on its side or is upside down
     * with respect to normal viewing orientation. For the general case,
     * Ascender Height is the characters most positive y-axis value.
     * For bounded character boxes, for a given character having an
     * ascender, ascender height and baseline offset are equal.
     *
     * @return the ascender value in millipoints
     */
    public int getAscender() {

        return getCharacterSetOrientation().getAscender();
    }

    /**
     * Cap height is the average height of the uppercase characters in
     * a font. This value is specified by the designer of a font and is
     * usually the height of the uppercase M.
     *
     * @return the cap height value in millipoints
     */
    public int getCapHeight() {

        return getCharacterSetOrientation().getCapHeight();
    }

    /**
     * Descender depth is the distance from the character baseline to
     * the bottom of a character box. A negative descender depth signifies
     * that all of the graphic character is above the character baseline.
     *
     * @return the descender value in millipoints
     */
    public int getDescender() {

        return getCharacterSetOrientation().getDescender();
    }

    /**
     * Returns the first character in the character set
     *
     * @return the first character in the character set (Unicode codepoint)
     */
    public char getFirstChar() {
        return getCharacterSetOrientation().getFirstChar();
    }

    /**
     * Returns the last character in the character set
     *
     * @return the last character in the character set (Unicode codepoint)
     */
    public char getLastChar() {
        return getCharacterSetOrientation().getLastChar();
    }

    /**
     * Returns the resource accessor to load the font resources with.
     * @return the resource accessor to load the font resources with
     */
    public AFPResourceAccessor getResourceAccessor() {
        return this.accessor;
    }

    /**
     * Get the width (in 1/1000ths of a point size) of all characters
     *
     * @return the widths of all characters
     */
    public int[] getWidths() {

        return getCharacterSetOrientation().getWidths();
    }

    /**
     * XHeight refers to the height of the lower case letters above the baseline.
     *
     * @return the typical height of characters
     */
    public int getXHeight() {

        return getCharacterSetOrientation().getXHeight();
    }

    /**
     * Get the width (in 1/1000ths of a point size) of the character
     * identified by the parameter passed.
     *
     * @param character the Unicode character from which the width will be calculated
     * @return the width of the character
     */
    public int getWidth(char character) {
        return getCharacterSetOrientation().getWidth(character);
    }



    /**
     * Returns the AFP character set identifier
     *
     * @return the AFP character set identifier
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the AFP character set identifier as a byte array
     *
     * @return the AFP character set identifier as a byte array
     */
    public byte[] getNameBytes() {
        byte[] nameBytes = null;
        try {
            nameBytes = name.getBytes(AFPConstants.EBCIDIC_ENCODING);
        } catch (UnsupportedEncodingException usee) {
            nameBytes = name.getBytes();
            LOG.warn(
                "UnsupportedEncodingException translating the name " + name);
        }
        return nameBytes;
    }

    /**
     * Returns the AFP code page identifier
     *
     * @return the AFP code page identifier
     */
    public String getCodePage() {
        return codePage;
    }

    /**
     * Returns the AFP code page encoding
     *
     * @return the AFP code page encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Helper method to return the current CharacterSetOrientation, note
     * that FOP does not yet implement the "reference-orientation"
     * attribute therefore we always use the orientation zero degrees,
     * Other orientation information is captured for use by a future
     * implementation (whenever FOP implement the mechanism). This is also
     * the case for landscape prints which use an orientation of 270 degrees,
     * in 99.9% of cases the font metrics will be the same as the 0 degrees
     * therefore the implementation currently will always use 0 degrees.
     *
     * @return characterSetOrentation The current orientation metrics.
     */
    private CharacterSetOrientation getCharacterSetOrientation() {
        CharacterSetOrientation c
            = characterSetOrientations.get(currentOrientation);
        return c;
    }

    /**
     * Indicates whether the given char in the character set.
     * @param c the character to check
     * @return true if the character is in the character set
     */
    public boolean hasChar(char c) {
        if (encoder != null) {
            return encoder.canEncode(c);
        } else {
            //Sun Java 1.4.2 compatibility
            return true;
        }
    }

    /**
     * Encodes a character sequence to a byte array.
     * @param chars the characters
     * @return the encoded characters
     * @throws CharacterCodingException if the encoding operation fails
     */
    public EncodedChars encodeChars(CharSequence chars) throws CharacterCodingException {
        return encoder.encode(chars);
    }

    /**
     * Map a Unicode character to a code point in the font.
     * The code tables are already converted to Unicode therefore
     * we can use the identity mapping.
     *
     * @param c the Unicode character to map
     * @return the mapped character
     */
    public char mapChar(char c) {
        //TODO This is not strictly correct but we'll let it be for the moment
        return c;
    }

    /**
     * Returns the increment for an space.
     * @return the space increment
     */
    public int getSpaceIncrement() {
        return getCharacterSetOrientation().getSpaceIncrement();
    }

    /**
     * Returns the increment for an em space.
     * @return the em space increment
     */
    public int getEmSpaceIncrement() {
        return getCharacterSetOrientation().getEmSpaceIncrement();
    }

}
