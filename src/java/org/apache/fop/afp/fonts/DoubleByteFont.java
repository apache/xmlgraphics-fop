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

import java.lang.Character.UnicodeBlock;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.afp.AFPEventProducer;

/**
 * Implementation of AbstractOutlineFont that supports double-byte fonts (CID Keyed font (Type 0)).
 * The width of characters that are not prescribed a width metrics in the font resource use
 * a fallback width.  The default width is 1 em.  A character can be supplied and queried for the
 *  fallback width of all non-ideograph characters.<p />
 */
public class DoubleByteFont extends AbstractOutlineFont {

    private static final Log log = LogFactory.getLog(DoubleByteFont.class);

    private final Set<Integer> charsProcessed;

    //See also http://unicode.org/reports/tr11/ which we've not closely looked at, yet
    //TODO the Unicode block listed here is probably not complete (ex. Hiragana, Katakana etc.)
    private static final Set<UnicodeBlock> IDEOGRAPHIC = new HashSet<UnicodeBlock>();
    static {
        IDEOGRAPHIC.add(Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS);
        //IDEOGRAPHIC.add(Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT);//Java 1.5
        IDEOGRAPHIC.add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
        IDEOGRAPHIC.add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A);
        //IDEOGRAPHIC.add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B); //Java 1.1
    }

    /**
     * Constructor for an double-byte outline font.
     * @param name the name of the font
     * @param embeddable whether or not this font is embeddable
     * @param charSet the character set
     * @param eventProducer Handles any AFP related events
     */
    public DoubleByteFont(String name, boolean embeddable, CharacterSet charSet,
            AFPEventProducer eventProducer) {
        super(name, embeddable, charSet, eventProducer);
        charsProcessed = new HashSet<Integer>();
    }

    /** {@inheritDoc} */
    public int getWidth(int character, int size) {
        int charWidth;
        try {
            charWidth = charSet.getWidth(toUnicodeCodepoint(character));
        } catch (IllegalArgumentException e) {
            if (!charsProcessed.contains(character)) {
                charsProcessed.add(character);
                getAFPEventProducer().charactersetMissingMetrics(this, (char)character,
                        charSet.getName().trim());
            }
            //  We shall try and handle characters that have no mapped width metric in font resource
            charWidth = -1;
        }

        if (charWidth == -1) {
            charWidth = inferCharWidth(character);
        }
        return charWidth * size;
    }

    private int inferCharWidth(int character) {

        //Is this character an ideograph?
        boolean isIdeographic = false;
        Character.UnicodeBlock charBlock = Character.UnicodeBlock.of((char)character);
        if (charBlock == null) {
            isIdeographic = false;
        } else if (IDEOGRAPHIC.contains(charBlock)) {
            isIdeographic = true;
        } else { //default
            isIdeographic = false;
        }

        if (isIdeographic) {
            return charSet.getEmSpaceIncrement();
        } else {
            return charSet.getSpaceIncrement();
        }
    }

}
