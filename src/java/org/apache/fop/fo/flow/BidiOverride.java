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

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.ColorTypeProperty;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.fo.properties.SpaceProperty;
import org.apache.fop.layoutmgr.BidiLayoutManager;
import org.apache.fop.layoutmgr.InlineLayoutManager;
import org.apache.fop.layoutmgr.LayoutManager;

/**
 * fo:bidi-override element.
 */
public class BidiOverride extends FObjMixed {

    // used for FO validation
    private boolean blockOrInlineItemFound = false;
    private boolean canHaveBlockLevelChildren = true;

    // The value of properties relevant for fo:bidi-override.
    private CommonAural commonAural;
    private CommonFont commonFont;
    private CommonRelativePosition commonRelativePosition;
    private ColorTypeProperty prColor;
    // private ToBeImplementedProperty prDirection;
    // private ToBeImplementedProperty prLetterSpacing;
    private Length prLineHeight;
    // private ToBeImplementedProperty prScoreSpaces;
    // private ToBeImplementedProperty prUnicodeBidi;
    private SpaceProperty prWordSpacing;
    // End of property values

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
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        commonAural = pList.getAuralProps();
        commonFont = pList.getFontProps();
        commonRelativePosition = pList.getRelativePositionProps();
        prColor = pList.get(PR_COLOR).getColorType();
        // prDirection = pList.get(PR_DIRECTION);
        // prLetterSpacing = pList.get(PR_LETTER_SPACING);
        prLineHeight = pList.get(PR_LINE_HEIGHT).getLength();
        // prScoreSpaces = pList.get(PR_SCORE_SPACES);
        // prUnicodeBidi = pList.get(PR_UNICODE_BIDI);
        prWordSpacing = pList.get(PR_WORD_SPACING).getSpace();
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
        throws ValidationException {
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
