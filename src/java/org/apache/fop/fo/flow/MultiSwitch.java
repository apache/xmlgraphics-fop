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

/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_multi-switch">
 * <code>fo:multi-switch</code></a> object.
 */
public class MultiSwitch extends FObj {
    // The value of properties relevant for fo:multi-switch.
    // private ToBeImplementedProperty autoRestore;
    // Unused but valid items, commented out for performance:
    //     private CommonAccessibility commonAccessibility;
    // End of property values

    private FONode currentlyVisibleMultiCase;
    private String autoToggle;
    private String fittingStrategy;

    /**
     * Base constructor
     *
     * @param parent {@link FONode} that is the parent of this object
     */
    public MultiSwitch(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    @Override
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        autoToggle = pList.get(PR_X_AUTO_TOGGLE).getString();
        // autoRestore = pList.get(PR_AUTO_RESTORE);
    }

    /** {@inheritDoc} */
    @Override
    public void endOfNode() throws FOPException {
        if (firstChild == null) {
            missingChildElementError("(multi-case+)");
        }
        super.endOfNode();
    }

    @Override
    public void finalizeNode() throws FOPException {
        if (autoToggle.equals("best-fit")) {
            // Nothing to do in this case
            setCurrentlyVisibleNode(null);
        } else {
            FONodeIterator nodeIter = getChildNodes();
            while (nodeIter.hasNext()) {
                MultiCase multiCase = (MultiCase) nodeIter.next();
                if (multiCase.hasToggle()) {
                    multiCase.getHandler().filter(this);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * <br>XSL Content Model: (multi-case+)
     */
    @Override
    protected void validateChildNode(Locator loc, String nsURI, String localName)
                throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (!localName.equals("multi-case")) {
                invalidChildError(loc, nsURI, localName);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getLocalName() {
        return "multi-switch";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_MULTI_SWITCH}
     */
    @Override
    public int getNameId() {
        return FO_MULTI_SWITCH;
    }

    public void setCurrentlyVisibleNode(FONode node) {
        currentlyVisibleMultiCase = node;
    }

    public FONode getCurrentlyVisibleNode() {
        return currentlyVisibleMultiCase;
    }

    public String getFittingStrategy() {
        return fittingStrategy;
    }

    public String getAutoToggle() {
        return autoToggle;
    }

}
