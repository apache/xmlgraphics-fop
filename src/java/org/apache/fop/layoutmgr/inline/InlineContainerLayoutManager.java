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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.Container;
import org.apache.fop.area.inline.InlineViewport;
import org.apache.fop.fo.flow.InlineContainer;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.layoutmgr.AbstractLayoutManager;
import org.apache.fop.layoutmgr.InlineKnuthSequence;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.layoutmgr.KnuthSequence;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.ListElement;
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
    private int alignmentBaseline = EN_BASELINE;
    private int contentAreaIPD;
    private int contentAreaBPD;

    private List<ListElement> childElements;
    private InlineViewport currentViewport;
    private Container referenceArea;

    public InlineContainerLayoutManager(InlineContainer node) {
        super(node);
    }

    @Override
    public void initialize() {
        InlineContainer node = (InlineContainer) fobj;
        borderProps = node.getCommonBorderPaddingBackground();
    }

    @Override
    public List<KnuthSequence> getNextKnuthElements(LayoutContext context, int alignment) {
        InlineContainer ic = (InlineContainer) fobj;
        contentAreaIPD = getLength(ic.getInlineProgressionDimension());
        contentAreaBPD = getLength(ic.getBlockProgressionDimension());
        LayoutContext childLC = LayoutContext.offspringOf(context); // TODO copyOf?
        childLC.setRefIPD(contentAreaIPD);
        childElements = getChildKnuthElements(childLC, alignment); // TODO which alignment?
        AlignmentContext alignmentContext = makeAlignmentContext(context); // TODO correct?
        Position position = new Position(this, 0);
        KnuthSequence knuthSequence = new InlineKnuthSequence();
        knuthSequence.add(new KnuthInlineBox(contentAreaIPD, alignmentContext, position, false));
        List<KnuthSequence> knuthElements = new ArrayList<KnuthSequence>(1);
        knuthElements.add(knuthSequence);
        setFinished(true);
        return knuthElements;
    }

    private int getLength(LengthRangeProperty property) {
        Property optimum = property.getOptimum(this); // TODO percent base context
        if (optimum.isAuto()) {
            throw new UnsupportedOperationException("auto dimension not supported");
        }
        return optimum.getLength().getValue(this); // TODO percent base context
    }

    private List<ListElement> getChildKnuthElements(LayoutContext layoutContext, int alignment) {
        List<ListElement> allChildElements = new LinkedList<ListElement>();
        LayoutManager childLM;
        while ((childLM = getChildLM()) != null) {
            LayoutContext childLC = LayoutContext.offspringOf(layoutContext); // TODO copyOf? newInstance?
            childLC.setRefIPD(layoutContext.getRefIPD());
            @SuppressWarnings("unchecked")
            List<ListElement> childElements = childLM.getNextKnuthElements(childLC, alignmentBaseline);
            allChildElements.addAll(childElements);
            // TODO breaks, keeps, empty content
        }
        SpaceResolver.resolveElementList(allChildElements);
        // TODO break-before, break-after
        return allChildElements;
    }

    @Override
    public void addAreas(PositionIterator posIter, LayoutContext context) {
        Position inlineContainerPosition = null;
        while (posIter.hasNext()) {
            Position pos = posIter.next();
            if (pos.getLM() == this) {
                inlineContainerPosition = pos;
            }
        }
        addId();
//        addMarkersToPage(
//                true,
//                true,
//                lastPos == null || isLast(lastPos));

        if (inlineContainerPosition != null) {
            LayoutManager childLM;
            KnuthPossPosIter childPosIter = new KnuthPossPosIter(childElements);
            while ((childLM = childPosIter.getNextChildLM()) != null) {
                LayoutContext childLC = LayoutContext.copyOf(context); // TODO correct?
                childLM.addAreas(childPosIter, childLC);
            }
        }

//        addMarkersToPage(
//                false,
//                true,
//                lastPos == null || isLast(lastPos));

//        boolean isLast = (context.isLastArea() && prevLM == lastChildLM);
//        context.setFlags(LayoutContext.LAST_AREA, isLast);
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
            currentViewport.setIPD(getContentAreaIPD());
            currentViewport.setBPD(getContentAreaBPD());
            TraitSetter.setProducerID(currentViewport, fobj.getId());
            TraitSetter.addBorders(currentViewport,
                    borderProps,
                    false, false, false, false, this);
            TraitSetter.addPadding(currentViewport,
                    borderProps,
                    false, false, false, false, this);
            TraitSetter.addBackground(currentViewport,
                    borderProps,
                    this);
            currentViewport.setClip(needClip());
            currentViewport.setContentPosition(
                    new java.awt.geom.Rectangle2D.Float(0, 0, getContentAreaIPD(), getContentAreaBPD()));
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
        referenceArea.addBlock((Block) childArea);
    }

    private boolean needClip() {
        int overflow = ((InlineContainer) fobj).getOverflow();
        return (overflow == EN_HIDDEN || overflow == EN_ERROR_IF_OVERFLOW);
    }

    protected AlignmentContext makeAlignmentContext(LayoutContext context) {
        InlineContainer ic = (InlineContainer) fobj;
        FontInfo fi = fobj.getFOEventHandler().getFontInfo();
        FontTriplet[] fontkeys = ic.getCommonFont().getFontState(fi);
        Font fs = fi.getFontInstance(fontkeys[0], ic.getCommonFont().fontSize.getValue(this));
        return new AlignmentContext(fs, ic.getLineHeight().getOptimum(this).getLength().getValue(this), // TODO
                context.getWritingMode());
    }

    public List addALetterSpaceTo(List oldList) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List addALetterSpaceTo(List oldList, int depth) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String getWordChars(Position pos) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void hyphenate(Position pos, HyphContext hyphContext) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean applyChanges(List oldList) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean applyChanges(List oldList, int depth) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List getChangedKnuthElements(List oldList, int alignment, int depth) {
        throw new UnsupportedOperationException("Not implemented");
    }

}
