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

/* $Id: Block.java,v 1.14 2004/04/02 13:50:52 cbowditch Exp $ */

package org.apache.fop.fo.flow;

// XML
import org.xml.sax.Attributes;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.CharIterator;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.RecursiveCharIterator;
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.fo.properties.CommonHyphenation;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.util.CharUtilities;

/*
  Modified by Mark Lillywhite mark-fop@inomial.com. The changes
  here are based on memory profiling and do not change functionality.
  Essentially, the Block object had a pointer to a BlockArea object
  that it created. The BlockArea was not referenced after the Block
  was finished except to determine the size of the BlockArea, however
  a reference to the BlockArea was maintained and this caused a lot of
  GC problems, and was a major reason for FOP memory leaks. So,
  the reference to BlockArea was made local, the required information
  is now stored (instead of a reference to the complex BlockArea object)
  and it appears that there are a lot of changes in this file, in fact
  there are only a few sematic changes; mostly I just got rid of
  "this." from blockArea since BlockArea is now local.
 */
 /**
  * Class modelling the fo:block object. See Sec. 6.5.2 of the XSL-FO Standard.
  */
public class Block extends FObjMixed {

    private int align;
    private int alignLast;
    private int breakAfter;
    private int lineHeight;
    private int startIndent;
    private int endIndent;
    private int spaceBefore;
    private int spaceAfter;
    private int textIndent;
    private int keepWithNext;
    private ColorType backgroundColor;
    private int blockWidows;
    private int blockOrphans;

    private String id;
    private int span;
    private int wsTreatment; //ENUMERATION
    private int lfTreatment; //ENUMERATION
    private boolean bWScollapse; //true if white-space-collapse=true

    // this may be helpful on other FOs too
    private boolean anythingLaidOut = false;

    /**
     * Index of first inline-type FO seen in a sequence.
     * Used during FO tree building to do white-space handling.
     */
    private FONode firstInlineChild = null;

