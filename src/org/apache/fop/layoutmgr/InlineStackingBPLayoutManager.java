/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyManager;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.traits.InlineProps;
import org.apache.fop.area.Area;
import org.apache.fop.area.MinOptMax;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.Space;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.HashMap;

/**
 * LayoutManager for objects which stack children in the inline direction,
 * such as Inline or Line
 */
public class InlineStackingBPLayoutManager extends AbstractBPLayoutManager {


    private static class StackingIter extends PositionIterator {

        StackingIter(Iterator parentIter) {
            super(parentIter);
        }

        protected BPLayoutManager getLM(Object nextObj) {
            return ((Position) nextObj).getPosition().getLM();
        }

        protected Position getPos(Object nextObj) {
            return ((Position) nextObj).getPosition();
        }

    }


    /**
     * Size of any start or end borders and padding.
     */
    private MinOptMax m_allocIPD = new MinOptMax(0);

    /**
     * Size of border and padding in BPD (ie, before and after).
     */
    private MinOptMax m_extraBPD;


    private InlineProps m_inlineProps = null;
    private BorderAndPadding m_borderProps = null;

    private Area m_currentArea; // LineArea or InlineParent

    private BreakPoss m_prevBP;
    private LayoutContext m_childLC ;

    private BPLayoutManager m_lastChildLM = null; // Set when return last breakposs
    private boolean m_bAreaCreated = false;

    /** Used to store previous content IPD for each child LM. */
    private HashMap m_hmPrevIPD = new HashMap();

    public InlineStackingBPLayoutManager(FObj fobj,
                                         ListIterator childLMiter) {
        super(fobj, childLMiter);
        // Initialize inline properties (borders, padding, space)
        // initProperties();
    }

    public boolean generatesInlineAreas() {
        return true;
    }

    protected void initProperties(PropertyManager propMgr) {
        // super.initProperties(propMgr);
        m_inlineProps = propMgr.getInlineProps();
        m_borderProps = propMgr.getBorderAndPadding();
        // Calculdate border and padding size in BPD
        int iPad = m_borderProps.getPadding(BorderAndPadding.BEFORE, false);
        iPad += m_borderProps.getBorderWidth(BorderAndPadding.BEFORE,
                                             false);
        iPad += m_borderProps.getPadding(BorderAndPadding.AFTER, false);
        iPad += m_borderProps.getBorderWidth(BorderAndPadding.AFTER, false);
        m_extraBPD = new MinOptMax(iPad);
    }

    private MinOptMax getExtraIPD(boolean bNotFirst, boolean bNotLast) {
        int iBP = m_borderProps.getPadding(BorderAndPadding.START,
                                           bNotFirst);
        iBP += m_borderProps.getBorderWidth(BorderAndPadding.START,
                                            bNotFirst);
        iBP += m_borderProps.getPadding(BorderAndPadding.END, bNotLast);
        iBP += m_borderProps.getBorderWidth(BorderAndPadding.END, bNotLast);
        return new MinOptMax(iBP);
    }


    protected boolean hasLeadingFence(boolean bNotFirst) {
        int iBP = m_borderProps.getPadding(BorderAndPadding.START,
                                           bNotFirst);
        iBP += m_borderProps.getBorderWidth(BorderAndPadding.START,
                                            bNotFirst);
        return (iBP > 0);
    }

    protected boolean hasTrailingFence(boolean bNotLast) {
        int iBP = m_borderProps.getPadding(BorderAndPadding.END, bNotLast);
        iBP += m_borderProps.getBorderWidth(BorderAndPadding.END, bNotLast);
        return (iBP > 0);
    }


    /**
     * Reset position for returning next BreakPossibility.
     * @param prevPos a Position returned by this layout manager
     * representing a potential break decision.
     */

    public void resetPosition(Position prevPos) {
        if (prevPos != null) {
            // ASSERT (prevPos.getLM() == this)
            if (prevPos.getLM() != this) {
                //log.error(
                //  "InlineStackingBPLayoutManager.resetPosition: " +
                //  "LM mismatch!!!");
            }
            // Back up the child LM Position
            Position childPos = prevPos.getPosition();
            reset(childPos);
            if (m_prevBP != null &&
                    m_prevBP.getLayoutManager() != childPos.getLM()) {
                m_childLC = null;
            }
            m_prevBP = new BreakPoss(childPos);
        } else {
            // Backup to start of first child layout manager
            m_prevBP = null;
            // super.resetPosition(prevPos);
            reset(prevPos);
            // If any areas created, we are restarting!
            m_bAreaCreated = false;
        }
        // Do we need to reset some context like pending or prevContent?
        // What about m_prevBP?
    }


