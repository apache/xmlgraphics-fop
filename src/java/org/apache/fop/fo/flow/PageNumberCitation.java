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

import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.fonts.Font;
import org.apache.fop.layoutmgr.LayoutContext; 	 
import org.apache.fop.layoutmgr.LayoutManager; 	 
import org.apache.fop.layoutmgr.LeafNodeLayoutManager; 	 
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.area.PageViewport; 	 
import org.apache.fop.area.Resolveable; 	 
import org.apache.fop.area.Trait; 	 
import org.apache.fop.area.inline.InlineArea; 	 
import org.apache.fop.area.inline.UnresolvedPageNumber; 	 
import org.apache.fop.area.inline.TextArea;

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
     * @param str string to be measured
     * @return width (in millipoints ??) of the string
     */
    public int getStringWidth(String str) {
        int width = 0;
        for (int count = 0; count < str.length(); count++) {
            width += fontState.getCharWidth(str.charAt(count));
        }
        return width;
    }

    private void setup() {

        // Common Accessibility Properties
        CommonAccessibility mAccProps = propMgr.getAccessibilityProps();

        // Common Aural Properties
        CommonAural mAurProps = propMgr.getAuralProps();

        // Common Border, Padding, and Background Properties
        CommonBorderAndPadding bap = propMgr.getBorderAndPadding();
        CommonBackground bProps = propMgr.getBackgroundProps();

        // Common Font Properties
        this.fontState = propMgr.getFontState(getFOInputHandler().getFontInfo());

        // Common Margin Properties-Inline
        CommonMarginInline mProps = propMgr.getMarginInlineProps();

        // Common Relative Position Properties
        CommonRelativePosition mRelProps =
          propMgr.getRelativePositionProps();

        setupID();

        ColorType c = this.propertyList.get(PR_COLOR).getColorType();
        this.red = c.getRed();
        this.green = c.getGreen();
        this.blue = c.getBlue();

        this.wrapOption = this.propertyList.get(PR_WRAP_OPTION).getEnum();
        this.refId = this.propertyList.get(PR_REF_ID).getString();

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

    public String getName() {
        return "fo:page-number-citation";
    }

    /**
     * @see org.apache.fop.fo.FObj#addLayoutManager(List)
     * @todo create a subclass for LeafNodeLayoutManager, moving the formatting
     *  logic to the layoutmgr package
     */
    public void addLayoutManager(List list) { 	 
        setup();
        LayoutManager lm;
        lm = new LeafNodeLayoutManager(this) {
                 public InlineArea get(LayoutContext context) {
                     curArea = getPageNumberCitationInlineArea(parentLM);
                     return curArea;
                 }
    
                 public void addAreas(PositionIterator posIter,
                                      LayoutContext context) {
                     super.addAreas(posIter, context);
                     if (getUnresolved()) {
                         parentLM.addUnresolvedArea(getRefId(),
                                                    (Resolveable) curArea);
                     }
                 }
    
                 protected void offsetArea(LayoutContext context) {
                     curArea.setOffset(context.getBaseline());
                 }
             };
        list.add(lm); 	 
    }

     // if id can be resolved then simply return a word, otherwise
     // return a resolveable area
     public InlineArea getPageNumberCitationInlineArea(LayoutManager parentLM) {
         if (getRefId().equals("")) {
             getLogger().error("page-number-citation must contain \"ref-id\"");
             return null;
         }
         PageViewport page = parentLM.resolveRefID(getRefId());
         InlineArea inline = null;
         if (page != null) {
             String str = page.getPageNumber();
             // get page string from parent, build area
             TextArea text = new TextArea();
             inline = text;
             int width = getStringWidth(str);
             text.setTextArea(str);
             inline.setIPD(width);
             inline.setHeight(getFontState().getAscender()
                              - getFontState().getDescender());
             inline.setOffset(getFontState().getAscender());

             inline.addTrait(Trait.FONT_NAME, getFontState().getFontName());
             inline.addTrait(Trait.FONT_SIZE,
                             new Integer(getFontState().getFontSize()));
             setUnresolved(false);
         } else {
             setUnresolved(true);
             inline = new UnresolvedPageNumber(getRefId());
             String str = "MMM"; // reserve three spaces for page number
             int width = getStringWidth(str);
             inline.setIPD(width);
             inline.setHeight(getFontState().getAscender()
                              - getFontState().getDescender());
             inline.setOffset(getFontState().getAscender());

             inline.addTrait(Trait.FONT_NAME, getFontState().getFontName());
             inline.addTrait(Trait.FONT_SIZE,
                             new Integer(getFontState().getFontSize()));
         }
         return inline;
     }
     
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_PAGE_NUMBER_CITATION;
    }
}
