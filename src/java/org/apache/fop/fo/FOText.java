/*
 * $Id: FOText.java,v 1.43 2003/03/05 21:48:01 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.fo;

// Java
import java.util.NoSuchElementException;
import java.util.List;

// FOP
import org.apache.fop.layout.TextState;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.TextLayoutManager;
import org.apache.fop.fo.properties.WhiteSpaceCollapse;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.pagination.Root;
import org.apache.fop.fo.properties.TextTransform;

/**
 * A text node in the formatting object tree.
 *
 * Unfortunately the BufferManager implementatation holds
 * onto references to the character data in this object
 * longer than the lifetime of the object itself, causing
 * excessive memory consumption and OOM errors.
 *
 * @author unascribed
 * @author <a href="mailto:mark-fop@inomial.com">Mark Lillywhite</a>
 */
public class FOText extends FObj {

    private char[] ca;
    private int start;
    private int length;
    private TextInfo textInfo;
    private TextState ts;

    /**
     * Keeps track of the last FOText object created within the current
     * block. This is used to create pointers between such objects.
     * TODO: As soon as the control hierarchy is straightened out, this static
     * variable needs to become an instance variable in some parent object,
     * probably the page-sequence.
     */
    private static FOText lastFOTextProcessed = null;

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

    private static final int IS_WORD_CHAR_FALSE = 0;
    private static final int IS_WORD_CHAR_TRUE = 1;
    private static final int IS_WORD_CHAR_MAYBE = 2;

    /**
     *
     * @param chars array of chars which contains the text in this object (may
     * be a superset of the text in this object
     * @param s starting index into char[] for the text in this object
     * @param e ending index into char[] for the text in this object
     * @param ti TextInfo object for the text in this object
     * @param parent FONode that is the parent of this object
     */
    public FOText(char[] chars, int s, int e, TextInfo ti, FONode parent) {
        super(parent);
        this.start = 0;
        this.ca = new char[e - s];
        System.arraycopy(chars, s, ca, 0, e - s);
        this.length = e - s;
        textInfo = ti;
        createBlockPointers();
        textTransform();
    }

    public void setStructHandler(StructureHandler st) {
        super.setStructHandler(st);
        structHandler.characters(ca, start, length);
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
        if (textInfo.whiteSpaceCollapse == WhiteSpaceCollapse.FALSE
                && length > 0) {
            return true;
        }

        for (int i = start; i < start + length; i++) {
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

    public void addLayoutManager(List list) {
        // if nothing left (length=0)?
        if (length == 0) {
            return;
        }

        if (length < ca.length) {
            char[] tmp = ca;
            ca = new char[length];
            System.arraycopy(tmp, 0, ca, 0, length);
        }
        LayoutManager lm = new TextLayoutManager(ca, textInfo);
        lm.setFObj(this);
        list.add(lm);
    }

    /**
     * @return a new TextCharIterator
     */
    public CharIterator charIterator() {
        return new TextCharIterator();
    }

    private class TextCharIterator extends AbstractCharIterator {
        private int curIndex = 0;

        public boolean hasNext() {
            return (curIndex < length);
        }

        public char nextChar() {
            if (curIndex < length) {
                // Just a char class? Don't actually care about the value!
                return ca[curIndex++];
            } else {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            if (curIndex > 0 && curIndex < length) {
                // copy from curIndex to end to curIndex-1
                System.arraycopy(ca, curIndex, ca, curIndex - 1,
                                 length - curIndex);
                length--;
                curIndex--;
            } else if (curIndex == length) {
                curIndex = --length;
            }
        }


        public void replaceChar(char c) {
            if (curIndex > 0 && curIndex <= length) {
                ca[curIndex - 1] = c;
            }
        }


    }

    /**
     * This method is run as part of the Constructor, to create xref pointers to
     * the previous FOText objects within the same Block
     */
    private void createBlockPointers() {
        // build pointers between the FOText objects withing the same Block
        //
        // find the ancestorBlock of the current node
        FONode ancestorFONode = this;
        while (this.ancestorBlock == null) {
            ancestorFONode = ancestorFONode.parent;
            Class myclass = ancestorFONode.getClass();
            if (ancestorFONode instanceof Root) {
                getLogger().warn("Unexpected: fo:text with no fo:block ancestor");
            }
            if (ancestorFONode instanceof Block) {
                this.ancestorBlock = (Block)ancestorFONode;
            }
        }
        // if the last FOText is a sibling, point to it, and have it point here
        if (lastFOTextProcessed != null) {
            if (lastFOTextProcessed.ancestorBlock == this.ancestorBlock) {
                prevFOTextThisBlock = lastFOTextProcessed;
                prevFOTextThisBlock.nextFOTextThisBlock = this;
            } else {
                prevFOTextThisBlock = null;
            }
        }
        // save the current node in static field so the next guy knows where
        // to look
        lastFOTextProcessed = this;
        return;
    }

    /**
     * This method is run as part of the Constructor, to handle the
     * text-transform property.
     */
    private void textTransform() {
        if (textInfo.textTransform == TextTransform.NONE) {
            return;
        }
        for (int i = 0; i < ca.length; i++) {
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
    public boolean isStartOfWord (int i) {
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
    public char getRelativeCharInBlock(int i, int offset) {
        // The easy case is where the desired character is in the same FOText
        if (((i + offset) >= 0) && ((i + offset) <= this.length)) {
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
            if ((nodeToTest.ca.length + remainingOffset) >= 0) {
                charToReturn = nodeToTest.ca[nodeToTest.ca.length + remainingOffset];
                foundChar = true;
            } else {
                remainingOffset = remainingOffset + nodeToTest.ca.length;
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
    public char charTransform(int i) {
        switch (textInfo.textTransform) {
        /* put NONE first, as this is probably the common case */
        case TextTransform.NONE:
            return ca[i];
        case TextTransform.UPPERCASE:
            return Character.toUpperCase(ca[i]);
        case TextTransform.LOWERCASE:
            return Character.toLowerCase(ca[i]);
        case TextTransform.CAPITALIZE:
            if (isStartOfWord(i)) {
                /*
                 Use toTitleCase here. Apparently, some languages use
                 a different character to represent a letter when using
                 initial caps than when all of the letters in the word
                 are capitalized. We will try to let Java handle this.
                */
                return Character.toTitleCase(ca[i]);
            } else {
                return Character.toLowerCase(ca[i]);
            }
        default:
            getLogger().warn("Invalid text-tranform value: "
                    + textInfo.textTransform);
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
    public static int isWordChar(char inputChar) {
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

}
