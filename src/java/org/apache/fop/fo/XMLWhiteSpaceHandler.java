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

import java.util.List;
import java.util.Stack;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.util.CharUtilities;

/**
 * Class encapsulating the functionality for white-space-handling
 * during refinement stage.
 * The <code>handleWhiteSpace()</code> methods are called during 
 * FOTree-building and marker-cloning:
 * <br>
 * <ul>
 * <li> from <code>FObjMixed.addChildNode()</code></li>
 * <li> from <code>FObjMixed.endOfNode()</code></li>
 * <li> from <code>FObjMixed.handleWhiteSpaceFor()</code></li>
 * </ul>
 * <br>
 * Each time one of the variants is called, white-space is handled
 * for all <code>FOText</code> or <code>Character</code> nodes that
 * were added:
 * <br>
 * <ul>
 * <li> either prior to <code>newChild</code> (and after the previous
 *      non-text child node)</li>
 * <li> or, if <code>newChild</code> is <code>null</code>,
 *      after the previous non-text child</li>
 * </ul>
 * <br>
 * The iteration always starts at <code>firstTextNode</code>, 
 * goes on until the last text-node is reached, and deals only 
 * with <code>FOText</code> or <code>Character</code> nodes.
 * <br>
 * <em>Note</em>: if the method is called from an inline's endOfNode(),
 *   there is too little context to decide whether trailing
 *   white-space may be removed, so the pending inline is stored
 *   in a List, together with an iterator for which the next()
 *   method returns the first in the trailing sequence of white-
 *   space characters. This List is processed again at the end
 *   of the ancestor block.
 */
public class XMLWhiteSpaceHandler {
    
    /** True if we are in a run of white space */
    private boolean inWhiteSpace = false;
    /** True if the last char was a linefeed */
    private boolean afterLinefeed = true;
    /** Counter, increased every time a non-white-space is encountered */
    private int nonWhiteSpaceCount;
    
    private int linefeedTreatment;
    private int whiteSpaceTreatment;
    private int whiteSpaceCollapse;
    private boolean endOfBlock;
    private boolean nextChildIsBlockLevel;
    private RecursiveCharIterator charIter;
    
    private List pendingInlines;
    private Stack nestedBlockStack = new java.util.Stack();
    private CharIterator firstWhiteSpaceInSeq;
    
    /**
     * Handle white-space for the fo that is passed in, starting at
     * firstTextNode
     * @param fo    the FO for which to handle white-space
     * @param firstTextNode the node at which to start
     * @param nextChild the node that will be added to the list
     *                  after firstTextNode
     */
    public void handleWhiteSpace(FObjMixed fo, FONode firstTextNode, FONode nextChild) {
        
        Block currentBlock = null;
        int foId = fo.getNameId();
        
        if (foId == Constants.FO_BLOCK) {
            currentBlock = (Block) fo;
            if (nestedBlockStack.isEmpty() || fo != nestedBlockStack.peek()) {
                if (nextChild != null) {
                    /* if already in a block, push the current block 
                     * onto the stack of nested blocks
                     */
                    nestedBlockStack.push(currentBlock);
                }
            } else {
                if (nextChild == null) {
                    nestedBlockStack.pop();
                }
            }
        } else if (foId == Constants.FO_RETRIEVE_MARKER) {
            /* look for the nearest block ancestor, if any */
            FONode ancestor = fo;
            do {
                ancestor = ancestor.getParent();
            } while (ancestor.getNameId() != Constants.FO_BLOCK
                    && ancestor.getNameId() != Constants.FO_STATIC_CONTENT);
            
            if (ancestor.getNameId() == Constants.FO_BLOCK) {
                currentBlock = (Block) ancestor;
                nestedBlockStack.push(currentBlock);
            }
        } else if (!nestedBlockStack.isEmpty()) {
            currentBlock = (Block) nestedBlockStack.peek();
        }
        
        if (currentBlock != null) {
            linefeedTreatment = currentBlock.getLinefeedTreatment();
            whiteSpaceCollapse = currentBlock.getWhitespaceCollapse();
            whiteSpaceTreatment = currentBlock.getWhitespaceTreatment();
        } else {
            linefeedTreatment = Constants.EN_TREAT_AS_SPACE;
            whiteSpaceCollapse = Constants.EN_TRUE;
            whiteSpaceTreatment = Constants.EN_IGNORE_IF_SURROUNDING_LINEFEED;
        }
        
        if (firstTextNode == null) {
            //nothing to do but initialize related properties
            return;
        }
        
        charIter = new RecursiveCharIterator(fo, firstTextNode);
        inWhiteSpace = false;
        
        if (fo == currentBlock
                || currentBlock == null
                || (foId == Constants.FO_RETRIEVE_MARKER
                        && fo.getParent() == currentBlock)) {
            afterLinefeed = (
                    (firstTextNode == fo.firstChild)
                        || (firstTextNode.siblings[0].getNameId()
                                == Constants.FO_BLOCK));
        }
        
        endOfBlock = (nextChild == null && fo == currentBlock);
        
        if (nextChild != null) {
            int nextChildId = nextChild.getNameId();
            nextChildIsBlockLevel = (
                    nextChildId == Constants.FO_BLOCK
                    || nextChildId == Constants.FO_TABLE_AND_CAPTION
                    || nextChildId == Constants.FO_TABLE
                    || nextChildId == Constants.FO_LIST_BLOCK
                    || nextChildId == Constants.FO_BLOCK_CONTAINER);
        } else {
            nextChildIsBlockLevel = false;
        }
        
        handleWhiteSpace();
        
        if (fo == currentBlock 
                && pendingInlines != null 
                && !pendingInlines.isEmpty()) {
            /* current FO is a block, and has pending inlines */
            if (endOfBlock || nextChildIsBlockLevel) {
                if (nonWhiteSpaceCount == 0) {
                    /* handle white-space for all pending inlines*/
                    PendingInline p;
                    for (int i = pendingInlines.size(); --i >= 0;) {
                        p = (PendingInline)pendingInlines.get(i);
                        charIter = (RecursiveCharIterator)p.firstTrailingWhiteSpace;
                        handleWhiteSpace();
                        pendingInlines.remove(p);
                    }
                } else {
                    /* there is non-white-space text between the pending
                     * inline(s) and the end of the block;
                     * clear list of pending inlines */
                    pendingInlines.clear();
                }
            }
        }
        
        if (nextChild == null) {
            if (fo != currentBlock) {
                /* current FO is not a block, and is about to end */
                if (nonWhiteSpaceCount > 0 && pendingInlines != null) {
                    /* there is non-white-space text between the pending 
                     * inline(s) and the end of the non-block node; 
                     * clear list of pending inlines */
                    pendingInlines.clear();
                }
                if (inWhiteSpace) {
                    /* means there is at least one trailing space in the
                       inline FO that is about to end */
                    addPendingInline(fo);
                }
            } else {
                /* end of block: clear the references and pop the 
                 * nested block stack */
                if (!nestedBlockStack.empty()) {
                    nestedBlockStack.pop();
                }
                charIter = null;
            }
        }
    }
    
