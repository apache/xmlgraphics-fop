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
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fonts.Font;
import org.apache.fop.layoutmgr.PageNumberLayoutManager;

/**
 * Class modelling the fo:page-number object.
 */
public class PageNumber extends FObj {
    /** FontState for this object */
    protected Font fontState;

    private float red;
    private float green;
    private float blue;
    private int wrapOption;

    /**
     * @param parent FONode that is the parent of this object
     */
    public PageNumber(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: empty
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws SAXParseException {
            invalidChildError(loc, nsURI, localName);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);

        // Common Font Properties
        this.fontState = propMgr.getFontState(getFOInputHandler().getFontInfo());

        ColorType c = this.propertyList.get(PR_COLOR).getColorType();
        this.red = c.getRed();
        this.green = c.getGreen();
        this.blue = c.getBlue();

        this.wrapOption = this.propertyList.get(PR_WRAP_OPTION).getEnum();

        getFOInputHandler().startPageNumber(this);
    }

    /**
     * @return the FontState object for this PageNumber
     */
    public Font getFontState() {
        return fontState;
    }

    protected void endOfNode() throws SAXParseException {
        getFOInputHandler().endPageNumber(this);
    }
    
    /**
     * @see org.apache.fop.fo.FObj#addLayoutManager(List)
     */
    public void addLayoutManager(List list) { 	 
        PageNumberLayoutManager lm = new PageNumberLayoutManager(this);
        list.add(lm);
    }

    public String getName() {
        return "fo:page-number";
    }
    
    public int getNameId() {
        return FO_PAGE_NUMBER;
    }
}
