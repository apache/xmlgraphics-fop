/*
 * $Id: PropertyManager.java,v 1.17 2003/03/05 20:38:27 jeremias Exp $
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
package org.apache.fop.fo;

// Java
import java.text.MessageFormat;

// FOP
import org.apache.fop.fonts.Font;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.fo.properties.CommonAbsolutePosition;
import org.apache.fop.traits.BlockProps;
import org.apache.fop.traits.InlineProps;
import org.apache.fop.traits.SpaceVal;
import org.apache.fop.traits.LayoutProps; // keep, break, span, space?
import org.apache.fop.fo.properties.Span;
import org.apache.fop.fonts.FontMetrics;
import org.apache.fop.fo.properties.CommonHyphenation;

/**
 * Helper class for managing groups of properties.
 */
public class PropertyManager implements Constants {

    private PropertyList propertyList;
    private FOTreeControl foTreeControl = null;
    private Font fontState = null;
    private CommonBorderAndPadding borderAndPadding = null;
    private CommonHyphenation hyphProps = null;
    private TextInfo textInfo = null;

    private static final String[] SA_BEFORE = new String[]{"before"};
    private static final String[] SA_AFTER = new String[]{"after"};
    private static final String[] SA_START = new String[]{"start"};
    private static final String[] SA_END = new String[]{"end"};

    private static final MessageFormat MSGFMT_COLOR = new MessageFormat("border-{0}-color");
    private static final MessageFormat MSGFMT_STYLE = new MessageFormat("border-{0}-style");
    private static final MessageFormat MSGFMT_WIDTH = new MessageFormat("border-{0}-width");
    private static final MessageFormat MSGFMT_PADDING = new MessageFormat("padding-{0}");

    private static final String NONE = "none";

    /**
     * Main constructor
     * @param pList property list
     */
    public PropertyManager(PropertyList pList) {
        this.propertyList = pList;
    }

    /**
     * Returns the property list that is used for lookup.
     * @return the property list
     */
    public PropertyList getPropertyList() {
        return propertyList;
    }

    /**
     * Sets the Document object telling the property manager which fonts are
     * available.
     * @param foTreeControl foTreeControl implementation containing font
     * information
     */
    public void setFontInfo(FOTreeControl foTreeControl) {
        this.foTreeControl = foTreeControl;
    }


    /**
     * Constructs a FontState object. If it was constructed before it is
     * reused.
     * @param foTreeControl FOTreeControl implementation containing the font
     * information
     * @return a FontState object
     */
    public Font getFontState(FOTreeControl foTreeControl) {
        if (fontState == null) {
            if (foTreeControl == null) {
                foTreeControl = this.foTreeControl;
            } else if (this.foTreeControl == null) {
                this.foTreeControl = foTreeControl;
            }
            /**@todo this is ugly. need to improve. */

            String fontFamily = propertyList.get(PR_FONT_FAMILY).getString();
            String fontStyle = propertyList.get(PR_FONT_STYLE).getString();
            String fw = propertyList.get(PR_FONT_WEIGHT).getString();
            int fontWeight = 400;
            if (fw.equals("bolder")) {
                // +100 from inherited
            } else if (fw.equals("lighter")) {
                // -100 from inherited
            } else {
                try {
                    fontWeight = Integer.parseInt(fw);
                } catch (NumberFormatException nfe) {
                } /** TODO: log that exception */
            }
            fontWeight = ((int) fontWeight / 100) * 100;
            if (fontWeight < 100) {
                fontWeight = 100;
            } else if (fontWeight > 900) {
                fontWeight = 900;
            }

            // NOTE: this is incomplete. font-size may be specified with
            // various kinds of keywords too
            int fontSize = propertyList.get(PR_FONT_SIZE).getLength().getValue();
            //int fontVariant = propertyList.get("font-variant").getEnum();
            String fname = foTreeControl.fontLookup(fontFamily, fontStyle,
                                               fontWeight);
            FontMetrics metrics = foTreeControl.getMetricsFor(fname);
            fontState = new Font(fname, metrics, fontSize);
        }
        return fontState;
    }


