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

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;

/**
 * Base class for representation of mixed content formatting objects
 * (i.e., those that can contain both child FO's and text nodes/PCDATA).
 * It should not be instantiated directly.
 */
public abstract class FObjMixed extends FObj {
    
    /** Represents accumulated, pending FO text. See flushText(). */
    protected FOText ft = null;
    
    /** Used for white-space handling; start CharIterator at node ... */
    protected FONode currentTextNode;
    
    /** Used in creating pointers between subsequent FOText nodes
     *  in the same Block (for handling text-transform) */
    protected FOText lastFOTextProcessed = null;
    
    /**
     * @param parent FONode that is the parent of this object
     */
    protected FObjMixed(FONode parent) {
        super(parent);
    }
    
    /** {@inheritDoc} */
    protected void addCharacters(char[] data, int start, int end,
                                 PropertyList pList,
                                 Locator locator) throws FOPException {
        if (ft == null) {
            ft = new FOText(this);
            ft.setLocator(locator);
            if (!inMarker()) {
                ft.bind(pList);
            }
        }
        ft.addCharacters(data, start, end, null, null);
    }

    /** {@inheritDoc} */
    protected void endOfNode() throws FOPException {
        flushText();
        if (!inMarker()
                || getNameId() == FO_MARKER) {
            getFOEventHandler().whiteSpaceHandler
                .handleWhiteSpace(this, currentTextNode);
        }
        super.endOfNode();
    }

    /**
     * Handles white-space for the node that is passed in, 
     * starting at its current text-node
     * (used by RetrieveMarker to trigger 'end-of-node' white-space
     *  handling)
     * @param fobj  the node for which to handle white-space
     */
    protected static void handleWhiteSpaceFor(FObjMixed fobj) {
        fobj.getFOEventHandler().getXMLWhiteSpaceHandler()
            .handleWhiteSpace(fobj, fobj.currentTextNode);
    }
    
    /**
     * Adds accumulated text as one FOText instance, unless
     * the one instance's char array contains more than 
     * Short.MAX_VALUE characters. In the latter case the 
     * instance is split up into more manageable chunks.
     * 
     * @throws FOPException if there is a problem during processing
     */
    protected void flushText() throws FOPException {
        if (ft != null) {
            FOText lft = ft;
            /* make sure nested calls to itself have no effect */
            ft = null;
            FOText tmpText;
            int indexStart = 0;
            int indexEnd = (lft.ca.length > Short.MAX_VALUE 
                            ? Short.MAX_VALUE : lft.ca.length) - 1;
            int charCount = 0;
            short tmpSize;
            while (charCount < lft.ca.length) {
                tmpSize = (short) (indexEnd - indexStart + 1);
                charCount += tmpSize;
                tmpText = (FOText) lft.clone(this, false);
                tmpText.ca = new char[tmpSize];
                tmpText.startIndex = 0;
                tmpText.endIndex = tmpSize;
                System.arraycopy(lft.ca, indexStart, 
                                tmpText.ca, 0, indexEnd - indexStart + 1);
                if (getNameId() == FO_BLOCK) {
                    tmpText.createBlockPointers((org.apache.fop.fo.flow.Block) this);
                    this.lastFOTextProcessed = tmpText;
                } else if (getNameId() != FO_MARKER
                        && getNameId() != FO_TITLE
                        && getNameId() != FO_BOOKMARK_TITLE) {
                    FONode fo = parent;
                    int foNameId = fo.getNameId();
                    while (foNameId != FO_BLOCK
                            && foNameId != FO_MARKER
                            && foNameId != FO_TITLE
                            && foNameId != FO_BOOKMARK_TITLE
                            && foNameId != FO_PAGE_SEQUENCE) {
                        fo = fo.getParent();
                        foNameId = fo.getNameId();
                    }
                    if (foNameId == FO_BLOCK) {
                        tmpText.createBlockPointers((org.apache.fop.fo.flow.Block) fo);
                        ((FObjMixed) fo).lastFOTextProcessed = tmpText;
                    } else if (foNameId == FO_PAGE_SEQUENCE) {
                        log.error("Could not create block pointers."
                                + " FOText w/o Block ancestor.");
                    }
                }
                tmpText.endOfNode();
                addChildNode(tmpText);
                indexStart = indexEnd + 1;
                indexEnd = (((lft.ca.length - charCount) < Short.MAX_VALUE)
                    ? lft.ca.length : charCount + Short.MAX_VALUE) - 1;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void addChildNode(FONode child) throws FOPException {
        flushText();
        if (!inMarker()) {
            if (child instanceof FOText || child.getNameId() == FO_CHARACTER) {
                if (currentTextNode == null) {
                    currentTextNode = child;
                }
            } else {
                // handle white-space for all text up to here
                getFOEventHandler().whiteSpaceHandler
                    .handleWhiteSpace(this, currentTextNode, child);
                currentTextNode = null;
            }
        }
        super.addChildNode(child);
    }
    
    /**
     * @return iterator for this object
     */
    public CharIterator charIterator() {
        return new RecursiveCharIterator(this);
    }    
}