/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;


import java.awt.geom.Rectangle2D;
import org.apache.fop.area.CTM;
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.fo.TextInfo; // should be somewhere else probably...
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
import org.apache.fop.traits.LayoutProps;  // keep, break, span, space?
import org.apache.fop.fo.properties.BreakAfter;
import org.apache.fop.fo.properties.BreakBefore;
import org.apache.fop.fo.properties.Constants;
import org.apache.fop.fo.properties.WritingMode;
import org.apache.fop.fo.properties.Span;
import org.apache.fop.layout.HyphenationProps;
import org.apache.fop.apps.FOPException;
import java.text.MessageFormat;
import java.text.FieldPosition;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.ColumnArea;

public class PropertyManager {

    private PropertyList properties;
    private FontInfo m_fontInfo = null;
    private FontState fontState = null;
    private BorderAndPadding borderAndPadding = null;
    private HyphenationProps hyphProps = null;
    private TextInfo textInfo = null;

    private static String[] saBefore = new String[] {"before"};
    private static String[] saAfter = new String[] {"after"};
    private static String[] saStart = new String[] {"start"};
    private static String[] saEnd = new String[] {"end"};

    private static MessageFormat msgColorFmt =
        new MessageFormat("border-{0}-color");
    private static MessageFormat msgStyleFmt =
        new MessageFormat("border-{0}-style");
    private static MessageFormat msgWidthFmt =
        new MessageFormat("border-{0}-width");
    private static MessageFormat msgPaddingFmt =
        new MessageFormat("padding-{0}");

    public PropertyManager(PropertyList pList) {
        this.properties = pList;
    }

    public void setFontInfo(FontInfo fontInfo) {
	m_fontInfo = fontInfo;
    }


    public FontState getFontState(FontInfo fontInfo) throws FOPException {
        if (fontState == null) {
	    if (fontInfo == null) {
		fontInfo = m_fontInfo;
	    }
	    else if (m_fontInfo == null) {
		m_fontInfo = fontInfo;
	    }
            String fontFamily = properties.get("font-family").getString();
            String fontStyle = properties.get("font-style").getString();
            String fontWeight = properties.get("font-weight").getString();
            // NOTE: this is incomplete. font-size may be specified with
            // various kinds of keywords too
            int fontSize = properties.get("font-size").getLength().mvalue();
            int fontVariant = properties.get("font-variant").getEnum();
            // fontInfo is same for the whole FOP run but set in all FontState
            fontState = new FontState(fontInfo, fontFamily, fontStyle,
                                      fontWeight, fontSize, fontVariant);
        }
        return fontState;
    }


    public BorderAndPadding getBorderAndPadding() {
        if (borderAndPadding == null) {
            this.borderAndPadding = new BorderAndPadding();

            initBorderInfo(BorderAndPadding.BEFORE, saBefore);
            initBorderInfo(BorderAndPadding.AFTER, saAfter);
            initBorderInfo(BorderAndPadding.START, saStart);
            initBorderInfo(BorderAndPadding.END, saEnd);
        }
        return borderAndPadding;
    }

    private void initBorderInfo(int whichSide, String[] saSide) {
        borderAndPadding.setPadding(whichSide,
                                    properties.get(msgPaddingFmt.format(saSide)).getCondLength());
        // If style = none, force width to 0, don't get Color
        int style = properties.get(msgStyleFmt.format(saSide)).getEnum();
        if (style != Constants.NONE) {
            borderAndPadding.setBorder(whichSide, style,
                                       properties.get(msgWidthFmt.format(saSide)).getCondLength(),
                                       properties.get(msgColorFmt.format(saSide)).getColorType());
        }
    }

    public HyphenationProps getHyphenationProps() {
        if (hyphProps == null) {
            this.hyphProps = new HyphenationProps();
            hyphProps.hyphenate = this.properties.get("hyphenate").getEnum();
            hyphProps.hyphenationChar =
                this.properties.get("hyphenation-character").getCharacter();
            hyphProps.hyphenationPushCharacterCount =
                this.properties.get("hyphenation-push-character-count").getNumber().intValue();
            hyphProps.hyphenationRemainCharacterCount =
                this.properties.get("hyphenation-remain-character-count").getNumber().intValue();
            hyphProps.language = this.properties.get("language").getString();
            hyphProps.country = this.properties.get("country").getString();
        }
        return hyphProps;
    }

    public int checkBreakBefore(Area area) {
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
    }


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
        props.spaceBefore = this.properties.get("space-before").getSpace().
	    getOptimum().getLength().mvalue();
        props.spaceAfter = this.properties.get("space-after").getSpace().
	    getOptimum().getLength().mvalue();
        props.startIndent = this.properties.get("start-indent").getLength().mvalue();
        props.endIndent = this.properties.get("end-indent").getLength().mvalue();