    /**
     * Constructs a BorderAndPadding object. If it was constructed before it is
     * reused.
     * @return a BorderAndPadding object
     */
    public CommonBorderAndPadding getBorderAndPadding() {
        if (borderAndPadding == null) {
            this.borderAndPadding = new CommonBorderAndPadding();

            initBorderInfo(CommonBorderAndPadding.BEFORE, SA_BEFORE);
            initBorderInfo(CommonBorderAndPadding.AFTER, SA_AFTER);
            initBorderInfo(CommonBorderAndPadding.START, SA_START);
            initBorderInfo(CommonBorderAndPadding.END, SA_END);
        }
        return borderAndPadding;
    }

    private void initBorderInfo(int whichSide, String[] saSide) {
        borderAndPadding.setPadding(whichSide,
                                    propertyList.get(
                                      MSGFMT_PADDING.format(saSide)).getCondLength());
        // If style = none, force width to 0, don't get Color (spec 7.7.20)
        int style = propertyList.get(MSGFMT_STYLE.format(saSide)).getEnum();
        if (style != Constants.NONE) {
            borderAndPadding.setBorder(whichSide, style,
                                       propertyList.get(
                                         MSGFMT_WIDTH.format(saSide)).getCondLength(),
                                       propertyList.get(
                                         MSGFMT_COLOR.format(saSide)).getColorType());
        }
    }

    /**
     * Constructs a HyphenationProps objects. If it was constructed before it is
     * reused.
     * @return a HyphenationProps object
     */
    public CommonHyphenation getHyphenationProps() {
        if (hyphProps == null) {
            this.hyphProps = new CommonHyphenation();
            hyphProps.hyphenate =
              this.propertyList.get(PR_HYPHENATE).getEnum();
            hyphProps.hyphenationChar = this.propertyList.get(
                                          PR_HYPHENATION_CHARACTER).getCharacter();
            hyphProps.hyphenationPushCharacterCount = this.propertyList.get(
                      PR_HYPHENATION_PUSH_CHARACTER_COUNT).getNumber().
                    intValue();
            hyphProps.hyphenationRemainCharacterCount = this.propertyList.get(
                      PR_HYPHENATION_REMAIN_CHARACTER_COUNT).getNumber().
                    intValue();
            hyphProps.language =
              this.propertyList.get(PR_LANGUAGE).getString();
            hyphProps.country = this.propertyList.get(PR_COUNTRY).getString();
        }
        return hyphProps;
    }

    /*public int checkBreakBefore(Area area) {
         if (!(area instanceof ColumnArea)) {
             switch (propertyList.get("break-before").getEnum()) {
             case BreakBefore.PAGE:
                 return Status.FORCE_PAGE_BREAK;
             case BreakBefore.ODD_PAGE:
                 return Status.FORCE_PAGE_BREAK_ODD;
             case BreakBefore.EVEN_PAGE:
                 return Status.FORCE_PAGE_BREAK_EVEN;
             case BreakBefore.COLUMN:
                 return Status.FORCE_COLUMN_BREAK;
             default:
                 return Status.OK;
             }
         } else {
             ColumnArea colArea = (ColumnArea)area;
             switch (propertyList.get("break-before").getEnum()) {
             case BreakBefore.PAGE:
                 // if first ColumnArea, and empty, return OK
                 if (!colArea.hasChildren() && (colArea.getColumnIndex() == 1))
                     return Status.OK;
                 else
                     return Status.FORCE_PAGE_BREAK;
             case BreakBefore.ODD_PAGE:
                 // if first ColumnArea, empty, _and_ in odd page,
                 // return OK
                 if (!colArea.hasChildren() && (colArea.getColumnIndex() == 1)
                         && (colArea.getPage().getNumber() % 2 != 0))
                     return Status.OK;
                 else
                     return Status.FORCE_PAGE_BREAK_ODD;
             case BreakBefore.EVEN_PAGE:
                 // if first ColumnArea, empty, _and_ in even page,
                 // return OK
                 if (!colArea.hasChildren() && (colArea.getColumnIndex() == 1)
                         && (colArea.getPage().getNumber() % 2 == 0))
                     return Status.OK;
                 else
                     return Status.FORCE_PAGE_BREAK_EVEN;
             case BreakBefore.COLUMN:
                 // if ColumnArea is empty return OK
                 if (!area.hasChildren())
                     return Status.OK;
                 else
                     return Status.FORCE_COLUMN_BREAK;
             default:
                 return Status.OK;
             }
         }
     }

     public int checkBreakAfter(Area area) {
         switch (propertyList.get("break-after").getEnum()) {
         case BreakAfter.PAGE:
             return Status.FORCE_PAGE_BREAK;
         case BreakAfter.ODD_PAGE:
             return Status.FORCE_PAGE_BREAK_ODD;
         case BreakAfter.EVEN_PAGE:
             return Status.FORCE_PAGE_BREAK_EVEN;
         case BreakAfter.COLUMN:
             return Status.FORCE_COLUMN_BREAK;
         default:
             return Status.OK;
         }
     }*/


