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

package org.apache.fop.fo;

import java.awt.Color;
import java.util.NoSuchElementException;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.fo.properties.CommonHyphenation;
import org.apache.fop.fo.properties.CommonTextDecoration;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.properties.SpaceProperty;

/**
 * A text node (PCDATA) in the formatting object tree.
 *
 * Unfortunately the BufferManager implementation holds
 * onto references to the character data in this object
 * longer than the lifetime of the object itself, causing
 * excessive memory consumption and OOM errors.
 */
public class FOText extends FONode {

    /**
     * the character array containing the text
     */
    public char[] ca;

    /**
     * The starting valid index of the ca array 
     * to be processed.
     *
     * This value is originally equal to 0, but becomes 
     * incremented during leading whitespace removal by the flow.Block class,  
     * via the TextCharIterator.remove() method below.
     */
    public int startIndex = 0;

    /**
     * The ending valid index of the ca array 
     * to be processed.
     *
     * This value is originally equal to ca.length, but becomes 
     * decremented during between-word whitespace removal by the 
     * XMLWhiteSpaceHandler via the TextCharIterator.remove() 
     * method below.
     */
    public int endIndex = 0;

    /** properties relevant for PCDATA */
    /* TODO: these are basically always the same as the parent FObj or FObjMixed
     *      so maybe those can be removed, and the accessors could  
     *      dispatch the call to the parent?
     */
    private CommonFont commonFont;
    private CommonHyphenation commonHyphenation;
    private Color color;
    private KeepProperty keepTogether;
    private Property letterSpacing;
    private SpaceProperty lineHeight;
    private int whiteSpaceTreatment;
    private int whiteSpaceCollapse;
    private int textTransform;
    private Property wordSpacing;
    private int wrapOption;
    private Length baselineShift;

    /**
     * Points to the previous FOText object created within the current
     * block. If this is "null", this is the first such object.
     */
    private FOText prevFOTextThisBlock = null;

    /**
     * Points to the next FOText object created within the current
     * block. If this is "null", this is the last such object.
     */
    private FOText nextFOTextThisBlock = null;

    /**
     * Points to the ancestor Block object. This is used to keep track of
     * which FOText nodes are descendants of the same block.
     */
    private Block ancestorBlock = null;
    
    /** Holds the text decoration values. May be null */
    private CommonTextDecoration textDecoration;
    
    private static final int IS_WORD_CHAR_FALSE = 0;
    private static final int IS_WORD_CHAR_TRUE = 1;
    private static final int IS_WORD_CHAR_MAYBE = 2;

