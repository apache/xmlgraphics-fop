/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import java.net.MalformedURLException;
import java.text.FieldPosition;
import java.text.MessageFormat;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.flow.AbstractFlow;
import org.apache.fop.fo.properties.BreakAfter;
import org.apache.fop.fo.properties.BreakBefore;
import org.apache.fop.fo.properties.Constants;
import org.apache.fop.fo.properties.TextDecoration;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.FopImageFactory;
import org.apache.fop.image.FopImageException;
import org.apache.fop.layout.AbsolutePositionProps;
import org.apache.fop.layout.AccessibilityProps;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.AuralProps;
import org.apache.fop.layout.BackgroundProps;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.ColumnArea;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.layout.FontState;
import org.apache.fop.layout.HyphenationProps;
import org.apache.fop.layout.MarginInlineProps;
import org.apache.fop.layout.MarginProps;
import org.apache.fop.layout.RelativePositionProps;
import org.apache.fop.layout.TextState;

public class PropertyManager {

    private PropertyList properties;
    private FontState fontState = null;
    private BorderAndPadding borderAndPadding = null;
    private HyphenationProps hyphProps = null;
    private BackgroundProps bgProps = null;

    private String[] saLeft;
    private String[] saRight;
    private String[] saTop;
    private String[] saBottom;

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

    private void initDirections() {
        saLeft = new String[1];
        saRight = new String[1];
        saTop = new String[1];
        saBottom = new String[1];
        saTop[0] = properties.wmAbsToRel(PropertyList.TOP);
        saBottom[0] = properties.wmAbsToRel(PropertyList.BOTTOM);
        saLeft[0] = properties.wmAbsToRel(PropertyList.LEFT);
        saRight[0] = properties.wmAbsToRel(PropertyList.RIGHT);
    }

    public FontState getFontState(FontInfo fontInfo) throws FOPException {
        if (fontState == null) {
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
            initDirections();

            initBorderInfo(BorderAndPadding.TOP, saTop);
            initBorderInfo(BorderAndPadding.BOTTOM, saBottom);
            initBorderInfo(BorderAndPadding.LEFT, saLeft);
            initBorderInfo(BorderAndPadding.RIGHT, saRight);
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
        /*
         * // need to get opt, min and max
         * props.spaceBefore = this.properties.get("space-before").getLength().mvalue();
         * props.spaceAfter = this.properties.get("space-after").getLength().mvalue();
         * props.startIndent = this.properties.get("start-indent").getLength().mvalue();
         * props.endIndent = this.properties.get("end-indent").getLength().mvalue();
         */
        return props;
    }

    public BackgroundProps getBackgroundProps() {
        if (bgProps == null) {
            this.bgProps = new BackgroundProps();
	    // bgProps.backAttachment = this.properties.get("background-attachment").getEnum();
	    bgProps.backColor =
		this.properties.get("background-color").getColorType();

	    String src = this.properties.get("background-image").getString();
	    if (src.equalsIgnoreCase("none")) {
		bgProps.backImage = null;
	    }
	    else if (src.equalsIgnoreCase("inherit")) {
		// XXX: implement this
		bgProps.backImage = null;
	    }
	    else {
		try {
		    bgProps.backImage = FopImageFactory.Make(src);
		}
		catch (MalformedURLException urlex) {
		    bgProps.backImage = null;
		    // XXX: use a logger instead
		    System.out.println("Error creating background image: "
			      + urlex.getMessage());
		}
		catch (FopImageException imgex) {
		    bgProps.backImage = null;
		    // XXX: use a logger instead
		    System.out.println("Error creating background image: "
				      + imgex.getMessage());
		}
	    }

	    bgProps.backRepeat = this.properties.get("background-repeat").getEnum();


	    // bgProps.backPosHorizontal = this.properties.get("background-position-horizontal").getLength();
	    // bgProps.backPosVertical = this.properties.get("background-position-vertical").getLength();
        }
	return bgProps;
    }

    public MarginInlineProps getMarginInlineProps() {
        MarginInlineProps props = new MarginInlineProps();
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

    public TextState getTextDecoration(FObj parent) throws FOPException {

        // TextState from parent Block/Inline
        TextState tsp = null;
        boolean found = false;

        do {
            if (parent instanceof AbstractFlow) {
                found = true;
            } else if (parent instanceof FObjMixed) {
                FObjMixed fom = (FObjMixed) parent;
                tsp = fom.getTextState();
                found = true;
            }
            parent = parent.getParent();
        } while (!found);

        TextState ts = new TextState();

        if (tsp != null) {
            ts.setUnderlined(tsp.getUnderlined());
            ts.setOverlined(tsp.getOverlined());
            ts.setLineThrough(tsp.getLineThrough());
        }

        int textDecoration = this.properties.get("text-decoration").getEnum();

        if (textDecoration == TextDecoration.UNDERLINE) {
            ts.setUnderlined(true);
        }
        if (textDecoration == TextDecoration.OVERLINE) {
            ts.setOverlined(true);
        }
        if (textDecoration == TextDecoration.LINE_THROUGH) {
            ts.setLineThrough(true);
        }
        if (textDecoration == TextDecoration.NO_UNDERLINE) {
            ts.setUnderlined(false);
        }
        if (textDecoration == TextDecoration.NO_OVERLINE) {
            ts.setOverlined(false);
        }
        if (textDecoration == TextDecoration.NO_LINE_THROUGH) {
            ts.setLineThrough(false);
        }

        return ts;
    }

}
