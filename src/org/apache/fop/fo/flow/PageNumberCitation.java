/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.pagination.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;
import org.apache.fop.layoutmgr.LeafNodeLayoutManager;
import org.apache.fop.area.inline.InlineArea;

import java.util.List;
import java.util.ArrayList;

/**
 * 6.6.11 fo:page-number-citation
 *
 * Common Usage:
 * The fo:page-number-citation is used to reference the page-number for the page containing the first normal area returned by
 * the cited formatting object.
 *
 * NOTE:
 * It may be used to provide the page-numbers in the table of contents, cross-references, and index entries.
 *
 * Areas:
 * The fo:page-number-citation formatting object generates and returns a single normal inline-area.
 * Constraints:
 *
 * The cited page-number is the number of the page containing, as a descendant, the first normal area returned by the
 * formatting object with an id trait matching the ref-id trait of the fo:page-number-citation (the referenced formatting
 * object).
 *
 * The cited page-number string is obtained by converting the cited page-number in accordance with the number to string
 * conversion properties specified on the ancestor fo:page-sequence of the referenced formatting object.
 *
 * The child areas of the generated inline-area are the same as the result of formatting a result-tree fragment consisting of
 * fo:character flow objects; one for each character in the cited page-number string and with only the "character" property
 * specified.
 *
 * Contents:
 *
 * EMPTY
 *
 * The following properties apply to this formatting object:
 *
 * [7.3 Common Accessibility Properties]
 * [7.5 Common Aural Properties]
 * [7.6 Common Border, Padding, and Background Properties]
 * [7.7 Common Font Properties]
 * [7.10 Common Margin Properties-Inline]
 * [7.11.1 "alignment-adjust"]
 * [7.11.2 "baseline-identifier"]
 * [7.11.3 "baseline-shift"]
 * [7.11.5 "dominant-baseline"]
 * [7.36.2 "id"]
 * [7.17.4 "keep-with-next"]
 * [7.17.5 "keep-with-previous"]
 * [7.14.2 "letter-spacing"]
 * [7.13.4 "line-height"]
 * [7.13.5 "line-height-shift-adjustment"]
 * [7.36.5 "ref-id"]
 * [7.18.4 "relative-position"]
 * [7.36.6 "score-spaces"]
 * [7.14.4 "text-decoration"]
 * [7.14.5 "text-shadow"]
 * [7.14.6 "text-transform"]
 * [7.14.8 "word-spacing"]
 */
public class PageNumberCitation extends FObj {

    float red;
    float green;
    float blue;
    int wrapOption;
    int whiteSpaceCollapse;
    Area area;
    String pageNumber;
    String refId;
    String id;
    TextState ts;


    public PageNumberCitation(FONode parent) {
        super(parent);
    }

    public void addLayoutManager(List lms) {
        LeafNodeLayoutManager lnlm = new LeafNodeLayoutManager(this);
        lnlm.setCurrentArea(getInlineArea());
        lms.add(lnlm);
    }

    // is id can be resolved then simply return a word, otherwise
    // return a resolveable area
    private InlineArea getInlineArea() {
        return null;
    }

    public Status layout(Area area) throws FOPException {
        if (!(area instanceof BlockArea)) {
            log.warn("page-number-citation outside block area");
            return new Status(Status.OK);
        }

        IDReferences idReferences = area.getIDReferences();
        this.area = area;
        if (this.marker == START) {

            // Common Accessibility Properties
            AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

            // Common Aural Properties
            AuralProps mAurProps = propMgr.getAuralProps();

            // Common Border, Padding, and Background Properties
            BorderAndPadding bap = propMgr.getBorderAndPadding();
            BackgroundProps bProps = propMgr.getBackgroundProps();

            // Common Font Properties
            //this.fontState = propMgr.getFontState(area.getFontInfo());

            // Common Margin Properties-Inline
            MarginInlineProps mProps = propMgr.getMarginInlineProps();

            // Common Relative Position Properties
            RelativePositionProps mRelProps =
              propMgr.getRelativePositionProps();

            // this.properties.get("alignment-adjust");
            // this.properties.get("alignment-baseline");
            // this.properties.get("baseline-shift");
            // this.properties.get("dominant-baseline");
            // this.properties.get("id");
            // this.properties.get("keep-with-next");
            // this.properties.get("keep-with-previous");
            // this.properties.get("letter-spacing");
            // this.properties.get("line-height");
            // this.properties.get("line-height-shift-adjustment");
            // this.properties.get("ref-id");
            // this.properties.get("score-spaces");
            // this.properties.get("text-decoration");
            // this.properties.get("text-shadow");
            // this.properties.get("text-transform");
            // this.properties.get("word-spacing");

            ColorType c = this.properties.get("color").getColorType();
            this.red = c.red();
            this.green = c.green();
            this.blue = c.blue();

            this.wrapOption = this.properties.get("wrap-option").getEnum();
            this.whiteSpaceCollapse =
              this.properties.get("white-space-collapse").getEnum();

            this.refId = this.properties.get("ref-id").getString();

            if (this.refId.equals("")) {
                throw new FOPException("page-number-citation must contain \"ref-id\"");
            }

            // create id
            this.id = this.properties.get("id").getString();
            idReferences.createID(id);
            ts = new TextState();

            this.marker = 0;
        }

        if (marker == 0) {
            idReferences.configureID(id, area);
        }


        pageNumber = idReferences.getPageNumber(refId);

        if (pageNumber != null) { // if we already know the page number
            this.marker = FOText.addText((BlockArea) area,
                                         propMgr.getFontState(area.getFontInfo()), red,
                                         green, blue, wrapOption, null, whiteSpaceCollapse,
                                         pageNumber.toCharArray(), 0, pageNumber.length(),
                                         ts, VerticalAlign.BASELINE);
        } else { // add pageNumberCitation to area to be resolved during rendering
            BlockArea blockArea = (BlockArea) area;
            LineArea la = blockArea.getCurrentLineArea();
            if (la == null) {
                return new Status(Status.AREA_FULL_NONE);
            }
            la.changeFont(propMgr.getFontState(area.getFontInfo()));
            la.changeColor(red, green, blue);
            la.changeWrapOption(wrapOption);
            la.changeWhiteSpaceCollapse(whiteSpaceCollapse);
            /*
             * la.changeHyphenation(language, country, hyphenate,
             * hyphenationChar, hyphenationPushCharacterCount,
             * hyphenationRemainCharacterCount);
             */

            // blockArea.setupLinkSet(null);
            la.addPageNumberCitation(refId, null);
            this.marker = -1;
        }


        if (this.marker == -1) {
            return new Status(Status.OK);
        } else {
            return new Status(Status.AREA_FULL_NONE);
        }

    }

}

