/*
 * $Id: Leader.java,v 1.35 2003/03/06 11:36:30 jeremias Exp $
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
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.FilledArea;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.Space;
import org.apache.fop.area.inline.Word;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.PercentLength;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.FOInputHandler;
import org.apache.fop.fo.properties.LeaderPattern;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.control.Document;
import org.apache.fop.fonts.Font;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.layoutmgr.ContentLayoutManager;
import org.apache.fop.layoutmgr.InlineStackingLayoutManager;
import org.apache.fop.layoutmgr.LMiter;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LeafNodeLayoutManager;
import org.apache.fop.layoutmgr.MinOptMax;
import org.apache.fop.util.CharUtilities;

/**
 * Class modelling fo:leader object. See Sec. 6.6.9 of the XSL-FO Standard.
 * The main property of fo:leader is leader-pattern.
 * The following patterns are treated: rule, space, dots and use-content.
 */
public class Leader extends FObjMixed {

    private int ruleStyle;
    private int ruleThickness;
    private int leaderPattern;
    private int patternWidth;
    /** FontInfo for this object */
    protected Document fontInfo = null;
    /** FontState for this object */
    protected Font fontState;
    protected InlineArea leaderArea = null;

    /**
     * @param parent FONode that is the parent of this object
     */
    public Leader(FONode parent) {
        super(parent);
    }

    /**
     *
     * @param list the list to which the layout manager(s) should be added
     */
    public void addLayoutManager(List list) {
        LeafNodeLayoutManager lm = new LeafNodeLayoutManager() {
                public InlineArea get(LayoutContext context) {
                    return getInlineArea();
                }

                protected MinOptMax getAllocationIPD(int refIPD) {
                   return getAllocIPD(refIPD);
                }

                /*protected void offsetArea(LayoutContext context) {
                    if(leaderPattern == LeaderPattern.DOTS) {
                        curArea.setOffset(context.getBaseline());
                    }
                }*/
            };
        lm.setUserAgent(getUserAgent());
        lm.setFObj(this);
        lm.setAlignment(properties.get("leader-alignment").getEnum());
        list.add(lm);
    }

    protected InlineArea getInlineArea() {
        if (leaderArea == null) {
            createLeaderArea();
        }
        return leaderArea;
    }

    protected void createLeaderArea() {
        setup();

        if (leaderPattern == LeaderPattern.RULE) {
            org.apache.fop.area.inline.Leader leader = new org.apache.fop.area.inline.Leader();

            leader.setRuleStyle(ruleStyle);
            leader.setRuleThickness(ruleThickness);

            leaderArea = leader;
        } else if (leaderPattern == LeaderPattern.SPACE) {
            leaderArea = new Space();
        } else if (leaderPattern == LeaderPattern.DOTS) {
            Word w = new Word();
            char dot = '.'; // userAgent.getLeaderDotCharacter();

            w.setWord("" + dot);
            w.addTrait(Trait.FONT_NAME, fontState.getFontName());
            w.addTrait(Trait.FONT_SIZE,
                             new Integer(fontState.getFontSize()));
            // set offset of dot within inline parent
            w.setOffset(fontState.getAscender());
            int width = CharUtilities.getCharWidth(dot, fontState);
            Space spacer = null;
            if (patternWidth > width) {
                spacer = new Space();
                spacer.setWidth(patternWidth - width);
                width = patternWidth;
            }
            FilledArea fa = new FilledArea();
            fa.setUnitWidth(width);
            fa.addChild(w);
            if (spacer != null) {
                fa.addChild(spacer);
            }
            fa.setHeight(fontState.getAscender());

            leaderArea = fa;
        } else if (leaderPattern == LeaderPattern.USECONTENT) {
            if (children == null) {
                getLogger().error("Leader use-content with no content");
                return;
            }
            InlineStackingLayoutManager lm;
            lm = new InlineStackingLayoutManager();
            lm.setUserAgent(getUserAgent());
            lm.setFObj(this);
            lm.setLMiter(new LMiter(children.listIterator()));
            lm.init();

            // get breaks then add areas to FilledArea
            FilledArea fa = new FilledArea();

            ContentLayoutManager clm = new ContentLayoutManager(fa);
            clm.setUserAgent(getUserAgent());
            lm.setParent(clm);

            clm.fillArea(lm);
            int width = clm.getStackingSize();
            Space spacer = null;
            if (patternWidth > width) {
                spacer = new Space();
                spacer.setWidth(patternWidth - width);
                width = patternWidth;
            }
            fa.setUnitWidth(width);
            if (spacer != null) {
                fa.addChild(spacer);
            }
            leaderArea = fa;
        }
    }

    /**
     * @param foih FOInputHandler to set
     */
    public void setFOInputHandler(FOInputHandler foih) {
        super.setFOInputHandler(foih);
        fontInfo = foih.getFontInfo();
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
        CommonRelativePosition mRelProps = propMgr.getRelativePositionProps();

        // this.properties.get("alignment-adjust");
        // this.properties.get("alignment-baseline");
        // this.properties.get("baseline-shift");
        // this.properties.get("color");
        // this.properties.get("dominant-baseline");
        // this.properties.get("text-depth");
        // this.properties.get("text-altitude");
        setupID();
        // this.properties.get("leader-alignment");
        // this.properties.get("leader-length");
        // this.properties.get("leader-pattern");
        // this.properties.get("leader-pattern-width");
        // this.properties.get("rule-style");
        // this.properties.get("rule-thickness");
        // this.properties.get("letter-spacing");
        // this.properties.get("line-height");
        // this.properties.get("line-height-shift-adjustment");
        // this.properties.get("text-shadow");
        // this.properties.get("visibility");
        // this.properties.get("word-spacing");
        // this.properties.get("z-index");

        // color properties
        ColorType c = this.properties.get("color").getColorType();
        float red = c.getRed();
        float green = c.getGreen();
        float blue = c.getBlue();

        // fo:leader specific properties
        // determines the pattern of leader; allowed values: space, rule,dots, use-content
        leaderPattern = this.properties.get("leader-pattern").getEnum();
        switch(leaderPattern) {
            case LeaderPattern.SPACE:
                // use Space
            break;
            case LeaderPattern.RULE:
                // the following properties only apply
                // for leader-pattern = "rule"
                ruleThickness =
                         properties.get("rule-thickness").getLength().getValue();
                ruleStyle = properties.get("rule-style").getEnum();
            break;
            case LeaderPattern.DOTS:
            break;
            case LeaderPattern.USECONTENT:
                // use inline layout manager to create inline areas
                // add the inline parent multiple times until leader full
            break;
        }

        // if leaderPatternWidth = 0 = default = use-font-metric
        patternWidth =
            this.properties.get("leader-pattern-width").getLength().getValue();

    }

    protected MinOptMax getAllocIPD(int ipd) {
        // length of the leader
        int opt = getLength("leader-length.optimum", ipd);
        int min = getLength("leader-length.minimum", ipd);
        int max = getLength("leader-length.maximum", ipd);

        return new MinOptMax(min, opt, max);
    }

    protected int getLength(String prop, int dim) {
        int length;
        Length maxlength = properties.get(prop).getLength();
        if (maxlength instanceof PercentLength) {
            length = (int)(((PercentLength)maxlength).value()
                                      * dim);
        } else {
            length = maxlength.getValue();
        }
        return length;
    }
}