    /**
     * Creates a new FO text node.
     *
     * @param parent FONode that is the parent of this object
     */
    public FOText(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    protected void addCharacters(char[] data, int start, int length,
            PropertyList list, Locator locator) throws FOPException {

        int calength = 0;
        char[] nca;
        if (ca != null) {
            calength = ca.length;
            nca = new char[calength + length];
            System.arraycopy(ca, 0, nca, 0, calength);
        } else {
            nca = new char[length];
        }
        System.arraycopy(data, start, nca, calength, length);
        endIndex = nca.length;
        this.ca = nca;
     }

    /**
     * {@inheritDoc} 
     */
    public FONode clone(FONode parent, boolean removeChildren)
            throws FOPException {
        FOText ft = (FOText) super.clone(parent, removeChildren);
        if (removeChildren) {
            //not really removing, but just make sure the char array 
            //pointed to is really a different one, and reset any
            //possible whitespace-handling effects
            if (ca != null) {
                ft.ca = new char[ca.length];
                System.arraycopy(ca, 0, ft.ca, 0, ca.length);
            }
        }
        ft.prevFOTextThisBlock = null;
        ft.nextFOTextThisBlock = null;
        ft.ancestorBlock = null;
        return ft;
    }

    /**
     * {@inheritDoc}
     */
    public void bind(PropertyList pList) throws FOPException {
        commonFont = pList.getFontProps();
        commonHyphenation = pList.getHyphenationProps();
        color = pList.get(Constants.PR_COLOR).getColor(getUserAgent());
        keepTogether = pList.get(Constants.PR_KEEP_TOGETHER).getKeep();
        lineHeight = pList.get(Constants.PR_LINE_HEIGHT).getSpace();
        letterSpacing = pList.get(Constants.PR_LETTER_SPACING);
        whiteSpaceCollapse = pList.get(Constants.PR_WHITE_SPACE_COLLAPSE).getEnum();
        whiteSpaceTreatment = pList.get(Constants.PR_WHITE_SPACE_TREATMENT).getEnum();
        textTransform = pList.get(Constants.PR_TEXT_TRANSFORM).getEnum();
        wordSpacing = pList.get(Constants.PR_WORD_SPACING);
        wrapOption = pList.get(Constants.PR_WRAP_OPTION).getEnum();
        textDecoration = pList.getTextDecorationProps();
        baselineShift = pList.get(Constants.PR_BASELINE_SHIFT).getLength();
    }

    /** {@inheritDoc} */
    protected void endOfNode() throws FOPException {
        textTransform();
        getFOEventHandler().characters(ca, startIndex, endIndex);
    }

    /**
     * Check if this text node will create an area.
     * This means either there is non-whitespace or it is
     * preserved whitespace.
     * Maybe this just needs to check length > 0, since char iterators
     * handle whitespace.
     *
     * @return true if this will create an area in the output
     */
    public boolean willCreateArea() {
        if (whiteSpaceCollapse == Constants.EN_FALSE
                && endIndex - startIndex > 0) {
            return true;
        }

        for (int i = startIndex; i < endIndex; i++) {
            char ch = ca[i];
            if (!((ch == ' ')
                    || (ch == '\n')
                    || (ch == '\r')
                    || (ch == '\t'))) { // whitespace
                return true;
            }
        }
        return false;
    }

    /**
     * @return a new TextCharIterator
     */
    public CharIterator charIterator() {
        return new TextCharIterator();
    }

     /**
     * This method is run as part of the ancestor Block's flushText(), to 
     * create xref pointers to the previous FOText objects within the same Block
     * @param  ancestorBlock the ancestor fo:block
     */
    protected void createBlockPointers(Block ancestorBlock) {
        this.ancestorBlock = ancestorBlock;
        // if the last FOText is a sibling, point to it, and have it point here
        if (ancestorBlock.lastFOTextProcessed != null) {
            if (ancestorBlock.lastFOTextProcessed.ancestorBlock 
                    == this.ancestorBlock) {
                prevFOTextThisBlock = ancestorBlock.lastFOTextProcessed;
                prevFOTextThisBlock.nextFOTextThisBlock = this;
            } else {
                prevFOTextThisBlock = null;
            }
        }
    }

    /**
     * This method is run as part of the Constructor, to handle the
     * text-transform property.
     */
    private void textTransform() {
        if (getBuilderContext().inMarker() 
                || textTransform == Constants.EN_NONE) {
            return;
        }
        for (int i = 0; i < endIndex; i++) {
            ca[i] = charTransform(i);
        }
    }

    /**
     * Determines whether a particular location in an FOText object's text is
     * the start of a new "word". The use of "word" here is specifically for
     * the text-transform property, but may be useful for other things as
     * well, such as word-spacing. The definition of "word" is somewhat ambiguous
     * and appears to be definable by the user agent.
     *
     * @param i index into ca[]
     *
     * @return True if the character at this location is the start of a new
     * word.
     */
    private boolean isStartOfWord(int i) {
        char prevChar = getRelativeCharInBlock(i, -1);
        /* All we are really concerned about here is of what type prevChar
           is. If inputChar is not part of a word, then the Java
           conversions will (we hope) simply return inputChar.
        */
        switch (isWordChar(prevChar)) {
        case IS_WORD_CHAR_TRUE:
            return false;
        case IS_WORD_CHAR_FALSE:
            return true;
        /* "MAYBE" implies that additional context is needed. An example is a
         * single-quote, either straight or closing, which might be interpreted
         * as a possessive or a contraction, or might be a closing quote.
         */
        case IS_WORD_CHAR_MAYBE:
            char prevPrevChar = getRelativeCharInBlock(i, -2);
            switch (isWordChar(prevPrevChar)) {
            case IS_WORD_CHAR_TRUE:
                return false;
            case IS_WORD_CHAR_FALSE:
                return true;
            case IS_WORD_CHAR_MAYBE:
                return true;
            default:
                return false;
        }
        default:
            return false;
        }
    }

    /**
     * Finds a character within the current Block that is relative in location
     * to a character in the current FOText. Treats all FOText objects within a
     * block as one unit, allowing text in adjoining FOText objects to be
     * returned if the parameters are outside of the current object.
     *
     * @param i index into ca[]
     * @param offset signed integer with relative position within the
     *   block of the character to return. To return the character immediately
     *   preceding i, pass -1. To return the character immediately after i,
     *   pass 1.
     * @return the character in the offset position within the block; \u0000 if
     * the offset points to an area outside of the block.
     */
    private char getRelativeCharInBlock(int i, int offset) {
        // The easy case is where the desired character is in the same FOText
        if (((i + offset) >= 0) && ((i + offset) <= this.endIndex)) {
            return ca[i + offset];
        }
        // For now, we can't look at following FOText nodes
        if (offset > 0) {
             return '\u0000';
         }
        // Remaining case has the text in some previous FOText node
        boolean foundChar = false;
        char charToReturn = '\u0000';
        FOText nodeToTest = this;
        int remainingOffset = offset + i;
        while (!foundChar) {
            if (nodeToTest.prevFOTextThisBlock == null) {
                foundChar = true;
                break;
            }
            nodeToTest = nodeToTest.prevFOTextThisBlock;
            if ((nodeToTest.endIndex + remainingOffset) >= 0) {
                charToReturn = nodeToTest.ca[nodeToTest.endIndex + remainingOffset];
                foundChar = true;
            } else {
                remainingOffset = remainingOffset + nodeToTest.endIndex;
            }
        }
        return charToReturn;
    }

    /**
     * @return The previous FOText node in this Block; null, if this is the
     * first FOText in this Block.
     */
    public FOText getPrevFOTextThisBlock () {
        return prevFOTextThisBlock;
    }

    /**
     * @return The next FOText node in this Block; null if this is the last
     * FOText in this Block; null if subsequent FOText nodes have not yet been
     * processed.
     */
    public FOText getNextFOTextThisBlock () {
        return nextFOTextThisBlock;
    }

    /**
     * @return The nearest ancestor block object which contains this FOText.
     */
    public Block getAncestorBlock () {
        return ancestorBlock;
    }

    /**
     * Transforms one character in ca[] using the text-transform property.
     *
     * @param i the index into ca[]
     * @return char with transformed value
     */
    private char charTransform(int i) {
        switch (textTransform) {
        /* put NONE first, as this is probably the common case */
        case Constants.EN_NONE:
            return ca[i];
        case Constants.EN_UPPERCASE:
            return Character.toUpperCase(ca[i]);
        case Constants.EN_LOWERCASE:
            return Character.toLowerCase(ca[i]);
        case Constants.EN_CAPITALIZE:
            if (isStartOfWord(i)) {
                /*
                 Use toTitleCase here. Apparently, some languages use
                 a different character to represent a letter when using
                 initial caps than when all of the letters in the word
                 are capitalized. We will try to let Java handle this.
                */
                return Character.toTitleCase(ca[i]);
            } else {
                return ca[i];
            }
        default:
            assert false; //should never happen as the property subsystem catches that case
            return ca[i];
        }
    }

    /**
     * Determines whether the input char should be considered part of a
     * "word". This is used primarily to determine whether the character
     * immediately following starts a new word, but may have other uses.
     * We have not found a definition of "word" in the standard (1.0), so the
     * logic used here is based on the programmer's best guess.
     *
     * @param inputChar the character to be tested.
     * @return int IS_WORD_CHAR_TRUE, IS_WORD_CHAR_FALSE, or IS_WORD_CHAR_MAYBE,
     * depending on whether the character should be considered part of a word
     * or not.
     */
    private static int isWordChar(char inputChar) {
        switch (Character.getType(inputChar)) {
        case Character.COMBINING_SPACING_MARK:
            return IS_WORD_CHAR_TRUE;
        case Character.CONNECTOR_PUNCTUATION:
            return IS_WORD_CHAR_TRUE;
        case Character.CONTROL:
            return IS_WORD_CHAR_FALSE;
        case Character.CURRENCY_SYMBOL:
            return IS_WORD_CHAR_TRUE;
        case Character.DASH_PUNCTUATION:
            if (inputChar == '-') {
                return IS_WORD_CHAR_TRUE; //hyphen
            }
            return IS_WORD_CHAR_FALSE;
        case Character.DECIMAL_DIGIT_NUMBER:
            return IS_WORD_CHAR_TRUE;
        case Character.ENCLOSING_MARK:
            return IS_WORD_CHAR_FALSE;
        case Character.END_PUNCTUATION:
            if (inputChar == '\u2019') {
                return IS_WORD_CHAR_MAYBE; //apostrophe, right single quote
            }
            return IS_WORD_CHAR_FALSE;
        case Character.FORMAT:
            return IS_WORD_CHAR_FALSE;
        case Character.LETTER_NUMBER:
            return IS_WORD_CHAR_TRUE;
        case Character.LINE_SEPARATOR:
            return IS_WORD_CHAR_FALSE;
        case Character.LOWERCASE_LETTER:
            return IS_WORD_CHAR_TRUE;
        case Character.MATH_SYMBOL:
            return IS_WORD_CHAR_FALSE;
        case Character.MODIFIER_LETTER:
            return IS_WORD_CHAR_TRUE;
        case Character.MODIFIER_SYMBOL:
            return IS_WORD_CHAR_TRUE;
        case Character.NON_SPACING_MARK:
            return IS_WORD_CHAR_TRUE;
        case Character.OTHER_LETTER:
            return IS_WORD_CHAR_TRUE;
        case Character.OTHER_NUMBER:
            return IS_WORD_CHAR_TRUE;
        case Character.OTHER_PUNCTUATION:
            if (inputChar == '\'') {
                return IS_WORD_CHAR_MAYBE; //ASCII apostrophe
            }
            return IS_WORD_CHAR_FALSE;
        case Character.OTHER_SYMBOL:
            return IS_WORD_CHAR_TRUE;
        case Character.PARAGRAPH_SEPARATOR:
            return IS_WORD_CHAR_FALSE;
        case Character.PRIVATE_USE:
            return IS_WORD_CHAR_FALSE;
        case Character.SPACE_SEPARATOR:
            return IS_WORD_CHAR_FALSE;
        case Character.START_PUNCTUATION:
            return IS_WORD_CHAR_FALSE;
        case Character.SURROGATE:
            return IS_WORD_CHAR_FALSE;
        case Character.TITLECASE_LETTER:
            return IS_WORD_CHAR_TRUE;
        case Character.UNASSIGNED:
            return IS_WORD_CHAR_FALSE;
        case Character.UPPERCASE_LETTER:
            return IS_WORD_CHAR_TRUE;
        default:
            return IS_WORD_CHAR_FALSE;
        }
    }

    private class TextCharIterator extends CharIterator {
        private int curIndex = 0;

        /* Current space removal process:  just increment the startIndex
           to "remove" leading spaces from ca, until an unremoved character
           is found.  Then perform arraycopy's to remove extra spaces
           between words.  nextCharCalled is used to determine if an 
           unremoved character has already been found--if its value > 2
           than it means that has occurred (it is reset to zero each time we 
           remove a space via incrementing the startIndex.)  */
        private int nextCharCalled = 0;
        
        public boolean hasNext() {
           if (curIndex == 0) {
//             log.debug("->" + new String(ca) + "<-");
          }
           return (curIndex < endIndex);
        }

        public char nextChar() {
            if (curIndex < endIndex) {
                nextCharCalled++;
                // Just a char class? Don't actually care about the value!
                return ca[curIndex++];
            } else {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            if (curIndex < endIndex && nextCharCalled < 2) {
                startIndex++;
                nextCharCalled = 0;
//              log.debug("removeA: " + new String(ca, startIndex, endIndex - startIndex));
            } else if (curIndex < endIndex) {
                // copy from curIndex to end to curIndex-1
                System.arraycopy(ca, curIndex, ca, curIndex - 1, 
                    endIndex - curIndex);
                endIndex--;
                curIndex--;
//              log.debug("removeB: " + new String(ca, startIndex, endIndex - startIndex));
            } else if (curIndex == endIndex) {
//              log.debug("removeC: " + new String(ca, startIndex, endIndex - startIndex));
                endIndex--;
                curIndex--;
            }
        }

        public void replaceChar(char c) {
            if (curIndex > 0 && curIndex <= endIndex) {
                ca[curIndex - 1] = c;
            }
        }

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
     * @return the "color" property.
     */
    public Color getColor() {
        return color;
    }

    /** 
     * @return the "keep-together" property.
     */
    public KeepProperty getKeepTogether() {
        return keepTogether;
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
    
    /**
     * @return the "white-space-treatment" property
     */
    public int getWhitespaceTreatment() {
        return whiteSpaceTreatment;
    }
    
    /**
     * @return the "word-spacing" property.
     */
    public Property getWordSpacing() {
        return wordSpacing; 
    }
    
    /**
     * @return the "wrap-option" property.
     */
    public int getWrapOption() {
        return wrapOption; 
    }
    
    /** @return the "text-decoration" property. */
    public CommonTextDecoration getTextDecoration() {
        return textDecoration; 
    }

    /** @return the baseline-shift property */
    public Length getBaseLineShift() {
        return baselineShift;
    }

    /** {@inheritDoc} */
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append(" (").append(ca).append(")");
        return sb.toString();
    }
 
    /** {@inheritDoc} */
    public String getLocalName() {
        return null;
    }

    /** {@inheritDoc} */
    public String getNormalNamespacePrefix() {
        return null;
    }

    /** {@inheritDoc} */
    protected String gatherContextInfo() {
        if (getLocator() != null) {
            return super.gatherContextInfo();
        } else {
            return new String(ca).trim();
        }
    }    
}