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
import java.awt.Color;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.fo.properties.SpaceProperty;

/**
 * Class modelling the fo:initial-property-set object.
 */
public class InitialPropertySet extends FObj {
    // The value of properties relevant for fo:initial-property-set.
    private String id;
    // private ToBeImplementedProperty letterSpacing;
    private SpaceProperty lineHeight;
    // private ToBeImplementedProperty textShadow;
    // Unused but valid items, commented out for performance:
    //     private CommonAccessibility commonAccessibility;
    //     private CommonAural commonAural;
    //     private CommonBorderPaddingBackground commonBorderPaddingBackground;
    //     private CommonFont commonFont;
    //     private CommonRelativePosition commonRelativePosition;
    //     private Color color;
    //     private int scoreSpaces;
    //     private int textDecoration;
    //     private int textTransform;
    //     private SpaceProperty wordSpacing;
    // End of property values

    /**
     * @param parent FONode that is the parent of this object
     */
    public InitialPropertySet(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        id = pList.get(PR_ID).getString();
        // letterSpacing = pList.get(PR_LETTER_SPACING);
        lineHeight = pList.get(PR_LINE_HEIGHT).getSpace();
        // textShadow = pList.get(PR_TEXT_SHADOW);
    }

    /**
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws FOPException {
        checkId(id);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: empty
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
            invalidChildError(loc, nsURI, localName);
    }

    /**
     * @return the "line-height" property.
     */
    public SpaceProperty getLineHeight() {
        return lineHeight;
    }

    /** @see org.apache.fop.fo.FONode#getLocalName() */
    public String getLocalName() {
        return "initial-property-set";
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_INITIAL_PROPERTY_SET;
    }
}
