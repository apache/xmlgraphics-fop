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

package org.apache.fop.fo.pagination;

// Java
import java.util.ArrayList;

// XML
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FOElementMapping;
import org.apache.fop.layoutmgr.AddLMVisitor;

/**
 * Class modelling the fo:flow object. See Sec. 6.4.18 in the XSL-FO Standard.
 */
public class Flow extends FObj {

    /**
     * PageSequence container
     */
    private PageSequence pageSequence;

    /**
     * ArrayList to store snapshot
     */
    private ArrayList markerSnapshot;

    /**
     * flow-name attribute
     */
    private String flowName;

    /**
     * Content-width of current column area during layout
     */
    private int contentWidth;

    /** used for FO validation */
    private boolean blockItemFound = false;

    /**
     * @param parent FONode that is the parent of this object
     */
    public Flow(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: marker* (%block;)+
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws SAXParseException {
        if (nsURI == FOElementMapping.URI && localName.equals("marker")) {
            if (blockItemFound) {
               nodesOutOfOrderError(loc, "fo:marker", "(%block;)");
            }
        } else if (!isBlockItem(nsURI, localName)) {
            invalidChildError(loc, nsURI, localName);
        } else {
            blockItemFound = true;
        }
    }

    /**
     * Make sure content model satisfied, if so then tell the
     * StructureRenderer that we are at the end of the flow.
     * @see org.apache.fop.fo.FONode#end
     */
    protected void endOfNode() throws SAXParseException {
        if (!blockItemFound) {
            missingChildElementError("marker* (%block;)+");
        }
        getFOInputHandler().endFlow(this);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);
        if (parent.getName().equals("fo:page-sequence")) {
            this.pageSequence = (PageSequence) parent;
        } else {
            throw new SAXParseException("flow must be child of "
                                 + "page-sequence, not " + parent.getName(), locator);
        }
        // according to communication from Paul Grosso (XSL-List,
        // 001228, Number 406), confusion in spec section 6.4.5 about
        // multiplicity of fo:flow in XSL 1.0 is cleared up - one (1)
        // fo:flow per fo:page-sequence only.

        /*        if (pageSequence.isFlowSet()) {
                    if (this.name.equals("fo:flow")) {
                        throw new FOPException("Only a single fo:flow permitted"
                                               + " per fo:page-sequence");
                    } else {
                        throw new FOPException(this.name
                                               + " not allowed after fo:flow");
                    }
                }
         */
        setFlowName(getProperty(PR_FLOW_NAME).getString());
        // Now done in addChild of page-sequence
        //pageSequence.addFlow(this);

        getFOInputHandler().startFlow(this);
    }

    /**
     * @param name the name of the flow to set
     * @throws FOPException for an empty name
     */
    protected void setFlowName(String name) throws SAXParseException {
        if (name == null || name.equals("")) {
            throw new SAXParseException("A 'flow-name' is required for "
                         + getName(), locator);
        } else {
            flowName = name;
        }
    }

    /**
     * @return the name of this flow
     */
    public String getFlowName() {
        return flowName;
    }

    /**
     * @param contentWidth content width of this flow, in millipoints (??)
     */
    protected void setContentWidth(int contentWidth) {
        this.contentWidth = contentWidth;
    }
    /**
     * @return the content width of this flow (really of the region
     * in which it is flowing), in millipoints (??).
     */
    public int getContentWidth() {
        return this.contentWidth;
    }

    /**
     * @return true (Flow can generate reference areas)
     */
    public boolean generatesReferenceAreas() {
        return true;
    }

    /**
     * This is a hook for the AddLMVisitor class to be able to access
     * this object.
     * @param aLMV the AddLMVisitor object that can access this object.
     */
    public void acceptVisitor(AddLMVisitor aLMV) {
        aLMV.serveFlow(this);
    }
    
    public String getName() {
        return "fo:flow";
    }
}
