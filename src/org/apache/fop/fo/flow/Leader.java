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
import org.apache.fop.layoutmgr.LeafNodeLayoutManager;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.area.MinOptMax;

import java.util.List;

/**
 * Implements fo:leader; main property of leader leader-pattern.
 * The following patterns are treated: rule, space, dots.
 * The pattern use-content is ignored, i.e. it still must be implemented.
 */
public class Leader extends FObjMixed {
    int ruleStyle;
    int ruleThickness;
    int leaderPattern;

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
        setup();

        org.apache.fop.area.inline.Leader leader = new org.apache.fop.area.inline.Leader();

        MinOptMax alloc = getAllocationIPD(refIPD);
        leader.setAllocationIPD(alloc);
        leader.setWidth(alloc.opt);

        if(leaderPattern == LeaderPattern.RULE) {
            leader.setRuleStyle(ruleStyle);
            leader.setRuleThickness(ruleThickness);
        }

        return leader;
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
        //this.fontState = propMgr.getFontState(area.getFontInfo());

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
        int leaderPatternWidth =
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

