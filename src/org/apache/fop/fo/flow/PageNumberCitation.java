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
import org.apache.fop.util.CharUtilities;
import org.apache.fop.apps.StructureHandler;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.area.inline.Word;
import org.apache.fop.area.inline.UnresolvedPageNumber;
import org.apache.fop.area.Resolveable;
import org.apache.fop.area.Trait;

import java.util.List;
import java.util.ArrayList;

/**
 * Page number citation.
 * This inline fo is replaced with the text for a page number.
 * The page number used is the page that contains the start of the
 * block referenced with the ref-id attribute.
 */
public class PageNumberCitation extends FObj {
    protected FontInfo fontInfo = null;
    protected FontState fontState;

    float red;
    float green;
    float blue;
    int wrapOption;
    int whiteSpaceCollapse;
    String pageNumber;
    String refId;
    TextState ts;
    InlineArea inline = null;
    boolean unresolved = false;

    public PageNumberCitation(FONode parent) {
        super(parent);
    }

    public void setStructHandler(StructureHandler st) {
        super.setStructHandler(st);
        fontInfo = st.getFontInfo();
    }

    public void addLayoutManager(List lms) {
        setup();
        lms.add(new LeafNodeLayoutManager(this) {
                    public InlineArea get(LayoutContext context) {
                        return getInlineArea(parentLM);
                    }

                    public void addAreas(PositionIterator posIter,
                                             LayoutContext context) {
                        super.addAreas(posIter, context);
                        if(unresolved) {
                        parentLM.addUnresolvedArea(refId, (Resolveable)inline);
                        }
                    }
                }
               );
    }

    // is id can be resolved then simply return a word, otherwise
    // return a resolveable area
    private InlineArea getInlineArea(LayoutManager parentLM) {
        if (refId.equals("")) {
            getLogger().error("page-number-citation must contain \"ref-id\"");
            return null;
        }
        String str = parentLM.resolveRefID(refId);
        if(str != null) {
        // get page string from parent, build area
        Word word = new Word();
        inline = word;
        int width = getStringWidth(str);
        word.setWord(str);
        inline.setIPD(width);
        inline.setHeight(fontState.getAscender() -
                         fontState.getDescender());
        inline.setOffset(fontState.getAscender());

        inline.addTrait(Trait.FONT_NAME, fontState.getFontName());
        inline.addTrait(Trait.FONT_SIZE,
                        new Integer(fontState.getFontSize()));
        } else {
        unresolved = true;
        inline = new UnresolvedPageNumber(refId);
        str = "MMM"; // reserve three spaces for page number
        int width = getStringWidth(str);
        inline.setIPD(width);
        inline.setHeight(fontState.getAscender() -
                         fontState.getDescender());
        inline.setOffset(fontState.getAscender());

        inline.addTrait(Trait.FONT_NAME, fontState.getFontName());
        inline.addTrait(Trait.FONT_SIZE,
                        new Integer(fontState.getFontSize()));

        }
        return inline;
    }

    protected int getStringWidth(String str) {
        int width = 0;
        for (int count = 0; count < str.length(); count++) {
            width += CharUtilities.getCharWidth(str.charAt(count),
                                                fontState);
        }
        return width;
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
        this.fontState = propMgr.getFontState(fontInfo);

        // Common Margin Properties-Inline
        MarginInlineProps mProps = propMgr.getMarginInlineProps();

        // Common Relative Position Properties
        RelativePositionProps mRelProps =
          propMgr.getRelativePositionProps();

        // this.properties.get("alignment-adjust");
        // this.properties.get("alignment-baseline");
        // this.properties.get("baseline-shift");
        // this.properties.get("dominant-baseline");
        setupID();
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
            //throw new FOPException("page-number-citation must contain \"ref-id\"");
        }

    }

}

