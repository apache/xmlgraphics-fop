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

package org.apache.fop.fo;

// FOP
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fo.properties.Property;
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
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.fonts.FontMetrics;
import org.apache.fop.fo.properties.CommonHyphenation;

/**
 * Helper class for managing groups of properties.
 */
public class PropertyManager implements Constants {

    private PropertyList propertyList;
    private FontInfo fontInfo = null;
    private Font fontState = null;
    private CommonBorderAndPadding borderAndPadding = null;
    private CommonHyphenation hyphProps = null;
    private TextInfo textInfo = null;
    private static final String NONE = "none";

    private static final int[] SA_BEFORE = new int[] {
        PR_BORDER_BEFORE_COLOR, PR_BORDER_BEFORE_STYLE, PR_BORDER_BEFORE_WIDTH, PR_PADDING_BEFORE};
    private static final int[] SA_AFTER = new int[]{
        PR_BORDER_AFTER_COLOR, PR_BORDER_AFTER_STYLE, PR_BORDER_AFTER_WIDTH, PR_PADDING_AFTER};
    private static final int[] SA_START = new int[]{
        PR_BORDER_START_COLOR, PR_BORDER_START_STYLE, PR_BORDER_START_WIDTH, PR_PADDING_START};
    private static final int[] SA_END = new int[]{
        PR_BORDER_END_COLOR, PR_BORDER_END_STYLE, PR_BORDER_END_WIDTH, PR_PADDING_END};
    
    /**
     * Main constructor
     * @param propList list of properties for the FO, initialized
     * from the attributes in the input source document
     */
    public PropertyManager(PropertyList propList) {
        propertyList = propList;
    }

    /**
     * Sets the FontInfo object telling the property manager which fonts are
     * available.
     * @param fontInfo FontInfo object
     */
    public void setFontInfo(FontInfo fontInfo) {
        this.fontInfo = fontInfo;
    }


    /**
     * Constructs a FontState object. If it was constructed before it is
     * reused.
     * @param fontInfo the FontInfo implementation containing the font
     * information
     * @return a FontState object
     */
    public Font getFontState(FontInfo fontInfo) {
        if (fontState == null) {
            if (fontInfo == null) {
                fontInfo = this.fontInfo;
            } else if (this.fontInfo == null) {
                this.fontInfo = fontInfo;
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
            String fname = fontInfo.fontLookup(fontFamily, fontStyle,
                                               fontWeight);
            FontMetrics metrics = fontInfo.getMetricsFor(fname);
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

    private void initBorderInfo(int whichSide, int[] saSide) {
        borderAndPadding.setPadding(whichSide,
                                    propertyList.get(saSide[3]).getCondLength());
        // If style = none, force width to 0, don't get Color (spec 7.7.20)
        int style = propertyList.get(saSide[1]).getEnum();
        if (style != Constants.NONE) {
            borderAndPadding.setBorder(whichSide, style,
                                       propertyList.get(saSide[2]).getCondLength(),
                                       propertyList.get(saSide[0]).getColorType());
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


        // For now we do the section 5.3.2 calculation here.
        // This is a hack that doesn't deal correctly with:
        // - Reference vs. non-reference areas.
        // - writing mode, it mixes start and left.
        // - inherited values of margins and indents.
        // When the indents properties calculate this values correctly,
        // the block below can be removed and replaced with simple
        // props.startIndent = this.propertyList.get(PR_START_INDENT)
        // props.endIndent = this.propertyList.get(PR_END_INDENT)
        CommonBorderAndPadding bpProps = getBorderAndPadding();

        int startIndent = 0;
        if (props.marginLeft != 0) {
            startIndent = props.marginLeft; 
        } else {
            startIndent = this.propertyList.get(PR_START_INDENT).
                                getLength().getValue(); 
        }
        props.startIndent = startIndent +
                            bpProps.getBorderStartWidth(false) +
                            bpProps.getPaddingStart(false);  

        int endIndent = 0;
        if (props.marginRight != 0) {
            endIndent = props.marginRight; 
        } else {
            endIndent = this.propertyList.get(PR_END_INDENT).
                                getLength().getValue(); 
        }
        props.endIndent = endIndent +
                          bpProps.getBorderEndWidth(false) +
                          bpProps.getPaddingEnd(false);

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
        props.firstIndent = this.propertyList.get(PR_TEXT_INDENT).getLength();
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
        props.bIsSpan = (this.propertyList.get(PR_SPAN).getEnum() == Constants.ALL);
        props.spaceBefore = new SpaceVal(
                              this.propertyList.get(PR_SPACE_BEFORE).getSpace());
        props.spaceAfter = new SpaceVal(
                             this.propertyList.get(PR_SPACE_AFTER).getSpace());
        return props;
    }

    /**
     * Constructs a TextInfo objects. If it was constructed before it is
     * reused.
     * @param fontInfo FontInfo object containing list of available fonts
     * @return a TextInfo object
     */
    public TextInfo getTextLayoutProps(FontInfo fontInfo) {
        if (textInfo == null) {
            textInfo = new TextInfo();
            textInfo.fs = getFontState(fontInfo);
            textInfo.color = propertyList.get(PR_COLOR).getColorType();

            textInfo.verticalAlign =
              propertyList.get(PR_VERTICAL_ALIGN).getEnum();

            textInfo.wrapOption = propertyList.get(PR_WRAP_OPTION).getEnum();
            textInfo.bWrap = (textInfo.wrapOption == Constants.WRAP);

            // if word-spacing or letter-spacing is "normal", convert it
            // into a suitable MinOptMax value
            Property wordSpacing = propertyList.get(PR_WORD_SPACING);
            Property letterSpacing = propertyList.get(PR_LETTER_SPACING);
            if (wordSpacing.getEnum() == NORMAL) {
                if (letterSpacing.getEnum() == NORMAL) {
                    // letter spaces are set to zero (or use different values?)
                    textInfo.letterSpacing
                        = new SpaceVal(new MinOptMax(0), true, true, 0);
                } else {
                    textInfo.letterSpacing
                        = new SpaceVal(letterSpacing.getSpace());
                }
                // give word spaces the possibility to shrink by a third,
                // and stretch by a half;
                int spaceCharIPD = textInfo.fs.getCharWidth(' ');
                textInfo.wordSpacing = new SpaceVal
                    (MinOptMax.add
                     (new MinOptMax(-spaceCharIPD / 3, 0, spaceCharIPD / 2),
                      MinOptMax.multiply(textInfo.letterSpacing.getSpace(), 2)),
                     true, true, 0);
            } else {
                textInfo.wordSpacing = new SpaceVal(wordSpacing.getSpace());
                if (letterSpacing.getEnum() == NORMAL) {
                    // letter spaces are set to zero (or use different values?)
                    textInfo.letterSpacing
                        = new SpaceVal(new MinOptMax(0), true, true, 0);
                } else {
                    textInfo.letterSpacing
                        = new SpaceVal(letterSpacing.getSpace());
                }
            }

            textInfo.whiteSpaceCollapse
                = propertyList.get(PR_WHITE_SPACE_COLLAPSE).getEnum();
            textInfo.lineHeight
                = this.propertyList.get(PR_LINE_HEIGHT).getLength().getValue();
            textInfo.textTransform
                = this.propertyList.get(PR_TEXT_TRANSFORM).getEnum();
            textInfo.hyphChar
                = this.propertyList.get(PR_HYPHENATION_CHARACTER).getCharacter();
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