        return props;
    }

    public BackgroundProps getBackgroundProps() {
        BackgroundProps bp = new BackgroundProps();
        return bp;
    }

    public MarginInlineProps getMarginInlineProps() {
        MarginInlineProps props = new MarginInlineProps();
        return props;
    }

    public InlineProps getInlineProps() {
        InlineProps props = new InlineProps();
        props.spaceStart = new SpaceVal(properties.get("space-start").
					getSpace());
        props.spaceEnd = new SpaceVal(properties.get("space-end").
					getSpace());
        return props;
    }

    public AccessibilityProps getAccessibilityProps() {
        AccessibilityProps props = new AccessibilityProps();
        String str;
        str = this.properties.get("source-document").getString();
        if(!"none".equals(str)) {
            props.sourceDoc = str;
        }
        str = this.properties.get("role").getString();
        if(!"none".equals(str)) {
            props.role = str;
        }
        return props;
    }

    public AuralProps getAuralProps() {
        AuralProps props = new AuralProps();
        return props;
    }

    public RelativePositionProps getRelativePositionProps() {
        RelativePositionProps props = new RelativePositionProps();
        return props;
    }

    public AbsolutePositionProps getAbsolutePositionProps() {
        AbsolutePositionProps props = new AbsolutePositionProps();
        return props;
    }

    public BlockProps getBlockProps() {
        BlockProps props = new BlockProps();
        props.firstIndent = this.properties.get("text-indent").
	    getLength().mvalue();
        props.lastIndent = 0; /*this.properties.get("last-line-end-indent").getLength().mvalue(); */
        props.textAlign = this.properties.get("text-align").getEnum();
        props.textAlignLast = this.properties.get("text-align-last").
	    getEnum();
	props.lineStackType = this.properties.
	    get("line-stacking-strategy").getEnum();

        return props;
    }

    public LayoutProps getLayoutProps() {
        LayoutProps props = new LayoutProps();
        props.breakBefore = this.properties.get("break-before").getEnum();
        props.breakAfter = this.properties.get("break-after").getEnum();
	props.bIsSpan = (this.properties.get("span").getEnum() == Span.ALL);
        props.spaceBefore = new SpaceVal(this.properties.get("space-before").
					 getSpace());
        props.spaceAfter = new SpaceVal(this.properties.get("space-after").
					getSpace());
        return props;
    }

    public TextInfo getTextLayoutProps(FontInfo fontInfo) {
	if (textInfo == null) {
	    textInfo = new TextInfo();
	    try {
		textInfo.fs = getFontState(fontInfo);
	    } catch (FOPException fopex) {
		/* log.error("Error setting FontState for characters: " +
		   fopex.getMessage());*/
		// Now what should we do ???
	    }
	    textInfo.color = properties.get("color").getColorType();

	    textInfo.verticalAlign =
                properties.get("vertical-align").getEnum();

	    textInfo.wrapOption = properties.get("wrap-option").getEnum();
	    textInfo.bWrap = (textInfo.wrapOption == Constants.WRAP);

	    textInfo.wordSpacing =
		new SpaceVal(properties.get("word-spacing").getSpace());

	    /* textInfo.letterSpacing =
	       new SpaceVal(properties.get("letter-spacing").getSpace());*/

	    textInfo.whiteSpaceCollapse =
                properties.get("white-space-collapse").getEnum();

	    textInfo.lineHeight = this.properties.
		get("line-height").getLength().mvalue();
	}
	return textInfo;
    }

    public CTM getCTMandRelDims(Rectangle2D absVPrect, FODimension reldims) {
	int width, height;
        // We will use the absolute reference-orientation to set up the CTM.
        // The value here is relative to its ancestor reference area.
        int absRefOrient =
            getAbsRefOrient(this.properties.get("reference-orientation").
			    getNumber().intValue());
        if (absRefOrient % 180 == 0) {
            width = (int)absVPrect.getWidth();
            height = (int)absVPrect.getHeight();
        }
        else {
            // invert width and height since top left are rotated by 90 (cl or ccl)
            height = (int)absVPrect.getWidth();
            width = (int)absVPrect.getHeight();
        }
        /* Set up the CTM for the content of this reference area. This will transform
         * region content coordinates in writing-mode relative into absolute page-relative
         * which will then be translated based on the position of the region viewport
         * (Note: scrolling between region vp and ref area when doing online content!)
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
                ctm = ctm.translate(height,0); // height = absVPrect.width
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
        }
        else {
            reldims.ipd=height;
            reldims.bpd=width;
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
