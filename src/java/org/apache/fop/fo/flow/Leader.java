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

// Java
import java.util.List;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.fo.properties.SpaceProperty;
import org.apache.fop.layoutmgr.LeaderLayoutManager;

/**
 * Class modelling fo:leader object.
 * The main property of fo:leader is leader-pattern.
 * The following patterns are treated: rule, space, dots and use-content.
 * @todo implement validateChildNode()
 */
public class Leader extends FObjMixed {
    // The value of properties relevant for fo:leader.
    private CommonAccessibility commonAccessibility;
    private CommonAural commonAural;
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private CommonFont commonFont;
    private CommonMarginInline commonMarginInline;
    private CommonRelativePosition commonRelativePosition;
    private Length alignmentAdjust;
    private int alignmentBaseline;
    private Length baselineShift;
    private ColorType color;
    private int dominantBaseline;
    private Length textDepth;
    private Length textAltitude;
    private String id;
    private KeepProperty keepWithNext;
    private KeepProperty keepWithPrevious;
    private int leaderAlignment;
    private LengthRangeProperty leaderLength;
    private int leaderPattern;
    private Length leaderPatternWidth;
    private int ruleStyle;
    private Length ruleThickness;
    // private ToBeImplementedProperty letterSpacing;
    private Length lineHeight;
    // private ToBeImplementedProperty textShadow;
    // private ToBeImplementedProperty visibility;
    private SpaceProperty wordSpacing;
    // End of property values

    /**
     * @param parent FONode that is the parent of this object
     */
    public Leader(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        commonAccessibility = pList.getAccessibilityProps();
        commonAural = pList.getAuralProps();
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        commonFont = pList.getFontProps();
        commonMarginInline = pList.getMarginInlineProps();
        commonRelativePosition = pList.getRelativePositionProps();
        alignmentAdjust = pList.get(PR_ALIGNMENT_ADJUST).getLength();
        alignmentBaseline = pList.get(PR_ALIGNMENT_BASELINE).getEnum();
        baselineShift = pList.get(PR_BASELINE_SHIFT).getLength();
        color = pList.get(PR_COLOR).getColorType();
        dominantBaseline = pList.get(PR_DOMINANT_BASELINE).getEnum();
        textDepth = pList.get(PR_TEXT_DEPTH).getLength();
        textAltitude = pList.get(PR_TEXT_ALTITUDE).getLength();
        id = pList.get(PR_ID).getString();
        keepWithNext = pList.get(PR_KEEP_WITH_NEXT).getKeep();
        keepWithPrevious = pList.get(PR_KEEP_WITH_PREVIOUS).getKeep();
        leaderAlignment = pList.get(PR_LEADER_ALIGNMENT).getEnum();
        leaderLength = pList.get(PR_LEADER_LENGTH).getLengthRange();
        leaderPattern = pList.get(PR_LEADER_PATTERN).getEnum();
        leaderPatternWidth = pList.get(PR_LEADER_PATTERN_WIDTH).getLength();
        switch(leaderPattern) {
        case LeaderPattern.SPACE:
            // use Space
            break;
        case LeaderPattern.RULE:
            // the following properties only apply
            // for leader-pattern = "rule"
            ruleStyle = pList.get(PR_RULE_STYLE).getEnum();
            ruleThickness = pList.get(PR_RULE_THICKNESS).getLength();
            break;
        case LeaderPattern.DOTS:
            break;
        case LeaderPattern.USECONTENT:
            // use inline layout manager to create inline areas
            // add the inline parent multiple times until leader full
            break;
        }
        // letterSpacing = pList.get(PR_LETTER_SPACING);
        lineHeight = pList.get(PR_LINE_HEIGHT).getLength();
        // textShadow = pList.get(PR_TEXT_SHADOW);
        // visibility = pList.get(PR_VISIBILITY);
        wordSpacing = pList.get(PR_WORD_SPACING).getSpace();
    }

    /**
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws FOPException {
        checkId(id);
    }
 
    /**
     * Return the Common Font Properties.
     */
    public CommonFont getCommonFont() {
        return commonFont;
    }

    /**
     * Return the "id" property.
     */
    public String getId() {
        return id;
    }

    /**
     * Return the "rule-style" property.
     */
    public int getRuleStyle() {
        return ruleStyle;
    }

    /**
     * Return the "rule-thickness" property.
     */
    public Length getRuleThickness() {
        return ruleThickness;
    }

    /**
     * Return the "leader-alignment" property.
     */
    public int getLeaderAlignment() {
        return leaderAlignment;
    }

    /**
     * Return the "leader-length" property.
     */
    public LengthRangeProperty getLeaderLength() {
        return leaderLength;
    }

    /**
     * Return the "leader-pattern" property.
     */
    public int getLeaderPattern() {
        return leaderPattern;
    }

    /**
     * Return the "leader-pattern-width" property.
     */
    public Length getLeaderPatternWidth() {
        return leaderPatternWidth;
    }

    /**
     * @see org.apache.fop.fo.FONode#addLayoutManager(List)
     */
    public void addLayoutManager(List list) {
        LeaderLayoutManager lm = new LeaderLayoutManager(this);
        list.add(lm);
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:leader";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_LEADER;
    }
}