    /**
     * @param parent FONode that is the parent of this object
     */
    public Block(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws FOPException {
        super.addProperties(attlist);
        this.span = this.propertyList.get(PR_SPAN).getEnum();
        this.wsTreatment =
          this.propertyList.get(PR_WHITE_SPACE_TREATMENT).getEnum();
        this.bWScollapse =
          (this.propertyList.get(PR_WHITE_SPACE_COLLAPSE).getEnum()
           == Constants.TRUE);
        this.lfTreatment =
          this.propertyList.get(PR_LINEFEED_TREATMENT).getEnum();

        setupID();

        getFOTreeControl().getFOInputHandler().startBlock(this);
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
            //this.fontState = propMgr.getFontState(area.getFontInfo());

            // Common Hyphenation Properties
            CommonHyphenation mHyphProps = propMgr.getHyphenationProps();

            // Common Margin Properties-Block
            CommonMarginBlock mProps = propMgr.getMarginProps();

            // Common Relative Position Properties
            CommonRelativePosition mRelProps =
              propMgr.getRelativePositionProps();

            // this.propertyList.get("break-after");
            // this.propertyList.get("break-before");
            // this.propertyList.get("color");
            // this.propertyList.get("text-depth");
            // this.propertyList.get("text-altitude");
            // this.propertyList.get("hyphenation-keep");
            // this.propertyList.get("hyphenation-ladder-count");
            setupID();
            // this.propertyList.get("keep-together");
            // this.propertyList.get("keep-with-next");
            // this.propertyList.get("keep-with-previous");
            // this.propertyList.get("last-line-end-indent");
            // this.propertyList.get("linefeed-treatment");
            // this.propertyList.get("line-height");
            // this.propertyList.get("line-height-shift-adjustment");
            // this.propertyList.get("line-stacking-strategy");
            // this.propertyList.get("orphans");
            // this.propertyList.get("white-space-treatment");
            // this.propertyList.get("span");
            // this.propertyList.get("text-align");
            // this.propertyList.get("text-align-last");
            // this.propertyList.get("text-indent");
            // this.propertyList.get("visibility");
            // this.propertyList.get("white-space-collapse");
            // this.propertyList.get("widows");
            // this.propertyList.get("wrap-option");
            // this.propertyList.get("z-index");

            this.align = this.propertyList.get(PR_TEXT_ALIGN).getEnum();
            this.alignLast =
              this.propertyList.get(PR_TEXT_ALIGN_LAST).getEnum();
            this.breakAfter = this.propertyList.get(PR_BREAK_AFTER).getEnum();
            this.lineHeight = this.propertyList.get(
                                PR_LINE_HEIGHT).getLength().getValue();
            this.startIndent = this.propertyList.get(
                                 PR_START_INDENT).getLength().getValue();
            this.endIndent = this.propertyList.get(
                               PR_END_INDENT).getLength().getValue();
            this.spaceBefore = this.propertyList.get(
                                 PR_SPACE_BEFORE | CP_OPTIMUM).getLength().getValue();
            this.spaceAfter = this.propertyList.get(
                                PR_SPACE_AFTER | CP_OPTIMUM).getLength().getValue();
            this.textIndent = this.propertyList.get(
                                PR_TEXT_INDENT).getLength().getValue();
            this.keepWithNext =
              this.propertyList.get(PR_KEEP_WITH_NEXT).getEnum();

            this.blockWidows =
              this.propertyList.get(PR_WIDOWS).getNumber().intValue();
            this.blockOrphans =
              this.propertyList.get(PR_ORPHANS).getNumber().intValue();

    }

    /**
     * @return true (Block can contain Markers)
     */
    protected boolean containsMarkers() {
        return true;
    }

    /**
     * @return span for this Block, in millipoints (??)
     */
    public int getSpan() {
        return this.span;
    }

    /**
     * @return false (Block cannot generate inline areas)
     */
    public boolean generatesInlineAreas() {
        return false;
    }

    /**
     * @see org.apache.fop.fo.FObj#addChild
     */
    public void addChild(FONode child) {
        // Handle whitespace based on values of properties
        // Handle a sequence of inline-producing children in
        // one pass
        if (child instanceof FObj && ((FObj) child).generatesInlineAreas()) {
            if (firstInlineChild == null) {
                firstInlineChild = child;
            }
            // lastInlineChild = children.size();
        } else {
            // Handle whitespace in preceeding inline areas if any
            handleWhiteSpace();
        }
        super.addChild(child);
    }

    /**
     * @see org.apache.fop.fo.FONode#end
     */
    public void end() {
        handleWhiteSpace();
        getFOTreeControl().getFOInputHandler().endBlock(this);
    }

    private void handleWhiteSpace() {
        //getLogger().debug("fo:block: handleWhiteSpace");
        if (firstInlineChild != null) {
            boolean bInWS = false;
            boolean bPrevWasLF = false;
            
            /* bSeenNonWSYet is an indicator used for trimming all leading 
               whitespace for the first inline child of the block
            */
            boolean bSeenNonWSYet = false;
            RecursiveCharIterator charIter =
              new RecursiveCharIterator(this, firstInlineChild);
            LFchecker lfCheck = new LFchecker(charIter);

            while (charIter.hasNext()) {
                char currentChar = charIter.nextChar();
                switch (CharUtilities.classOf(currentChar)) {
                    case CharUtilities.XMLWHITESPACE:
                        /* Some kind of whitespace character, except linefeed. */
                        boolean bIgnore = false;

                        switch (wsTreatment) {
                            case Constants.IGNORE:
                                bIgnore = true;
                                break;
                            case Constants.IGNORE_IF_BEFORE_LINEFEED:
                                bIgnore = lfCheck.nextIsLF();
                                break;
                            case Constants.IGNORE_IF_SURROUNDING_LINEFEED:
                                bIgnore = (bPrevWasLF
                                           || lfCheck.nextIsLF());
                                break;
                            case Constants.IGNORE_IF_AFTER_LINEFEED:
                                bIgnore = bPrevWasLF;
                                break;
                            case Constants.PRESERVE:
                                // nothing to do now, replacement takes place later
                                break;
                        }
                        // Handle ignore and replacement
                        if (bIgnore) {
                            charIter.remove();
                        } else if (bWScollapse) {
                            if (bInWS || (lfTreatment == Constants.PRESERVE
                                        && (bPrevWasLF || lfCheck.nextIsLF()))) {
                                charIter.remove();
                            } else {
                                // this is to retain a single space between words
                                bInWS = true;
                                // remove the space if no word in block 
                                // encountered yet
                                if (!bSeenNonWSYet) {
                                    charIter.remove();
                                } else {
                                    if (currentChar != '\u0020') {
                                        charIter.replaceChar('\u0020');
                                    }
                                }
                            }
                        } else {
                            // !bWScollapse
                            if (currentChar != '\u0020') {
                                charIter.replaceChar('\u0020');
                            }
                        }
                        break;

                    case CharUtilities.LINEFEED:
                        /* A linefeed */
                        lfCheck.reset();
                        bPrevWasLF = true; // for following whitespace

                        switch (lfTreatment) {
                            case Constants.IGNORE:
                                charIter.remove();
                                break;
                            case Constants.TREAT_AS_SPACE:
                                if (bInWS) {
                                    // only if bWScollapse=true
                                    charIter.remove();
                                } else {
                                    if (bWScollapse) {
                                        bInWS = true;
                                        // remove the linefeed if no word in block 
                                        // encountered yet
                                        if (!bSeenNonWSYet) {
                                            charIter.remove();
                                        }
                                    }
                                    charIter.replaceChar('\u0020');
                                }
                                break;
                            case Constants.TREAT_AS_ZERO_WIDTH_SPACE:
                                charIter.replaceChar('\u200b');
                                // Fall through: this isn't XML whitespace
                            case Constants.PRESERVE:
                                bInWS = false;
                                break;
                        }
                        break;

                    case CharUtilities.EOT:
                        // A "boundary" objects such as non-character inline
                        // or nested block object was encountered.
                        // If any whitespace run in progress, finish it.
                        // FALL THROUGH

                    case CharUtilities.UCWHITESPACE: // Non XML-whitespace
                    case CharUtilities.NONWHITESPACE:
                        /* Any other character */
                        bInWS = bPrevWasLF = false;
                        bSeenNonWSYet = true;
                        lfCheck.reset();
                        break;
                }
            }
            firstInlineChild = null;
        }
    }

    private static class LFchecker {
        private boolean bNextIsLF = false;
        private RecursiveCharIterator charIter;

        LFchecker(RecursiveCharIterator charIter) {
            this.charIter = charIter;
        }

        boolean nextIsLF() {
            if (bNextIsLF == false) {
                CharIterator lfIter = charIter.mark();
                while (lfIter.hasNext()) {
                    char c = lfIter.nextChar();
                    if (c == '\n') {
                        bNextIsLF = true;
                        break;
                    } else if (CharUtilities.classOf(c)
                            != CharUtilities.XMLWHITESPACE) {
                        break;
                    }
                }
            }
            return bNextIsLF;
        }

        void reset() {
            bNextIsLF = false;
        }
    }

    /**
     * This is a hook for an FOTreeVisitor subclass to be able to access
     * this object.
     * @param fotv the FOTreeVisitor subclass that can access this object.
     * @see org.apache.fop.fo.FOTreeVisitor
     */
    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveBlock(this);
    }

    public String getName() {
        return "fo:block";
    }

}
