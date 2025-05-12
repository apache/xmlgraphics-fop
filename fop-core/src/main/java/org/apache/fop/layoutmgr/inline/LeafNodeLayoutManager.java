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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.area.Area;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.layoutmgr.AbstractLayoutManager;
import org.apache.fop.layoutmgr.InlineKnuthSequence;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.KnuthSequence;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LeafPosition;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.traits.MinOptMax;


/**
 * Base LayoutManager for leaf-node FObj, ie: ones which have no children.
 * These are all inline objects. Most of them cannot be split (Text is
 * an exception to this rule.)
 * This class can be extended to handle the creation and adding of the
 * inline area.
 */
public abstract class LeafNodeLayoutManager extends AbstractLayoutManager
                                   implements InlineLevelLayoutManager {

    /** logging instance */
    protected static final Log log = LogFactory.getLog(LeafNodeLayoutManager.class);

    /** The inline area that this leafnode will add. */
    protected InlineArea curArea;
    /** Any border, padding and background properties applying to this area */
    protected CommonBorderPaddingBackground commonBorderPaddingBackground;
    /** The alignment context applying to this area */
    protected AlignmentContext alignmentContext;

    /** Flag to indicate if something was changed as part of the getChangeKnuthElements sequence */
    protected boolean somethingChanged;
    /** Our area info for the Knuth elements */
    protected AreaInfo areaInfo;

    /**
     * Store information about the inline area
     */
    protected static class AreaInfo {
        /** letter space count */
        protected short letterSpaces;
        /** ipd of area */
        protected MinOptMax ipdArea;
        /** true if hyphenated */
        protected boolean isHyphenated;
        /** alignment context */
        protected AlignmentContext alignmentContext;

        /**
         * Construct an area information item.
         * @param letterSpaces letter space count
         * @param ipd inline progression dimension
         * @param isHyphenated true if hyphenated
         * @param alignmentContext an alignment context
         */
        public AreaInfo(short letterSpaces, MinOptMax ipd, boolean isHyphenated,
                        AlignmentContext alignmentContext) {
            this.letterSpaces = letterSpaces;
            this.ipdArea = ipd;
            this.isHyphenated = isHyphenated;
            this.alignmentContext = alignmentContext;
        }

    }

    /**
     * Create a Leaf node layout manager.
     * @param node the FObj to attach to this LM.
     */
    public LeafNodeLayoutManager(FObj node) {
        super(node);
    }

    /**
     * Create a Leaf node layout manager.
     */
    public LeafNodeLayoutManager() {
    }

    /**
     * get the inline area.
     * @param context the context used to create the area
     * @return the current inline area for this layout manager
     */
    public InlineArea get(LayoutContext context) {
        return curArea;
    }

    /**
     * Check if this inline area is resolved due to changes in
     * page or ipd.
     * Currently not used.
     * @return true if the area is resolved when adding
     */
    public boolean resolved() {
        return false;
    }

    /**
     * Set the current inline area.
     * @param ia the inline area to set for this layout manager
     */
    public void setCurrentArea(InlineArea ia) {
        curArea = ia;
    }

    /**
     * This is a leaf-node, so this method should never be called.
     * @param childArea the childArea to add
     */
    @Override
    public void addChildArea(Area childArea) {
        assert false;
    }

    /**
     * This is a leaf-node, so this method should never be called.
     * @param childArea the childArea to get the parent for
     * @return the parent area
     */
    @Override
    public Area getParentArea(Area childArea) {
        assert false;
        return null;
    }

    /**
     * Set the border and padding properties of the inline area.
     * @param commonBorderPaddingBackground the alignment adjust property
     */
    protected void setCommonBorderPaddingBackground(
            CommonBorderPaddingBackground commonBorderPaddingBackground) {
        this.commonBorderPaddingBackground = commonBorderPaddingBackground;
    }

    /**
     * Get the allocation ipd of the inline area.
     * This method may be overridden to handle percentage values.
     * @param refIPD the ipd of the parent reference area
     * @return the min/opt/max ipd of the inline area
     */
    protected MinOptMax getAllocationIPD(int refIPD) {
        return MinOptMax.getInstance(curArea.getIPD());
    }

    /**
     * Add the area for this layout manager.
     * This adds the single inline area to the parent.
     * @param posIter the position iterator
     * @param context the layout context for adding the area
     */
    @Override
    public void addAreas(PositionIterator posIter, LayoutContext context) {
        addId();

        InlineArea area = getEffectiveArea(context);
        if (area.getAllocIPD() > 0 || area.getAllocBPD() > 0) {
            offsetArea(area, context);
            widthAdjustArea(area, context);
            if (commonBorderPaddingBackground != null) {
                // Add border and padding to area
                TraitSetter.setBorderPaddingTraits(area,
                                                   commonBorderPaddingBackground,
                                                   false, false, this);
                TraitSetter.addBackground(area, commonBorderPaddingBackground, this);
            }
            parentLayoutManager.addChildArea(area);
        }

        while (posIter.hasNext()) {
            posIter.next();
        }
    }

    /**
     * @return the effective area to be added to the area tree. Normally, this is simply "curArea"
     * but in the case of page-number(-citation) curArea is cloned, updated and returned.
     */
    protected InlineArea getEffectiveArea(LayoutContext layoutContext) {
        return curArea;
    }

    /**
     * Offset this area.
     * Offset the inline area in the bpd direction when adding the
     * inline area.
     * This is used for vertical alignment.
     * Subclasses should override this if necessary.
     * @param area the inline area to be updated
     * @param context the layout context used for adding the area
     */
    protected void offsetArea(InlineArea area, LayoutContext context) {
        area.setBlockProgressionOffset(alignmentContext.getOffset());
    }

    /**
     * Creates a new alignment context or returns the current
     * alignment context.
     * This is used for vertical alignment.
     * Subclasses should override this if necessary.
     * @param context the layout context used
     * @return the appropriate alignment context
     */
    protected AlignmentContext makeAlignmentContext(LayoutContext context) {
        return context.getAlignmentContext();
    }

    /**
     * Adjust the width of the area when adding.
     * This uses the min/opt/max values to adjust the with
     * of the inline area by a percentage.
     * @param area the inline area to be updated
     * @param context the layout context for adding this area
     */
    protected void widthAdjustArea(InlineArea area, LayoutContext context) {
        double dAdjust = context.getIPDAdjust();
        int adjustment = 0;
        if (dAdjust < 0) {
            adjustment += (int) (dAdjust * areaInfo.ipdArea.getShrink());
        } else if (dAdjust > 0) {
            adjustment += (int) (dAdjust * areaInfo.ipdArea.getStretch());
        }
        area.setIPD(areaInfo.ipdArea.getOpt() + adjustment);
        area.setAdjustment(adjustment);
    }

    /** {@inheritDoc} */
    @Override
    public List<KnuthSequence> getNextKnuthElements(LayoutContext context, int alignment) {
        curArea = get(context);

        alignmentContext = makeAlignmentContext(context);

        MinOptMax ipd = getAllocationIPD(context.getRefIPD());

        // create the AreaInfo object to store the computed values
        areaInfo = new AreaInfo((short) 0, ipd, false, alignmentContext);

        // node is a fo:ExternalGraphic, fo:InstreamForeignObject,
        // fo:PageNumber or fo:PageNumberCitation
        KnuthSequence seq = new InlineKnuthSequence();

        addKnuthElementsForBorderPaddingStart(seq);

        seq.add(makeBox());

        addKnuthElementsForBorderPaddingEnd(seq);

        setFinished(true);
        return Collections.singletonList(seq);
    }

    protected KnuthBox makeBox() {
        return new KnuthInlineBox(areaInfo.ipdArea.getOpt(), alignmentContext,
                notifyPos(new LeafPosition(this, 0)), false);
    }

    /** {@inheritDoc} */
    public List<ListElement> addALetterSpaceTo(List<ListElement> oldList) {
        // return the unchanged elements
        return oldList;
    }

    /**
     * {@inheritDoc}
     * Only TextLM has a meaningful implementation of this method
     */
    public List<ListElement> addALetterSpaceTo(List<ListElement> oldList, int depth) {
        return addALetterSpaceTo(oldList);
    }

    /** {@inheritDoc} */
    public String getWordChars(Position pos) {
        return "";
    }

    /** {@inheritDoc} */
    public void hyphenate(Position pos, HyphContext hyphContext) {
    }

    /** {@inheritDoc} */
    public boolean applyChanges(List<ListElement> oldList) {
        setFinished(false);
        return false;
    }

    /**
     * {@inheritDoc}
     * Only TextLM has a meaningful implementation of this method
     */
    public boolean applyChanges(List<ListElement> oldList, int depth) {
        return applyChanges(oldList);
    }

    /**
     * {@inheritDoc}
     * No subclass has a meaningful implementation of this method
     */
    public List<ListElement> getChangedKnuthElements(List<ListElement> oldList, int alignment, int depth) {
        return getChangedKnuthElements(oldList, alignment);
    }

    /** {@inheritDoc} */
    @Override
    public List<ListElement> getChangedKnuthElements(List<ListElement> oldList, int alignment) {
        if (isFinished()) {
            return null;
        }

        LinkedList<ListElement> returnList = new LinkedList<>();

        addKnuthElementsForBorderPaddingStart(returnList);

        // fobj is a fo:ExternalGraphic, fo:InstreamForeignObject,
        // fo:PageNumber or fo:PageNumberCitation
        returnList.add(new KnuthInlineBox(areaInfo.ipdArea.getOpt(), areaInfo.alignmentContext,
                                          notifyPos(new LeafPosition(this, 0)), true));

        addKnuthElementsForBorderPaddingEnd(returnList);

        setFinished(true);
        return returnList;
    }

    /**
     * Creates Knuth elements for start border padding and adds them to the return list.
     * @param returnList return list to add the additional elements to
     */
    protected void addKnuthElementsForBorderPaddingStart(List<ListElement> returnList) {
        //Border and Padding (start)
        if (commonBorderPaddingBackground != null) {
            int ipStart = commonBorderPaddingBackground.getBorderStartWidth(false)
                         + commonBorderPaddingBackground.getPaddingStart(false, this);
            if (ipStart > 0) {
                // Add a non breakable glue
                returnList.add(new KnuthPenalty(0, KnuthPenalty.INFINITE,
                                                false, new LeafPosition(this, -1), true));
                returnList.add(new KnuthGlue(ipStart, 0, 0, new LeafPosition(this, -1), true));
            }
        }
    }

    /**
     * Creates Knuth elements for end border padding and adds them to the return list.
     * @param returnList return list to add the additional elements to
     */
    protected void addKnuthElementsForBorderPaddingEnd(List<ListElement> returnList) {
        //Border and Padding (after)
        if (commonBorderPaddingBackground != null) {
            int ipEnd = commonBorderPaddingBackground.getBorderEndWidth(false)
                        + commonBorderPaddingBackground.getPaddingEnd(false, this);
            if (ipEnd > 0) {
                // Add a non breakable glue
                returnList.add(new KnuthPenalty(0, KnuthPenalty.INFINITE,
                                                false, new LeafPosition(this, -1), true));
                returnList.add(new KnuthGlue(ipEnd, 0, 0, new LeafPosition(this, -1), true));
            }
        }
    }

}

