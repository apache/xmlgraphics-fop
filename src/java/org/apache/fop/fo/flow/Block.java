/*
 * $Id: Block.java,v 1.68 2003/03/06 11:36:31 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
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
import org.apache.fop.fo.properties.Constants;
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
     * @see org.apache.fop.fo.FObj#handleAttrs
     */
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
                                "line-height").getLength().getValue();
            this.startIndent = this.properties.get(
                                 "start-indent").getLength().getValue();
            this.endIndent = this.properties.get(
                               "end-indent").getLength().getValue();
            this.spaceBefore = this.properties.get(
                                 "space-before.optimum").getLength().getValue();
            this.spaceAfter = this.properties.get(
                                "space-after.optimum").getLength().getValue();
            this.textIndent = this.properties.get(
                                "text-indent").getLength().getValue();
            this.keepWithNext =
              this.properties.get("keep-with-next").getEnum();

            this.blockWidows =
              this.properties.get("widows").getNumber().intValue();
            this.blockOrphans =
              this.properties.get("orphans").getNumber().intValue();

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
                                bIgnore = (bPrevWasLF
                                           || lfCheck.nextIsLF());
                                break;
                            case Constants.IGNORE_IF_AFTER_LINEFEED:
                                bIgnore = bPrevWasLF;
                                break;
                        }
                        // Handle ignore
                        if (bIgnore) {
                            charIter.remove();
                        } else if (bWScollapse) {
                            if (bInWS || (lfTreatment == Constants.PRESERVE
                                        && (bPrevWasLF || lfCheck.nextIsLF()))) {
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
                                    if (bWScollapse) {
                                        bInWS = true;
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

}
