/*
 * $Id: LeafNodeLayoutManager.java,v 1.23 2003/03/07 07:58:51 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.layoutmgr;

import org.apache.fop.area.Area;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.fo.properties.VerticalAlign;

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
    private int alignment;
    private int lead;
    private MinOptMax ipd;

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
        bp.setNonStackingSize(new MinOptMax(curArea.getHeight()));
        bp.setTrailingSpace(new SpaceSpecifier(false));

        int bpd = curArea.getHeight();
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
        parentLM.addChild(curArea);

        addID();

        offsetArea(context);
        widthAdjustArea(context);

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
        int bpd = curArea.getHeight();
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
        int width = ipd.opt;
        if (dAdjust < 0) {
            width = (int)(width + dAdjust * (ipd.opt - ipd.min));
        } else if (dAdjust > 0) {
            width = (int)(width + dAdjust * (ipd.max - ipd.opt));
        }
        curArea.setWidth(width);
    }

    /**
     * Check if can break before this area.
     * @param context the layout context to check for the break
     * @return true if can break before this area in the context
     */
    public boolean canBreakBefore(LayoutContext context) {
        return true;
    }
}

