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
import org.apache.fop.layoutmgr.PageNumberCitationLayoutManager;

/**
 * Class modelling the fo:page-number-citation object. See Sec. 6.6.11 of the
 * XSL-FO Standard.
 * This inline fo is replaced with the text for a page number.
 * The page number used is the page that contains the start of the
 * block referenced with the ref-id attribute.
 */
public class PageNumberCitation extends FObj {
    /** Fontstate for this object **/
    protected Font fontState;

    private float red;
    private float green;
    private float blue;
    private int wrapOption;
    private String pageNumber;
    private String refId;
    private boolean unresolved = false;

    /**
     * @param parent FONode that is the parent of this object
     */
    public PageNumberCitation(FONode parent) {
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
     * @todo switch this method to addProperties()
     */
    private void setup() {
        // Common Font Properties
        this.fontState = propMgr.getFontState(getFOInputHandler().getFontInfo());

        ColorType c = this.propertyList.get(PR_COLOR).getColorType();
        this.red = c.getRed();
        this.green = c.getGreen();
        this.blue = c.getBlue();

        this.wrapOption = this.propertyList.get(PR_WRAP_OPTION).getEnum();
        this.refId = getPropString(PR_REF_ID);

        if (this.refId.equals("")) {
            //throw new FOPException("page-number-citation must contain \"ref-id\"");
        }

    }

    public String getRefId() {
        return refId;
    }

    public boolean getUnresolved() {
        return unresolved;
    }

    public void setUnresolved(boolean isUnresolved) {
        unresolved = isUnresolved;
    }

    public Font getFontState() {
        return fontState;
    }

    /**
     * @see org.apache.fop.fo.FObj#addLayoutManager(List)
     */
    public void addLayoutManager(List list) { 	 
        setup();
        PageNumberCitationLayoutManager lm = 
            new PageNumberCitationLayoutManager(this);
        list.add(lm); 	 
    }
     
    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:page-number-citation";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_PAGE_NUMBER_CITATION;
    }
}
