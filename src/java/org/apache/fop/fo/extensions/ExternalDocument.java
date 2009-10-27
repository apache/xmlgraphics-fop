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

package org.apache.fop.fo.extensions;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.GraphicsProperties;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.pagination.AbstractPageSequence;
import org.apache.fop.fo.properties.LengthRangeProperty;

/**
 * Class for the fox:external-document extension element.
 */
public class ExternalDocument extends AbstractPageSequence implements GraphicsProperties {

    // The value of properties relevant for fox:external-document
    private LengthRangeProperty blockProgressionDimension;
    private Length contentHeight;
    private Length contentWidth;
    private int displayAlign;
    private Length height;
    private LengthRangeProperty inlineProgressionDimension;
    private int overflow;
    private int scaling;
    private String src;
    private int textAlign;
    private Length width;
    // Unused but valid items, commented out for performance:
    //     private CommonAccessibility commonAccessibility;
    //     private CommonAural commonAural;
    //     private String contentType;
    //     private int scalingMethod;
    // End of property values

    /**
     * Constructs a ExternalDocument object (called by Maker).
     * @param parent the parent formatting object
     */
    public ExternalDocument(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        blockProgressionDimension = pList.get(PR_BLOCK_PROGRESSION_DIMENSION).getLengthRange();
        contentHeight = pList.get(PR_CONTENT_HEIGHT).getLength();
        contentWidth = pList.get(PR_CONTENT_WIDTH).getLength();
        displayAlign = pList.get(PR_DISPLAY_ALIGN).getEnum();
        height = pList.get(PR_HEIGHT).getLength();
        inlineProgressionDimension = pList.get(PR_INLINE_PROGRESSION_DIMENSION).getLengthRange();
        overflow = pList.get(PR_OVERFLOW).getEnum();
        scaling = pList.get(PR_SCALING).getEnum();
        textAlign = pList.get(PR_TEXT_ALIGN).getEnum();
        width = pList.get(PR_WIDTH).getLength();
        src = pList.get(PR_SRC).getString();

        if (this.src == null || this.src.length() == 0) {
            missingPropertyError("src");
        }
    }

    protected void startOfNode() throws FOPException {
        super.startOfNode();
        getFOEventHandler().startExternalDocument(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#endOfNode
     */
    protected void endOfNode() throws FOPException {
        getFOEventHandler().endExternalDocument(this);
        super.endOfNode();
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
        XSL/FOP: empty
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
        throws ValidationException {
            invalidChildError(loc, nsURI, localName);
    }

    /**
     * Returns the src attribute (the URI to the embedded document).
     * @return the src attribute
     */
    public String getSrc() {
        return this.src;
    }

    /** {@inheritDoc} */
    public LengthRangeProperty getInlineProgressionDimension() {
        return inlineProgressionDimension;
    }

    /** {@inheritDoc} */
    public LengthRangeProperty getBlockProgressionDimension() {
        return blockProgressionDimension;
    }

    /** {@inheritDoc} */
    public Length getHeight() {
        return height;
    }

    /** {@inheritDoc} */
    public Length getWidth() {
        return width;
    }

    /** {@inheritDoc} */
    public Length getContentHeight() {
        return contentHeight;
    }

    /** {@inheritDoc} */
    public Length getContentWidth() {
        return contentWidth;
    }

    /** {@inheritDoc} */
    public int getScaling() {
        return scaling;
    }

    /** {@inheritDoc} */
    public int getOverflow() {
        return overflow;
    }

    /** {@inheritDoc} */
    public int getDisplayAlign() {
        return displayAlign;
    }

    /** {@inheritDoc} */
    public int getTextAlign() {
        return textAlign;
    }

    /** @see org.apache.fop.fo.FONode#getNamespaceURI() */
    public String getNamespaceURI() {
        return ExtensionElementMapping.URI;
    }

    /** @see org.apache.fop.fo.FONode#getNormalNamespacePrefix() */
    public String getNormalNamespacePrefix() {
        return "fox";
    }

    /** @see org.apache.fop.fo.FONode#getLocalName() */
    public String getLocalName() {
        return "external-document";
    }

}

