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
public class LeafNodeLayoutManager extends AbstractLayoutManager {
    /**
     * The inline area that this leafnode will add.
     */
    protected InlineArea curArea = null;
    protected int alignment;
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
        alignment = al;
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
    public void addChild(Area childArea) {
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
     * Get the next break position.
     * Since this holds an inline area it will return a single
     * break position.
     * @param context the layout context for this inline area
     * @return the break poisition for adding this inline area
     */
    public BreakPoss getNextBreakPoss(LayoutContext context) {
        curArea = get(context);
        if (curArea == null) {
            setFinished(true);
            return null;
        }
        BreakPoss bp = new BreakPoss(new LeafPosition(this, 0),
                                     BreakPoss.CAN_BREAK_AFTER
                                     | BreakPoss.CAN_BREAK_BEFORE | BreakPoss.ISFIRST
                                     | BreakPoss.ISLAST);
        ipd = getAllocationIPD(context.getRefIPD());
        bp.setStackingSize(ipd);
        bp.setNonStackingSize(new MinOptMax(curArea.getBPD()));
        bp.setTrailingSpace(new SpaceSpecifier(false));

        int bpd = curArea.getBPD();
        switch (alignment) {
            case VerticalAlign.MIDDLE:
                bp.setMiddle(bpd / 2 /* - fontLead/2 */);
                bp.setLead(bpd / 2 /* + fontLead/2 */);
            break;
            case VerticalAlign.TOP:
                bp.setTotal(bpd);
            break;
            case VerticalAlign.BOTTOM:
                bp.setTotal(bpd);
            break;
            case VerticalAlign.BASELINE:
            default:
                bp.setLead(bpd);
            break;
        }
        setFinished(true);
        return bp;
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
     * Reset the position.
     * If the reset position is null then this inline area should be
     * restarted.
     * @param resetPos the position to reset.
     */
    public void resetPosition(Position resetPos) {
        // only reset if setting null, start again
        if (resetPos == null) {
            setFinished(false);
        }
    }

    /**
     * Add the area for this layout manager.
     * This adds the single inline area to the parent.
     * @param posIter the position iterator
     * @param context the layout context for adding the area
     */
    public void addAreas(PositionIterator posIter, LayoutContext context) {
        addID();

        offsetArea(context);
        widthAdjustArea(context);
        parentLM.addChild(curArea);

        while (posIter.hasNext()) {
            posIter.next();
        }
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
        switch (alignment) {
            case VerticalAlign.MIDDLE:
                curArea.setOffset(context.getBaseline() - bpd / 2 /* - fontLead/2 */);
            break;
            case VerticalAlign.TOP:
                //curArea.setOffset(0);
            break;
            case VerticalAlign.BOTTOM:
                curArea.setOffset(context.getLineHeight() - bpd);
            break;
            case VerticalAlign.BASELINE:
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

        // node is a fo:ExternalGraphic, fo:InstreamForeignObject,
        // fo:PageNumber or fo:PageNumberCitation
        returnList.add(new KnuthBox(areaInfo.ipdArea.opt, areaInfo.lead,
                                    areaInfo.total, areaInfo.middle,
                                    new LeafPosition(this, 0), false));
        setFinished(true);
        return returnList;
    }

    public KnuthElement addALetterSpaceTo(KnuthElement element) {
        // return the unchanged box object
        return new KnuthBox(areaInfo.ipdArea.opt, areaInfo.lead,
                            areaInfo.total, areaInfo.middle,
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

        // fobj is a fo:ExternalGraphic, fo:InstreamForeignObject,
        // fo:PageNumber or fo:PageNumberCitation
        returnList.add(new KnuthBox(areaInfo.ipdArea.opt, areaInfo.lead,
                                    areaInfo.total, areaInfo.middle,
                                    new LeafPosition(this, 0), true));

        setFinished(true);
        return returnList;
    }
}

