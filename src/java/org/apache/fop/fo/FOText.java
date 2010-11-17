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
import java.nio.CharBuffer;
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
import org.apache.fop.util.CharUtilities;

/**
 * A text node (PCDATA) in the formatting object tree.
 */
public class FOText extends FONode implements CharSequence {

    /** the <code>CharBuffer</code> containing the text */
    private CharBuffer charBuffer;

    /** properties relevant for #PCDATA */
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
    protected void characters(char[] data, int start, int length,
            PropertyList list, Locator locator) throws FOPException {

        if (this.charBuffer == null) {
            // buffer not yet initialized, do so now
            this.charBuffer = CharBuffer.allocate(length);
        } else {
            // allocate a larger buffer, and transfer contents
            int newLength = this.charBuffer.limit() + length;
            CharBuffer newBuffer = CharBuffer.allocate(newLength);
            this.charBuffer.rewind();
            newBuffer.put(this.charBuffer);
            this.charBuffer = newBuffer;
        }
        // append characters
        this.charBuffer.put(data, start, length);

    }

    /**
     * Return the array of characters for this instance.
     *
     * @return  a char array containing the text
     */
    public char[] getCharArray() {

        if (this.charBuffer == null) {
            return null;
        }

        if (this.charBuffer.hasArray()) {
            return this.charBuffer.array();
        }

        // only if the buffer implementation has
        // no accessible backing array, return a new one
        char[] ca = new char[this.charBuffer.limit()];
        this.charBuffer.rewind();
        this.charBuffer.get(ca);
        return ca;

    }

    /** {@inheritDoc} */
    public FONode clone(FONode parent, boolean removeChildren)
            throws FOPException {
        FOText ft = (FOText) super.clone(parent, removeChildren);
        if (removeChildren) {
            // not really removing, just make sure the char buffer
            // pointed to is really a different one
            if (this.charBuffer != null) {
                ft.charBuffer = CharBuffer.allocate(this.charBuffer.limit());
                this.charBuffer.rewind();
                ft.charBuffer.put(this.charBuffer);
                ft.charBuffer.rewind();
            }
        }
        ft.prevFOTextThisBlock = null;
        ft.nextFOTextThisBlock = null;
        ft.ancestorBlock = null;
        return ft;
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        this.commonFont = pList.getFontProps();
        this.commonHyphenation = pList.getHyphenationProps();
        this.color = pList.get(Constants.PR_COLOR).getColor(getUserAgent());
        this.keepTogether = pList.get(Constants.PR_KEEP_TOGETHER).getKeep();
        this.lineHeight = pList.get(Constants.PR_LINE_HEIGHT).getSpace();
        this.letterSpacing = pList.get(Constants.PR_LETTER_SPACING);
        this.whiteSpaceCollapse = pList.get(Constants.PR_WHITE_SPACE_COLLAPSE).getEnum();
        this.whiteSpaceTreatment = pList.get(Constants.PR_WHITE_SPACE_TREATMENT).getEnum();
        this.textTransform = pList.get(Constants.PR_TEXT_TRANSFORM).getEnum();
        this.wordSpacing = pList.get(Constants.PR_WORD_SPACING);
        this.wrapOption = pList.get(Constants.PR_WRAP_OPTION).getEnum();
        this.textDecoration = pList.getTextDecorationProps();
        this.baselineShift = pList.get(Constants.PR_BASELINE_SHIFT).getLength();
    }

    /** {@inheritDoc} */
    protected void endOfNode() throws FOPException {
        super.endOfNode();
        getFOEventHandler().characters(
                this.getCharArray(), 0, this.charBuffer.limit());
    }

    /** {@inheritDoc} */
    public void finalizeNode() {
        textTransform();
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
                && this.charBuffer.limit() > 0) {
            return true;
        }

