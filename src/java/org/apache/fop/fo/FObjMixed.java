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
 * Abstract base class for representation of mixed content formatting objects
 * (= those that can contain both child {@link FONode}s and <code>#PCDATA</code>).
 */
public abstract class FObjMixed extends FObj {

    /** Represents accumulated, pending FO text. See {@link #flushText()}. */
    private FOText ft = null;

    /** Used for white-space handling; start CharIterator at node ... */
    protected FONode currentTextNode;

    /** Used in creating pointers between subsequent {@link FOText} nodes
     *  in the same {@link org.apache.fop.fo.flow.Block}
     *  (for handling text-transform) */
    protected FOText lastFOTextProcessed = null;

    /**
     * Base constructor
     *
     * @param parent FONode that is the parent of this object
     */
    protected FObjMixed(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    @Override
    protected void characters(char[] data, int start, int length,
                                 PropertyList pList,
                                 Locator locator) throws FOPException {
        if (ft == null) {
            ft = new FOText(this);
            ft.setLocator(locator);
            if (!inMarker()) {
                ft.bind(pList);
            }
        }
        ft.characters(data, start, length, null, null);
    }

    /** {@inheritDoc} */
    @Override
    protected void endOfNode() throws FOPException {

        super.endOfNode();
        if (!inMarker() || getNameId() == FO_MARKER) {
            // send character[s]() events to the FOEventHandler
            sendCharacters();
        }

    }

    /**
     * Handles white-space for the node that is passed in,
     * starting at its current text-node
     * (used by {@link org.apache.fop.fo.flow.RetrieveMarker}
     *  to trigger 'end-of-node' white-space handling)
     *
     * @param fobj  the node for which to handle white-space
     * @param nextChild the next child to be added
     */
    protected static void handleWhiteSpaceFor(FObjMixed fobj, FONode nextChild) {
        fobj.getBuilderContext().getXMLWhiteSpaceHandler()
            .handleWhiteSpace(fobj, fobj.currentTextNode, nextChild);
    }

    /**
     * Creates block-pointers between subsequent FOText nodes
     * in the same Block. (used for handling text-transform)
     *
     * TODO: !! Revisit: does not take into account fo:characters !!
     *
     * @throws FOPException if there is a problem during processing
     */
    private void flushText() throws FOPException {
        if (ft != null) {
            FOText lft = ft;
            /* make sure nested calls to itself have no effect */
            ft = null;
            if (getNameId() == FO_BLOCK) {
                lft.createBlockPointers((org.apache.fop.fo.flow.Block) this);
                this.lastFOTextProcessed = lft;
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
                    lft.createBlockPointers((org.apache.fop.fo.flow.Block) fo);
                    ((FObjMixed) fo).lastFOTextProcessed = lft;
                } else if (foNameId == FO_PAGE_SEQUENCE
                            && lft.willCreateArea()) {
                    log.error("Could not create block pointers."
                            + " FOText w/o Block ancestor.");
                }
            }
            this.addChildNode(lft);
        }
    }

    private void sendCharacters() throws FOPException {

        if (this.currentTextNode != null) {
            FONodeIterator nodeIter
                    = this.getChildNodes(this.currentTextNode);
            FONode node;
            while (nodeIter.hasNext()) {
                node = nodeIter.nextNode();
                assert (node instanceof FOText
                        || node.getNameId() == FO_CHARACTER);
                if (node.getNameId() == FO_CHARACTER) {
                    node.startOfNode();
                }
                node.endOfNode();
            }
        }
        this.currentTextNode = null;
    }

    /** {@inheritDoc} */
    @Override
    protected void addChildNode(FONode child) throws FOPException {

        flushText();
        if (!inMarker()) {
            if (child instanceof FOText || child.getNameId() == FO_CHARACTER) {
                if (this.currentTextNode == null) {
                    this.currentTextNode = child;
                }
            } else {
                // handle white-space for all text up to here
                handleWhiteSpaceFor(this, child);
                // send character[s]() events to the FOEventHandler
                sendCharacters();
            }
        }
        super.addChildNode(child);
    }

    /** {@inheritDoc} */
    @Override
    public void removeChild(FONode child) {
        super.removeChild(child);
        if (child == this.currentTextNode) {
            // reset to following sibling
            this.currentTextNode = child.siblings != null ? child.siblings[1] : null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void finalizeNode() throws FOPException {

        flushText();
        if (!inMarker() || getNameId() == FO_MARKER) {
            handleWhiteSpaceFor(this, null);
        }

    }

    /**
     * Returns a {@link CharIterator} over this FO's character content
     *
     * @return iterator for this object
     */
    @Override
    public CharIterator charIterator() {
        return new RecursiveCharIterator(this);
    }
}