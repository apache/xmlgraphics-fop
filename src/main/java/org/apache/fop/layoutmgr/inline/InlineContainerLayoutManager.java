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

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.fop.area.Area;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.Container;
import org.apache.fop.area.inline.InlineViewport;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.LengthBase;
import org.apache.fop.datatypes.SimplePercentBaseContext;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.flow.InlineContainer;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.layoutmgr.AbstractLayoutManager;
import org.apache.fop.layoutmgr.AreaAdditionUtil;
import org.apache.fop.layoutmgr.BlockLevelEventProducer;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.InlineKnuthSequence;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.layoutmgr.KnuthSequence;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.NonLeafPosition;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.SpaceResolver;
import org.apache.fop.layoutmgr.TraitSetter;

/**
 * This creates a single inline container area after
 * laying out the child block areas. All footnotes, floats
 * and id areas are maintained for later retrieval.
 */
public class InlineContainerLayoutManager extends AbstractLayoutManager implements InlineLevelLayoutManager {

    private CommonBorderPaddingBackground borderProps;
    private int contentAreaIPD;
    private int contentAreaBPD;

    private List<ListElement> childElements;
    private int ipdOverflow;
    private AlignmentContext alignmentContext;
    private InlineViewport currentViewport;
    private Container referenceArea;

    public InlineContainerLayoutManager(InlineContainer node) {
        super(node);
        setGeneratesReferenceArea(true);
    }

    @Override
    public void initialize() {
        InlineContainer node = (InlineContainer) fobj;
        borderProps = node.getCommonBorderPaddingBackground();
    }

    private InlineContainer getInlineContainer() {
        assert fobj instanceof InlineContainer;
        return (InlineContainer) fobj;
    }

    @Override
    public List<KnuthSequence> getNextKnuthElements(LayoutContext context, int alignment) {
        determineIPD(context);
        childElements = getChildKnuthElements(context, alignment);
        determineBPD();
        alignmentContext = makeAlignmentContext(context);
        Position position = new Position(this, 0);
        KnuthSequence knuthSequence = new InlineKnuthSequence();
        knuthSequence.add(new KnuthInlineBox(contentAreaIPD, alignmentContext, position, false));
        List<KnuthSequence> knuthElements = new ArrayList<KnuthSequence>(1);
        knuthElements.add(knuthSequence);
        setFinished(true);
        return knuthElements;
    }

    private void determineIPD(LayoutContext layoutContext) {
        LengthRangeProperty ipd = getInlineContainer().getInlineProgressionDimension();
        Property optimum = ipd.getOptimum(this);
        if (optimum.isAuto()) {
            contentAreaIPD = layoutContext.getRefIPD();
            InlineLevelEventProducer eventProducer = InlineLevelEventProducer.Provider.get(
                    fobj.getUserAgent().getEventBroadcaster());
            eventProducer.inlineContainerAutoIPDNotSupported(this, contentAreaIPD / 1000f);
        } else {
            contentAreaIPD = optimum.getLength().getValue(this);
        }
    }

    private List<ListElement> getChildKnuthElements(LayoutContext layoutContext, int alignment) {
        List<ListElement> allChildElements = new LinkedList<ListElement>();
        LayoutManager childLM;
        while ((childLM = getChildLM()) != null) {
            LayoutContext childLC = LayoutContext.offspringOf(layoutContext);
            childLC.setRefIPD(contentAreaIPD);
            @SuppressWarnings("unchecked")
            List<ListElement> childElements = childLM.getNextKnuthElements(childLC, alignment);
            allChildElements.addAll(childElements);
        }
        handleIPDOverflow();
        wrapPositions(allChildElements);
        SpaceResolver.resolveElementList(allChildElements);
        SpaceResolver.performConditionalsNotification(allChildElements, 0, allChildElements.size() - 1, -1);
        return allChildElements;
    }

    private void determineBPD() {
        LengthRangeProperty bpd = getInlineContainer().getBlockProgressionDimension();
        Property optimum = bpd.getOptimum(this);
        int actualBPD = ElementListUtils.calcContentLength(childElements);
        if (optimum.isAuto()) {
            contentAreaBPD = actualBPD;
        } else {
            double bpdValue = optimum.getLength().getNumericValue(this);
            if (bpdValue < 0) {
                contentAreaBPD = actualBPD;
            } else {
                contentAreaBPD = (int) Math.round(bpdValue);
                if (contentAreaBPD < actualBPD) {
                    BlockLevelEventProducer eventProducer = getBlockLevelEventProducer();
                    eventProducer.viewportBPDOverflow(this, fobj.getName(),
                            actualBPD - contentAreaBPD, needClip(), canRecoverFromOverflow(),
                            fobj.getLocator());
                }
            }
        }
    }

    protected AlignmentContext makeAlignmentContext(LayoutContext context) {
        InlineContainer ic = (InlineContainer) fobj;
        AlignmentContext ac = new AlignmentContext(contentAreaBPD,
                ic.getAlignmentAdjust(), ic.getAlignmentBaseline(),
                ic.getBaselineShift(), ic.getDominantBaseline(),
                context.getAlignmentContext());
        int baselineOffset = getAlignmentPoint(ac.getDominantBaselineIdentifier());
        ac.resizeLine(contentAreaBPD, baselineOffset);
        return ac;
    }

    private void handleIPDOverflow() {
        if (ipdOverflow > 0) {
            BlockLevelEventProducer eventProducer = getBlockLevelEventProducer();
            eventProducer.viewportIPDOverflow(this, fobj.getName(),
                    ipdOverflow, needClip(), canRecoverFromOverflow(),
                    fobj.getLocator());
        }
    }

