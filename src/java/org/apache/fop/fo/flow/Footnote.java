/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

// Java
import java.util.List;

// XML
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;

/**
 * Class modelling the fo:footnote object. See Sec. 6.10.3 of the XSL-FO
 * Standard.
 */
public class Footnote extends FObj {

    private Inline inlineFO = null;
    private FootnoteBody footnoteBody;

    /**
     * @param parent FONode that is the parent of this object
     */
    public Footnote(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);
        getFOInputHandler().startFootnote(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: (inline,footnote-body)
     * @todo: implement additional constraint: An fo:footnote is not permitted
     *      to have an fo:float, fo:footnote, or fo:marker as a descendant.
     * @todo: implement additional constraint: An an fo:footnote is not 
     *      permitted to have as a descendant an fo:block-container that 
     *      generates an absolutely positioned area.
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws SAXParseException {
            if (nsURI == FO_URI && localName.equals("inline")) {
                if (inlineFO != null) {
                    tooManyNodesError(loc, "fo:inline");
                }
            } else if (nsURI == FO_URI && localName.equals("footnote-body")) {
                if (inlineFO == null) {
                    nodesOutOfOrderError(loc, "fo:inline", "fo:footnote-body");
                } else if (footnoteBody != null) {
                    tooManyNodesError(loc, "fo:footnote-body");
                }                
            } else {
                invalidChildError(loc, nsURI, localName);
            }
    }

    /**
     * Make sure content model satisfied, if so then tell the
     * FOInputHandler that we are at the end of the flow.
     * @see org.apache.fop.fo.FONode#end
     */
    protected void endOfNode() throws SAXParseException {
        super.endOfNode();
        if (inlineFO == null || footnoteBody == null) {
            missingChildElementError("(inline,footnote-body)");
        }
        getFOInputHandler().endFootnote(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#addChildNode(FONode)
     */
    public void addChildNode(FONode child) {
        if (((FObj)child).getNameId() == FO_INLINE) {
            inlineFO = (Inline) child;
        } else if (((FObj)child).getNameId() == FO_FOOTNOTE_BODY) {
            footnoteBody = (FootnoteBody) child;
        }
    }

    /**
     * Public accessor for inline FO
     * @return the Inline object stored as inline FO
     */
    public Inline getInlineFO() {
        return inlineFO;
    }

    /**
     * @see org.apache.fop.fo.FObj#addLayoutManager(List)
     */
    public void addLayoutManager(List list) { 	 
        if (getInlineFO() == null) {
            getLogger().error("inline required in footnote");
            return;
        }
        getInlineFO().addLayoutManager(list);
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:footnote";
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_FOOTNOTE;
    }
}