    /**
     * Handle white-space for the fo that is passed in, starting at
     * firstTextNode (when a nested FO is encountered)
     * @param fo    the FO for which to handle white-space
     * @param firstTextNode the node at which to start
     */
    public void handleWhiteSpace(FObjMixed fo, FONode firstTextNode) {
        handleWhiteSpace(fo, firstTextNode, null);
    }
    
    private void handleWhiteSpace() {
        
        EOLchecker lfCheck = new EOLchecker(charIter);
        
        nonWhiteSpaceCount = 0;
        
        while (charIter.hasNext()) {
            if (!inWhiteSpace) {
                firstWhiteSpaceInSeq = charIter.mark();
            }
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
                    if (inWhiteSpace 
                            && whiteSpaceCollapse == Constants.EN_TRUE) {
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
                                //nothing to do now, replacement takes place later
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
                    // or nested block object was encountered. (? can't happen)
                    // If any whitespace run in progress, finish it.
                    // FALL THROUGH

                default:
                    // Any other character
                    inWhiteSpace = false;
                    afterLinefeed = false;
                    nonWhiteSpaceCount++;
                    lfCheck.reset();
                    break;
            }
        }
    }
    
    private void addPendingInline(FObjMixed fo) {
        if (pendingInlines == null) {
            pendingInlines = new java.util.ArrayList(5);
        }
        pendingInlines.add(new PendingInline(fo, firstWhiteSpaceInSeq));
    }
    
    /**
     * Helper class, used during white-space handling to look ahead, and
     * see if the next character is a linefeed (or if there will be
     * an equivalent effect during layout, i.e. end-of-block or
     * the following child is a block-level FO)
     */
    private class EOLchecker {
        private boolean nextIsEOL = false;
        private RecursiveCharIterator charIter;

        EOLchecker(CharIterator charIter) {
            this.charIter = (RecursiveCharIterator) charIter;
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
                nextIsEOL = nextChildIsBlockLevel || endOfBlock;
            }
            return nextIsEOL;
        }

        void reset() {
            nextIsEOL = false;
        }
    }
    
    /**
     * Helper class to store unfinished inline nodes together 
     * with an iterator that starts at the first white-space
     * character in the sequence of trailing white-space
     */
    private class PendingInline {
        protected FObjMixed fo;
        protected CharIterator firstTrailingWhiteSpace;
        
        PendingInline(FObjMixed fo, CharIterator firstTrailingWhiteSpace) {
            this.fo = fo;
            this.firstTrailingWhiteSpace = firstTrailingWhiteSpace;
        }
    }
}
