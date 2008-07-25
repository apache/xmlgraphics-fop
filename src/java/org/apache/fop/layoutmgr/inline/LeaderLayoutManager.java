/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.fop.layoutmgr.inline;

import java.util.LinkedList;
import java.util.List;

import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.FilledArea;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.Space;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.layoutmgr.InlineKnuthSequence;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.layoutmgr.KnuthSequence;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LeafPosition;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.traits.MinOptMax;

/**
 * LayoutManager for the fo:leader formatting object
 */
public class LeaderLayoutManager extends LeafNodeLayoutManager {
    private Leader fobj;
    private Font font = null;

    private List contentList = null;
    private ContentLayoutManager clm = null;

    private int contentAreaIPD = 0;

    /**
     * Constructor
     *
     * @param node the formatting object that creates this area
     */
    public LeaderLayoutManager(Leader node) {
        super(node);
        fobj = node;
    }

    /** {@inheritDoc} */
    public void initialize() {
        FontInfo fi = fobj.getFOEventHandler().getFontInfo();
        FontTriplet[] fontkeys = fobj.getCommonFont().getFontState(fi);
        font = fi.getFontInstance(fontkeys[0], fobj.getCommonFont().fontSize.getValue(this));
        // the property leader-alignment does not affect vertical positioning
        // (see section 7.21.1 in the XSL Recommendation)
        // setAlignment(node.getLeaderAlignment());
        setCommonBorderPaddingBackground(fobj.getCommonBorderPaddingBackground());
    }

    /**
     * Return the inline area for this leader.
     * @param context the layout context
     * @return the inline area
     */
    public InlineArea get(LayoutContext context) {
        return getLeaderInlineArea(context);
    }

    /**
     * Return the allocated IPD for this area.
     * @param refIPD the IPD of the reference area
     * @return the allocated IPD
     */
    protected MinOptMax getAllocationIPD(int refIPD) {
        return getLeaderAllocIPD(refIPD);
    }

    private MinOptMax getLeaderAllocIPD(int ipd) {
        // length of the leader
        int borderPaddingWidth = 0;
        if (commonBorderPaddingBackground != null) {
            borderPaddingWidth = commonBorderPaddingBackground.getIPPaddingAndBorder(false, this);
        }
        setContentAreaIPD(ipd - borderPaddingWidth);
        int opt = fobj.getLeaderLength().getOptimum(this).getLength().getValue(this)
                    - borderPaddingWidth;
        int min = fobj.getLeaderLength().getMinimum(this).getLength().getValue(this)
                    - borderPaddingWidth;
        int max = fobj.getLeaderLength().getMaximum(this).getLength().getValue(this)
                    - borderPaddingWidth;
        return new MinOptMax(min, opt, max);
    }

    private InlineArea getLeaderInlineArea(LayoutContext context) {
        InlineArea leaderArea = null;

        if (fobj.getLeaderPattern() == EN_RULE) {
            if (fobj.getRuleStyle() != EN_NONE) {
                org.apache.fop.area.inline.Leader leader
                    = new org.apache.fop.area.inline.Leader();
                leader.setRuleStyle(fobj.getRuleStyle());
                leader.setRuleThickness(fobj.getRuleThickness().getValue(this));
                leader.setBPD(fobj.getRuleThickness().getValue(this));
                leaderArea = leader;
            } else {
                leaderArea = new Space();
                leaderArea.setBPD(1);
            }
            leaderArea.addTrait(Trait.COLOR, fobj.getColor());
        } else if (fobj.getLeaderPattern() == EN_SPACE) {
            leaderArea = new Space();
            leaderArea.setBPD(1);
        } else if (fobj.getLeaderPattern() == EN_DOTS) {
            TextArea t = new TextArea();
            char dot = '.'; // userAgent.getLeaderDotCharacter();

            int width = font.getCharWidth(dot);
            t.addWord("" + dot, 0);
            t.setIPD(width);
            t.setBPD(width);
            t.setBaselineOffset(width);
            TraitSetter.addFontTraits(t, font);
            t.addTrait(Trait.COLOR, fobj.getColor());
            Space spacer = null;
            if (fobj.getLeaderPatternWidth().getValue(this) > width) {
                spacer = new Space();
                spacer.setIPD(fobj.getLeaderPatternWidth().getValue(this) - width);
                width = fobj.getLeaderPatternWidth().getValue(this);
            }
            FilledArea fa = new FilledArea();
            fa.setUnitWidth(width);
            fa.addChildArea(t);
            if (spacer != null) {
                fa.addChildArea(spacer);
            }
            fa.setBPD(t.getBPD());

            leaderArea = fa;
        } else if (fobj.getLeaderPattern() == EN_USECONTENT) {
            if (fobj.getChildNodes() == null) {
                InlineLevelEventProducer eventProducer = InlineLevelEventProducer.Provider.get(
                        getFObj().getUserAgent().getEventBroadcaster());
                eventProducer.leaderWithoutContent(this, getFObj().getLocator());
                return null;
            }

            // child FOs are assigned to the InlineStackingLM
            fobjIter = null;

            // get breaks then add areas to FilledArea
            FilledArea fa = new FilledArea();

            clm = new ContentLayoutManager(fa, this);
            addChildLM(clm);

            InlineLayoutManager lm;
            lm = new InlineLayoutManager(fobj);
            clm.addChildLM(lm);
            lm.initialize();

            LayoutContext childContext = new LayoutContext(0);
            childContext.setAlignmentContext(context.getAlignmentContext());
            contentList = clm.getNextKnuthElements(childContext, 0);
            int width = clm.getStackingSize();
            Space spacer = null;
            if (fobj.getLeaderPatternWidth().getValue(this) > width) {
                spacer = new Space();
                spacer.setIPD(fobj.getLeaderPatternWidth().getValue(this) - width);
                width = fobj.getLeaderPatternWidth().getValue(this);
            }
            fa.setUnitWidth(width);
            if (spacer != null) {
                fa.addChildArea(spacer);
            }
            leaderArea = fa;
        }
        TraitSetter.setProducerID(leaderArea, fobj.getId());
        return leaderArea;
     }

