/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fo.flow;

import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.layoutmgr.AddLMVisitor;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.fo.properties.PercentLength;
import org.apache.fop.fonts.Font;

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
    /** FontState for this object */
    protected Font fontState;

    /**
     * @param parent FONode that is the parent of this object
     */
    public Leader(FONode parent) {
        super(parent);
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
        this.fontState = propMgr.getFontState(getFOInputHandler().getFontInfo());

        // Common Margin Properties-Inline
        CommonMarginInline mProps = propMgr.getMarginInlineProps();

        // Common Relative Position Properties
        CommonRelativePosition mRelProps = propMgr.getRelativePositionProps();

        // this.propertyList.get("alignment-adjust");
        // this.propertyList.get("alignment-baseline");
        // this.propertyList.get("baseline-shift");
        // this.propertyList.get("color");
        // this.propertyList.get("dominant-baseline");
        // this.propertyList.get("text-depth");
        // this.propertyList.get("text-altitude");
        setupID();
        // this.propertyList.get("leader-alignment");
        // this.propertyList.get("leader-length");
        // this.propertyList.get("leader-pattern");
        // this.propertyList.get("leader-pattern-width");
        // this.propertyList.get("rule-style");
        // this.propertyList.get("rule-thickness");
        // this.propertyList.get("letter-spacing");
        // this.propertyList.get("line-height");
        // this.propertyList.get("line-height-shift-adjustment");
        // this.propertyList.get("text-shadow");
        // this.propertyList.get("visibility");
        // this.propertyList.get("word-spacing");
        // this.propertyList.get("z-index");

        // color properties
        ColorType c = this.propertyList.get(PR_COLOR).getColorType();
        float red = c.getRed();
        float green = c.getGreen();
        float blue = c.getBlue();

        // fo:leader specific properties
        // determines the pattern of leader; allowed values: space, rule,dots, use-content
        leaderPattern = this.propertyList.get(PR_LEADER_PATTERN).getEnum();
        switch(leaderPattern) {
            case LeaderPattern.SPACE:
                // use Space
            break;
            case LeaderPattern.RULE:
                // the following properties only apply
                // for leader-pattern = "rule"
                ruleThickness =
                         propertyList.get(PR_RULE_THICKNESS).getLength().getValue();
                ruleStyle = propertyList.get(PR_RULE_STYLE).getEnum();
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
            this.propertyList.get(PR_LEADER_PATTERN_WIDTH).getLength().getValue();

    }

    public int getLength(int propId, int dim) {
        int length;
        Length maxlength = propertyList.get(propId).getLength();
        if (maxlength instanceof PercentLength) {
            length = (int)(((PercentLength)maxlength).value()
                                      * dim);
        } else {
            length = maxlength.getValue();
        }
        return length;
    }

    public int getRuleStyle() {
        return ruleStyle;
    }

    public int getRuleThickness() {
        return ruleThickness;
    }

    public int getLeaderPattern() {
        return leaderPattern;
    }

    public Font getFontState() {
        return fontState;
    }

    public int getPatternWidth() {
        return patternWidth;
    }

    public String getName() {
        return "fo:leader";
    }
    
    /**
     * This is a hook for the AddLMVisitor class to be able to access
     * this object.
     * @param aLMV the AddLMVisitor object that can access this object.
     */
    public void acceptVisitor(AddLMVisitor aLMV) {
        setup();
        aLMV.serveLeader(this);
    }
}
