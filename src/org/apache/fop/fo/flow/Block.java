/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.apps.FOPException;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.BlockLayoutManager;
import org.apache.fop.util.CharUtilities;

import org.xml.sax.Attributes;

import java.util.List;

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

public class Block extends FObjMixed {

    int align;
    int alignLast;
    int breakAfter;
    int lineHeight;
    int startIndent;
    int endIndent;
    int spaceBefore;
    int spaceAfter;
    int textIndent;
    int keepWithNext;
    ColorType backgroundColor;
    int blockWidows;
    int blockOrphans;

    int areaHeight = 0;
    int contentWidth = 0;

    String id;
    int span;
    private int wsTreatment; //ENUMERATION
    private int lfTreatment; //ENUMERATION
    private boolean bWScollapse; //true if white-space-collapse=true

    // this may be helpful on other FOs too
    boolean anythingLaidOut = false;

    /**
     * Index of first inline-type FO seen in a sequence.
     * Used during FO tree building to do white-space handling.
     */
    private FONode firstInlineChild = null;

    public Block(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
        this.span = this.properties.get("span").getEnum();
        this.wsTreatment =
          this.properties.get("white-space-treatment").getEnum();
        this.bWScollapse =
          (this.properties.get("white-space-collapse").getEnum()
           == Constants.TRUE);
        this.lfTreatment =
          this.properties.get("linefeed-treatment").getEnum();

        setupID();

        structHandler.startBlock(this);
    }

    public void setup() {

            // Common Accessibility Properties
            AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

            // Common Aural Properties
            AuralProps mAurProps = propMgr.getAuralProps();

            // Common Border, Padding, and Background Properties
            BorderAndPadding bap = propMgr.getBorderAndPadding();
            BackgroundProps bProps = propMgr.getBackgroundProps();

            // Common Font Properties
            //this.fontState = propMgr.getFontState(area.getFontInfo());

            // Common Hyphenation Properties
            HyphenationProps mHyphProps = propMgr.getHyphenationProps();

            // Common Margin Properties-Block
            MarginProps mProps = propMgr.getMarginProps();

            // Common Relative Position Properties
            RelativePositionProps mRelProps =
              propMgr.getRelativePositionProps();

            // this.properties.get("break-after");
            // this.properties.get("break-before");
            // this.properties.get("color");
            // this.properties.get("text-depth");
            // this.properties.get("text-altitude");
            // this.properties.get("hyphenation-keep");
            // this.properties.get("hyphenation-ladder-count");
            setupID();
            // this.properties.get("keep-together");
            // this.properties.get("keep-with-next");
            // this.properties.get("keep-with-previous");
            // this.properties.get("last-line-end-indent");
            // this.properties.get("linefeed-treatment");
            // this.properties.get("line-height");
            // this.properties.get("line-height-shift-adjustment");
            // this.properties.get("line-stacking-strategy");
            // this.properties.get("orphans");
            // this.properties.get("white-space-treatment");
            // this.properties.get("span");
            // this.properties.get("text-align");
            // this.properties.get("text-align-last");
            // this.properties.get("text-indent");
            // this.properties.get("visibility");
            // this.properties.get("white-space-collapse");
            // this.properties.get("widows");
            // this.properties.get("wrap-option");
            // this.properties.get("z-index");

            this.align = this.properties.get("text-align").getEnum();
            this.alignLast =
              this.properties.get("text-align-last").getEnum();
            this.breakAfter = this.properties.get("break-after").getEnum();
            this.lineHeight = this.properties.get(
                                "line-height").getLength().mvalue();
            this.startIndent = this.properties.get(
                                 "start-indent").getLength().mvalue();
            this.endIndent = this.properties.get(
                               "end-indent").getLength().mvalue();
            this.spaceBefore = this.properties.get(
                                 "space-before.optimum").getLength().mvalue();
            this.spaceAfter = this.properties.get(
                                "space-after.optimum").getLength().mvalue();
            this.textIndent = this.properties.get(
                                "text-indent").getLength().mvalue();
            this.keepWithNext =
              this.properties.get("keep-with-next").getEnum();
            this.backgroundColor = this.properties.get(
                                     "background-color").getColorType();

            this.blockWidows =
              this.properties.get("widows").getNumber().intValue();
            this.blockOrphans =
              this.properties.get("orphans").getNumber().intValue();

    }

    public int getAreaHeight() {
        return areaHeight;
    }


    /**
     * Return the content width of the boxes generated by this FO.
     */
    public int getContentWidth() {
        return contentWidth; // getAllocationWidth()??
    }


    public int getSpan() {
        return this.span;
    }

    public void addLayoutManager(List list) {
        BlockLayoutManager blm = new BlockLayoutManager(this);
        TextInfo ti = propMgr.getTextLayoutProps(fontInfo);
        blm.setBlockTextInfo(ti);
        list.add(blm);
    }

    public boolean generatesInlineAreas() {
        return false;
    }

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

    public void end() {
        handleWhiteSpace();
        structHandler.endBlock(this);
    }

    private void handleWhiteSpace() {
        //getLogger().debug("fo:block: handleWhiteSpace");
        if (firstInlineChild != null) {
            boolean bInWS = false;
            boolean bPrevWasLF = false;
            RecursiveCharIterator charIter =
              new RecursiveCharIterator(this, firstInlineChild);
            LFchecker lfCheck = new LFchecker(charIter);

            while (charIter.hasNext()) {
                switch (CharUtilities.classOf(charIter.nextChar())) {
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
                                bIgnore = (bPrevWasLF ||
                                           lfCheck.nextIsLF());
                                break;
                            case Constants.IGNORE_IF_AFTER_LINEFEED:
                                bIgnore = bPrevWasLF;
                                break;
                        }
                        // Handle ignore
                        if (bIgnore) {
                            charIter.remove();
                        } else if (bWScollapse) {
                            if (bInWS || (lfTreatment ==
                                          Constants.PRESERVE &&
                                          (bPrevWasLF || lfCheck.nextIsLF()))) {
                                charIter.remove();
                            } else {
                                bInWS = true;
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
                                    if (bWScollapse)
                                        bInWS = true;
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
                        //   A "boundary" objects such as non-character inline
                        // or nested block object was encountered.
                        // If any whitespace run in progress, finish it.
                        // FALL THROUGH

                    case CharUtilities.UCWHITESPACE: // Non XML-whitespace
                    case CharUtilities.NONWHITESPACE:
                        /* Any other character */
                        bInWS = bPrevWasLF = false;
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
                    } else if (CharUtilities.classOf(c) !=
                        CharUtilities.XMLWHITESPACE) {
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
}

