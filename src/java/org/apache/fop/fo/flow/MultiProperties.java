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

// XML
import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.ValidationException;

/**
 * Class modelling the fo:multi-properties object.
 */
public class MultiProperties extends FObj {
    // The value of properties relevant for fo:multi-properties.
    // Unused but valid items, commented out for performance:
    //     private CommonAccessibility commonAccessibility;
    // End of property values

    static boolean notImplementedWarningGiven = false;

    // used for input FO validation
    boolean hasMultiPropertySet = false;
    boolean hasWrapper = false;

    /**
     * @param parent FONode that is the parent of this object
     */
    public MultiProperties(FONode parent) {
        super(parent);

        if (!notImplementedWarningGiven) {
            getFOValidationEventProducer().unimplementedFeature(this, getName(),
                    getName(), getLocator());
            notImplementedWarningGiven = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void endOfNode() throws FOPException {
        if (!hasMultiPropertySet || !hasWrapper) {
            missingChildElementError("(multi-property-set+, wrapper)");
        }
    }

    /**
     * {@inheritDoc}
     * XSL Content Model: (multi-property-set+, wrapper)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (localName.equals("multi-property-set")) {
                if (hasWrapper) {
                    nodesOutOfOrderError(loc, "fo:multi-property-set", "fo:wrapper");
                } else {
                    hasMultiPropertySet = true;
                }
            } else if (localName.equals("wrapper")) {
                if (hasWrapper) {
                    tooManyNodesError(loc, "fo:wrapper");
                } else {
                    hasWrapper = true;
                }
            } else {
                invalidChildError(loc, nsURI, localName);
            }
        }
    }
    
    /** {@inheritDoc} */
    public String getLocalName() {
        return "multi-properties";
    }

    /**
     * {@inheritDoc}
     */
    public int getNameId() {
        return FO_MULTI_PROPERTIES;
    }
}
