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
import org.apache.fop.fo.FONode;
import org.apache.fop.layoutmgr.BasicLinkLayoutManager;

/**
 * The fo:basic-link formatting object.
 *
 * This class contains the logic to determine the link represented by this FO,
 * and whether that link is external (uses a URI) or internal (an id 
 * reference).
 */
public class BasicLink extends Inline {

    // link represented by this FO
    private String link = null;
    
    // indicator of whether link is internal or external
    private boolean isExternalLink = false;

    // used only for FO validation
    private boolean blockOrInlineItemFound = false;

    /**
     * @param parent FONode that is the parent of this object
     */
    public BasicLink(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);
        
        // This logic is for determining the link represented by this FO.
        String ext =  propertyList.get(PR_EXTERNAL_DESTINATION).getString();
        String internal = propertyList.get(PR_INTERNAL_DESTINATION).getString();

        // per spec, internal takes precedence if both specified        
        if (internal.length() > 0) { 
            link = internal;
        } else if (ext.length() > 0) {
            link = ext;
            isExternalLink = true;
        } else {
            // slightly stronger than spec "should be specified"
            attributeError("Missing attribute:  Either external-destination or " +
                "internal-destination must be specified.");
        }
        
        getFOInputHandler().startLink(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: marker* (#PCDATA|%inline;|%block;)*
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws SAXParseException {
        if (nsURI == FO_URI && localName.equals("marker")) {
            if (blockOrInlineItemFound) {
               nodesOutOfOrderError(loc, "fo:marker", "(#PCDATA|%inline;|%block;)");
            }
        } else if (!isBlockOrInlineItem(nsURI, localName)) {
            invalidChildError(loc, nsURI, localName);
        } else {
            blockOrInlineItemFound = true;
        }
    }

    /**
     * @see org.apache.fop.fo.FONode#end
     */
    protected void endOfNode() throws SAXParseException {
        super.endOfNode();
        getFOInputHandler().endLink();
    }

    /**
     * @see org.apache.fop.fo.FObj#addLayoutManager(List)
     */
    public void addLayoutManager(List list) { 	 
        BasicLinkLayoutManager lm = new BasicLinkLayoutManager(this);
        list.add(lm);
    }

    /**
     * @return link represented by this fo:basic-link
     */
    public String getLink() {
        return link;
    }
 
    /**
     * @return true if link is external, false if internal
     */
    public boolean isExternalLink() {
        return isExternalLink;
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:basic-link";
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_BASIC_LINK;
    }
}
