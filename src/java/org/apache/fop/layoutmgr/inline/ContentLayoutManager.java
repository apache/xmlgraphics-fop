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

package org.apache.fop.layoutmgr.inline;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.pagination.Title;
import org.apache.fop.layoutmgr.AbstractBaseLayoutManager;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.layoutmgr.KnuthSequence;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.PageSequenceLayoutManager;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.SpaceSpecifier;
import org.apache.fop.area.Area;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.inline.InlineArea;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.fop.traits.MinOptMax;

import org.apache.fop.area.Block;

/**
 * Content Layout Manager.
 * For use with objects that contain inline areas such as
 * leader use-content and title.
 */
public class ContentLayoutManager extends AbstractBaseLayoutManager
        implements InlineLevelLayoutManager {
    private FOUserAgent userAgent;
    private Area holder;
    private int stackSize;
    private LayoutManager parentLM;
    private InlineLevelLayoutManager childLM = null;

    /**
     * Constructs a new ContentLayoutManager
     *
     * @param area  The parent area
     */
    public ContentLayoutManager(Area area, LayoutManager parentLM) {
        holder = area;
        this.parentLM = parentLM;
    }

    /**
     * Constructor using a fo:title formatting object and its
     * PageSequenceLayoutManager parent.
     */
    public ContentLayoutManager(Title foTitle, PageSequenceLayoutManager pslm) {
        // get breaks then add areas to title
        this.parentLM = pslm;
        holder = new LineArea();

        //        setUserAgent(foTitle.getUserAgent());

        // use special layout manager to add the inline areas
        // to the Title.
        InlineLayoutManager lm;
        lm = new InlineLayoutManager(foTitle);
        addChildLM(lm);
        fillArea(lm);
    }

    public void initialize() {
        // Empty
    }

    public void fillArea(LayoutManager curLM) {

        int ipd = 1000000;

        LayoutContext childLC = new LayoutContext(LayoutContext.NEW_AREA);
        childLC.setLeadingSpace(new SpaceSpecifier(false));
        childLC.setTrailingSpace(new SpaceSpecifier(false));
        // set stackLimit for lines
        childLC.setStackLimit(new MinOptMax(ipd));
        childLC.setRefIPD(ipd);

        int lineHeight = 14000;
        int lead = 12000;
        int follow = 2000;

        int halfLeading = (lineHeight - lead - follow) / 2;
        // height before baseline
        int lineLead = lead + halfLeading;
        // maximum size of top and bottom alignment
        int maxtb = follow + halfLeading;
        // max size of middle alignment below baseline
        int middlefollow = maxtb;

        stackSize = 0;

        LinkedList contentList =
            getNextKnuthElements(childLC, Constants.EN_START);
        ListIterator contentIter = contentList.listIterator();
        while (contentIter.hasNext()) {
            KnuthElement element = (KnuthElement) contentIter.next();
            if (element instanceof KnuthInlineBox) {
                KnuthInlineBox box = (KnuthInlineBox) element;
                // TODO handle alignment here?
            }
        }

        if (maxtb - lineLead > middlefollow) {
            middlefollow = maxtb - lineLead;
        }

        LayoutContext lc = new LayoutContext(0);

        lc.setFlags(LayoutContext.RESOLVE_LEADING_SPACE, true);
        lc.setLeadingSpace(new SpaceSpecifier(false));
        lc.setTrailingSpace(new SpaceSpecifier(false));
        KnuthPossPosIter contentPosIter =
            new KnuthPossPosIter(contentList, 0, contentList.size());
        curLM.addAreas(contentPosIter, lc);
    }

    public void addAreas(PositionIterator posIter, LayoutContext context) {
        // add the content areas
        // the area width has already been adjusted, and it must remain unchanged
        // so save its value before calling addAreas, and set it again afterwards
        int savedIPD = ((InlineArea)holder).getIPD();
        // set to zero the ipd adjustment ratio, to avoid spaces in the pattern
        // to be modified
        LayoutContext childContext = new LayoutContext(context);
        childContext.setIPDAdjust(0.0);
        childLM.addAreas(posIter, childContext);
        ((InlineArea)holder).setIPD(savedIPD);
    }

    public int getStackingSize() {
        return stackSize;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public Area getParentArea(Area childArea) {
        return holder;
    }

    /** 
     * @see org.apache.fop.layoutmgr.LayoutManager#addChildArea(Area)
     **/
    public void addChildArea(Area childArea) {
        holder.addChildArea(childArea);
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#setParent(LayoutManager)
     */
    public void setParent(LayoutManager lm) {
        parentLM = lm;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager#getParent() */
    public LayoutManager getParent() {
        return this.parentLM;
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#isFinished()
     */
    public boolean isFinished() {
        return false;
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#setFinished(boolean)
     */
    public void setFinished(boolean isFinished) {
        //to be done
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#resetPosition(Position)
     */
    public void resetPosition(Position position) {
        //to be done
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#createNextChildLMs(int)
     */
    public boolean createNextChildLMs(int pos) {
        return false;
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getChildLMs()
     */
    public List getChildLMs() {
        List childLMs = new ArrayList(1);
        childLMs.add(childLM);
        return childLMs;
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#addChildLM
     */
    public void addChildLM(LayoutManager lm) {
        if (lm == null) {
            return;
        }
        lm.setParent(this);
        childLM = (InlineLevelLayoutManager)lm;
        log.trace(this.getClass().getName()
                  + ": Adding child LM " + lm.getClass().getName());
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#addChildLMs
     */
    public void addChildLMs(List newLMs) {
        if (newLMs == null || newLMs.size() == 0) {
            return;
        }
        ListIterator iter = newLMs.listIterator();
        while (iter.hasNext()) {
            LayoutManager lm = (LayoutManager) iter.next();
            addChildLM(lm);
        }
    }

    public LinkedList getNextKnuthElements(LayoutContext context, int alignment) {
        LinkedList contentList = new LinkedList();
        LinkedList returnedList;

        while (!childLM.isFinished()) {
            // get KnuthElements from childLM
            returnedList = childLM.getNextKnuthElements(context, alignment);

            if (returnedList != null) {
                // move elements to contentList, and accumulate their size
               KnuthElement contentElement;
               while (returnedList.size() > 0) {
                    Object obj = returnedList.removeFirst();
                    if (obj instanceof KnuthSequence) {
                        KnuthSequence ks = (KnuthSequence)obj;
                        for (Iterator it = ks.iterator(); it.hasNext(); ) {
                            contentElement = (KnuthElement)it.next();
                            stackSize += contentElement.getW();
                            contentList.add(contentElement);
                        }
                    } else {
                        contentElement = (KnuthElement)obj;
                        stackSize += contentElement.getW();
                        contentList.add(contentElement);
                    }
                }
            }
        }

        setFinished(true);
        return contentList;
    }

    public List addALetterSpaceTo(List oldList) {
        return oldList;
    }

    /**
     * Remove the word space represented by the given elements
     *
     * @param oldList the elements representing the word space
     */
    public void removeWordSpace(List oldList) {
        // do nothing
        log.warn(this.getClass().getName() + " should not receive a call to removeWordSpace(list)");
    }

    public void getWordChars(StringBuffer sbChars, Position pos) {
    }

    public void hyphenate(Position pos, HyphContext hc) {
    }

    public boolean applyChanges(List oldList) {
        return false;
    }

    public LinkedList getChangedKnuthElements(List oldList,
                                              /*int flaggedPenalty,*/
                                              int alignment) {
        return null;
    }
    
    public PageSequenceLayoutManager getPSLM() {
        return parentLM.getPSLM();
    }

    // --------- Property Resolution related functions --------- //
    
    /**
     * Returns the IPD of the content area
     * @return the IPD of the content area
     */
    public int getContentAreaIPD() {
        return holder.getIPD();
    }
   
    /**
     * Returns the BPD of the content area
     * @return the BPD of the content area
     */
    public int getContentAreaBPD() {
        return holder.getBPD();
    }
    
    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getGeneratesReferenceArea
     */
    public boolean getGeneratesReferenceArea() {
        return false;
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getGeneratesBlockArea
     */
    public boolean getGeneratesBlockArea() {
        return getGeneratesLineArea() || holder instanceof Block;
    }
   
    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getGeneratesLineArea
     */
    public boolean getGeneratesLineArea() {
        return holder instanceof LineArea;
    }

    /* (non-Javadoc)
     * @see org.apache.fop.layoutmgr.LayoutManager#notifyPos(org.apache.fop.layoutmgr.Position)
     */
    public Position notifyPos(Position pos) {
        return pos;
    }
   
}

