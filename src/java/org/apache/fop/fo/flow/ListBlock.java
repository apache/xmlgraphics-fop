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

// XML
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.layoutmgr.AddLMVisitor;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.CommonRelativePosition;

/**
 * Class modelling the fo:list-block object. See Sec. 6.8.2 of the XSL-FO
 * Standard.
 */
public class ListBlock extends FObj {

    private int align;
    private int alignLast;
    private int breakBefore;
    private int breakAfter;
    private int lineHeight;
    private int startIndent;
    private int endIndent;
    private int spaceBefore;
    private int spaceAfter;
    private int spaceBetweenListRows = 0;
    private ColorType backgroundColor;

    /**
     * @param parent FONode that is the parent of this object
     */
    public ListBlock(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);
        setupID();

        this.align = this.propertyList.get(PR_TEXT_ALIGN).getEnum();
        this.alignLast = this.propertyList.get(PR_TEXT_ALIGN_LAST).getEnum();
        this.lineHeight =
            this.propertyList.get(PR_LINE_HEIGHT).getLength().getValue();
        this.startIndent =
            this.propertyList.get(PR_START_INDENT).getLength().getValue();
        this.endIndent =
            this.propertyList.get(PR_END_INDENT).getLength().getValue();
        this.spaceBefore =
            this.propertyList.get(PR_SPACE_BEFORE | CP_OPTIMUM).getLength().getValue();
        this.spaceAfter =
            this.propertyList.get(PR_SPACE_AFTER | CP_OPTIMUM).getLength().getValue();
        this.spaceBetweenListRows = 0;    // not used at present
        this.backgroundColor =
            this.propertyList.get(PR_BACKGROUND_COLOR).getColorType();
        getFOInputHandler().startList(this);
    }

    /**
     * @return false (ListBlock does not generate inline areas)
     */
    public boolean generatesInlineAreas() {
        return false;
    }

    /**
     * @return true (ListBlock can contain Markers)
     */
    protected boolean containsMarkers() {
        return true;
    }

    /**
     * This is a hook for the AddLMVisitor class to be able to access
     * this object.
     * @param aLMV the AddLMVisitor object that can access this object.
     */
    public void acceptVisitor(AddLMVisitor aLMV) {
        aLMV.serveListBlock(this);
    }

    protected void endOfNode() throws SAXParseException {
        super.endOfNode();
        getFOInputHandler().endList(this);
    }
    
    public String getName() {
        return "fo:list-block";
    }
}

