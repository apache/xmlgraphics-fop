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

import java.util.Iterator;
import java.util.ListIterator;
import java.util.HashMap;

/**
 * LayoutManager for objects which stack children in the inline direction,
 * such as Inline or Line
 */
public class InlineStackingBPLayoutManager extends AbstractBPLayoutManager {

    /**
     * Private class to store information about a lower-level BreakPosition.
     * Note: fields are directly readable in this class
     */
    private static class WrappedPosition implements BreakPoss.Position {
        BPLayoutManager m_childLM;
        BreakPoss.Position m_childPosition;

        WrappedPosition(BPLayoutManager childLM,
			BreakPoss.Position childPosition) {
            m_childLM = childLM;
            m_childPosition = childPosition;
        }
    }

    private static class StackingIter extends PositionIterator {

	StackingIter(Iterator parentIter) {
	    super(parentIter);
	}

	protected BPLayoutManager getLM(Object nextObj) {
	    return ((WrappedPosition)nextObj).m_childLM;
	}

	protected BreakPoss.Position getPos(Object nextObj) {
	    return ((WrappedPosition)nextObj).m_childPosition;
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

    private InlineParent m_inlineArea;

    private BreakPoss m_prevBP;
    private LayoutContext m_childLC ;

    /** Used to store previous content IPD for each child LM. */
    private HashMap m_hmPrevIPD = new HashMap();

    public InlineStackingBPLayoutManager(FObj fobj, ListIterator childLMiter) {
	super(fobj, childLMiter);
	// Initialize inline properties (borders, padding, space)
	// initProperties();
    }

    public boolean generatesInlineAreas() {
        return true;
    }

    protected void initProperties(PropertyManager propMgr) {
	// super.initProperties(propMgr);
	System.err.println("InlineStackingBPLayoutManager.initProperties called");
        m_inlineProps = propMgr.getInlineProps();
        m_borderProps = propMgr.getBorderAndPadding();
	// Calculdate border and padding size in BPD
	int iPad = m_borderProps.getPadding(BorderAndPadding.BEFORE, false);
	iPad += m_borderProps.getBorderWidth(BorderAndPadding.BEFORE, false);
	iPad += m_borderProps.getPadding(BorderAndPadding.AFTER, false);
	iPad += m_borderProps.getBorderWidth(BorderAndPadding.AFTER, false);
	m_extraBPD = new MinOptMax(iPad);
    }

    private MinOptMax getExtraIPD(boolean bNotFirst, boolean bNotLast) {
	int iBP = m_borderProps.getPadding(BorderAndPadding.START, bNotFirst);
	iBP += m_borderProps.getBorderWidth(BorderAndPadding.START, bNotFirst);
	iBP += m_borderProps.getPadding(BorderAndPadding.END, bNotLast);
	iBP += m_borderProps.getBorderWidth(BorderAndPadding.END, bNotLast);
	return new MinOptMax(iBP);
    }

    protected boolean hasLeadingFence(boolean bNotFirst) {
	int iBP = m_borderProps.getPadding(BorderAndPadding.START, bNotFirst);
	iBP += m_borderProps.getBorderWidth(BorderAndPadding.START, bNotFirst);
	return (iBP > 0);
    }
    
    protected boolean hasTrailingFence(boolean bNotLast) {
	int iBP = m_borderProps.getPadding(BorderAndPadding.END, bNotLast);
	iBP += m_borderProps.getBorderWidth(BorderAndPadding.END, bNotLast);
	return (iBP > 0);
    }
    

    /** Reset position for returning next BreakPossibility. */

    public void resetPosition(BreakPoss.Position prevPos) {
	WrappedPosition wrappedPos = (WrappedPosition)prevPos;
	if (wrappedPos != null) {
	    // Back up the layout manager iterator
	    reset(wrappedPos.m_childLM, wrappedPos.m_childPosition);
	    if (m_prevBP != null && 
		m_prevBP.getLayoutManager() !=wrappedPos.m_childLM) {
		m_childLC = null;
	    }
	    m_prevBP = new BreakPoss(wrappedPos.m_childLM,
				     wrappedPos.m_childPosition);
	}
	else {
	    // Backup to start of first child layout manager
	    System.err.println("InlineStackingBPLM: resetPosition(null)");
	    m_prevBP = null;
	    super.resetPosition(prevPos);
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
	}
	else return false; // ??? NO child LM?
    }

    protected MinOptMax getPrevIPD(LayoutManager lm) {
	return (MinOptMax)m_hmPrevIPD.get(lm);
    }

    protected void clearPrevIPD() {
	m_hmPrevIPD.clear();
    }


    public BreakPoss getNextBreakPoss(LayoutContext lc,
				      BreakPoss.Position pbp) {
        // Get a break from currently active child LM
        BreakPoss bp =null;
	BPLayoutManager curLM ;
	SpaceSpecifier leadingSpace = lc.getPendingSpace();

	if (lc.startsNewArea()) {
	    // First call to this LM in new parent "area", but this may
	    // not be the first area created by this inline
	    m_childLC = new LayoutContext(lc);
	    lc.getPendingSpace().addSpace(m_inlineProps.spaceStart);
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
				     m_prevBP.getLayoutManager()!=curLM);

	    initChildLC(m_childLC, m_prevBP, lc.startsNewArea(), bFirstChildBP,
			leadingSpace);

	    if (((bp = curLM.getNextBreakPoss(m_childLC)) != null)) {
		break;
	    }
	    // If LM has no content, should it generate any area? If not,
	    // should trailing space from a previous area interact with
	    // leading space from a following area?
        }
        if (bp==null) {
	    setFinished(true);
            return null; // There was no childLM with anything to layout
            // Alternative is to return a BP with the isLast flag set
        }
        else {
            return makeBreakPoss(bp, lc, (getChildLM() == null));
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
	    childLC.setFlags(LayoutContext.FIRST_AREA, bFirstChildBP );
	    childLC.setPendingSpace(leadingSpace);
	}
	else if (bFirstChildBP) {
	    // Space-after sequence from previous "area"
	    childLC.setFlags(LayoutContext.FIRST_AREA, true);
	    childLC.setPendingSpace(prevBP.getTrailingSpace());
	}
	else {
	    childLC.setPendingSpace(null);
	}

    }

//     protected boolean couldEndLine(BreakPoss bp) {
// 	if (bp.canBreakAfter()) {
// 	    return true; // no keep, ends on break char
// 	}
// 	else if (bp.isSuppressible()) {
// 	    // NOTE: except at end of content for this LM!!
// 	    // Never break after only space chars or any other sequence
// 	    // of areas which would be suppressed at the end of the line.
// 	    return false; 
// 	}
// 	else {
// 	    // See if could break before next area
// 	    LayoutContext lc=new LayoutContext();
// 	    BPLayoutManager nextLM = getChildLM();
// 	    return (nextLM == null || 
// 		    nextLM.canBreakBefore(lc));
// 	}
//     }



