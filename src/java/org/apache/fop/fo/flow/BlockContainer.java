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

// FOP
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.properties.CommonAbsolutePosition;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.layoutmgr.BlockContainerLayoutManager;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

/**
 * Class modelling the fo:block-container object. See Sec. 6.5.3 of the XSL-FO
 * Standard.
 */
public class BlockContainer extends FObj {

    private ColorType backgroundColor;
    private int position;

    private int top;
    private int bottom;
    private int left;
    private int right;
    private int width;
    private int height;

    private int span;

    /**
     * @param parent FONode that is the parent of this object
     */
    public BlockContainer(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);
        this.span = this.propertyList.get(PR_SPAN).getEnum();
        setupID();
    }

    private void setup() {

            // Common Accessibility Properties
            CommonAbsolutePosition mAbsProps = propMgr.getAbsolutePositionProps();

            // Common Border, Padding, and Background Properties
            CommonBorderAndPadding bap = propMgr.getBorderAndPadding();
            CommonBackground bProps = propMgr.getBackgroundProps();

            // Common Margin-Block Properties
            CommonMarginBlock mProps = propMgr.getMarginProps();

            // this.propertyList.get("block-progression-dimension");
            // this.propertyList.get("break-after");
            // this.propertyList.get("break-before");
            // this.propertyList.get("clip");
            // this.propertyList.get("display-align");
            // this.propertyList.get("height");
            setupID();
            // this.propertyList.get("keep-together");
            // this.propertyList.get("keep-with-next");
            // this.propertyList.get("keep-with-previous");
            // this.propertyList.get("overflow");
            // this.propertyList.get("reference-orientation");
            // this.propertyList.get("span");
            // this.propertyList.get("width");
            // this.propertyList.get("writing-mode");

            this.backgroundColor =
                this.propertyList.get(PR_BACKGROUND_COLOR).getColorType();

            this.width = this.propertyList.get(PR_WIDTH).getLength().getValue();
            this.height = this.propertyList.get(PR_HEIGHT).getLength().getValue();
            span = this.propertyList.get(PR_SPAN).getEnum();

    }

    /**
     * @return true (BlockContainer can generate Reference Areas)
     */
    public boolean generatesReferenceAreas() {
        return true;
    }

    /**
     * @return false (BlockContainer cannot generate inline areas)
     */
    public boolean generatesInlineAreas() {
        return false;
    }

    /**
     * @return the span for this object
     */
    public int getSpan() {
        return this.span;
    }

    /**
     * @see org.apache.fop.fo.FObj#addLayoutManager(List)
     */
    public void addLayoutManager(List list) { 	 
        BlockContainerLayoutManager blm = new BlockContainerLayoutManager(this);
        blm.setOverflow(getProperty(PR_OVERFLOW).getEnum());
        list.add(blm);
    }

    public String getName() {
        return "fo:block-container";
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_BLOCK_CONTAINER;
    }
}

