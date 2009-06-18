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

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.CharIterator;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.fo.properties.CommonHyphenation;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.fo.properties.CommonTextDecoration;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.properties.SpaceProperty;
import org.apache.fop.util.CharUtilities;
import org.xml.sax.Locator;

/**
 * This class represents the flow object 'fo:character'. Its use is defined by
 * the spec: "The fo:character flow object represents a character that is mapped to
 * a glyph for presentation. It is an atomic unit to the formatter.
 * When the result tree is interpreted as a tree of formatting objects,
 * a character in the result tree is treated as if it were an empty
 * element of type fo:character with a character attribute
 * equal to the Unicode representation of the character.
 * The semantics of an "auto" value for character properties, which is
 * typically their initial value,  are based on the Unicode codepoint.
 * Overrides may be specified in an implementation-specific manner." (6.6.3)
 *
 */
public class Character extends FObj {
    // The value of properties relevant for fo:character.
    private CommonAural commonAural;
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private CommonFont commonFont;
    private CommonHyphenation commonHyphenation;
    private CommonMarginInline commonMarginInline;
    private CommonRelativePosition commonRelativePosition;
    private Length alignmentAdjust;
    private int treatAsWordSpace;
    private int alignmentBaseline;
    private Length baselineShift;
    private char character;
    private Color color;
    private int dominantBaseline;
    private Length textDepth;
    private Length textAltitude;
    // private ToBeImplementedProperty glyphOrientationHorizontal;
    // private ToBeImplementedProperty glyphOrientationVertical;
    private String id;
    private KeepProperty keepWithNext;
    private KeepProperty keepWithPrevious;
    private Property letterSpacing;
    private SpaceProperty lineHeight;
    private int scoreSpaces;
    private int suppressAtLineBreak;
    /** Holds the text decoration values. May be null */
    private CommonTextDecoration textDecoration;
    // private ToBeImplementedProperty textShadow;
    private int textTransform;
    private int visibility;
    private Property wordSpacing;
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
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        commonAural = pList.getAuralProps();
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        commonFont = pList.getFontProps();
        commonHyphenation = pList.getHyphenationProps();
        commonMarginInline = pList.getMarginInlineProps();
        commonRelativePosition = pList.getRelativePositionProps();

        alignmentAdjust = pList.get(PR_ALIGNMENT_ADJUST).getLength();
        treatAsWordSpace = pList.get(PR_TREAT_AS_WORD_SPACE).getEnum();
        alignmentBaseline = pList.get(PR_ALIGNMENT_BASELINE).getEnum();
        baselineShift = pList.get(PR_BASELINE_SHIFT).getLength();
        character = pList.get(PR_CHARACTER).getCharacter();
        color = pList.get(PR_COLOR).getColor();
        dominantBaseline = pList.get(PR_DOMINANT_BASELINE).getEnum();
        textDepth = pList.get(PR_TEXT_DEPTH).getLength();
        textAltitude = pList.get(PR_TEXT_ALTITUDE).getLength();
        // glyphOrientationHorizontal = pList.get(PR_GLYPH_ORIENTATION_HORIZONTAL);
        // glyphOrientationVertical = pList.get(PR_GLYPH_ORIENTATION_VERTICAL);
        id = pList.get(PR_ID).getString();
        keepWithNext = pList.get(PR_KEEP_WITH_NEXT).getKeep();
        keepWithPrevious = pList.get(PR_KEEP_WITH_PREVIOUS).getKeep();
        letterSpacing = pList.get(PR_LETTER_SPACING);
        lineHeight = pList.get(PR_LINE_HEIGHT).getSpace();
        scoreSpaces = pList.get(PR_SCORE_SPACES).getEnum();
        suppressAtLineBreak = pList.get(PR_SUPPRESS_AT_LINE_BREAK).getEnum();
        textDecoration = pList.getTextDecorationProps();
        // textShadow = pList.get(PR_TEXT_SHADOW);
        textTransform = pList.get(PR_TEXT_TRANSFORM).getEnum();
        visibility = pList.get(PR_VISIBILITY).getEnum();
        wordSpacing = pList.get(PR_WORD_SPACING);
    }

    /**
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws FOPException {
        checkId(id);
        getFOEventHandler().character(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: empty
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
            invalidChildError(loc, nsURI, localName);
    }

    /**
     * @see org.apache.fop.fo.FObj#charIterator
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
     * @return the "id" property.
     */
    public String getId() {
        return id;
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

    /** @see org.apache.fop.fo.FONode#getLocalName() */
    public String getLocalName() {
        return "character";
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
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
            foChar.character = CharUtilities.CODE_EOT;
            getFOEventHandler().getXMLWhiteSpaceHandler()
                .addDiscardableFOChar(foChar);
        }

        public void replaceChar(char c) {
            foChar.character = c;
        }

    }

}
