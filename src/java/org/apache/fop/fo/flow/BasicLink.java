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

import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.StructureTreeElementHolder;

/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_basic-link">
 * <code>fo:basic-link</code></a> object.
 *
 * This class contains the logic to determine the link represented by this FO,
 * and whether that link is external (uses a URI) or internal (an id
 * reference).
 */
public class BasicLink extends InlineLevel implements StructureTreeElementHolder {

    // The value of properties relevant for fo:basic-link.
    private Length alignmentAdjust;
    private int alignmentBaseline;
    private Length baselineShift;
    private int dominantBaseline;
    private StructureTreeElement structureTreeElement;
    // private ToBeImplementedProperty destinationPlacementOffset;
    private String externalDestination;
    // private ToBeImplementedProperty indicateDestination;
    private String internalDestination;
    private int showDestination;
    // private ToBeImplementedProperty targetProcessingContext;
    // private ToBeImplementedProperty targetPresentationContext;
    // private ToBeImplementedProperty targetStylesheet;
    // Unused but valid items, commented out for performance:
    //     private int dominantBaseline;
    // End of property values

    // used only for FO validation
    private boolean blockOrInlineItemFound;

    /**
     * Construct a BasicLink instance with the given {@link FONode}
     * as its parent.
     *
     * @param parent {@link FONode} that is the parent of this object
     */
    public BasicLink(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        alignmentAdjust = pList.get(PR_ALIGNMENT_ADJUST).getLength();
        alignmentBaseline = pList.get(PR_ALIGNMENT_BASELINE).getEnum();
        baselineShift = pList.get(PR_BASELINE_SHIFT).getLength();
        dominantBaseline = pList.get(PR_DOMINANT_BASELINE).getEnum();
        // destinationPlacementOffset = pList.get(PR_DESTINATION_PLACEMENT_OFFSET);
        externalDestination = pList.get(PR_EXTERNAL_DESTINATION).getString();
        // indicateDestination = pList.get(PR_INDICATE_DESTINATION);
        internalDestination = pList.get(PR_INTERNAL_DESTINATION).getString();
        showDestination = pList.get(PR_SHOW_DESTINATION).getEnum();
        // targetProcessingContext = pList.get(PR_TARGET_PROCESSING_CONTEXT);
        // targetPresentationContext = pList.get(PR_TARGET_PRESENTATION_CONTEXT);
        // targetStylesheet = pList.get(PR_TARGET_STYLESHEET);

        // per spec, internal takes precedence if both specified
        if (internalDestination.length() > 0) {
            externalDestination = null;
        } else if (externalDestination.length() == 0) {
            // slightly stronger than spec "should be specified"
            getFOValidationEventProducer().missingLinkDestination(this, getName(), locator);
        }
    }

    /** {@inheritDoc} */
    public void startOfNode() throws FOPException {
        super.startOfNode();
        getFOEventHandler().startLink(this);
    }

    /** {@inheritDoc} */
    public void endOfNode() throws FOPException {
        super.endOfNode();
        getFOEventHandler().endLink(this);
    }

    /** {@inheritDoc} */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
                throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (localName.equals("marker")) {
                if (blockOrInlineItemFound) {
                   nodesOutOfOrderError(loc, "fo:marker", "(#PCDATA|%inline;|%block;)");
                }
            } else if (!isBlockOrInlineItem(nsURI, localName)) {
                invalidChildError(loc, nsURI, localName);
            } else {
                blockOrInlineItemFound = true;
            }
        }
    }

    /** @return the "alignment-adjust" property */
    public Length getAlignmentAdjust() {
        return alignmentAdjust;
    }

    /** @return the "alignment-baseline" property */
    public int getAlignmentBaseline() {
        return alignmentBaseline;
    }

    /** @return the "baseline-shift" property */
    public Length getBaselineShift() {
        return baselineShift;
    }

    /** @return the "dominant-baseline" property */
    public int getDominantBaseline() {
        return dominantBaseline;
    }

    @Override
    public void setStructureTreeElement(StructureTreeElement structureTreeElement) {
        this.structureTreeElement = structureTreeElement;
    }

    @Override
    public StructureTreeElement getStructureTreeElement() {
        return structureTreeElement;
    }

    /**
     * Get the value of the <code>internal-destination</code> property.
     *
     * @return the "internal-destination" property
     */
    public String getInternalDestination() {
        return internalDestination;
    }

    /**
     * Get the value of the <code>external-destination</code> property.
     *
     * @return the "external-destination" property
     */
    public String getExternalDestination() {
        return externalDestination;
    }

    /**
     * Convenience method to check if this instance has an internal destination.
     *
     * @return <code>true</code> if this basic link has an internal destination;
     *          <code>false</code> otherwise
     */
    public boolean hasInternalDestination() {
        return internalDestination != null && internalDestination.length() > 0;
    }

    /**
     * Convenience method to check if this instance has an external destination
     *
     * @return <code>true</code> if this basic link has an external destination;
     *          <code>false</code> otherwise
     */
    public boolean hasExternalDestination() {
        return externalDestination != null && externalDestination.length() > 0;
    }

    /**
     * Get the value of the <code>show-destination</code> property.
     *
     * @return the "show-destination" property
     */
    public int getShowDestination() {
        return this.showDestination;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "basic-link";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_BASIC_LINK}
     */
    public int getNameId() {
        return FO_BASIC_LINK;
    }
}
