/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

// Java
import java.text.MessageFormat;
import java.awt.geom.Rectangle2D;

// FOP
import org.apache.fop.area.CTM;
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.layout.FontState;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.MarginProps;
import org.apache.fop.layout.MarginInlineProps;
import org.apache.fop.layout.BackgroundProps;
import org.apache.fop.layout.AccessibilityProps;
import org.apache.fop.layout.AuralProps;
import org.apache.fop.layout.RelativePositionProps;
import org.apache.fop.layout.AbsolutePositionProps;
import org.apache.fop.traits.BlockProps;
import org.apache.fop.traits.InlineProps;
import org.apache.fop.traits.SpaceVal;
import org.apache.fop.traits.LayoutProps; // keep, break, span, space?
import org.apache.fop.fo.properties.Constants;
import org.apache.fop.fo.properties.WritingMode;
import org.apache.fop.fo.properties.Span;
import org.apache.fop.fonts.FontMetrics;
import org.apache.fop.layout.HyphenationProps;

/**
 * Helper class for managing groups of properties.
 */
public class PropertyManager {

    private PropertyList properties;
    private FontInfo fontInfo = null;
    private FontState fontState = null;
    private BorderAndPadding borderAndPadding = null;
    private HyphenationProps hyphProps = null;
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
        this.properties = pList;
    }

    /**
     * Returns the property list that is used for lookup.
     * @return the property list
     */
    public PropertyList getProperties() {
        return properties;
    }

    /**
     * Sets the FontInfo object telling the property manager which fonts are 
     * available.
     * @param fontInfo available fonts
     */
    public void setFontInfo(FontInfo fontInfo) {
        this.fontInfo = fontInfo;
    }


    /**
     * Constructs a FontState object. If it was constructed before it is 
     * reused.
     * @param fontInfo FontInfo to work with
     * @return a FontState object
     */
    public FontState getFontState(FontInfo fontInfo) {
        if (fontState == null) {
            if (fontInfo == null) {
                fontInfo = this.fontInfo;
            } else if (this.fontInfo == null) {
                this.fontInfo = fontInfo;
            }
            /**@todo this is ugly. need to improve. */
            
            String fontFamily = properties.get("font-family").getString();
            String fontStyle = properties.get("font-style").getString();
            String fw = properties.get("font-weight").getString();
            int fontWeight = 400;
            if (fw.equals("bolder")) {
                // +100 from inherited
            } else if (fw.equals("lighter")) {
                // -100 from inherited
            } else {
                try {
                    fontWeight = Integer.parseInt(fw);
                } catch (NumberFormatException nfe) {
                } /**@todo log that exception */
            }
            fontWeight = ((int) fontWeight / 100) * 100;
            if (fontWeight < 100) {
                fontWeight = 100;
            } else if (fontWeight > 900) {
                fontWeight = 900;
            }

            // NOTE: this is incomplete. font-size may be specified with
            // various kinds of keywords too
            int fontSize = properties.get("font-size").getLength().mvalue();
            //int fontVariant = properties.get("font-variant").getEnum();
            String fname = fontInfo.fontLookup(fontFamily, fontStyle,
                                               fontWeight);
            FontMetrics metrics = fontInfo.getMetricsFor(fname);
            fontState = new FontState(fname, metrics, fontSize);
        }
        return fontState;
    }


    /**
     * Constructs a BorderAndPadding object. If it was constructed before it is 
     * reused.
     * @return a BorderAndPadding object
     */
    public BorderAndPadding getBorderAndPadding() {
        if (borderAndPadding == null) {
            this.borderAndPadding = new BorderAndPadding();

            initBorderInfo(BorderAndPadding.BEFORE, SA_BEFORE);
            initBorderInfo(BorderAndPadding.AFTER, SA_AFTER);
            initBorderInfo(BorderAndPadding.START, SA_START);
            initBorderInfo(BorderAndPadding.END, SA_END);
        }
        return borderAndPadding;
    }

    private void initBorderInfo(int whichSide, String[] saSide) {
        borderAndPadding.setPadding(whichSide,
                                    properties.get(
                                      MSGFMT_PADDING.format(saSide)).getCondLength());
        // If style = none, force width to 0, don't get Color
        int style = properties.get(MSGFMT_STYLE.format(saSide)).getEnum();
        if (style != Constants.NONE) {
            borderAndPadding.setBorder(whichSide, style,
                                       properties.get(
                                         MSGFMT_WIDTH.format(saSide)).getCondLength(),
                                       properties.get(
                                         MSGFMT_COLOR.format(saSide)).getColorType());
        }
    }

    /**
     * Constructs a HyphenationProps objects. If it was constructed before it is 
     * reused.
     * @return a HyphenationProps object
     */
    public HyphenationProps getHyphenationProps() {
        if (hyphProps == null) {
            this.hyphProps = new HyphenationProps();
            hyphProps.hyphenate =
              this.properties.get("hyphenate").getEnum();
            hyphProps.hyphenationChar = this.properties.get(
                                          "hyphenation-character").getCharacter();
            hyphProps.hyphenationPushCharacterCount = this.properties.get(
                      "hyphenation-push-character-count").getNumber().
                    intValue();
            hyphProps.hyphenationRemainCharacterCount = this.properties.get(
                      "hyphenation-remain-character-count").getNumber().
                    intValue();
            hyphProps.language =
              this.properties.get("language").getString();
            hyphProps.country = this.properties.get("country").getString();
        }
        return hyphProps;
    }

    /*public int checkBreakBefore(Area area) {
         if (!(area instanceof ColumnArea)) {
             switch (properties.get("break-before").getEnum()) {
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
             switch (properties.get("break-before").getEnum()) {
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
         switch (properties.get("break-after").getEnum()) {
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
    public MarginProps getMarginProps() {
        MarginProps props = new MarginProps();

        // Common Margin Properties-Block
        props.marginTop =
          this.properties.get("margin-top").getLength().mvalue();
        props.marginBottom =
          this.properties.get("margin-bottom").getLength().mvalue();
        props.marginLeft =
          this.properties.get("margin-left").getLength().mvalue();
        props.marginRight =
          this.properties.get("margin-right").getLength().mvalue();

        // For now, we only get the optimum value for space-before and after
        props.spaceBefore = this.properties.get("space-before").
                        getSpace().getOptimum().getLength().mvalue();
        props.spaceAfter = this.properties.get("space-after").
                        getSpace().getOptimum().getLength().mvalue();
        props.startIndent = this.properties.get("start-indent").
                        getLength().mvalue();
        props.endIndent = this.properties.get("end-indent").
                        getLength().mvalue();

        return props;
    }

    /**
     * Constructs a BackgroundProps objects. If it was constructed before it is 
     * reused.
     * @return a BackgroundProps object
     */
    public BackgroundProps getBackgroundProps() {
        BackgroundProps bp = new BackgroundProps();
        bp.backAttachment = properties.get("background-attachment").getEnum();
        bp.backColor = properties.get("background-color").getColorType();
        if (bp.backColor.alpha() == 1) {
            bp.backColor = null;
        }

        bp.backImage = properties.get("background-image").getString();
        if (bp.backImage == null || NONE.equals(bp.backImage)) {
            bp.backImage = null;
        } else {
            bp.backRepeat = properties.get("background-repeat").getEnum();
            Property prop = properties.get("background-position-horizontal");
            if (prop != null) {
                bp.backPosHorizontal = prop.getLength();
            }
            prop = properties.get("background-position-vertical");
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
    public MarginInlineProps getMarginInlineProps() {
        MarginInlineProps props = new MarginInlineProps();
        return props;
    }

    /**
     * Constructs a InlineProps objects. If it was constructed before it is 
     * reused.
     * @return a InlineProps object
     */
    public InlineProps getInlineProps() {
        InlineProps props = new InlineProps();
        props.spaceStart =  new SpaceVal(properties.get("space-start").getSpace());
        props.spaceEnd =    new SpaceVal(properties.get("space-end").getSpace());
        return props;
    }

    /**
     * Constructs a AccessibilityProps objects. If it was constructed before it is 
     * reused.
     * @return a AccessibilityProps object
     */
    public AccessibilityProps getAccessibilityProps() {
        AccessibilityProps props = new AccessibilityProps();
        String str;
        str = this.properties.get("source-document").getString();
        if (!NONE.equals(str)) {
            props.sourceDoc = str;
        }
        str = this.properties.get("role").getString();
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
    public AuralProps getAuralProps() {
        AuralProps props = new AuralProps();
        return props;
    }

    /**
     * Constructs a RelativePositionProps objects. If it was constructed before it is 
     * reused.
     * @return a RelativePositionProps object
     */
    public RelativePositionProps getRelativePositionProps() {
        RelativePositionProps props = new RelativePositionProps();
        return props;
    }

    /**
     * Constructs a AbsolutePositionProps objects. If it was constructed before 
     * it is reused.
     * @return a AbsolutePositionProps object
     */
    public AbsolutePositionProps getAbsolutePositionProps() {
        AbsolutePositionProps props = new AbsolutePositionProps();
        props.absolutePosition =
          this.properties.get("absolute-position").getEnum();
        props.top = this.properties.get("top").getLength().mvalue();
        props.bottom = this.properties.get("bottom").getLength().mvalue();
        props.left = this.properties.get("left").getLength().mvalue();
        props.right = this.properties.get("right").getLength().mvalue();
        return props;
    }

    /**
     * Constructs a BlockProps objects. If it was constructed before it is 
     * reused.
     * @return a BlockProps object
     */
    public BlockProps getBlockProps() {
        BlockProps props = new BlockProps();
        props.firstIndent = this.properties.get("text-indent").getLength().mvalue();
        props.lastIndent = 0; 
            /*this.properties.get("last-line-end-indent").getLength().mvalue(); */
        props.textAlign = this.properties.get("text-align").getEnum();
        props.textAlignLast = this.properties.get("text-align-last").getEnum();
        props.lineStackType = this.properties.get("line-stacking-strategy").getEnum();

        return props;
    }

    /**
     * Constructs a LayoutProps objects. If it was constructed before it is 
     * reused.
     * @return a LayoutProps object
     */
    public LayoutProps getLayoutProps() {
        LayoutProps props = new LayoutProps();
        props.breakBefore = this.properties.get("break-before").getEnum();
        props.breakAfter = this.properties.get("break-after").getEnum();
        props.bIsSpan = (this.properties.get("span").getEnum() == Span.ALL);
        props.spaceBefore = new SpaceVal(
                              this.properties.get("space-before").getSpace());
        props.spaceAfter = new SpaceVal(
                             this.properties.get("space-after").getSpace());
        return props;
    }

    /**
     * Constructs a TextInfo objects. If it was constructed before it is 
     * reused.
     * @param fontInfo available fonts
     * @return a TextInfo object
     */
    public TextInfo getTextLayoutProps(FontInfo fontInfo) {
        if (textInfo == null) {
            textInfo = new TextInfo();
            textInfo.fs = getFontState(fontInfo);
            textInfo.color = properties.get("color").getColorType();

            textInfo.verticalAlign =
              properties.get("vertical-align").getEnum();

            textInfo.wrapOption = properties.get("wrap-option").getEnum();
            textInfo.bWrap = (textInfo.wrapOption == Constants.WRAP);

            textInfo.wordSpacing = new SpaceVal(
                                     properties.get("word-spacing").getSpace());

            /* textInfo.letterSpacing =
               new SpaceVal(properties.get("letter-spacing").getSpace());*/

            textInfo.whiteSpaceCollapse =
              properties.get("white-space-collapse").getEnum();

            textInfo.lineHeight = this.properties.get(
                                    "line-height").getLength().mvalue();
        }
        return textInfo;
    }

    /**
     * Construct a coordinate transformation matrix (CTM).
     * @param absVPrect absolute viewpoint rectangle
     * @param reldims relative dimensions
     * @return CTM the coordinate transformation matrix (CTM)
     */
    public CTM getCTMandRelDims(Rectangle2D absVPrect,
                                FODimension reldims) {
        int width, height;
        // We will use the absolute reference-orientation to set up the CTM.
        // The value here is relative to its ancestor reference area.
        int absRefOrient = getAbsRefOrient(
                this.properties.get("reference-orientation").getNumber().intValue());
        if (absRefOrient % 180 == 0) {
            width = (int) absVPrect.getWidth();
            height = (int) absVPrect.getHeight();
        } else {
            // invert width and height since top left are rotated by 90 (cl or ccl)
            height = (int) absVPrect.getWidth();
            width = (int) absVPrect.getHeight();
        }
        /* Set up the CTM for the content of this reference area.
         * This will transform region content coordinates in
         * writing-mode relative into absolute page-relative
         * which will then be translated based on the position of
         * the region viewport.
         * (Note: scrolling between region vp and ref area when
         * doing online content!)
         */
        CTM ctm = new CTM(absVPrect.getX(), absVPrect.getY());

        // First transform for rotation
        if (absRefOrient != 0) {
            // Rotation implies translation to keep the drawing area in the
            // first quadrant. Note: rotation is counter-clockwise
            switch (absRefOrient) {
                case 90:
                    ctm = ctm.translate(0, width); // width = absVPrect.height
                    break;
                case 180:
                    ctm = ctm.translate(width, height);
                    break;
                case 270:
                    ctm = ctm.translate(height, 0); // height = absVPrect.width
                    break;
            }
            ctm = ctm.rotate(absRefOrient);
        }
        int wm = this.properties.get("writing-mode").getEnum();
        /* Since we've already put adjusted width and height values for the
         * top and left positions implied by the reference-orientation, we
         * can set ipd and bpd appropriately based on the writing mode.
         */

        if (wm == WritingMode.LR_TB || wm == WritingMode.RL_TB) {
            reldims.ipd = width;
            reldims.bpd = height;
        } else {
            reldims.ipd = height;
            reldims.bpd = width;
        }
        // Set a rectangle to be the writing-mode relative version???
        // Now transform for writing mode
        return ctm.multiply(CTM.getWMctm(wm, reldims.ipd, reldims.bpd));
    }

    /**
     * Calculate absolute reference-orientation relative to media orientation.
     */
    private int getAbsRefOrient(int myRefOrient) {
        return myRefOrient;
    }
}

