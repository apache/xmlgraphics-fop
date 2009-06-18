/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import org.apache.fop.area.Area;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.fo.FObj;
import org.apache.fop.traits.MinOptMax;

import java.util.List;
import java.util.LinkedList;

/**
 * Base LayoutManager for leaf-node FObj, ie: ones which have no children.
 * These are all inline objects. Most of them cannot be split (Text is
 * an exception to this rule.)
 * This class can be extended to handle the creation and adding of the
 * inline area.
 */
public class LeafNodeLayoutManager extends AbstractLayoutManager 
                                   implements InlineLevelLayoutManager {
    /**
     * The inline area that this leafnode will add.
     */
    protected InlineArea curArea = null;
    protected int verticalAlignment;
    private int lead;
    private MinOptMax ipd;

    protected boolean bSomethingChanged = false;
    protected AreaInfo areaInfo = null;

    /**
     * Store information about the inline area
     */
    protected class AreaInfo {
        protected short iLScount;
        protected MinOptMax ipdArea;
        protected boolean bHyphenated;
        protected int lead;
        protected int total;
        protected int middle;

        public AreaInfo(short iLS, MinOptMax ipd, boolean bHyph,
                        int l, int t, int m) {
            iLScount = iLS;
            ipdArea = ipd;
            bHyphenated = bHyph;
            lead = l;
            total = t;
            middle = m;
        }
    }


    /**
     * Create a Leaf node layout mananger.
     * @param node the FObj to attach to this LM.
     */
    public LeafNodeLayoutManager(FObj node) {
        super(node);
    }

    /**
     * Create a Leaf node layout mananger.
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
     * Check if this generates inline areas.
     * @return true always since this is an inline area manager
     */
    public boolean generatesInlineAreas() {
        return true;
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
     * Set the alignment of the inline area.
     * @param al the vertical alignment positioning
     */
    public void setAlignment(int al) {
        verticalAlignment = al;
    }

    /**
     * Set the lead for this inline area.
     * The lead is the distance from the top of the object
     * to the baseline.
     * Currently not used.
     * @param l the lead value
     */
    public void setLead(int l) {
        lead = l;
    }

    /**
     * This is a leaf-node, so this method is never called.
     * @param childArea the childArea to add
     */
    public void addChildArea(Area childArea) {
    }

    /**
     * This is a leaf-node, so this method is never called.
     * @param childArea the childArea to get the parent for
     * @return the parent area
     */
    public Area getParentArea(Area childArea) {
        return null;
    }

    /**
     * Get the allocation ipd of the inline area.
     * This method may be overridden to handle percentage values.
     * @param refIPD the ipd of the parent reference area
     * @return the min/opt/max ipd of the inline area
     */
    protected MinOptMax getAllocationIPD(int refIPD) {
        return new MinOptMax(curArea.getIPD());
    }

    /**
     * Add the area for this layout manager.
     * This adds the single inline area to the parent.
     * @param posIter the position iterator
     * @param context the layout context for adding the area
     */
    public void addAreas(PositionIterator posIter, LayoutContext context) {
        addId();

        offsetArea(context);
        widthAdjustArea(context);
        parentLM.addChildArea(curArea);

        while (posIter.hasNext()) {
            posIter.next();
        }
    }

    protected void addId() {
        // Do nothing here, overriden in subclasses that has a 'id' property.
    }
    
    /**
     * Offset this area.
     * Offset the inline area in the bpd direction when adding the
     * inline area.
     * This is used for vertical alignment.
     * Subclasses should override this if necessary.
     * @param context the layout context used for adding the area
     */
    protected void offsetArea(LayoutContext context) {
        int bpd = curArea.getBPD();
        switch (verticalAlignment) {
            case EN_MIDDLE:
                curArea.setOffset(context.getMiddleBaseline() - bpd / 2);
            break;
            case EN_TOP:
                curArea.setOffset(context.getTopBaseline());
            break;
            case EN_BOTTOM:
                curArea.setOffset(context.getBottomBaseline() - bpd);
            break;
            case EN_BASELINE:
            default:
                curArea.setOffset(context.getBaseline() - bpd);
            break;
        }
    }

    /**
     * Adjust the width of the area when adding.
     * This uses the min/opt/max values to adjust the with
     * of the inline area by a percentage.
     * @param context the layout context for adding this area
     */
    protected void widthAdjustArea(LayoutContext context) {
        double dAdjust = context.getIPDAdjust();
        int width = areaInfo.ipdArea.opt;
        if (dAdjust < 0) {
            width = (int) (width + dAdjust * (areaInfo.ipdArea.opt
                                             - areaInfo.ipdArea.min));
        } else if (dAdjust > 0) {
            width = (int) (width + dAdjust * (areaInfo.ipdArea.max
                                             - areaInfo.ipdArea.opt));
        }
        curArea.setIPD(width);
    }

    /**
     * Check if can break before this area.
     * @param context the layout context to check for the break
     * @return true if can break before this area in the context
     */
    public boolean canBreakBefore(LayoutContext context) {
        return true;
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
        switch (verticalAlignment) {
            case EN_MIDDLE  : middle = bpd / 2 ;
                                         lead = bpd / 2 ;
                                         break;
            case EN_TOP     : total = bpd;
                                         break;
            case EN_BOTTOM  : total = bpd;
                                         break;
            case EN_BASELINE:
            default:                     lead = bpd;
                                         break;
        }

        // create the AreaInfo object to store the computed values
        areaInfo = new AreaInfo((short) 0, ipd, false,
                                lead, total, middle);

        // node is a fo:ExternalGraphic, fo:InstreamForeignObject,
        // fo:PageNumber or fo:PageNumberCitation
        returnList.add(new KnuthInlineBox(areaInfo.ipdArea.opt, areaInfo.lead,
                                    areaInfo.total, areaInfo.middle,
                                    new LeafPosition(this, 0), false));
        setFinished(true);
        return returnList;
    }

    public List addALetterSpaceTo(List oldList) {
        // return the unchanged elements
        return oldList;
    }

    public void getWordChars(StringBuffer sbChars, Position pos) {
    }

    public void hyphenate(Position pos, HyphContext hc) {
    }

    public boolean applyChanges(List oldList) {
        setFinished(false);
        return false;
    }

    public LinkedList getChangedKnuthElements(List oldList,
                                              /*int flaggedPenalty,*/
                                              int alignment) {
        if (isFinished()) {
            return null;
        }

        LinkedList returnList = new LinkedList();

        // fobj is a fo:ExternalGraphic, fo:InstreamForeignObject,
        // fo:PageNumber or fo:PageNumberCitation
        returnList.add(new KnuthInlineBox(areaInfo.ipdArea.opt, areaInfo.lead,
                                          areaInfo.total, areaInfo.middle,
                                          new LeafPosition(this, 0), true));

        setFinished(true);
        return returnList;
    }
}

