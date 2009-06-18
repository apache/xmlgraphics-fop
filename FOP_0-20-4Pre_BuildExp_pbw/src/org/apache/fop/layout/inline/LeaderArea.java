/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout.inline;

import org.apache.fop.render.Renderer;
import org.apache.fop.layout.*;

import java.util.Vector;
import java.util.Enumeration;

public class LeaderArea extends InlineArea {

    int ruleThickness;
    int leaderLengthOptimum;
    int leaderPattern;
    int ruleStyle;

    float red, green, blue;
    public LeaderArea(FontState fontState, float red, float green,
                      float blue, String text, int leaderLengthOptimum,
                      int leaderPattern, int ruleThickness, int ruleStyle) {
        super(fontState, leaderLengthOptimum, red, green, blue);

        this.leaderPattern = leaderPattern;
        this.leaderLengthOptimum = leaderLengthOptimum;
        this.ruleStyle = ruleStyle;
        // following the xsl spec rule: if rule-style="none" set thickness to 0;
        // actually in pdf this doesn't work, because a very thin line is still shown
        // this is handled in the pdf renderer
        if (ruleStyle == org.apache.fop.fo.properties.RuleStyle.NONE) {
            ruleThickness = 0;
        }
        this.ruleThickness = ruleThickness;
    }

    public int getRuleThickness() {
        return this.ruleThickness;
    }

    public int getRuleStyle() {
        return this.ruleStyle;
    }

    public int getLeaderPattern() {
        return this.leaderPattern;
    }

    public int getLeaderLength() {
        return this.contentRectangleWidth;
    }



}
