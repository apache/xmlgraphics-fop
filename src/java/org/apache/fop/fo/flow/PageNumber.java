/*
 * $Id: PageNumber.java,v 1.30 2003/03/05 20:38:21 jeremias Exp $
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
package org.apache.fop.fo.flow;

// Java
import java.util.List;

// FOP
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.control.Document;
import org.apache.fop.layout.FontState;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.layout.TextState;
import org.apache.fop.util.CharUtilities;

import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.LeafNodeLayoutManager;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.Word;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FOInputHandler;
import org.apache.fop.area.Trait;

/**
 * Class modelling the fo:page-number object. See Sec. 6.6.10 of the XSL-FO
 * Standard.
 */
public class PageNumber extends FObj {
    /** FontInfo for this object */
    protected Document fontInfo = null;
    /** FontState for this object */
    protected FontState fontState;

    private float red;
    private float green;
    private float blue;
    private int wrapOption;
    private TextState ts;

    /**
     * @param parent FONode that is the parent of this object
     */
    public PageNumber(FONode parent) {
        super(parent);
    }

    /**
     * @param foih FOInputHandler to be set
     */
    public void setFOInputHandler(FOInputHandler foih) {
        super.setFOInputHandler(foih);
        fontInfo = foih.getFontInfo();
    }

    /**
     * Overridden from FObj
     * @param lms the list to which the layout manager(s) should be added
     */
    public void addLayoutManager(List lms) {
        setup();
        LayoutManager lm;
        lm = new LeafNodeLayoutManager() {
                    public InlineArea get(LayoutContext context) {
                        // get page string from parent, build area
                        Word inline = new Word();
                        String str = parentLM.getCurrentPageNumber();
                        int width = 0;
                    for (int count = 0; count < str.length(); count++) {
                            width += CharUtilities.getCharWidth(
                                       str.charAt(count), fontState);
                        }
                        inline.setWord(str);
                        inline.setIPD(width);
                        inline.setHeight(fontState.getAscender()
                                         - fontState.getDescender());
                        inline.setOffset(fontState.getAscender());

                        inline.addTrait(Trait.FONT_NAME,
                                        fontState.getFontName());
                        inline.addTrait(Trait.FONT_SIZE,
                                        new Integer(fontState.getFontSize()));

                        return inline;
                    }

                    protected void offsetArea(LayoutContext context) {
                        curArea.setOffset(context.getBaseline());
                    }
                };
        lm.setUserAgent(getUserAgent());
        lm.setFObj(this);
        lms.add(lm);
    }

    private void setup() {

        // Common Accessibility Properties
        CommonAccessibility mAccProps = propMgr.getAccessibilityProps();

        // Common Aural Properties
        CommonAural mAurProps = propMgr.getAuralProps();

        // Common Border, Padding, and Background Properties
        CommonBorderAndPadding bap = propMgr.getBorderAndPadding();
        CommonBackground bProps = propMgr.getBackgroundProps();

        // Common Font Properties
        this.fontState = propMgr.getFontState(fontInfo);

        // Common Margin Properties-Inline
        CommonMarginInline mProps = propMgr.getMarginInlineProps();

        // Common Relative Position Properties
        CommonRelativePosition mRelProps =
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
        // this.properties.get("score-spaces");
        // this.properties.get("text-decoration");
        // this.properties.get("text-shadow");
        // this.properties.get("text-transform");
        // this.properties.get("word-spacing");

        ColorType c = this.properties.get("color").getColorType();
        this.red = c.getRed();
        this.green = c.getGreen();
        this.blue = c.getBlue();

        this.wrapOption = this.properties.get("wrap-option").getEnum();
        ts = new TextState();

    }

}
