/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

package org.apache.fop.fo.flow;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonAccessibility;

/**
 * Class modelling the fo:footnote object.
 */
public class Footnote extends FObj {
    // The value of properties relevant for fo:footnote.
    private CommonAccessibility commonAccessibility;
    // End of property values

    private Inline footnoteCitation = null;
    private FootnoteBody footnoteBody;

    /**
     * @param parent FONode that is the parent of this object
     */
    public Footnote(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        commonAccessibility = pList.getAccessibilityProps();
    }
    
    /**
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws FOPException {
        getFOEventHandler().startFootnote(this);
    }

    /**
     * Make sure content model satisfied, if so then tell the
     * FOEventHandler that we are at the end of the flow.
     * @see org.apache.fop.fo.FONode#endOfNode
     */
    protected void endOfNode() throws FOPException {
        super.endOfNode();
        if (footnoteCitation == null || footnoteBody == null) {
            missingChildElementError("(inline,footnote-body)");
        }
        getFOEventHandler().endFootnote(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: (inline,footnote-body)
     * @todo implement additional constraint: A fo:footnote is not permitted
     *      to have a fo:float, fo:footnote, or fo:marker as a descendant.
     * @todo implement additional constraint: A fo:footnote is not 
     *      permitted to have as a descendant a fo:block-container that 
     *      generates an absolutely positioned area.
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
            if (FO_URI.equals(nsURI) && localName.equals("inline")) {
                if (footnoteCitation != null) {
                    tooManyNodesError(loc, "fo:inline");
                }
            } else if (FO_URI.equals(nsURI) && localName.equals("footnote-body")) {
                if (footnoteCitation == null) {
                    nodesOutOfOrderError(loc, "fo:inline", "fo:footnote-body");
                } else if (footnoteBody != null) {
                    tooManyNodesError(loc, "fo:footnote-body");
                }                
            } else {
                invalidChildError(loc, nsURI, localName);
            }
    }

    /**
     * @see org.apache.fop.fo.FONode#addChildNode(FONode)
     */
    public void addChildNode(FONode child) {
        if (((FObj)child).getNameId() == FO_INLINE) {
            footnoteCitation = (Inline) child;
        } else if (((FObj)child).getNameId() == FO_FOOTNOTE_BODY) {
            footnoteBody = (FootnoteBody) child;
        }
    }

    /**
     * Public accessor for inline FO
     * @return the Inline child
     */
    public Inline getFootnoteCitation() {
        return footnoteCitation;
    }

    /**
     * Public accessor for footnote-body FO
     * @return the FootnoteBody child
     */
    public FootnoteBody getFootnoteBody() {
        return footnoteBody;
    }

    /** @see org.apache.fop.fo.FONode#getLocalName() */
    public String getLocalName() {
        return "footnote";
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_FOOTNOTE;
    }
}