    /**
     * Return value indicating whether the next area to be generated could
     * start a new line. This should only be called in the "START" condition
     * if a previous inline BP couldn't end the line.
     * Return true if any space-start, border-start or padding-start, else
     * propagate to first child LM
     */
    public boolean canBreakBefore(LayoutContext context) {
        if (m_inlineProps.spaceStart.space.min > 0 ||
                hasLeadingFence(false)) {
            return true;
        }
        BPLayoutManager lm = getChildLM();
        if (lm != null) {
            return lm.canBreakBefore(context);
        } else
            return false; // ??? NO child LM?
    }

    protected MinOptMax getPrevIPD(LayoutManager lm) {
        return (MinOptMax) m_hmPrevIPD.get(lm);
    }


    protected void clearPrevIPD() {
        m_hmPrevIPD.clear();
    }


    public BreakPoss getNextBreakPoss(LayoutContext lc, Position pbp) {
        // Get a break from currently active child LM
        BreakPoss bp = null;
        BPLayoutManager curLM ;
        SpaceSpecifier leadingSpace = lc.getLeadingSpace();

        if (lc.startsNewArea()) {
            // First call to this LM in new parent "area", but this may
            // not be the first area created by this inline
            m_childLC = new LayoutContext(lc);
            lc.getLeadingSpace().addSpace(m_inlineProps.spaceStart);

            // Check for "fence"
            if (hasLeadingFence(!lc.isFirstArea())) {
                // Reset leading space sequence for child areas
                leadingSpace = new SpaceSpecifier(false);
            }
            // Reset state variables
            clearPrevIPD(); // Clear stored prev content dimensions
        }

        // We only do this loop more than once if a childLM returns
        // a null BreakPoss, meaning it has nothing (more) to layout.
        while ((curLM = getChildLM()) != null) {
            /* If first break for this child LM, set START_AREA flag
             * and initialize pending space from previous LM sibling's
             * trailing space specifiers.
             */
            boolean bFirstChildBP = (m_prevBP == null ||
                                     m_prevBP.getLayoutManager() != curLM);

            initChildLC(m_childLC, m_prevBP, lc.startsNewArea(),
                        bFirstChildBP, leadingSpace);
            if (lc.tryHyphenate()) {
                m_childLC.setHyphContext(lc.getHyphContext());
            }

            if (((bp = curLM.getNextBreakPoss(m_childLC)) != null) ||
                    (lc.tryHyphenate() &&
                     !lc.getHyphContext().hasMoreHyphPoints())) {
                break;
            }
            // If LM has no content, should it generate any area? If not,
            // should trailing space from a previous area interact with
            // leading space from a following area?
        }
        if (bp == null) {
            setFinished(true);
            return null; // There was no childLM with anything to layout
            // Alternative is to return a BP with the isLast flag set
        } else {
            boolean bIsLast = false;
            if (getChildLM() == null) {
                bIsLast = true;
                setFinished(true);
            } else if (bp.couldEndLine()) {
                /* Child LM ends with suppressible spaces. See if it could
                 * end this LM's area too. Child LM finish flag is NOT set!
                 */
                bIsLast = !hasMoreLM(bp.getLayoutManager());
            }
            return makeBreakPoss(bp, lc, bIsLast);
        }
    }

    /** ATTENTION: ALSO USED BY LineBPLayoutManager! */
    protected void initChildLC(LayoutContext childLC, BreakPoss prevBP,
                               boolean bStartParent, boolean bFirstChildBP,
                               SpaceSpecifier leadingSpace) {

        childLC.setFlags(LayoutContext.NEW_AREA,
                         (bFirstChildBP || bStartParent));
        if (bStartParent) {
            // Start of a new line area or inline parent area
            childLC.setFlags(LayoutContext.FIRST_AREA, bFirstChildBP);
            childLC.setLeadingSpace(leadingSpace);
        } else if (bFirstChildBP) {
            // Space-after sequence from previous "area"
            childLC.setFlags(LayoutContext.FIRST_AREA, true);
            childLC.setLeadingSpace(prevBP.getTrailingSpace());
        } else {
            childLC.setLeadingSpace(null);
        }

    }