    /**
     * Constructs a MarginProps objects. If it was constructed before it is
     * reused.
     * @return a MarginProps object
     */
    public CommonMarginBlock getMarginProps() {
        CommonMarginBlock props = new CommonMarginBlock();

        // Common Margin Properties-Block
        props.marginTop =
          this.propertyList.get(PR_MARGIN_TOP).getLength().getValue();
        props.marginBottom =
          this.propertyList.get(PR_MARGIN_BOTTOM).getLength().getValue();
        props.marginLeft =
          this.propertyList.get(PR_MARGIN_LEFT).getLength().getValue();
        props.marginRight =
          this.propertyList.get(PR_MARGIN_RIGHT).getLength().getValue();

        // For now, we only get the optimum value for space-before and after
        props.spaceBefore = this.propertyList.get(PR_SPACE_BEFORE).
                        getSpace().getOptimum().getLength().getValue();
        props.spaceAfter = this.propertyList.get(PR_SPACE_AFTER).
                        getSpace().getOptimum().getLength().getValue();
        props.startIndent = this.propertyList.get(PR_START_INDENT).
                        getLength().getValue();
        props.endIndent = this.propertyList.get(PR_END_INDENT).
                        getLength().getValue();

        return props;
    }

    /**
     * Constructs a BackgroundProps objects. If it was constructed before it is
     * reused.
     * @return a BackgroundProps object
     */
    public CommonBackground getBackgroundProps() {
        CommonBackground bp = new CommonBackground();
        bp.backAttachment = propertyList.get(PR_BACKGROUND_ATTACHMENT).getEnum();
        bp.backColor = propertyList.get(PR_BACKGROUND_COLOR).getColorType();
        if (bp.backColor.getAlpha() == 0) {
            bp.backColor = null;
        }

        bp.backImage = propertyList.get(PR_BACKGROUND_IMAGE).getString();
        if (bp.backImage == null || NONE.equals(bp.backImage)) {
            bp.backImage = null;
        } else {
            bp.backRepeat = propertyList.get(PR_BACKGROUND_REPEAT).getEnum();
            Property prop = propertyList.get(PR_BACKGROUND_POSITION_HORIZONTAL);
            if (prop != null) {
                bp.backPosHorizontal = prop.getLength();
            }
            prop = propertyList.get(PR_BACKGROUND_POSITION_VERTICAL);
            if (prop != null) {
                bp.backPosVertical = prop.getLength();
            }
        }

        return bp;
    }

    /**
     * Constructs a MarginInlineProps objects. If it was constructed before it is
     * reused.
     * @return a MarginInlineProps object
     */
    public CommonMarginInline getMarginInlineProps() {
        CommonMarginInline props = new CommonMarginInline();
        return props;
    }

    /**
     * Constructs a InlineProps objects. If it was constructed before it is
     * reused.
     * @return a InlineProps object
     */
    public InlineProps getInlineProps() {
        InlineProps props = new InlineProps();
        props.spaceStart =  new SpaceVal(propertyList.get(PR_SPACE_START).getSpace());
        props.spaceEnd =    new SpaceVal(propertyList.get(PR_SPACE_END).getSpace());
        return props;
    }

    /**
     * Constructs a AccessibilityProps objects. If it was constructed before it is
     * reused.
     * @return a AccessibilityProps object
     */
    public CommonAccessibility getAccessibilityProps() {
        CommonAccessibility props = new CommonAccessibility();
        String str;
        str = this.propertyList.get(PR_SOURCE_DOCUMENT).getString();
        if (!NONE.equals(str)) {
            props.sourceDoc = str;
        }
        str = this.propertyList.get(PR_ROLE).getString();
        if (!NONE.equals(str)) {
            props.role = str;
        }
        return props;
    }

    /**
     * Constructs a AuralProps objects. If it was constructed before it is
     * reused.
     * @return a AuralProps object
     */
    public CommonAural getAuralProps() {
        CommonAural props = new CommonAural();
        return props;
    }

