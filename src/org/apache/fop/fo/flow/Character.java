/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// fop
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.layout.BlockArea;
import org.apache.fop.layout.*;
import org.apache.fop.layout.inline.InlineArea;
import org.apache.fop.fo.FObj;
import org.apache.fop.layout.FontState;
import org.apache.fop.layout.LineArea;
import org.apache.fop.apps.FOPException;


/**
 * this class represents the flow object 'fo:character'. Its use is defined by
 * the spec: "The fo:character flow object represents a character that is mapped to
 * a glyph for presentation. It is an atomic unit to the formatter.
 * When the result tree is interpreted as a tree of formatting objects,
 * a character in the result tree is treated as if it were an empty
 * element of type fo:character with a character attribute
 * equal to the Unicode representation of the character.
 * The semantics of an "auto" value for character properties, which is
 * typically their initial value,  are based on the Unicode codepoint.
 * Overrides may be specified in an implementation-specific manner." (6.6.3)
 *
 */
public class Character extends FObj {
    public final static int OK = 0;
    public final static int DOESNOT_FIT = 1;

    private char characterValue;

    public Character(FONode parent) {
        super(parent);
    }

    public Status layout(Area area) throws FOPException {
        BlockArea blockArea;
        if (!(area instanceof BlockArea)) {
            log.warn("currently Character can only be in a BlockArea");
            return new Status(Status.OK);
        }
        blockArea = (BlockArea)area;
        boolean textDecoration;

        // Common Aural Properties
        AuralProps mAurProps = propMgr.getAuralProps();

        // Common Border, Padding, and Background Properties
        BorderAndPadding bap = propMgr.getBorderAndPadding();
        BackgroundProps bProps = propMgr.getBackgroundProps();

        // Common Font Properties
        //this.fontState = propMgr.getFontState(area.getFontInfo());

        // Common Hyphenation Properties
        HyphenationProps mHyphProps = propMgr.getHyphenationProps();

        // Common Margin Properties-Inline
        MarginInlineProps mProps = propMgr.getMarginInlineProps();

        // Common Relative Position Properties
        RelativePositionProps mRelProps = propMgr.getRelativePositionProps();

        // this.properties.get("alignment-adjust");
        // this.properties.get("treat-as-word-space");
        // this.properties.get("alignment-baseline");
        // this.properties.get("baseline-shift");
        // this.properties.get("character");
        // this.properties.get("color");
        // this.properties.get("dominant-baseline");
        // this.properties.get("text-depth");
        // this.properties.get("text-altitude");
        // this.properties.get("glyph-orientation-horizontal");
        // this.properties.get("glyph-orientation-vertical");
        // this.properties.get("id");
        // this.properties.get("keep-with-next");
        // this.properties.get("keep-with-previous");
        // this.properties.get("letter-spacing");
        // this.properties.get("line-height");
        // this.properties.get("line-height-shift-adjustment");
        // this.properties.get("score-spaces");
        // this.properties.get("suppress-at-line-break");
        // this.properties.get("text-decoration");
        // this.properties.get("text-shadow");
        // this.properties.get("text-transform");
        // this.properties.get("word-spacing");

        // color properties
        ColorType c = this.properties.get("color").getColorType();
        float red = c.red();
        float green = c.green();
        float blue = c.blue();

        int whiteSpaceCollapse =
            this.properties.get("white-space-collapse").getEnum();
        int wrapOption = ((FObj)this.parent).properties.get("wrap-option").getEnum();

        int tmp = this.properties.get("text-decoration").getEnum();
        if (tmp == org.apache.fop.fo.properties.TextDecoration.UNDERLINE) {
            textDecoration = true;
        } else {
            textDecoration = false;
        }

        // Character specific properties
        characterValue = this.properties.get("character").getCharacter();


        // initialize id
        String id = this.properties.get("id").getString();
        blockArea.getIDReferences().initializeID(id, blockArea);

        LineArea la = blockArea.getCurrentLineArea();
        if (la == null) {
            return new Status(Status.AREA_FULL_NONE);
        }
        la.changeFont(propMgr.getFontState(area.getFontInfo()));
        la.changeColor(red, green, blue);
        la.changeWrapOption(wrapOption);
        la.changeWhiteSpaceCollapse(whiteSpaceCollapse);
        blockArea.setupLinkSet(this.getLinkSet());
        int result = la.addCharacter(characterValue, this.getLinkSet(),
                                     textDecoration);
        if (result == Character.DOESNOT_FIT) {
            la = blockArea.createNextLineArea();
            if (la == null) {
                return new Status(Status.AREA_FULL_NONE);
            }
            la.changeFont(propMgr.getFontState(area.getFontInfo()));
            la.changeColor(red, green, blue);
            la.changeWrapOption(wrapOption);
            la.changeWhiteSpaceCollapse(whiteSpaceCollapse);
            blockArea.setupLinkSet(this.getLinkSet());
            la.addCharacter(characterValue, this.getLinkSet(),
                            textDecoration);
        }
        return new Status(Status.OK);

    }

    public CharIterator charIterator() {
	return new OneCharIterator(characterValue);
	// But what it the character is ignored due to white space handling?
    }


}
