/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.util.List;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.Character;
import org.apache.fop.util.CharUtilities;

/**
 * Class encapsulating the functionality for white-space-handling
 * during refinement stage.
 *
 */
public class XMLWhiteSpaceHandler {
    
    // True if we are in a run of white space
    private boolean inWhiteSpace = false;
    // True if the last char was a linefeed
    private boolean afterLinefeed = true;
    
    private Block currentBlock;
    private FObj currentFO;
    private int linefeedTreatment;
    private int whiteSpaceTreatment;
    private int whiteSpaceCollapse;
    private FONode nextChild;
    private boolean endOfBlock;
    private boolean nextChildIsBlock;
    private RecursiveCharIterator charIter;
    
    private List discardableFOCharacters;
    
    /**
     * Marks a Character object as discardable, so that it is effectively
     * removed from the FOTree at the end of handleWhitespace()
     * @param foChar the Character object to be removed from the list of
     *               childNodes
     */
    public void addDiscardableFOChar(Character foChar) {
        if (discardableFOCharacters == null) {
            discardableFOCharacters = new java.util.ArrayList();
        }
        discardableFOCharacters.add(foChar);
    }
    
    /**
     * Handle white-space for the fo that is passed in, starting at
     * firstTextNode
     * @param fo    the FO for which to handle white-space
     * @param firstTextNode the node at which to start
     */
    public void handleWhiteSpace(FObjMixed fo, FONode firstTextNode) {
        if (fo.getNameId() == Constants.FO_BLOCK) {
            this.currentBlock = (Block) fo;
            this.linefeedTreatment = currentBlock.getLinefeedTreatment();
            this.whiteSpaceCollapse = currentBlock.getWhitespaceCollapse();
            this.whiteSpaceTreatment = currentBlock.getWhitespaceTreatment();
        }
        currentFO = fo;
        if (firstTextNode == null) {
            //nothing to do but initialize related properties
            return;
        }
        charIter = new RecursiveCharIterator(fo, firstTextNode);
        inWhiteSpace = false;
        if (fo.getNameId() == Constants.FO_BLOCK) {
            int textNodeIndex = -1;
            if (fo.childNodes != null) {
                textNodeIndex = fo.childNodes.indexOf(firstTextNode);
            }
            afterLinefeed = (textNodeIndex == 0
                    || ((FONode) fo.childNodes.get(textNodeIndex - 1))
                            .getNameId() == Constants.FO_BLOCK);
        }
        endOfBlock = (nextChild == null && currentFO == currentBlock);
        nextChildIsBlock = (nextChild != null 
                && nextChild.getNameId() == Constants.FO_BLOCK);
        handleWhiteSpace();
    }
    
    /**
     * Handle white-space for the fo that is passed in, starting at
     * firstTextNode (when a nested FO is encountered)
     * @param fo    the FO for which to handle white-space
     * @param firstTextNode the node at which to start
     * @param nextChild the child-node that will be added to the list after
     *                  the last text-node
     */
    public void handleWhiteSpace(FObjMixed fo, FONode firstTextNode, FONode nextChild) {
        this.nextChild = nextChild;
        handleWhiteSpace(fo, firstTextNode);
        this.nextChild = null;
    }
    
    private void handleWhiteSpace() {
        
        EOLchecker lfCheck = new EOLchecker(charIter);

        while (charIter.hasNext()) {
            char currentChar = charIter.nextChar();
            int currentCharClass = CharUtilities.classOf(currentChar);
            if (currentCharClass == CharUtilities.LINEFEED
                && linefeedTreatment == Constants.EN_TREAT_AS_SPACE) {
                // if we have a linefeed and it is supposed to be treated
                // like a space, that's what we do and continue
                currentChar = '\u0020';
                charIter.replaceChar('\u0020');
                currentCharClass = CharUtilities.classOf(currentChar);
            }
            switch (CharUtilities.classOf(currentChar)) {
                case CharUtilities.XMLWHITESPACE:
                    // Some kind of whitespace character, except linefeed.
                    if (inWhiteSpace && whiteSpaceCollapse == Constants.EN_TRUE) {
                        // We are in a run of whitespace and should collapse
                        // Just delete the char
                        charIter.remove();
                    } else {
                        // Do the white space treatment here
                        boolean bIgnore = false;

                        switch (whiteSpaceTreatment) {
                            case Constants.EN_IGNORE:
                                bIgnore = true;
                                break;
                            case Constants.EN_IGNORE_IF_BEFORE_LINEFEED:
                                bIgnore = lfCheck.beforeLinefeed();
                                break;
                            case Constants.EN_IGNORE_IF_SURROUNDING_LINEFEED:
                                bIgnore = afterLinefeed
                                           || lfCheck.beforeLinefeed();
                                break;
                            case Constants.EN_IGNORE_IF_AFTER_LINEFEED:
                                bIgnore = afterLinefeed;
                                break;
                            case Constants.EN_PRESERVE:
                                // nothing to do now, replacement takes place later
                                break;
                            default:
                                //nop
                        }
                        // Handle ignore and replacement
                        if (bIgnore) {
                            charIter.remove();
                        } else {
                            // this is to retain a single space between words
                            inWhiteSpace = true;
                            if (currentChar != '\u0020') {
                                charIter.replaceChar('\u0020');
                            }
                        }
                    }
                    break;

                case CharUtilities.LINEFEED:
                    // A linefeed
                    switch (linefeedTreatment) {
                        case Constants.EN_IGNORE:
                            charIter.remove();
                            break;
                        case Constants.EN_TREAT_AS_ZERO_WIDTH_SPACE:
                            charIter.replaceChar(CharUtilities.ZERO_WIDTH_SPACE);
                            inWhiteSpace = false;
                            break;
                        case Constants.EN_PRESERVE:
                            lfCheck.reset();
                            inWhiteSpace = false;
                            afterLinefeed = true; // for following whitespace
                            break;
                        default:
                            //nop
                    }
                    break;

                case CharUtilities.EOT:
                    // A "boundary" objects such as non-character inline
                    // or nested block object was encountered.
                    // If any whitespace run in progress, finish it.
                    // FALL THROUGH

                default:
                    // Any other character
                    inWhiteSpace = false;
                    afterLinefeed = false;
                    lfCheck.reset();
                    break;
            }
        }
        if (discardableFOCharacters != null
                && !discardableFOCharacters.isEmpty()) {
            currentFO.childNodes.removeAll(discardableFOCharacters);
            discardableFOCharacters.clear();
        }
    }
    
    private class EOLchecker {
        private boolean nextIsEOL = false;
        private RecursiveCharIterator charIter;

        EOLchecker(RecursiveCharIterator charIter) {
            this.charIter = charIter;
        }

        boolean beforeLinefeed() {
            if (!nextIsEOL) {
                CharIterator lfIter = charIter.mark();
                while (lfIter.hasNext()) {
                    int charClass = CharUtilities.classOf(lfIter.nextChar());
                    if (charClass == CharUtilities.LINEFEED) {
                        if (linefeedTreatment == Constants.EN_PRESERVE) {
                            nextIsEOL = true;
                            return nextIsEOL;
                        }
                    } else if (charClass != CharUtilities.XMLWHITESPACE) {
                        return nextIsEOL;
                    }
                }
                // No more characters == end of text run
                // means EOL if there either is a nested block to be added,
                // or if this is the last text node in the current block   
                nextIsEOL = nextChildIsBlock || endOfBlock;
            }
            return nextIsEOL;
        }

        void reset() {
            nextIsEOL = false;
        }
    }
}