    private BreakPoss makeBreakPoss(BreakPoss bp, LayoutContext lc,
                                    boolean bIsLast) {
        NonLeafPosition inlbp = new NonLeafPosition(this, bp.getPosition());
        BreakPoss myBP = new BreakPoss(inlbp, bp.getFlags());

        myBP.setFlag(BreakPoss.ISFIRST, lc.isFirstArea());
        myBP.setFlag(BreakPoss.ISLAST, bIsLast);

        if (bIsLast) {
            m_lastChildLM = bp.getLayoutManager();
        }

        // Update dimension information for our allocation area,
        // including child areas
        // generated by previous childLM which have completed layout
        // Update pending area measure
        // This includes all previous breakinfo

        MinOptMax bpDim = (MinOptMax) bp.getStackingSize().clone();
        MinOptMax prevIPD = updatePrevIPD(bp, m_prevBP, lc.startsNewArea(),
                                          lc.isFirstArea());

        if (lc.startsNewArea()) {
            myBP.setLeadingSpace(lc.getLeadingSpace());
        }


        // Add size of previous child areas which are finished
        bpDim.add(prevIPD);

        SpaceSpecifier trailingSpace = bp.getTrailingSpace();
        if (hasTrailingFence(!bIsLast)) {
            bpDim.add(bp.resolveTrailingSpace(false));
            trailingSpace = new SpaceSpecifier(false);
        } else {
            // Need this to avoid modifying pending space specifiers
            // on previous BP from child as we use these on the next
            // call in this LM
            trailingSpace = (SpaceSpecifier) trailingSpace.clone();
        }
        trailingSpace.addSpace(m_inlineProps.spaceEnd);
        myBP.setTrailingSpace(trailingSpace);

        // Add start and end borders and padding
        bpDim.add(getExtraIPD(!lc.isFirstArea(), !bIsLast));
        myBP.setStackingSize(bpDim);
        myBP.setNonStackingSize(
          MinOptMax.add(bp.getNonStackingSize(), m_extraBPD));

        m_prevBP = bp;
        //         if (bIsLast) {
        // 	    setFinished(true);  // Our last area, so indicate done
        //         }
        return myBP;
    }


    /** ATTENTION: ALSO USED BY LineBPLayoutManager! */
    protected MinOptMax updatePrevIPD(BreakPoss bp, BreakPoss prevBP,
                                      boolean bStartParent, boolean bFirstArea) {
        MinOptMax prevIPD = new MinOptMax(0);

        if (bStartParent) {
            if (hasLeadingFence(!bFirstArea)) {
                // Space-start before first child area placed
                prevIPD.add(bp.resolveLeadingSpace());
            }
            m_hmPrevIPD.put(bp.getLayoutManager(), prevIPD);
        } else {
            // In case of reset to a previous position, it may already
            // be calculated
            prevIPD = (MinOptMax) m_hmPrevIPD.get(bp.getLayoutManager());
            if (prevIPD == null) {
                // ASSERT(prevBP.getLayoutManager() != bp.getLayoutManager());
                /* This is first bp generated by child (in this parent area).
                 * Calculate space-start on this area in combination with any
                 * pending space-end with previous break.
                 * Corresponds to Space between two child areas.
                 */
                prevIPD = (MinOptMax) m_hmPrevIPD.get(
                            prevBP.getLayoutManager());
                prevIPD = MinOptMax.add(prevIPD, bp.resolveLeadingSpace());
                prevIPD.add(prevBP.getStackingSize());
                m_hmPrevIPD.put(bp.getLayoutManager(), prevIPD);
            }
        }
        return prevIPD;
    }

    public void getWordChars(StringBuffer sbChars, Position bp1,
                             Position bp2) {
        Position endPos = ((NonLeafPosition) bp2).getPosition();
        Position prevPos = null;
        if (bp1 != null) {
            prevPos = ((NonLeafPosition) bp1).getPosition();
            if (prevPos.getLM() != endPos.getLM()) {
                prevPos = null;
            }
        }
        endPos.getLM().getWordChars(sbChars, prevPos, endPos);
    }

    /******
      protected BreakableText getText(BreakPoss prevBP, BreakPoss lastBP) {
      }
     *****/

