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

package org.apache.fop.fo.flow;

// Java
import java.util.List;

// XML
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.layoutmgr.table.Row;

/**
 * Class modelling the fo:table-row object.
 */
public class TableRow extends FObj {
    // The value of properties relevant for fo:table-row.
    private CommonAccessibility commonAccessibility;
    private LengthRangeProperty blockProgressionDimension;
    private CommonAural commonAural;
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private CommonRelativePosition commonRelativePosition;
    // private ToBeImplementedProperty borderAfterPrecedence;
    // private ToBeImplementedProperty borderBeforePrecedence;
    // private ToBeImplementedProperty borderEndPrecedence;
    // private ToBeImplementedProperty borderStartPrecedence;
    private int breakAfter;
    private int breakBefore;
    private Length height;
    private String id;
    private KeepProperty keepTogether;
    private KeepProperty keepWithNext;
    private KeepProperty keepWithPrevious;
    // private ToBeImplementedProperty visibility;
    // End of property values
    
    private boolean setup = false;

    /**
     * @param parent FONode that is the parent of this object
     */
    public TableRow(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) {
        commonAccessibility = pList.getAccessibilityProps();
        blockProgressionDimension = pList.get(PR_BLOCK_PROGRESSION_DIMENSION).getLengthRange();
        commonAural = pList.getAuralProps();
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        commonRelativePosition = pList.getRelativePositionProps();
        // borderAfterPrecedence = pList.get(PR_BORDER_AFTER_PRECEDENCE);
        // borderBeforePrecedence = pList.get(PR_BORDER_BEFORE_PRECEDENCE);
        // borderEndPrecedence = pList.get(PR_BORDER_END_PRECEDENCE);
        // borderStartPrecedence = pList.get(PR_BORDER_START_PRECEDENCE);
        breakAfter = pList.get(PR_BREAK_AFTER).getEnum();
        breakBefore = pList.get(PR_BREAK_BEFORE).getEnum();
        id = pList.get(PR_ID).getString();
        height = pList.get(PR_HEIGHT).getLength();
        keepTogether = pList.get(PR_KEEP_TOGETHER).getKeep();
        keepWithNext = pList.get(PR_KEEP_WITH_NEXT).getKeep();
        keepWithPrevious = pList.get(PR_KEEP_WITH_PREVIOUS).getKeep();
        // visibility = pList.get(PR_VISIBILITY);
    }

    /**
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws SAXParseException {
        checkId(id);
        getFOEventHandler().startRow(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#endOfNode
     */
    protected void endOfNode() throws SAXParseException {
        if (childNodes == null) {
            missingChildElementError("(table-cell+)");
        }
        getFOEventHandler().endRow(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: (table-cell+)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws SAXParseException {
        if (!(nsURI == FO_URI && localName.equals("table-cell"))) {
            invalidChildError(loc, nsURI, localName);
        }
    }

    /**
     * Return the "id" property.
     */
    public String getId() {
        return id;
    }

    /**
     * Return the "keep-with-previous" property.
     */
    public KeepProperty getKeepWithPrevious() {
        return keepWithPrevious;
    }

    /**
     * Return the "keep-with-next" property.
     */
    public KeepProperty getKeepWithNext() {
        return keepWithNext;
    }

    /**
     * Return the "keep-together" property.
     */
    public KeepProperty getKeepTogether() {
        return keepTogether;
    }

    /**
     * Return the "height" property.
     */
    public Length getHeight() {
        return height;
    }

    /**
     * Return the Common Border, Padding, and Background Properties.
     */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    }

    /**
     * @see org.apache.fop.fo.FONode#addLayoutManager(List)
     */
    public void addLayoutManager(List list) {
        Row rlm = new Row(this);
        list.add(rlm);
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:table-row";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_TABLE_ROW;
    }
}