    /** {@inheritDoc} */
    public void addAreas(PositionIterator posIter, LayoutContext context) {
        if (fobj.getLeaderPattern() != EN_USECONTENT) {
            // use LeafNodeLayoutManager.addAreas()
            super.addAreas(posIter, context);
        } else {
            addId();

            widthAdjustArea(curArea, context);

            if (commonBorderPaddingBackground != null) {
                // Add border and padding to area
                TraitSetter.setBorderPaddingTraits(curArea,
                                                   commonBorderPaddingBackground,
                                                   false, false, this);
                TraitSetter.addBackground(curArea, commonBorderPaddingBackground, this);
            }

            // add content areas
            KnuthPossPosIter contentIter = new KnuthPossPosIter(contentList, 0, contentList.size());
            clm.addAreas(contentIter, context);

            parentLM.addChildArea(curArea);

            while (posIter.hasNext()) {
                posIter.next();
            }
        }
    }

    /** {@inheritDoc} */
    public List getNextKnuthElements(LayoutContext context,
                                           int alignment) {
        MinOptMax ipd;
        curArea = get(context);
        KnuthSequence seq = new InlineKnuthSequence();

        if (curArea == null) {
            setFinished(true);
            return null;
        }

        alignmentContext = new AlignmentContext(curArea.getBPD()
                                    , fobj.getAlignmentAdjust()
                                    , fobj.getAlignmentBaseline()
                                    , fobj.getBaselineShift()
                                    , fobj.getDominantBaseline()
                                    , context.getAlignmentContext());

        ipd = getAllocationIPD(context.getRefIPD());
        if (fobj.getLeaderPattern() == EN_USECONTENT && curArea instanceof FilledArea) {
            // If we have user supplied content make it fit if we can
            int unitWidth = ((FilledArea)curArea).getUnitWidth();
            if (ipd.opt < unitWidth && ipd.max >= unitWidth) {
                ipd.opt = unitWidth;
            }
        }

        // create the AreaInfo object to store the computed values
        areaInfo = new AreaInfo((short) 0, ipd, false, context.getAlignmentContext());
        curArea.setAdjustingInfo(ipd.max - ipd.opt, ipd.opt - ipd.min, 0);

        addKnuthElementsForBorderPaddingStart(seq);

        // node is a fo:Leader
        seq.add(new KnuthInlineBox(0, alignmentContext,
                                    new LeafPosition(this, -1), true));
        seq.add(new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                        new LeafPosition(this, -1), true));
        if (alignment == EN_JUSTIFY || alignment == 0) {
            seq.add
                (new KnuthGlue(areaInfo.ipdArea.opt,
                               areaInfo.ipdArea.max - areaInfo.ipdArea.opt,
                               areaInfo.ipdArea.opt - areaInfo.ipdArea.min,
                               new LeafPosition(this, 0), false));
        } else {
            seq.add
                (new KnuthGlue(areaInfo.ipdArea.opt,
                               0,
                               0,
                               new LeafPosition(this, 0), false));
        }
        seq.add(new KnuthInlineBox(0, alignmentContext,
                                    new LeafPosition(this, -1), true));

        addKnuthElementsForBorderPaddingEnd(seq);

        LinkedList returnList = new LinkedList();
        returnList.add(seq);
        setFinished(true);
        return returnList;
    }

    /** {@inheritDoc} */
    public void hyphenate(Position pos, HyphContext hc) {
        // use the AbstractLayoutManager.hyphenate() null implementation
        super.hyphenate(pos, hc);
    }

    /** {@inheritDoc} */
    public boolean applyChanges(List oldList) {
        setFinished(false);
        return false;
    }

    /** {@inheritDoc} */
    public List getChangedKnuthElements(List oldList,
                                              int alignment) {
        if (isFinished()) {
            return null;
        }

        List returnList = new LinkedList();

        addKnuthElementsForBorderPaddingStart(returnList);

        returnList.add(new KnuthInlineBox(0, areaInfo.alignmentContext,
                                    new LeafPosition(this, -1), true));
        returnList.add(new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                        new LeafPosition(this, -1), true));
        if (alignment == EN_JUSTIFY || alignment == 0) {
            returnList.add
                (new KnuthGlue(areaInfo.ipdArea.opt,
                               areaInfo.ipdArea.max - areaInfo.ipdArea.opt,
                               areaInfo.ipdArea.opt - areaInfo.ipdArea.min,
                               new LeafPosition(this, 0), false));
        } else {
            returnList.add
                (new KnuthGlue(areaInfo.ipdArea.opt,
                               0,
                               0,
                               new LeafPosition(this, 0), false));
        }
        returnList.add(new KnuthInlineBox(0, areaInfo.alignmentContext,
                                    new LeafPosition(this, -1), true));

        addKnuthElementsForBorderPaddingEnd(returnList);

        setFinished(true);
        return returnList;
    }

    /** {@inheritDoc} */
    public int getBaseLength(int lengthBase, FObj fobj) {
        return getParent().getBaseLength(lengthBase, getParent().getFObj());
    }

    /**
     * Returns the IPD of the content area
     * @return the IPD of the content area
     */
    public int getContentAreaIPD() {
        return contentAreaIPD;
    }

    private void setContentAreaIPD(int contentAreaIPD) {
        this.contentAreaIPD = contentAreaIPD;
    }

}