    /**
     * Constructs a RelativePositionProps objects. If it was constructed before it is
     * reused.
     * @return a RelativePositionProps object
     */
    public CommonRelativePosition getRelativePositionProps() {
        CommonRelativePosition props = new CommonRelativePosition();
        return props;
    }

    /**
     * Constructs a AbsolutePositionProps objects. If it was constructed before
     * it is reused.
     * @return a AbsolutePositionProps object
     */
    public CommonAbsolutePosition getAbsolutePositionProps() {
        CommonAbsolutePosition props = new CommonAbsolutePosition();
        props.absolutePosition =
          this.propertyList.get(PR_ABSOLUTE_POSITION).getEnum();
        props.top = this.propertyList.get(PR_TOP).getLength().getValue();
        props.bottom = this.propertyList.get(PR_BOTTOM).getLength().getValue();
        props.left = this.propertyList.get(PR_LEFT).getLength().getValue();
        props.right = this.propertyList.get(PR_RIGHT).getLength().getValue();
        return props;
    }

    /**
     * Constructs a BlockProps objects. If it was constructed before it is
     * reused.
     * @return a BlockProps object
     */
    public BlockProps getBlockProps() {
        BlockProps props = new BlockProps();
        props.firstIndent = this.propertyList.get(PR_TEXT_INDENT).getLength().getValue();
        props.lastIndent = 0;
            /*this.propertyList.get("last-line-end-indent").getLength().mvalue(); */
        props.textAlign = this.propertyList.get(PR_TEXT_ALIGN).getEnum();
        props.textAlignLast = this.propertyList.get(PR_TEXT_ALIGN_LAST).getEnum();
        props.lineStackType = this.propertyList.get(PR_LINE_STACKING_STRATEGY).getEnum();

        return props;
    }

    /**
     * Constructs a LayoutProps objects. If it was constructed before it is
     * reused.
     * @return a LayoutProps object
     */
    public LayoutProps getLayoutProps() {
        LayoutProps props = new LayoutProps();
        props.breakBefore = this.propertyList.get(PR_BREAK_BEFORE).getEnum();
        props.breakAfter = this.propertyList.get(PR_BREAK_AFTER).getEnum();
        props.bIsSpan = (this.propertyList.get(PR_SPAN).getEnum() == Span.ALL);
        props.spaceBefore = new SpaceVal(
                              this.propertyList.get(PR_SPACE_BEFORE).getSpace());
        props.spaceAfter = new SpaceVal(
                             this.propertyList.get(PR_SPACE_AFTER).getSpace());
        return props;
    }

    /**
     * Constructs a TextInfo objects. If it was constructed before it is
     * reused.
     * @param foTreeControl FOTreeControl implementation containing list of
     * available fonts
     * @return a TextInfo object
     */
    public TextInfo getTextLayoutProps(FOTreeControl foTreeControl) {
        if (textInfo == null) {
            textInfo = new TextInfo();
            textInfo.fs = getFontState(foTreeControl);
            textInfo.color = propertyList.get(PR_COLOR).getColorType();

            textInfo.verticalAlign =
              propertyList.get(PR_VERTICAL_ALIGN).getEnum();

            textInfo.wrapOption = propertyList.get(PR_WRAP_OPTION).getEnum();
            textInfo.bWrap = (textInfo.wrapOption == Constants.WRAP);

            textInfo.wordSpacing = new SpaceVal(
                                     propertyList.get(PR_WORD_SPACING).getSpace());

            /* textInfo.letterSpacing =
               new SpaceVal(propertyList.get("letter-spacing").getSpace());*/

            textInfo.whiteSpaceCollapse =
              propertyList.get(PR_WHITE_SPACE_COLLAPSE).getEnum();

            textInfo.lineHeight = this.propertyList.get(
                                    PR_LINE_HEIGHT).getLength().getValue();

            textInfo.textTransform
                    = this.propertyList.get(PR_TEXT_TRANSFORM).getEnum();

        }
        return textInfo;
    }

    /**
     * Calculate absolute reference-orientation relative to media orientation.
     * @return the enumerated reference-orientation
     */
    public int getAbsRefOrient() {
        return propertyList.get(PR_REFERENCE_ORIENTATION).getNumber().intValue();
    }

    /**
     * @return the enumerated writing-mode
     */
    public int getWritingMode() {
        return propertyList.get(PR_WRITING_MODE).getEnum();
    }

}
