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

package org.apache.fop.fo.flow;

import java.awt.Color;
import java.util.NoSuchElementException;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.CharIterator;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.fo.properties.CommonHyphenation;
import org.apache.fop.fo.properties.CommonTextDecoration;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.properties.SpaceProperty;

/**
 * Class modelling the fo:character object. 
 * Its use is defined by the spec: 
 * "The fo:character flow object represents a character that is mapped to
 *  a glyph for presentation. It is an atomic unit to the formatter.
 *  When the result tree is interpreted as a tree of formatting objects,
 *  a character in the result tree is treated as if it were an empty
 *  element of type fo:character with a character attribute
 *  equal to the Unicode representation of the character.
 *  The semantics of an "auto" value for character properties, which is
 *  typically their initial value,  are based on the Unicode codepoint.
 *  Overrides may be specified in an implementation-specific manner." (6.6.3)
 *
 */
public class Character extends FObj {
    // The value of properties relevant for fo:character.
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private CommonFont commonFont;
    private CommonHyphenation commonHyphenation;
    private Length alignmentAdjust;
    private int alignmentBaseline;
    private Length baselineShift;
    private char character;
    private Color color;
    private int dominantBaseline;
    // private ToBeImplementedProperty glyphOrientationHorizontal;
    // private ToBeImplementedProperty glyphOrientationVertical;
    private Property letterSpacing;
    private SpaceProperty lineHeight;
    /** Holds the text decoration values. May be null */
    private CommonTextDecoration textDecoration;
    // private ToBeImplementedProperty textShadow;
    private Property wordSpacing;
    // Unused but valid items, commented out for performance:
    //     private CommonAural commonAural;
    //     private CommonMarginInline commonMarginInline;
    //     private CommonRelativePosition commonRelativePosition;
    //     private int treatAsWordSpace;
    //     private Length textDepth;
    //     private Length textAltitude;
    //     private KeepProperty keepWithNext;
    //     private KeepProperty keepWithPrevious;
    //     private int scoreSpaces;
    //     private int suppressAtLineBreak;
    //     private int textTransform;
    //     private int visibility;
    // End of property values

    /** constant indicating that the character is OK */
    public static final int OK = 0;
    /** constant indicating that the character does not fit */
    public static final int DOESNOT_FIT = 1;

    /**
     * @param parent FONode that is the parent of this object
     */
    public Character(FONode parent) {
        super(parent);
    }

    /**
     * {@inheritDoc}
     */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        commonFont = pList.getFontProps();
        commonHyphenation = pList.getHyphenationProps();

        alignmentAdjust = pList.get(PR_ALIGNMENT_ADJUST).getLength();
        alignmentBaseline = pList.get(PR_ALIGNMENT_BASELINE).getEnum();
        baselineShift = pList.get(PR_BASELINE_SHIFT).getLength();
        character = pList.get(PR_CHARACTER).getCharacter();
        color = pList.get(PR_COLOR).getColor(getUserAgent());
        dominantBaseline = pList.get(PR_DOMINANT_BASELINE).getEnum();
        // glyphOrientationHorizontal = pList.get(PR_GLYPH_ORIENTATION_HORIZONTAL);
        // glyphOrientationVertical = pList.get(PR_GLYPH_ORIENTATION_VERTICAL);
        letterSpacing = pList.get(PR_LETTER_SPACING);
        lineHeight = pList.get(PR_LINE_HEIGHT).getSpace();
        textDecoration = pList.getTextDecorationProps();
        // textShadow = pList.get(PR_TEXT_SHADOW);
        wordSpacing = pList.get(PR_WORD_SPACING);
    }

    /**
     * {@inheritDoc}
     */
    protected void startOfNode() throws FOPException {
        super.startOfNode();
        getFOEventHandler().character(this);
    }

    /**
     * {@inheritDoc}
     * XSL Content Model: empty
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
                throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            invalidChildError(loc, nsURI, localName);
        }
    }

    /**
     * {@inheritDoc}
     */
    public CharIterator charIterator() {
        return new FOCharIterator(this);
    }

    /**
     * @return the Common Border, Padding, and Background Properties.
     */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    }

    /**
     * @return the Common Font Properties.
     */
    public CommonFont getCommonFont() {
        return commonFont;
    }

    /**
     * @return the Common Hyphenation Properties.
     */
    public CommonHyphenation getCommonHyphenation() {
        return commonHyphenation;
    }

    /**
     * @return the "character" property.
     */
    public char getCharacter() {
        return character;
    }

    /**
     * @return the "color" property.
     */
    public Color getColor() {
        return color;
    }

    /**
     * @return the "alignment-adjust" property
     */
    public Length getAlignmentAdjust() {
        return alignmentAdjust;
    }
    
    /**
     * @return the "alignment-baseline" property
     */
    public int getAlignmentBaseline() {
        return alignmentBaseline;
    }
    
    /**
     * @return the "baseline-shift" property
     */
    public Length getBaselineShift() {
        return baselineShift;
    }
    
    /**
     * @return the "dominant-baseline" property
     */
    public int getDominantBaseline() {
        return dominantBaseline;
    }
    
    /**
     * @return the "letter-spacing" property.
     */
    public Property getLetterSpacing() {
        return letterSpacing; 
    }

    /**
     * @return the "line-height" property.
     */
    public SpaceProperty getLineHeight() {
        return lineHeight;
    }

    /** @return the "text-decoration" property. */
    public CommonTextDecoration getTextDecoration() {
        return textDecoration; 
    }
    
    /**
     * @return the "word-spacing" property.
     */
    public Property getWordSpacing() {
        return wordSpacing; 
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "character";
    }
    
    /**
     * {@inheritDoc}
     */
    public int getNameId() {
        return FO_CHARACTER;
    }
    
    private class FOCharIterator extends CharIterator {

        private boolean bFirst = true;
        private Character foChar;
        
        FOCharIterator(Character foChar) {
            this.foChar = foChar;
        }
        
        public boolean hasNext() {
            return bFirst;
        }

        public char nextChar() {
            if (bFirst) {
                bFirst = false;
                return foChar.character;
            } else {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            foChar.parent.removeChild(foChar);
        }

        public void replaceChar(char c) {
            foChar.character = c;
        }

    }

}