    /**
     * Generate and add areas to parent area.
     * Set size of each area.
     * @param parentIter Iterator over Position information returned
     * by this LayoutManager.
     * @param dSpaceAdjust Factor controlling how much extra space to add
     * in order to justify the line.
     */
    public void addAreas(PositionIterator parentIter,
                         LayoutContext context) {
        setCurrentArea(new InlineParent());

        setChildContext(new LayoutContext(context)); // Store current value

        // If has fence, make a new leadingSS
        /* How to know if first area created by this LM? Keep a count and
         * reset it if getNextBreakPoss() is called again.
         */
        if (hasLeadingFence(m_bAreaCreated)) {
            getContext().setLeadingSpace(new SpaceSpecifier(false));
            getContext().setFlags(LayoutContext.RESOLVE_LEADING_SPACE,
                                  true);
        } else {
            getContext().setFlags(LayoutContext.RESOLVE_LEADING_SPACE,
                                  false);
        }

        context.getLeadingSpace().addSpace(m_inlineProps.spaceStart);


        // posIter iterates over positions returned by this LM
        StackingIter childPosIter = new StackingIter(parentIter);
        BPLayoutManager prevLM = null;
        BPLayoutManager childLM ;
        while ((childLM = childPosIter.getNextChildLM()) != null) {
            //getContext().setTrailingSpace(new SpaceSpecifier(false));
            childLM.addAreas(childPosIter, getContext());
            getContext().setLeadingSpace(getContext().getTrailingSpace());
            getContext().setFlags(LayoutContext.RESOLVE_LEADING_SPACE,
                                  true);
            prevLM = childLM;
        }

        /* If has trailing fence, resolve trailing space specs from descendants.
         * Otherwise, propagate any trailing space specs to parent LM via
         * the context object.
         * If the last child LM called return ISLAST in the context object
         * and it is the last child LM for this LM, then this must be
         * the last area for the current LM also.
         */
        boolean bIsLast =
          (getContext().isLastArea() && prevLM == m_lastChildLM);
        if (hasTrailingFence(bIsLast)) {
            addSpace(getCurrentArea(),
                     getContext().getTrailingSpace().resolve(false),
                     getContext().getSpaceAdjust());
            context.setTrailingSpace(new SpaceSpecifier(false));
        } else {
            // Propagate trailing space-spec sequence to parent LM in context
            context.setTrailingSpace(getContext().getTrailingSpace());
        }
        // Add own trailing space to parent context (or set on area?)
        context.getTrailingSpace().addSpace(m_inlineProps.spaceEnd);

        // Add border and padding to current area and set flags (FIRST, LAST ...)
        TraitSetter.setBorderPaddingTraits(getCurrentArea(),
                                           m_borderProps, m_bAreaCreated, !bIsLast);
        parentLM.addChild(getCurrentArea());
        context.setFlags(LayoutContext.LAST_AREA, bIsLast);
        m_bAreaCreated = true;
    }

    protected Area getCurrentArea() {
        return m_currentArea;
    }

    protected void setCurrentArea(Area area) {
        m_currentArea = area;
    }


    public boolean addChild(Area childArea) {
        // Make sure childArea is inline area
        if (childArea instanceof InlineArea) {
            Area parent = getCurrentArea();
            if (getContext().resolveLeadingSpace()) {
                addSpace(parent,
                         getContext().getLeadingSpace().resolve(false),
                         getContext().getSpaceAdjust());
            }
            parent.addChild(childArea);
        }
        return false;
    }

    protected void setChildContext(LayoutContext lc) {
        m_childLC = lc;
    }

    // Current child layout context
    protected LayoutContext getContext() {
        return m_childLC ;
    }

    protected void addSpace(Area parentArea, MinOptMax spaceRange,
                            double dSpaceAdjust) {
        if (spaceRange != null) {
            int iAdjust = spaceRange.opt;
            if (dSpaceAdjust > 0.0) {
                // Stretch by factor
                iAdjust += (int)((double)(spaceRange.max -
                                          spaceRange.opt) * dSpaceAdjust);
            } else if (dSpaceAdjust < 0.0) {
                // Shrink by factor
                iAdjust += (int)((double)(spaceRange.opt -
                                          spaceRange.min) * dSpaceAdjust);
            }
            if (iAdjust != 0) {
                //log.error("Add leading space: " + iAdjust);
                Space ls = new Space();
                ls.setWidth(iAdjust);
                parentArea.addChild(ls);
            }
        }
    }


}
