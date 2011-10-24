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

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;

/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_footnote">
 * <code>fo:footnote</code></a> object.
 */
public class Footnote extends FObj implements CommonAccessibilityHolder {

    private CommonAccessibility commonAccessibility;

    private Inline footnoteCitation = null;
    private FootnoteBody footnoteBody;

    /**
     * Create a Footnote instance that is a child of the
     * given {@link FONode}
     *
     * @param parent {@link FONode} that is the parent of this object
     */
    public Footnote(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        commonAccessibility = CommonAccessibility.getInstance(pList);
    }

    /** {@inheritDoc} */
    protected void startOfNode() throws FOPException {
        getFOEventHandler().startFootnote(this);
    }

    /**
     * Make sure content model satisfied, if so then tell the
     * {@link org.apache.fop.fo.FOEventHandler} that we are at the end of the footnote.
     *
     * {@inheritDoc}
     */
    protected void endOfNode() throws FOPException {
        super.endOfNode();
        if (footnoteCitation == null || footnoteBody == null) {
            missingChildElementError("(inline,footnote-body)");
        }
        getFOEventHandler().endFootnote(this);
    }

    /**
     * {@inheritDoc}
     * <br>XSL Content Model: (inline,footnote-body)
     * TODO implement additional constraint: A fo:footnote is not permitted
     *      to have a fo:float, fo:footnote, or fo:marker as a descendant.
     * TODO implement additional constraint: A fo:footnote is not
     *      permitted to have as a descendant a fo:block-container that
     *      generates an absolutely positioned area.
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
                throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (localName.equals("inline")) {
                if (footnoteCitation != null) {
                    tooManyNodesError(loc, "fo:inline");
                }
            } else if (localName.equals("footnote-body")) {
                if (footnoteCitation == null) {
                    nodesOutOfOrderError(loc, "fo:inline", "fo:footnote-body");
                } else if (footnoteBody != null) {
                    tooManyNodesError(loc, "fo:footnote-body");
                }
            } else {
                invalidChildError(loc, nsURI, localName);
            }
        }
    }

    /** {@inheritDoc} */
    public void addChildNode(FONode child) {
        if (child.getNameId() == FO_INLINE) {
            footnoteCitation = (Inline) child;
        } else if (child.getNameId() == FO_FOOTNOTE_BODY) {
            footnoteBody = (FootnoteBody) child;
        }
    }

    public CommonAccessibility getCommonAccessibility() {
        return commonAccessibility;
    }

    /**
     * Public accessor for inline FO
     *
     * @return the {@link Inline} child
     */
    public Inline getFootnoteCitation() {
        return footnoteCitation;
    }

    /**
     * Public accessor for footnote-body FO
     *
     * @return the {@link FootnoteBody} child
     */
    public FootnoteBody getFootnoteBody() {
        return footnoteBody;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "footnote";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_FOOTNOTE}
     */
    public int getNameId() {
        return FO_FOOTNOTE;
    }
}

