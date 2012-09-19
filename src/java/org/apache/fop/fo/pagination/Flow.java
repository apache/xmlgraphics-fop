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

package org.apache.fop.fo.pagination;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;

/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_flow">
 * <code>fo:flow</code></a> object.
 *
 */
public class Flow extends FObj implements CommonAccessibilityHolder {

    /** The "flow-name" property name. */
    public static final String FLOW_NAME = "flow-name";

    private String flowName;

    private CommonAccessibility commonAccessibility;

    /** used for FO validation */
    private boolean blockItemFound = false;

    /**
     * Create a Flow instance that is a child of the given {@link FONode}.
     * @param parent the {@link FONode} that is the parent of this object
     */
    public Flow(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        flowName = pList.get(PR_FLOW_NAME).getString();
        commonAccessibility = CommonAccessibility.getInstance(pList);
    }

    /** {@inheritDoc} */
    protected void startOfNode() throws FOPException {
        if (flowName == null || flowName.equals("")) {
            missingPropertyError(FLOW_NAME);
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

    /** {@inheritDoc} */
    protected void endOfNode() throws FOPException {
        if (!blockItemFound) {
            missingChildElementError("marker* (%block;)+");
        }
        getFOEventHandler().endFlow(this);
    }

    /**
     * {@inheritDoc}
     * <br>XSL Content Model: marker* (%block;)+
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
                throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (localName.equals("marker")) {
                if (blockItemFound) {
                   nodesOutOfOrderError(loc, "fo:marker", "(%block;)");
                }
            } else if (!isBlockItem(nsURI, localName)) {
                invalidChildError(loc, nsURI, localName);
            } else {
                blockItemFound = true;
            }
        }
    }

    /**
     * {@inheritDoc}
     * @return true (Flow can generate reference areas)
     */
    public boolean generatesReferenceAreas() {
        return true;
    }

    /** @return "flow-name" property. */
    public String getFlowName() {
        return flowName;
    }

    public CommonAccessibility getCommonAccessibility() {
        return commonAccessibility;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "flow";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_FLOW}
     */
    public int getNameId() {
        return FO_FLOW;
    }
}
