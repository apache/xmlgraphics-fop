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
import org.apache.fop.fo.properties.PercentLength;
import org.apache.fop.fonts.Font;
import org.apache.fop.fo.LMVisited;

/**
 * Class modelling fo:leader object. See Sec. 6.6.9 of the XSL-FO Standard.
 * The main property of fo:leader is leader-pattern.
 * The following patterns are treated: rule, space, dots and use-content.
 */
public class Leader extends FObjMixed implements LMVisited {

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

    /**
     * @todo convert to addProperties()
     */
    private void setup() {
        // Common Font Properties
        this.fontState = propMgr.getFontState(getFOInputHandler().getFontInfo());

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
    
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_LEADER;
    }
}
