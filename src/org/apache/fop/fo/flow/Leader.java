/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.layout.*;
import org.apache.fop.layout.FontState;
import org.apache.fop.apps.FOPException;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.InlineStackingLayoutManager;
import org.apache.fop.layoutmgr.LeafNodeLayoutManager;
import org.apache.fop.layoutmgr.ContentLayoutManager;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LMiter;
import org.apache.fop.area.MinOptMax;
import org.apache.fop.area.inline.Space;
import org.apache.fop.area.inline.Word;
import org.apache.fop.area.inline.Stretch;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.FilledArea;
import org.apache.fop.util.CharUtilities;
import org.apache.fop.apps.StructureHandler;
import org.apache.fop.area.Trait;

import java.util.List;
import java.util.ArrayList;

/**
 * Implements fo:leader; main property of leader leader-pattern.
 * The following patterns are treated: rule, space, dots.
 * The pattern use-content is ignored, i.e. it still must be implemented.
 */
public class Leader extends FObjMixed {
    int ruleStyle;
    int ruleThickness;
    int leaderPattern;
    int patternWidth;
    protected FontInfo fontInfo = null;
    protected FontState fontState;
    protected InlineArea leaderArea = null;

    public Leader(FONode parent) {
        super(parent);
    }

    public void addLayoutManager(List list) {
        LeafNodeLayoutManager lm = new LeafNodeLayoutManager(this) {
                public InlineArea get(LayoutContext context) {
                    int refIPD = context.getRefIPD();
                    return getInlineArea(refIPD);
                }
            };
        lm.setAlignment(properties.get("leader-alignment").getEnum());
        list.add(lm);
    }

    protected InlineArea getInlineArea(int refIPD) {
        if(leaderArea == null) {
            createLeaderArea();
        }
        MinOptMax alloc = getAllocationIPD(refIPD);
        if(leaderArea instanceof Stretch) {
            ((Stretch)leaderArea).setAllocationIPD(alloc);
        } else if(leaderArea instanceof FilledArea) {
            ((FilledArea)leaderArea).setAllocationIPD(alloc);
        }
        leaderArea.setWidth(alloc.opt);
        return leaderArea;
    }

    protected void createLeaderArea() {
        setup();

        if(leaderPattern == LeaderPattern.RULE) {
            org.apache.fop.area.inline.Leader leader = new org.apache.fop.area.inline.Leader();

            leader.setRuleStyle(ruleStyle);
            leader.setRuleThickness(ruleThickness);

            leaderArea = leader;
        } else if (leaderPattern == LeaderPattern.SPACE) {
            Space space = new Space();

            leaderArea = space;
        } else if(leaderPattern == LeaderPattern.DOTS) {
            Word w = new Word();
            char dot = '.'; // userAgent.getLeaderDotChar();

            w.setWord("" + dot);
            w.addTrait(Trait.FONT_NAME, fontState.getFontName());
            w.addTrait(Trait.FONT_SIZE,
                             new Integer(fontState.getFontSize()));
            // set offset of dot within inline parent
            w.setOffset(fontState.getAscender());
            int width = CharUtilities.getCharWidth(dot, fontState);
            Space spacer = null;
            if(patternWidth > width) {
                spacer = new Space();
                spacer.setWidth(patternWidth - width);
                width = patternWidth;
            }
            FilledArea fa = new FilledArea();
            fa.setUnitWidth(width);
            fa.addChild(w);
            if(spacer != null) {
                fa.addChild(spacer);
            }

            leaderArea = fa;
        } else if(leaderPattern == LeaderPattern.USECONTENT) {
            InlineStackingLayoutManager lm;
            lm = new InlineStackingLayoutManager(this,
                     new LMiter(children.listIterator()));
            lm.init();

            // get breaks then add areas to FilledArea
            FilledArea fa = new FilledArea();

            ContentLayoutManager clm = new ContentLayoutManager(fa);
            lm.setParentLM(clm);

            clm.fillArea(lm);
            int width = clm.getStackingSize();
            Space spacer = null;
            if(patternWidth > width) {
                spacer = new Space();
                spacer.setWidth(patternWidth - width);
                width = patternWidth;
            }
            fa.setUnitWidth(width);
            if(spacer != null) {
                fa.addChild(spacer);
            }
            leaderArea = fa;
        }
    }

    public void setStructHandler(StructureHandler st) {
        super.setStructHandler(st);
        fontInfo = st.getFontInfo();
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
        RelativePositionProps mRelProps = propMgr.getRelativePositionProps();

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
        float red = c.red();
        float green = c.green();
        float blue = c.blue();

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
                         properties.get("rule-thickness").getLength().mvalue();
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
            this.properties.get("leader-pattern-width").getLength().mvalue();

    }

    protected MinOptMax getAllocationIPD(int ipd) {
        // length of the leader
        int opt = getLength("leader-length.optimum", ipd);
        int min = getLength("leader-length.minimum", ipd);
        int max = getLength("leader-length.maximum", ipd);

        return new MinOptMax(min, opt, max);
    }

    protected int getLength(String prop, int dim) {
        int length;
        Length maxlength = properties.get(prop).getLength();
        if(maxlength instanceof PercentLength) {
            length = (int)(((PercentLength)maxlength).value()
                                      * dim);
        } else {
            length = maxlength.mvalue();
        }
        return length;
    }
}

