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
import java.util.ArrayList;

// XML
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.ICLayoutManager;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.CommonRelativePosition;

/**
 * Class modelling the fo:inline-container object. See Sec. 6.6.8 of the XSL-FO
 * Standard.
 */
public class InlineContainer extends FObj {

    /**
     * @param parent FONode that is the parent of this object
     */
    public InlineContainer(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);
        // Common Border, Padding, and Background Properties
        CommonBorderAndPadding bap = propMgr.getBorderAndPadding();
        CommonBackground bProps = propMgr.getBackgroundProps();

        // Common Margin Properties-Inline
        CommonMarginInline mProps = propMgr.getMarginInlineProps();

        // Common Relative Position Properties
        CommonRelativePosition mRelProps =
          propMgr.getRelativePositionProps();

        // this.propertyList.get("alignment-adjust");
        // this.propertyList.get("alignment-baseline");
        // this.propertyList.get("baseline-shift");
        // this.propertyList.get("block-progression-dimension");
        // this.propertyList.get("clip");
        // this.propertyList.get("display-align");
        // this.propertyList.get("dominant-baseline");
        // this.propertyList.get("height");
        setupID();
        // this.propertyList.get("inline-progression-dimension");
        // this.propertyList.get("keep-together");
        // this.propertyList.get("keep-with-next");
        // this.propertyList.get("keep-with-previous");
        // this.propertyList.get("line-height");
        // this.propertyList.get("line-height-shift-adjustment");
        // this.propertyList.get("overflow");
        // this.propertyList.get("reference-orientation");
        // this.propertyList.get("width");
        // this.propertyList.get("writing-mode");
    }

    /**
     * @return true (InlineContainer can contain Markers)
     */
    protected boolean containsMarkers() {
        return true;
    }

    /**
     * @param list the list to which the layout manager(s) should be added
     */
    public void addLayoutManager(List list) { 	 
        ArrayList childList = new ArrayList();
        super.addLayoutManager(childList);
        LayoutManager lm = new ICLayoutManager(this, childList);
        list.add(lm);
    }

    public String getName() {
        return "fo:inline-container";
    }
}