    private BreakPoss makeBreakPoss(BreakPoss bp, LayoutContext lc,
				   boolean bIsLast) {
        WrappedPosition inlbp =
            new WrappedPosition(bp.getLayoutManager(), bp.getPosition());
	BreakPoss myBP = new BreakPoss(this, inlbp, bp.getFlags());

	myBP.setFlag(BreakPoss.ISFIRST, lc.isFirstArea());
	myBP.setFlag(BreakPoss.ISLAST, bIsLast);

        // Update dimension information for our allocation area,
	// including child areas
        // generated by previous childLM which have completed layout
        // Update pending area measure
        // This includes all previous breakinfo
	
	MinOptMax bpDim = (MinOptMax)bp.getStackingSize().clone();
	MinOptMax prevIPD = updatePrevIPD(bp, m_prevBP,
					  lc.startsNewArea(), lc.isFirstArea()); 

	if (lc.startsNewArea()) {
	    myBP.setLeadingSpace(lc.getPendingSpace());
	}

// 	if (lc.startsNewArea()) {
// 	    if (hasLeadingFence(!lc.isFirstArea())) {
// 		// Space-start before first child area placed
// 		prevIPD.add(bp.resolveLeadingSpace());
// 	    }
// 	    // Space-start sequence passed to ancestors
// 	    myBP.setLeadingSpace(lc.getPendingSpace());
// 	    m_hmPrevIPD.put(bp.getLayoutManager(), prevIPD);
// 	}
// 	else {
// 	    // In case of reset to a previous position, it may already
// 	    // be calculated
// 	    prevIPD = (MinOptMax)m_hmPrevIPD.get(bp.getLayoutManager());
// 	    if (prevIPD == null) {
// 		// ASSERT(m_prevBP.getLayoutManager() != bp.getLayoutManager());
// 		/* This is first bp generated by child (in this parent area).
// 		 * Calculate space-start on this area in combination with any
// 		 * pending space-end with previous break.
// 		 * Corresponds to Space between two child areas.
// 		 */
// 		prevIPD = 
// 		    (MinOptMax)m_hmPrevIPD.get(m_prevBP.getLayoutManager());
// 		prevIPD = MinOptMax.add(prevIPD,  bp.resolveLeadingSpace());
// 		prevIPD.add(m_prevBP.getStackingSize());
// 		m_hmPrevIPD.put(bp.getLayoutManager(), prevIPD);
// 	    }
// 	}
	// Add size of previous child areas which are finished
	bpDim.add(prevIPD);
//         if (bp.isLastArea()) {
// 	    m_childLC.setPendingSpace((SpaceSpecifier)bp.getTrailingSpace().
// 				      clone());
//         }

	SpaceSpecifier trailingSpace = bp.getTrailingSpace();
	if (hasTrailingFence(!bIsLast)) {
	    bpDim.add(bp.resolveTrailingSpace(false));
	    trailingSpace = new SpaceSpecifier(false);
	}
	trailingSpace.addSpace(m_inlineProps.spaceEnd);
	myBP.setTrailingSpace(trailingSpace);

	// Add start and end borders and padding
	bpDim.add(getExtraIPD(!lc.isFirstArea(), !bIsLast));
	myBP.setStackingSize(bpDim);
	myBP.setNonStackingSize(MinOptMax.add(bp.getNonStackingSize(),
					      m_extraBPD));

	m_prevBP = bp;
        if (bIsLast) {
	    setFinished(true);  // Our last area, so indicate done
        }
	return myBP;
    }


