/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources."
 */

package org.apache.fop.fo;

// FOP
import org.apache.fop.layout.Area;
import org.apache.fop.layout.BlockArea;
import org.apache.fop.layout.FontState;
import org.apache.fop.layout.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.apps.FOPException;
import org.apache.fop.system.BufferManager;

/**
 * a text node in the formatting object tree
 *
 * Modified by Mark Lillywhite, mark-fop@inomial.com.
 * Unfortunately the BufferManager implementatation holds
 * onto references to the character data in this object
 * longer than the lifetime of the object itself, causing
 * excessive memory consumption and OOM errors.
 */
public class FOText extends FObj {

    protected char[] ca;
    protected int start;
    protected int length;
    TextInfo textInfo;

    public static class TextInfo {
        FontState fs;
        float red;
        float green;
        float blue;
        int wrapOption;
        int whiteSpaceCollapse;
        int verticalAlign;

        // Textdecoration
        protected boolean underlined = false;
        protected boolean overlined = false;
        protected boolean lineThrough = false;
    }

    TextState ts;

    public FOText(char[] chars, int s, int e, TextInfo ti) {
        super(null);
        this.start = 0;
        this.ca = new char[e - s];
        for (int i = s; i < e; i++)
            ca[i - s] = chars[i];
        this.length = e - s;
        textInfo = ti;
    }

    public boolean willCreateArea() {
        if (textInfo.whiteSpaceCollapse == WhiteSpaceCollapse.FALSE
                && length > 0) {
            return true;
        }

        for (int i = start; i < start + length; i++) {
            char ch = ca[i];
            if (!((ch == ' ') || (ch == '\n') || (ch == '\r')
                    || (ch == '\t'))) {    // whitespace
                return true;
            }
        }
        return false;
    }

    public Status layout(Area area) throws FOPException {
        if (!(area instanceof BlockArea)) {
            log.error("text outside block area"
                                   + new String(ca, start, length));
            return new Status(Status.OK);
        }
        if (this.marker == START) {
            this.ts = new TextState();
            ts.setUnderlined(textInfo.underlined);
            ts.setOverlined(textInfo.overlined);
            ts.setLineThrough(textInfo.lineThrough);

            this.marker = this.start;
        }
        int orig_start = this.marker;
        this.marker = addText((BlockArea)area, textInfo.fs, textInfo.red, textInfo.green, textInfo.blue,
                              textInfo.wrapOption, this.getLinkSet(),
                              textInfo.whiteSpaceCollapse, ca, this.marker, length,
                              ts, textInfo.verticalAlign);
        if (this.marker == -1) {


            // commented out by Hani Elabed, 11/28/2000
            // if this object has been laid out
            // successfully, leave it alone....
            // Now, to prevent the array index out of
            // bound of LineArea.addText(), I have added
            // the following test at the beginning of that method.
            // if( start == -1 ) return -1;
            // see LineArea.addText()

            // this.marker = 0;
            return new Status(Status.OK);
        } else if (this.marker != orig_start) {
            return new Status(Status.AREA_FULL_SOME);
        } else {
            return new Status(Status.AREA_FULL_NONE);
        }
    }

    // font-variant support : addText is a wrapper for addRealText
    // added by Eric SCHAEFFER
    public static int addText(BlockArea ba, FontState fontState, float red,
                              float green, float blue, int wrapOption,
                              LinkSet ls, int whiteSpaceCollapse,
                              char data[], int start, int end,
                              TextState textState, int vAlign) {
        if (fontState.getFontVariant() == FontVariant.SMALL_CAPS) {
            FontState smallCapsFontState;
            try {
                int smallCapsFontHeight =
                    (int)(((double)fontState.getFontSize()) * 0.8d);
                smallCapsFontState = new FontState(fontState.getFontInfo(),
                                                   fontState.getFontFamily(),
                                                   fontState.getFontStyle(),
                                                   fontState.getFontWeight(),
                                                   smallCapsFontHeight,
                                                   FontVariant.NORMAL);
            } catch (FOPException ex) {
                smallCapsFontState = fontState;
                //log.error("Error creating small-caps FontState: "
                //                       + ex.getMessage());
            }

            // parse text for upper/lower case and call addRealText
            char c;
            boolean isLowerCase;
            int caseStart;
            FontState fontStateToUse;
            for (int i = start; i < end; ) {
                caseStart = i;
                c = data[i];
                isLowerCase = (java.lang.Character.isLetter(c)
                               && java.lang.Character.isLowerCase(c));
                while (isLowerCase
                        == (java.lang.Character.isLetter(c)
                            && java.lang.Character.isLowerCase(c))) {
                    if (isLowerCase) {
                        data[i] = java.lang.Character.toUpperCase(c);
                    }
                    i++;
                    if (i == end)
                        break;
                    c = data[i];
                }
                if (isLowerCase) {
                    fontStateToUse = smallCapsFontState;
                } else {
                    fontStateToUse = fontState;
                }
                int index = addRealText(ba, fontStateToUse, red, green, blue,
                                        wrapOption, ls, whiteSpaceCollapse,
                                        data, caseStart, i, textState,
                                        vAlign);
                if (index != -1) {
                    return index;
                }
            }

            return -1;
        }

        // font-variant normal
        return addRealText(ba, fontState, red, green, blue, wrapOption, ls,
                           whiteSpaceCollapse, data, start, end, textState,
                           vAlign);
    }

    protected static int addRealText(BlockArea ba, FontState fontState,
                                     float red, float green, float blue,
                                     int wrapOption, LinkSet ls,
                                     int whiteSpaceCollapse, char data[],
                                     int start, int end, TextState textState,
                                     int vAlign) {
        int ts, te;
        char[] ca;

        ts = start;
        te = end;
        ca = data;

        LineArea la = ba.getCurrentLineArea();
        if (la == null) {
            return start;
        }

        la.changeFont(fontState);
        la.changeColor(red, green, blue);
        la.changeWrapOption(wrapOption);
        la.changeWhiteSpaceCollapse(whiteSpaceCollapse);
        la.changeVerticalAlign(vAlign);
        // la.changeHyphenation(language, country, hyphenate,
        // hyphenationChar, hyphenationPushCharacterCount,
        // hyphenationRemainCharacterCount);
        ba.setupLinkSet(ls);

        ts = la.addText(ca, ts, te, ls, textState);
        // this.hasLines = true;

        while (ts != -1) {
            la = ba.createNextLineArea();
            if (la == null) {
                return ts;
            }
            la.changeFont(fontState);
            la.changeColor(red, green, blue);
            la.changeWrapOption(wrapOption);
            la.changeWhiteSpaceCollapse(whiteSpaceCollapse);
            // la.changeHyphenation(language, country, hyphenate,
            // hyphenationChar, hyphenationPushCharacterCount,
            // hyphenationRemainCharacterCount);
            ba.setupLinkSet(ls);

            ts = la.addText(ca, ts, te, ls, textState);
        }
        return -1;
    }

}

