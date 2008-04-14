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
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.KeepProperty;

/**
 * Class modelling the fo:list-block object.
 */
public class ListBlock extends FObj {
    // The value of properties relevant for fo:list-block.
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private CommonMarginBlock commonMarginBlock;
    private int breakAfter;
    private int breakBefore;
    private KeepProperty keepTogether;
    private KeepProperty keepWithNext;
    private KeepProperty keepWithPrevious;
    // Unused but valid items, commented out for performance:
    //     private CommonAccessibility commonAccessibility;
    //     private CommonAural commonAural;
    //     private CommonRelativePosition commonRelativePosition;
    //     private int intrusionDisplace;
    //     private Length provisionalDistanceBetweenStarts;
    //     private Length provisionalLabelSeparation;
    // End of property values

    /** extension properties */
    private Length widowContentLimit;
    private Length orphanContentLimit;
    
    // used for child node validation
    private boolean hasListItem = false;

    /**
     * @param parent FONode that is the parent of this object
     */
    public ListBlock(FONode parent) {
        super(parent);
    }

    /**
     * {@inheritDoc}
     */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        commonMarginBlock = pList.getMarginBlockProps();
        breakAfter = pList.get(PR_BREAK_AFTER).getEnum();
        breakBefore = pList.get(PR_BREAK_BEFORE).getEnum();
        keepTogether = pList.get(PR_KEEP_TOGETHER).getKeep();
        keepWithNext = pList.get(PR_KEEP_WITH_NEXT).getKeep();
        keepWithPrevious = pList.get(PR_KEEP_WITH_PREVIOUS).getKeep();
        //Bind extension properties
        widowContentLimit = pList.get(PR_X_WIDOW_CONTENT_LIMIT).getLength();
        orphanContentLimit = pList.get(PR_X_ORPHAN_CONTENT_LIMIT).getLength();
    }

    /**
     * {@inheritDoc}
     */
    protected void startOfNode() throws FOPException {
        super.startOfNode();
        getFOEventHandler().startList(this);
    }
    
    /**
     * Make sure content model satisfied, if so then tell the
     * FOEventHandler that we are at the end of the flow.
     * {@inheritDoc}
     */
    protected void endOfNode() throws FOPException {
        if (!hasListItem) {
            missingChildElementError("marker* (list-item)+");
        }
        getFOEventHandler().endList(this);
    }

    /**
     * {@inheritDoc}
     * XSL Content Model: marker* (list-item)+
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
            throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (localName.equals("marker")) {
                if (hasListItem) {
                    nodesOutOfOrderError(loc, "fo:marker", "fo:list-item");
                }
            } else if (localName.equals("list-item")) {
                hasListItem = true;
            } else {
                invalidChildError(loc, nsURI, localName);
            }
        }
    }

    /**
     * @return the Common Margin Properties-Block.
     */
    public CommonMarginBlock getCommonMarginBlock() {
        return commonMarginBlock;
    }

    /**
     * @return the Common Border, Padding, and Background Properties.
     */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    }

    /**
     * @return the "break-after" property.
     */
    public int getBreakAfter() {
        return breakAfter;
    }

    /**
     * @return the "break-before" property.
     */
    public int getBreakBefore() {
        return breakBefore;
    }

    /** @return the "keep-with-next" property.  */
    public KeepProperty getKeepWithNext() {
        return keepWithNext;
    }

    /** @return the "keep-with-previous" property.  */
    public KeepProperty getKeepWithPrevious() {
        return keepWithPrevious;
    }

    /** @return the "keep-together" property.  */
    public KeepProperty getKeepTogether() {
        return keepTogether;
    }

    /** @return the "fox:widow-content-limit" extension property */
    public Length getWidowContentLimit() {
        return widowContentLimit;
    }

    /** @return the "fox:orphan-content-limit" extension property */
    public Length getOrphanContentLimit() {
        return orphanContentLimit;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "list-block";
    }
    
    /** {@inheritDoc} */
    public int getNameId() {
        return FO_LIST_BLOCK;
    }
}