    /** ATTENTION: ALSO USED BY LineBPLayoutManager! */
    protected MinOptMax updatePrevIPD(BreakPoss bp, BreakPoss prevBP,
				      boolean bStartParent, boolean bFirstArea)
    {
	MinOptMax prevIPD = new MinOptMax(0);

	if (bStartParent) {
	    if (hasLeadingFence(!bFirstArea)) {
		// Space-start before first child area placed
		prevIPD.add(bp.resolveLeadingSpace());
	    }
	    // Space-start sequence passed to ancestors
	    // myBP.setLeadingSpace(lc.getPendingSpace());
	    m_hmPrevIPD.put(bp.getLayoutManager(), prevIPD);
	}
	else {
	    // In case of reset to a previous position, it may already
	    // be calculated
	    prevIPD = (MinOptMax)m_hmPrevIPD.get(bp.getLayoutManager());
	    if (prevIPD == null) {
		// ASSERT(prevBP.getLayoutManager() != bp.getLayoutManager());
		/* This is first bp generated by child (in this parent area).
		 * Calculate space-start on this area in combination with any
		 * pending space-end with previous break.
		 * Corresponds to Space between two child areas.
		 */
		prevIPD = 
		    (MinOptMax)m_hmPrevIPD.get(prevBP.getLayoutManager());
		prevIPD = MinOptMax.add(prevIPD,  bp.resolveLeadingSpace());
		prevIPD.add(prevBP.getStackingSize());
		m_hmPrevIPD.put(bp.getLayoutManager(), prevIPD);
	    }
	}
	return prevIPD;
    }


    /******
    protected BreakableText getText(BreakPoss prevBP, BreakPoss lastBP) {
    }
    *****/

    // Generate and add areas to parent area
    // Set size etc
    public void addAreas(PositionIterator parentIter) {
        // Make areas from start to end
        // Update childLM based on bpEnd
        // It might be a previous sibling of the current one!

        m_inlineArea = new InlineParent();
       
	// Note: if first, bpStart is perhaps null????
	// If we are first in parent, set ISFIRST...


	// posIter iterates over positions returned by this LM
	StackingIter childPosIter = new StackingIter(parentIter);
	BPLayoutManager childLM ;
	while  ((childLM = childPosIter.getNextChildLM())!= null) {
	    childLM.addAreas(childPosIter);
	}
	
	parentLM.addChild(m_inlineArea);
    }


//     protected Area createArea() {
//         return new InlineParent();
//     }

    public boolean addChild(Area childArea) {
	// Make sure childArea is inline area
	if (childArea instanceof InlineArea) {
	    m_inlineArea.addChild((InlineArea)childArea);
	}
	return false;
    }


}
