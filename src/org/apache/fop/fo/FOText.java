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

/**
 * a text node in the formatting object tree
 *
 */
public class FOText extends FONode {

    private char[] ca;

    private FontState fs;
    private float red;
    private float green;
    private float blue;
    private int wrapOption;
    private int whiteSpaceCollapse;
    private int verticalAlign;

    // Textdecoration
    private TextState ts;

    public FOText(StringBuffer b, FObj parent) {
        super(parent);
        this.ca = new char[b.length()];
        b.getChars(0,b.length(),ca,0);
    }

    public void setTextState(TextState ts) {
        this.ts = ts;
    }

    public boolean willCreateArea() {
        this.whiteSpaceCollapse =
            this.parent.properties.get("white-space-collapse").getEnum();
        if (this.whiteSpaceCollapse == WhiteSpaceCollapse.FALSE
                && ca.length > 0) {
            return true;
        }

        for (int i = 0; i < ca.length; i++) {
            char ch = ca[i];
            if (!((ch == ' ') || (ch == '\n') || (ch == '\r')
                    || (ch == '\t'))) {    // whitespace
                return true;
            }
        }
        return false;
    }

    public boolean mayPrecedeMarker() {
        for (int i = 0; i < ca.length; i++) {
            char ch = ca[i];
            if ((ch != ' ') || (ch != '\n') || (ch != '\r')
                    || (ch != '\t')) {    // whitespace
                return true;
            }
        }
        return false;
    }
  
    public Status layout(Area area) throws FOPException {
        if (!(area instanceof BlockArea)) {
            log.error("text outside block area"
                                   + new String(ca, 0, ca.length));
            return new Status(Status.OK);
        }
        if (this.marker == START) {
            String fontFamily =
                this.parent.properties.get("font-family").getString();
            String fontStyle =
                this.parent.properties.get("font-style").getString();
            String fontWeight =
                this.parent.properties.get("font-weight").getString();
            int fontSize =
                this.parent.properties.get("font-size").getLength().mvalue();
            // font-variant support
            // added by Eric SCHAEFFER
            int fontVariant =
                this.parent.properties.get("font-variant").getEnum();

            int letterSpacing =
                this.parent.properties.get("letter-spacing").getLength().mvalue();
            this.fs = new FontState(area.getFontInfo(), fontFamily,
                                    fontStyle, fontWeight, fontSize,
                                    fontVariant, letterSpacing);

            ColorType c = this.parent.properties.get("color").getColorType();
            this.red = c.red();
            this.green = c.green();
            this.blue = c.blue();

            this.verticalAlign =
                this.parent.properties.get("vertical-align").getEnum();

            this.wrapOption =
                this.parent.properties.get("wrap-option").getEnum();
            this.whiteSpaceCollapse =
                this.parent.properties.get("white-space-collapse").getEnum();
            this.marker = 0;
        }
        int orig_start = this.marker;
        this.marker = addText((BlockArea)area, fs, red, green, blue,
                              wrapOption, this.getLinkSet(),
                              whiteSpaceCollapse, ca, this.marker, ca.length,
                              ts, verticalAlign);
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

        start = la.addText(data, start, end, ls, textState);
        // this.hasLines = true;

        while ( start != -1) {
            la = ba.createNextLineArea();
            if (la == null) {
                return start;
            }
            la.changeFont(fontState);
            la.changeColor(red, green, blue);
            la.changeWrapOption(wrapOption);
            la.changeWhiteSpaceCollapse(whiteSpaceCollapse);
            // la.changeHyphenation(language, country, hyphenate,
            // hyphenationChar, hyphenationPushCharacterCount,
            // hyphenationRemainCharacterCount);
            ba.setupLinkSet(ls);

            start = la.addText(data, start, end, ls, textState);
        }
        return -1;
    }


}
