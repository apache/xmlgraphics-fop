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
import org.apache.fop.datatypes.PercentBase;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fonts.Font;
import org.apache.fop.traits.MinOptMax;

import java.util.List;
import java.util.LinkedList;

/**
 * LayoutManager for the fo:leader formatting object
 */
public class LeaderLayoutManager extends LeafNodeLayoutManager {
    private Leader fobj;
    Font font = null;
    
    /**
     * Constructor
     *
     * @param node the formatting object that creates this area
     * @todo better null checking of font object
     */
    public LeaderLayoutManager(Leader node) {
        super(node);
        fobj = node;
        font = node.getFontState();
        setAlignment(node.getLeaderAlignment());
    }

    public InlineArea get(LayoutContext context) {
        return getLeaderInlineArea();
    }

    protected MinOptMax getAllocationIPD(int refIPD) {
        return getLeaderAllocIPD(refIPD);
    }

    private MinOptMax getLeaderAllocIPD(int ipd) {
        // length of the leader
        fobj.setLayoutDimension(PercentBase.BLOCK_IPD, ipd);
        int opt = fobj.getLeaderLength().getOptimum().getLength().getValue();
        int min = fobj.getLeaderLength().getMinimum().getLength().getValue();
        int max = fobj.getLeaderLength().getMaximum().getLength().getValue();
        return new MinOptMax(min, opt, max);
    }

    private InlineArea getLeaderInlineArea() {
        InlineArea leaderArea = null;

        if (fobj.getLeaderPattern() == LeaderPattern.RULE) {
            org.apache.fop.area.inline.Leader leader = 
                new org.apache.fop.area.inline.Leader();
            leader.setRuleStyle(fobj.getRuleStyle());
            leader.setRuleThickness(fobj.getRuleThickness());
            leaderArea = leader;
        } else if (fobj.getLeaderPattern() == LeaderPattern.SPACE) {
            leaderArea = new Space();
        } else if (fobj.getLeaderPattern() == LeaderPattern.DOTS) {
            TextArea t = new TextArea();
            char dot = '.'; // userAgent.getLeaderDotCharacter();

            t.setTextArea("" + dot);
            t.addTrait(Trait.FONT_NAME, font.getFontName());
            t.addTrait(Trait.FONT_SIZE, new Integer(font.getFontSize()));
            // set offset of dot within inline parent
            t.setOffset(font.getAscender());
            int width = font.getCharWidth(dot);
            Space spacer = null;
            if (fobj.getPatternWidth() > width) {
                spacer = new Space();
                spacer.setIPD(fobj.getPatternWidth() - width);
                width = fobj.getPatternWidth();
            }
            FilledArea fa = new FilledArea();
            fa.setUnitWidth(width);
            fa.addChild(t);
            if (spacer != null) {
                fa.addChild(spacer);
            }
            fa.setBPD(font.getAscender());

            leaderArea = fa;
        } else if (fobj.getLeaderPattern() == LeaderPattern.USECONTENT) {
            if (fobj.getChildNodes() == null) {
                fobj.getLogger().error("Leader use-content with no content");
                return null;
            }

            // child FOs are assigned to the InlineStackingLM
            fobjIter = null;
            
            // get breaks then add areas to FilledArea
            FilledArea fa = new FilledArea();

            ContentLayoutManager clm = new ContentLayoutManager(fa);
            clm.setUserAgent(fobj.getUserAgent());
            addChildLM(clm);

            InlineStackingLayoutManager lm;
            lm = new InlineStackingLayoutManager(fobj);
            clm.addChildLM(lm);

            clm.fillArea(lm);
            int width = clm.getStackingSize();
            Space spacer = null;
            if (fobj.getPatternWidth() > width) {
                spacer = new Space();
                spacer.setIPD(fobj.getPatternWidth() - width);
                width = fobj.getPatternWidth();
            }
            fa.setUnitWidth(width);
            if (spacer != null) {
                fa.addChild(spacer);
            }
            leaderArea = fa;
        }
        return leaderArea;
     }

    public LinkedList getNextKnuthElements(LayoutContext context,
                                           int alignment) {
        MinOptMax ipd;
        curArea = get(context);
        LinkedList returnList = new LinkedList();

        if (curArea == null) {
            setFinished(true);
            return null;
        }

        ipd = getAllocationIPD(context.getRefIPD());

        int bpd = curArea.getBPD();
        int lead = 0;
        int total = 0;
        int middle = 0;
        switch (alignment) {
            case VerticalAlign.MIDDLE  : middle = bpd / 2 ;
                                         lead = bpd / 2 ;
                                         break;
            case VerticalAlign.TOP     : total = bpd;
                                         break;
            case VerticalAlign.BOTTOM  : total = bpd;
                                         break;
            case VerticalAlign.BASELINE:
            default:                     lead = bpd;
                                         break;
        }

        // create the AreaInfo object to store the computed values
        areaInfo = new AreaInfo((short) 0, ipd, false,
                                lead, total, middle);

        // node is a fo:Leader
        returnList.add(new KnuthBox(0, areaInfo.lead, areaInfo.total,
                                    areaInfo.middle,
                                    new LeafPosition(this, -1), true));
        returnList.add(new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                        new LeafPosition(this, -1), true));
        returnList.add
            (new KnuthGlue(areaInfo.ipdArea.opt,
                           areaInfo.ipdArea.max - areaInfo.ipdArea.opt,
                           areaInfo.ipdArea.opt - areaInfo.ipdArea.min, 
                           new LeafPosition(this, 0), false));
        returnList.add(new KnuthBox(0, areaInfo.lead, areaInfo.total,
                                    areaInfo.middle,
                                    new LeafPosition(this, -1), true));

        setFinished(true);
        return returnList;
    }

    public KnuthElement addALetterSpaceTo(KnuthElement element) {
        // return the unchanged glue object
        return new KnuthGlue(areaInfo.ipdArea.opt,
                             areaInfo.ipdArea.max - areaInfo.ipdArea.opt,
                             areaInfo.ipdArea.opt - areaInfo.ipdArea.min, 
                             new LeafPosition(this, 0), false);
    }

    public void hyphenate(Position pos, HyphContext hc) {
        // use the AbstractLayoutManager.hyphenate() null implementation
        super.hyphenate(pos, hc);
    }

    public boolean applyChanges(List oldList) {
        setFinished(false);
        return false;
    }

    public LinkedList getChangedKnuthElements(List oldList,
                                              int flaggedPenalty,
                                              int alignment) {
        if (isFinished()) {
            return null;
        }

        LinkedList returnList = new LinkedList();

        returnList.add(new KnuthBox(0, areaInfo.lead, areaInfo.total,
                                    areaInfo.middle,
                                    new LeafPosition(this, -1), true));
        returnList.add(new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                        new LeafPosition(this, -1), true));
        returnList.add
            (new KnuthGlue(areaInfo.ipdArea.opt,
                           areaInfo.ipdArea.max - areaInfo.ipdArea.opt,
                           areaInfo.ipdArea.opt - areaInfo.ipdArea.min, 
                           new LeafPosition(this, 0), false));
        returnList.add(new KnuthBox(0, areaInfo.lead, areaInfo.total,
                                    areaInfo.middle,
                                    new LeafPosition(this, -1), true));

        setFinished(true);
        return returnList;
    }

    protected void addId() {
        addID(fobj.getId());
    }
}
