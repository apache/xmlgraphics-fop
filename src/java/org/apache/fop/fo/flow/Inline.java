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
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.fo.CharIterator;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.InlineCharIterator;

/**
 * Class modelling the fo:inline formatting object.
 */
public class Inline extends FObjMixed {

    // used for FO validation
    private boolean blockOrInlineItemFound = false;
    private boolean canHaveBlockLevelChildren = true;

    /**
     * @param parent FONode that is the parent of this object
     */
    public Inline(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);

       /* Check to see if this node can have block-level children.
        * See validateChildNode() below.
        */
       int lvlLeader = findAncestor(FO_LEADER);
       int lvlFootnote = findAncestor(FO_FOOTNOTE);
       int lvlInCntr = findAncestor(FO_INLINE_CONTAINER);

       if (lvlLeader > 0) {
           if (lvlInCntr < 0 ||
               (lvlInCntr > 0 && lvlInCntr > lvlLeader)) {
               canHaveBlockLevelChildren = false;
           }
       } else if (lvlFootnote > 0) {
           if (lvlInCntr < 0 || lvlInCntr > lvlFootnote) {
               canHaveBlockLevelChildren = false;
           }
       }

        getFOInputHandler().startInline(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: marker* (#PCDATA|%inline;|%block;)*
     * Additionally: " An fo:inline that is a descendant of an fo:leader
     *  or fo:footnote may not have block-level children, unless it has a
     *  nearer ancestor that is an fo:inline-container." (paraphrased)
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
            String ruleViolated = 
                " An fo:inline that is a descendant of an fo:leader" +
                " or fo:footnote may not have block-level children," +
                " unless it has a nearer ancestor that is an" +
                " fo:inline-container.";
            invalidChildError(loc, nsURI, localName, ruleViolated);
        } else {
            blockOrInlineItemFound = true;
        }
    }


    /**
     * @see org.apache.fop.fo.FONode#end
     */
    protected void endOfNode() throws SAXParseException {
        getFOInputHandler().endInline(this);
    }

    /**
     * @see org.apache.fop.fo.FObjMixed#charIterator
     */
    public CharIterator charIterator() {
        return new InlineCharIterator(this, propMgr.getBorderAndPadding());
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:inline";
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_INLINE;
    }
}
