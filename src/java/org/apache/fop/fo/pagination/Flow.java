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
import java.util.List;

// XML
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.layoutmgr.FlowLayoutManager;

/**
 * Class modelling the fo:flow object.
 * @todo check need for markerSnapshot, contentWidth
 */
public class Flow extends FObj {

    /**
     * ArrayList to store snapshot
     */
    private ArrayList markerSnapshot;

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
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);

        // check flow_name property
        String flowName = getPropString(PR_FLOW_NAME);

        if (flowName == null || flowName.equals("")) {
            missingPropertyError("flow-name");
        }

        getFOEventHandler().startFlow(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: marker* (%block;)+
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws SAXParseException {
        if (nsURI == FO_URI && localName.equals("marker")) {
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
     * FOEventHandler that we are at the end of the flow.
     * @see org.apache.fop.fo.FONode#end
     */
    protected void endOfNode() throws SAXParseException {
        if (!blockItemFound) {
            missingChildElementError("marker* (%block;)+");
        }
        getFOEventHandler().endFlow(this);
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
     * @see org.apache.fop.fo.FObj#addLayoutManager(List)
     */
    public void addLayoutManager(List list) { 	 
        FlowLayoutManager lm = new FlowLayoutManager(this);
        list.add(lm);
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:flow";
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_FLOW;
    }
}