        char ch;
        this.charBuffer.rewind();
        while (this.charBuffer.hasRemaining()) {
            ch = this.charBuffer.get();
            if (!((ch == CharUtilities.SPACE)
                    || (ch == CharUtilities.LINEFEED_CHAR)
                    || (ch == CharUtilities.CARRIAGE_RETURN)
                    || (ch == CharUtilities.TAB))) {
                // not whitespace
                this.charBuffer.rewind();
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
     * This method is run as part of endOfNode(), to handle the
     * text-transform property for accumulated FOText
     */
    private void textTransform() {
        if (getBuilderContext().inMarker()
                || textTransform == Constants.EN_NONE) {
            return;
        }

        this.charBuffer.rewind();
        CharBuffer tmp = this.charBuffer.slice();
        char c;
        int lim = this.charBuffer.limit();
        int pos = -1;
        while (++pos < lim) {
            c = this.charBuffer.get();
            switch (textTransform) {
                case Constants.EN_UPPERCASE:
                    tmp.put(Character.toUpperCase(c));
                    break;
                case Constants.EN_LOWERCASE:
                    tmp.put(Character.toLowerCase(c));
                    break;
                case Constants.EN_CAPITALIZE:
                    if (isStartOfWord(pos)) {
                        /*
                         Use toTitleCase here. Apparently, some languages use
                         a different character to represent a letter when using
                         initial caps than when all of the letters in the word
                         are capitalized. We will try to let Java handle this.
                        */
                        tmp.put(Character.toTitleCase(c));
                    } else {
                        tmp.put(c);
                    }
                    break;
                default:
                     //should never happen as the property subsystem catches that case
                    assert false;
                    //nop
            }
        }
    }

    /**
     * Determines whether a particular location in an FOText object's text is
     * the start of a new "word". The use of "word" here is specifically for
     * the text-transform property, but may be useful for other things as
     * well, such as word-spacing. The definition of "word" is somewhat ambiguous
     * and appears to be definable by the user agent.
     *
     * @param i index into charBuffer
     *
     * @return True if the character at this location is the start of a new
     * word.
     */
    private boolean isStartOfWord(int i) {
        char prevChar = getRelativeCharInBlock(i, -1);
        /* All we are really concerned about here is of what type prevChar
         * is. If inputChar is not part of a word, then the Java
         * conversions will (we hope) simply return inputChar.
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
     * @param i index into the CharBuffer
     * @param offset signed integer with relative position within the
     *   block of the character to return. To return the character immediately
     *   preceding i, pass -1. To return the character immediately after i,
     *   pass 1.
     * @return the character in the offset position within the block; \u0000 if
     * the offset points to an area outside of the block.
     */
    private char getRelativeCharInBlock(int i, int offset) {

        int charIndex = i + offset;
        // The easy case is where the desired character is in the same FOText
        if (charIndex >= 0 && charIndex < this.length()) {
            return this.charAt(i + offset);
        }

        // For now, we can't look at following FOText nodes
        if (offset > 0) {
             return CharUtilities.NULL_CHAR;
        }

        // Remaining case has the text in some previous FOText node
        boolean foundChar = false;
        char charToReturn = CharUtilities.NULL_CHAR;
        FOText nodeToTest = this;
        int remainingOffset = offset + i;
        while (!foundChar) {
            if (nodeToTest.prevFOTextThisBlock == null) {
                break;
            }
            nodeToTest = nodeToTest.prevFOTextThisBlock;
            int diff = nodeToTest.length() + remainingOffset - 1;
            if (diff >= 0) {
                charToReturn = nodeToTest.charAt(diff);
                foundChar = true;
            } else {
                remainingOffset += diff;
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

        private int currentPosition = 0;

        private boolean canRemove = false;
        private boolean canReplace = false;

        /** {@inheritDoc} */
        public boolean hasNext() {
           return (this.currentPosition < charBuffer.limit());
        }

        /** {@inheritDoc} */
        public char nextChar() {

            if (this.currentPosition < charBuffer.limit()) {
                this.canRemove = true;
                this.canReplace = true;
                return charBuffer.get(currentPosition++);
            } else {
                throw new NoSuchElementException();
            }

        }

        /** {@inheritDoc} */
        public void remove() {

            if (this.canRemove) {
                charBuffer.position(currentPosition);
                // Slice the buffer at the current position
                CharBuffer tmp = charBuffer.slice();
                // Reset position to before current character
                charBuffer.position(--currentPosition);
                if (tmp.hasRemaining()) {
                    // Transfer any remaining characters
                    charBuffer.mark();
                    charBuffer.put(tmp);
                    charBuffer.reset();
                }
                // Decrease limit
                charBuffer.limit(charBuffer.limit() - 1);
                // Make sure following calls fail, unless nextChar() was called
                this.canRemove = false;
            } else {
                throw new IllegalStateException();
            }

        }

        /** {@inheritDoc} */
        public void replaceChar(char c) {

            if (this.canReplace) {
                charBuffer.put(currentPosition - 1, c);
            } else {
                throw new IllegalStateException();
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
    public Length getBaselineShift() {
        return baselineShift;
    }

    /** {@inheritDoc} */
    public String toString() {
        return (this.charBuffer == null) ? "" : this.charBuffer.toString();
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "#PCDATA";
    }

    /** {@inheritDoc} */
    public String getNormalNamespacePrefix() {
        return null;
    }

    /** {@inheritDoc} */
    protected String gatherContextInfo() {
        if (this.locator != null) {
            return super.gatherContextInfo();
        } else {
            return this.toString();
        }
    }

    /** {@inheritDoc} */
    public char charAt(int position) {
        return this.charBuffer.get(position);
    }

    /** {@inheritDoc} */
    public CharSequence subSequence(int start, int end) {
        return this.charBuffer.subSequence(start, end);
    }

    /** {@inheritDoc} */
    public int length() {
        return this.charBuffer.limit();
    }

    /**
     * Resets the backing <code>java.nio.CharBuffer</code>
     */
    public void resetBuffer() {
        if (this.charBuffer != null) {
            this.charBuffer.rewind();
        }
    }
}