    private void wrapPositions(List<ListElement> elements) {
        for (ListElement element : elements) {
            Position position = new NonLeafPosition(this, element.getPosition());
            notifyPos(position);
            element.setPosition(position);
        }
    }

    private BlockLevelEventProducer getBlockLevelEventProducer() {
        return BlockLevelEventProducer.Provider.get(fobj.getUserAgent().getEventBroadcaster());
    }

    private boolean canRecoverFromOverflow() {
        return getInlineContainer().getOverflow() != EN_ERROR_IF_OVERFLOW;
    }

    private int getAlignmentPoint(int dominantBaseline) {
        Length alignmentAdjust = getInlineContainer().getAlignmentAdjust();
        int baseline = alignmentAdjust.getEnum();
        if (baseline == Constants.EN_AUTO) {
            return getInlineContainerBaselineOffset(getInlineContainer().getAlignmentBaseline());
        } else if (baseline == Constants.EN_BASELINE) {
            return getInlineContainerBaselineOffset(dominantBaseline);
        } else if (baseline != 0) {
            return getInlineContainerBaselineOffset(baseline);
        } else {
            int baselineOffset = getInlineContainerBaselineOffset(dominantBaseline);
            int lineHeight = getInlineContainer().getLineHeight().getOptimum(this).getLength().getValue(this);
            int adjust = alignmentAdjust.getValue(
                    new SimplePercentBaseContext(null, LengthBase.ALIGNMENT_ADJUST, lineHeight));
            return baselineOffset + adjust;
        }
    }

    private int getInlineContainerBaselineOffset(int property) {
        switch (property) {
        case Constants.EN_BEFORE_EDGE:
        case Constants.EN_TEXT_BEFORE_EDGE:
            return 0;
        case Constants.EN_AFTER_EDGE:
        case Constants.EN_TEXT_AFTER_EDGE:
            return contentAreaBPD;
        case Constants.EN_MIDDLE:
        case Constants.EN_CENTRAL:
        case Constants.EN_MATHEMATICAL:
            return contentAreaBPD / 2;
        case Constants.EN_IDEOGRAPHIC:
            return contentAreaBPD * 7 / 10;
        case Constants.EN_ALPHABETIC:
            return contentAreaBPD * 6 / 10;
        case Constants.EN_HANGING:
            return contentAreaBPD * 2 / 10;
        case Constants.EN_AUTO:
        case Constants.EN_BASELINE:
            return hasLineAreaDescendant() ? getBaselineOffset() : contentAreaBPD;
        default:
            throw new AssertionError("Unknown baseline value: " + property);
        }
    }

    @Override
    public void addAreas(PositionIterator posIter, LayoutContext context) {
        Position inlineContainerPosition = null;
        while (posIter.hasNext()) {
            /*
             * Should iterate only once, but hasNext must be called twice for its
             * side-effects to apply and the iterator to switch to the next LM.
             */
            assert inlineContainerPosition == null;
            inlineContainerPosition = posIter.next();
            assert inlineContainerPosition.getLM() == this;
        }
        assert inlineContainerPosition != null;
        KnuthPossPosIter childPosIter = new KnuthPossPosIter(childElements);
        AreaAdditionUtil.addAreas(this, childPosIter, context);
    }

    @Override
    public Area getParentArea(Area childArea) {
        if (referenceArea == null) {
            referenceArea = new Container();
            referenceArea.addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
            TraitSetter.setProducerID(referenceArea, fobj.getId());
            referenceArea.setIPD(contentAreaIPD);
            currentViewport = new InlineViewport(referenceArea);
            currentViewport.addTrait(Trait.IS_VIEWPORT_AREA, Boolean.TRUE);
            TraitSetter.setProducerID(currentViewport, fobj.getId());
            currentViewport.setBlockProgressionOffset(alignmentContext.getOffset());
            currentViewport.setIPD(getContentAreaIPD());
            currentViewport.setBPD(getContentAreaBPD());
            TraitSetter.addBackground(currentViewport, borderProps, this);
            currentViewport.setClip(needClip());
            currentViewport.setContentPosition(
                    new Rectangle2D.Float(0, 0, getContentAreaIPD(), getContentAreaBPD()));
            getParent().addChildArea(currentViewport);
        }
        return referenceArea;
    }

    @Override
    public int getContentAreaIPD() {
        return contentAreaIPD;
    }

    @Override
    public int getContentAreaBPD() {
        return contentAreaBPD;
    }

    @Override
    public void addChildArea(Area childArea) {
        referenceArea.addChildArea(childArea);
    }

    private boolean needClip() {
        int overflow = getInlineContainer().getOverflow();
        return (overflow == EN_HIDDEN || overflow == EN_ERROR_IF_OVERFLOW);
    }

    public boolean handleOverflow(int milliPoints) {
        ipdOverflow = Math.max(ipdOverflow, milliPoints);
        return true;
    }

    public List addALetterSpaceTo(List oldList) {
        return oldList;
    }

    public List addALetterSpaceTo(List oldList, int depth) {
        return oldList;
    }

    public String getWordChars(Position pos) {
        return "";
    }

    public void hyphenate(Position pos, HyphContext hyphContext) {
    }

    public boolean applyChanges(List oldList) {
        return false;
    }

    public boolean applyChanges(List oldList, int depth) {
        return false;
    }

    public List getChangedKnuthElements(List oldList, int alignment, int depth) {
        return oldList;
    }

}
