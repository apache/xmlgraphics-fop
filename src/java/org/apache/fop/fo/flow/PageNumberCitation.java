/*
 * $Id: PageNumberCitation.java,v 1.30 2003/03/05 20:38:22 jeremias Exp $
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
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Resolveable;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.UnresolvedPageNumber;
import org.apache.fop.area.inline.Word;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FOInputHandler;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.control.Document;
import org.apache.fop.fonts.Font;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.layout.TextState;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.LayoutProcessor;
import org.apache.fop.layoutmgr.LeafNodeLayoutManager;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.util.CharUtilities;

/**
 * Class modelling the fo:page-number-citation object. See Sec. 6.6.11 of the
 * XSL-FO Standard.
 * This inline fo is replaced with the text for a page number.
 * The page number used is the page that contains the start of the
 * block referenced with the ref-id attribute.
 */
public class PageNumberCitation extends FObj {
    /** FontInfo for this object **/
    protected Document fontInfo = null;
    /** Fontstate for this object **/
    protected Font fontState;

    private float red;
    private float green;
    private float blue;
    private int wrapOption;
    private String pageNumber;
    private String refId;
    private TextState ts;
    private InlineArea inline = null;
    private boolean unresolved = false;

    /**
     * @param parent FONode that is the parent of this object
     */
    public PageNumberCitation(FONode parent) {
        super(parent);
    }

    /**
     * @param st StuctureHandler object to set
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
                        return getInlineArea(parentLM);
                    }

                    public void addAreas(PositionIterator posIter,
                                         LayoutContext context) {
                        super.addAreas(posIter, context);
                        if (unresolved) {
                            parentLM.addUnresolvedArea(refId,
                                                       (Resolveable) inline);
                        }
                    }

                    protected void offsetArea(LayoutContext context) {
                        curArea.setOffset(context.getBaseline());
                    }
                };
        lm.setUserAgent(getUserAgent());
        lm.setFObj(this);
        lms.add(lm);
    }

    // if id can be resolved then simply return a word, otherwise
    // return a resolveable area
    private InlineArea getInlineArea(LayoutProcessor parentLM) {
        if (refId.equals("")) {
            getLogger().error("page-number-citation must contain \"ref-id\"");
            return null;
        }
        PageViewport page = parentLM.resolveRefID(refId);
        if (page != null) {
            String str = page.getPageNumber();
            // get page string from parent, build area
            Word word = new Word();
            inline = word;
            int width = getStringWidth(str);
            word.setWord(str);
            inline.setIPD(width);
            inline.setHeight(fontState.getAscender()
                             - fontState.getDescender());
            inline.setOffset(fontState.getAscender());

            inline.addTrait(Trait.FONT_NAME, fontState.getFontName());
            inline.addTrait(Trait.FONT_SIZE,
                            new Integer(fontState.getFontSize()));
            unresolved = false;
        } else {
            unresolved = true;
            inline = new UnresolvedPageNumber(refId);
            String str = "MMM"; // reserve three spaces for page number
            int width = getStringWidth(str);
            inline.setIPD(width);
            inline.setHeight(fontState.getAscender()
                             - fontState.getDescender());
            inline.setOffset(fontState.getAscender());

            inline.addTrait(Trait.FONT_NAME, fontState.getFontName());
            inline.addTrait(Trait.FONT_SIZE,
                            new Integer(fontState.getFontSize()));
        }
        return inline;
    }

    /**
     * @param str string to be measured
     * @return width (in millipoints ??) of the string
     */
    protected int getStringWidth(String str) {
        int width = 0;
        for (int count = 0; count < str.length(); count++) {
            width += CharUtilities.getCharWidth(str.charAt(count),
                                                fontState);
        }
        return width;
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
        // this.properties.get("ref-id");
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
        this.refId = this.properties.get("ref-id").getString();

        if (this.refId.equals("")) {
            //throw new FOPException("page-number-citation must contain \"ref-id\"");
        }

    }

}

