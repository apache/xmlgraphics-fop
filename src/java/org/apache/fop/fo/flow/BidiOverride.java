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
import java.util.ArrayList;
import java.util.List;

// XML
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.layoutmgr.BidiLayoutManager;
import org.apache.fop.layoutmgr.InlineStackingLayoutManager;
import org.apache.fop.layoutmgr.LayoutManager;

/**
 * fo:bidi-override element.
 */
public class BidiOverride extends FObjMixed {

    // used for FO validation
    private boolean blockOrInlineItemFound = false;
    private boolean canHaveBlockLevelChildren = true;

    /**
     * @param parent FONode that is the parent of this object
     */
    public BidiOverride(FONode parent) {
        super(parent);
        
       /* Check to see if this node can have block-level children.
        * See validateChildNode() below.
        */
       int lvlLeader = findAncestor(FO_LEADER);
       int lvlInCntr = findAncestor(FO_INLINE_CONTAINER);
       int lvlInline = findAncestor(FO_INLINE);
       int lvlFootnote = findAncestor(FO_FOOTNOTE);

       if (lvlLeader > 0) {
           if (lvlInCntr < 0 ||
               (lvlInCntr > 0 && lvlInCntr > lvlLeader)) {
               canHaveBlockLevelChildren = false;
           }
       } else if (lvlInline > 0 && lvlFootnote == (lvlInline + 1)) {
           if (lvlInCntr < 0 ||
           (lvlInCntr > 0 && lvlInCntr > lvlInline)) {
               canHaveBlockLevelChildren = false;
           }
       }

    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     * @todo see if can use a BitSet to determine if an FO should
     * have its ID setup; then move setupID() instances to FObj.
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: marker* (#PCDATA|%inline;|%block;)*
     * Additionally: "An fo:bidi-override that is a descendant of an fo:leader
     *  or of the fo:inline child of an fo:footnote may not have block-level
     *  children, unless it has a nearer ancestor that is an 
     *  fo:inline-container."
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws SAXParseException {
        if (nsURI == FO_URI && localName.equals("marker")) {
            if (blockOrInlineItemFound) {
               nodesOutOfOrderError(loc, "fo:marker", 
                    "(#PCDATA|%inline;|%block;)");
            }
        } else if (!isBlockOrInlineItem(nsURI, localName)) {
            invalidChildError(loc, nsURI, localName);
        } else if (!canHaveBlockLevelChildren && isBlockItem(nsURI, localName)) {
            String ruleViolated = "An fo:bidi-override" +
                " that is a descendant of an fo:leader or of the fo:inline child" +
                " of an fo:footnote may not have block-level children, unless it" +
                " has a nearer ancestor that is an fo:inline-container.";
            invalidChildError(loc, nsURI, localName, ruleViolated);
        } else {
            blockOrInlineItemFound = true;
        }
    }
    
    /**
     * @see org.apache.fop.fo.FObj#addLayoutManager(List)
     * @todo see if can/should move the child iteration logic 
     *      to BidiLayoutManager
     */
    public void addLayoutManager(List list) { 	 
        if (false) {
            super.addLayoutManager(list);
        } else {
            ArrayList childList = new ArrayList();
            super.addLayoutManager(list);
            for (int count = childList.size() - 1; count >= 0; count--) {
                LayoutManager lm = (LayoutManager) childList.get(count);
                if (lm.generatesInlineAreas()) {
                    LayoutManager blm = new BidiLayoutManager(this,
                        (InlineStackingLayoutManager) lm);
                    list.add(blm);
                } else {
                    list.add(lm);
                }
            }
        }
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:bidi-override";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_BIDI_OVERRIDE;
    }
}
