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

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.layoutmgr.FlowLayoutManager;

/**
 * Class modelling the fo:flow object.
 * @todo check need for markerSnapshot, contentWidth
 */
public class Flow extends FObj {
    // The value of properties relevant for fo:flow.
    private String flowName;
    // End of property values
    
    /**
     * ArrayList to store snapshot
     */
    private ArrayList markerSnapshot;

    /** used for FO validation */
    private boolean blockItemFound = false;

    /**
     * @param parent FONode that is the parent of this object
     */
    public Flow(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        flowName = pList.get(PR_FLOW_NAME).getString();
    }
    
    /**
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws FOPException {
        if (flowName == null || flowName.equals("")) {
            missingPropertyError("flow-name");
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
        // Now done in addChild of page-sequence
        //pageSequence.addFlow(this);
        getFOEventHandler().startFlow(this);
    }

    /**
     * Make sure content model satisfied, if so then tell the
     * FOEventHandler that we are at the end of the flow.
     * @see org.apache.fop.fo.FONode#endOfNode
     */
    protected void endOfNode() throws FOPException {
        if (!blockItemFound) {
            missingChildElementError("marker* (%block;)+");
        }
        getFOEventHandler().endFlow(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: marker* (%block;)+
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
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
     * @return true (Flow can generate reference areas)
     */
    public boolean generatesReferenceAreas() {
        return true;
    }

    /**
     * Return "flow-name" property.
     */
    public String getFlowName() {
        return flowName;
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
