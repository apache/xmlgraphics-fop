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

package org.apache.fop.layoutmgr;

import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.FilledArea;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.Space;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fonts.Font;
import org.apache.fop.traits.MinOptMax;

/**
 * LayoutManager for the fo:leader formatting object
 */
public class LeaderLayoutManager extends LeafNodeLayoutManager {

    Leader ldrNode;
    Font font = null;
    
    /**
     * Constructor
     *
     * @param node the formatting object that creates this area
     * @todo better null checking of font object
     */
    public LeaderLayoutManager(Leader node) {
        super(node);
        ldrNode = node;
        font = node.getFontState();
        setAlignment(node.getPropEnum(PR_LEADER_ALIGNMENT));
    }

    public InlineArea get(LayoutContext context) {
        return getLeaderInlineArea();
    }

    protected MinOptMax getAllocationIPD(int refIPD) {
        return getLeaderAllocIPD(refIPD);
    }

    private MinOptMax getLeaderAllocIPD(int ipd) {
        // length of the leader
        int opt = ldrNode.getLength(PR_LEADER_LENGTH | CP_OPTIMUM, ipd);
        int min = ldrNode.getLength(PR_LEADER_LENGTH | CP_MINIMUM, ipd);
        int max = ldrNode.getLength(PR_LEADER_LENGTH | CP_MAXIMUM, ipd);
        return new MinOptMax(min, opt, max);
    }

    private InlineArea getLeaderInlineArea() {
        InlineArea leaderArea = null;

        if (ldrNode.getLeaderPattern() == LeaderPattern.RULE) {
            org.apache.fop.area.inline.Leader leader = 
                new org.apache.fop.area.inline.Leader();
            leader.setRuleStyle(ldrNode.getRuleStyle());
            leader.setRuleThickness(ldrNode.getRuleThickness());
            leaderArea = leader;
        } else if (ldrNode.getLeaderPattern() == LeaderPattern.SPACE) {
            leaderArea = new Space();
        } else if (ldrNode.getLeaderPattern() == LeaderPattern.DOTS) {
            TextArea t = new TextArea();
            char dot = '.'; // userAgent.getLeaderDotCharacter();

            t.setTextArea("" + dot);
            t.addTrait(Trait.FONT_NAME, font.getFontName());
            t.addTrait(Trait.FONT_SIZE, new Integer(font.getFontSize()));
            // set offset of dot within inline parent
            t.setOffset(font.getAscender());
            int width = font.getCharWidth(dot);
            Space spacer = null;
            if (ldrNode.getPatternWidth() > width) {
                spacer = new Space();
                spacer.setWidth(ldrNode.getPatternWidth() - width);
                width = ldrNode.getPatternWidth();
            }
            FilledArea fa = new FilledArea();
            fa.setUnitWidth(width);
            fa.addChild(t);
            if (spacer != null) {
                fa.addChild(spacer);
            }
            fa.setHeight(font.getAscender());

            leaderArea = fa;
        } else if (ldrNode.getLeaderPattern() == LeaderPattern.USECONTENT) {
            if (ldrNode.getChildNodes() == null) {
                ldrNode.getLogger().error("Leader use-content with no content");
                return null;
            }

            // child FOs are assigned to the InlineStackingLM
            fobjIter = null;
            
            // get breaks then add areas to FilledArea
            FilledArea fa = new FilledArea();

            ContentLayoutManager clm = new ContentLayoutManager(fa);
            clm.setUserAgent(ldrNode.getUserAgent());
            addChildLM(clm);

            InlineStackingLayoutManager lm;
            lm = new InlineStackingLayoutManager(ldrNode);
            clm.addChildLM(lm);

            clm.fillArea(lm);
            int width = clm.getStackingSize();
            Space spacer = null;
            if (ldrNode.getPatternWidth() > width) {
                spacer = new Space();
                spacer.setWidth(ldrNode.getPatternWidth() - width);
                width = ldrNode.getPatternWidth();
            }
            fa.setUnitWidth(width);
            if (spacer != null) {
                fa.addChild(spacer);
            }
            leaderArea = fa;
        }
        return leaderArea;
     }
}
