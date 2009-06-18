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
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;

/**
 * The fo:basic-link formatting object.
 *
 * This class contains the logic to determine the link represented by this FO,
 * and whether that link is external (uses a URI) or internal (an id 
 * reference).
 */
public class BasicLink extends Inline {
    // The value of properties relevant for fo:basic-link.
    // private ToBeImplementedProperty destinationPlacementOffset;
    private int dominantBaseline;
    private String externalDestination;
    // private ToBeImplementedProperty indicateDestination;
    private String internalDestination;
    // private ToBeImplementedProperty showDestination;
    // private ToBeImplementedProperty targetProcessingContext;
    // private ToBeImplementedProperty targetPresentationContext;
    // private ToBeImplementedProperty targetStylesheet;
    // End of property values

    // used only for FO validation
    private boolean blockOrInlineItemFound = false;

    /**
     * @param parent FONode that is the parent of this object
     */
    public BasicLink(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        // destinationPlacementOffset = pList.get(PR_DESTINATION_PLACEMENT_OFFSET);
        dominantBaseline = pList.get(PR_DOMINANT_BASELINE).getEnum();
        externalDestination = pList.get(PR_EXTERNAL_DESTINATION).getString();
        // indicateDestination = pList.get(PR_INDICATE_DESTINATION);
        internalDestination = pList.get(PR_INTERNAL_DESTINATION).getString();
        // showDestination = pList.get(PR_SHOW_DESTINATION);
        // targetProcessingContext = pList.get(PR_TARGET_PROCESSING_CONTEXT);
        // targetPresentationContext = pList.get(PR_TARGET_PRESENTATION_CONTEXT);
        // targetStylesheet = pList.get(PR_TARGET_STYLESHEET);

        // per spec, internal takes precedence if both specified        
        if (internalDestination.length() > 0) { 
            externalDestination = null;
        } else if (externalDestination.length() == 0) {
            // slightly stronger than spec "should be specified"
            attributeError("Missing attribute:  Either external-destination or " +
                "internal-destination must be specified.");
        }
    }

    /**
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws FOPException {
        super.startOfNode();
        getFOEventHandler().startLink(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#endOfNode
     */
    protected void endOfNode() throws FOPException {
        super.endOfNode();
        getFOEventHandler().endLink();
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: marker* (#PCDATA|%inline;|%block;)*
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
        if (FO_URI.equals(nsURI) && localName.equals("marker")) {
            if (blockOrInlineItemFound) {
               nodesOutOfOrderError(loc, "fo:marker", "(#PCDATA|%inline;|%block;)");
            }
        } else if (!isBlockOrInlineItem(nsURI, localName)) {
            invalidChildError(loc, nsURI, localName);
        } else {
            blockOrInlineItemFound = true;
        }
    }

    /**
     * @return the "internal-destination" property.
     */
    public String getInternalDestination() {
        return internalDestination;
    }

    /**
     * @return the "external-destination" property.
     */
    public String getExternalDestination() {
        return externalDestination;
    }

    /** @see org.apache.fop.fo.FObj#getLocalName() */
    public String getLocalName() {
        return "basic-link";
    }
    
    /** @see org.apache.fop.fo.FObj#getNameId() */
    public int getNameId() {
        return FO_BASIC_LINK;
    }
}
