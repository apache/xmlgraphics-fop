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

package org.apache.fop.fo.flow.table;

// XML
import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;


/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_table-caption">
 * <code>fo:table-caption</code></a> object.
 */
public class TableCaption extends FObj implements CommonAccessibilityHolder {

    private CommonAccessibility commonAccessibility;

    // The value of properties relevant for fo:table-caption.
    // Unused but valid items, commented out for performance:
    //     private CommonAural commonAural;
    //     private CommonRelativePosition commonRelativePosition;
    //     private LengthRangeProperty blockProgressionDimension;
    //     private Length height;
    //     private LengthRangeProperty inlineProgressionDimension;
    //     private int intrusionDisplace;
    //     private KeepProperty keepTogether;
    //     private Length width;
    // End of property values

    /** used for FO validation */
    private boolean blockItemFound;

    static boolean notImplementedWarningGiven;

    /**
     * Create a TableCaption instance with the given {@link FONode}
     * as parent.
     * @param parent {@link FONode} that is the parent of this object
     */
    public TableCaption(FONode parent) {
        super(parent);

        if (!notImplementedWarningGiven) {
            getFOValidationEventProducer().unimplementedFeature(this, getName(),
                    "fo:table-caption", getLocator());
            // @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
            notImplementedWarningGiven = true;
        }
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        commonAccessibility = CommonAccessibility.getInstance(pList);
    }

    /** {@inheritDoc} */
    public void endOfNode() throws FOPException {
        if (firstChild == null) {
            missingChildElementError("marker* (%block;)");
        }
    }

    /**
     * {@inheritDoc}
     * <br>XSL Content Model: marker* (%block;)
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

    /** {@inheritDoc} */
    public String getLocalName() {
        return "table-caption";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_TABLE_CAPTION}
     */
    public int getNameId() {
        return FO_TABLE_CAPTION;
    }

    /** {@inheritDoc} */
    public CommonAccessibility getCommonAccessibility() {
        return commonAccessibility;
    }

}

