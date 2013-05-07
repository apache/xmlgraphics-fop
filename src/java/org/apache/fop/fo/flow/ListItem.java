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

import java.util.Stack;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.complexscripts.bidi.DelimitedTextRange;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.BreakPropertySet;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.KeepProperty;

/**
 * Class modelling the <a href=http://www.w3.org/TR/xsl/#fo_list-item">
 * <code>fo:list-item</code></a> object.
 */
public class ListItem extends FObj implements BreakPropertySet, CommonAccessibilityHolder {
    // The value of properties relevant for fo:list-item.
    private CommonAccessibility commonAccessibility;
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private CommonMarginBlock commonMarginBlock;
    private int breakAfter;
    private int breakBefore;
    private KeepProperty keepTogether;
    private KeepProperty keepWithNext;
    private KeepProperty keepWithPrevious;
    // Unused but valid items, commented out for performance:
    //     private CommonAural commonAural;
    //     private CommonRelativePosition commonRelativePosition;
    //     private int intrusionDisplace;
    //     private int relativeAlign;
    // End of property values

    private ListItemLabel label = null;
    private ListItemBody body = null;

    /**
     * Base constructor
     *
     * @param parent {@link FONode} that is the parent of this object
     */
    public ListItem(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        commonAccessibility = CommonAccessibility.getInstance(pList);
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        commonMarginBlock = pList.getMarginBlockProps();
        breakAfter = pList.get(PR_BREAK_AFTER).getEnum();
        breakBefore = pList.get(PR_BREAK_BEFORE).getEnum();
        keepTogether = pList.get(PR_KEEP_TOGETHER).getKeep();
        keepWithNext = pList.get(PR_KEEP_WITH_NEXT).getKeep();
        keepWithPrevious = pList.get(PR_KEEP_WITH_PREVIOUS).getKeep();
    }

    /** {@inheritDoc} */
    public void startOfNode() throws FOPException {
        super.startOfNode();
        getFOEventHandler().startListItem(this);
    }

    /** {@inheritDoc} */
    public void endOfNode() throws FOPException {
        if (label == null || body == null) {
            missingChildElementError("marker* (list-item-label,list-item-body)");
        }
        getFOEventHandler().endListItem(this);
    }

    /**
     * {@inheritDoc}
     * <br>XSL Content Model: marker* (list-item-label,list-item-body)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
        throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (localName.equals("marker")) {
                if (label != null) {
                    nodesOutOfOrderError(loc, "fo:marker", "fo:list-item-label");
                }
            } else if (localName.equals("list-item-label")) {
                if (label != null) {
                    tooManyNodesError(loc, "fo:list-item-label");
                }
            } else if (localName.equals("list-item-body")) {
                if (label == null) {
                    nodesOutOfOrderError(loc, "fo:list-item-label", "fo:list-item-body");
                } else if (body != null) {
                    tooManyNodesError(loc, "fo:list-item-body");
                }
            } else {
                invalidChildError(loc, nsURI, localName);
            }
        }
    }

    /**
     * {@inheritDoc}
     * TODO see if can/should rely on base class for this
     *    (i.e., add to childNodes instead)
     */
    public void addChildNode(FONode child) {
        int nameId = child.getNameId();

        if (nameId == FO_LIST_ITEM_LABEL) {
            label = (ListItemLabel) child;
        } else if (nameId == FO_LIST_ITEM_BODY) {
            body = (ListItemBody) child;
        } else if (nameId == FO_MARKER) {
            addMarker((Marker) child);
        }
    }

    /** {@inheritDoc} */
    public CommonAccessibility getCommonAccessibility() {
        return commonAccessibility;
    }

    /** @return the {@link CommonMarginBlock} */
    public CommonMarginBlock getCommonMarginBlock() {
        return commonMarginBlock;
    }

    /** @return the {@link CommonBorderPaddingBackground} */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    }

    /** @return the "break-after" property */
    public int getBreakAfter() {
        return breakAfter;
    }

    /** @return the "break-before" property */
    public int getBreakBefore() {
        return breakBefore;
    }

    /** @return the "keep-with-next" property */
    public KeepProperty getKeepWithNext() {
        return keepWithNext;
    }

    /** @return the "keep-with-previous" property  */
    public KeepProperty getKeepWithPrevious() {
        return keepWithPrevious;
    }

    /** @return the "keep-together" property  */
    public KeepProperty getKeepTogether() {
        return keepTogether;
    }

    /** @return the label of the list item */
    public ListItemLabel getLabel() {
        return label;
    }

    /**
     * @return the body of the list item
     */
    public ListItemBody getBody() {
        return body;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "list-item";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_LIST_ITEM}
     */
    public int getNameId() {
        return FO_LIST_ITEM;
    }

    @Override
    protected Stack collectDelimitedTextRanges(Stack ranges, DelimitedTextRange currentRange) {
        ListItemLabel label = getLabel();
        if (label != null) {
            ranges = label.collectDelimitedTextRanges(ranges);
        }
        ListItemBody body = getBody();
        if (body != null) {
            ranges = body.collectDelimitedTextRanges(ranges);
        }
        return ranges;
    }

}

