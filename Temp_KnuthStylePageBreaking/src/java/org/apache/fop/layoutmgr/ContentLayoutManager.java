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

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.pagination.Title;
import org.apache.fop.area.Area;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.inline.InlineArea;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import org.apache.fop.traits.MinOptMax;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Content Layout Manager.
 * For use with objects that contain inline areas such as
 * leader use-content and title.
 */
public class ContentLayoutManager implements InlineLevelLayoutManager {
    private FOUserAgent userAgent;
    private Area holder;
    private int stackSize;
    private LayoutManager parentLM;
    private InlineLevelLayoutManager childLM = null;

    /**
     * logging instance
     */
    protected static Log log = LogFactory.getLog(LayoutManager.class);

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
     * Constructor using a fo:title formatting object
     */
    public ContentLayoutManager(Title foTitle, LayoutManager parentLM) {
        // get breaks then add areas to title
        this.parentLM = parentLM;
        holder = new LineArea();

        setUserAgent(foTitle.getUserAgent());

        // use special layout manager to add the inline areas
        // to the Title.
        InlineLayoutManager lm;
        lm = new InlineLayoutManager(foTitle);
        addChildLM(lm);
        fillArea(lm);
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
            if (element.isBox()) {
                KnuthInlineBox box = (KnuthInlineBox) element;
                if (box.getLead() > lineLead) {
                    lineLead = box.getLead();
                }
                if (box.getTotal() > maxtb) {
                    maxtb = box.getTotal();
                }
                // Is this needed? cf. LineLM.makeLineBreakPosition
                // if (box.getMiddle() > lineLead) {
                //     lineLead = box.getMiddle();
                // }
                if (box.getMiddle() > middlefollow) {
                    middlefollow = box.getMiddle();
                }
            }
        }

        if (maxtb - lineLead > middlefollow) {
            middlefollow = maxtb - lineLead;
        }

        LayoutContext lc = new LayoutContext(0);
        lc.setBaseline(lineLead);
        lc.setLineHeight(lineHeight);

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
    public boolean generatesInlineAreas() {
        return true;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager#isBogus() */
    public boolean isBogus() {
        return false;
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
     * Set the user agent.
     *
     * @param ua the user agent
     */
    public void setUserAgent(FOUserAgent ua) {
        userAgent = ua;
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getUserAgent()
     */
    public FOUserAgent getUserAgent() {
        return userAgent;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void setParent(LayoutManager lm) {
        parentLM = lm;
    }

    public LayoutManager getParent() {
        return this.parentLM;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public boolean canBreakBefore(LayoutContext lc) {
        return false;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public BreakPoss getNextBreakPoss(LayoutContext context) {
        return null;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public boolean isFinished() {
        return false;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void setFinished(boolean isFinished) {
        //to be done
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void initialize() {
        //to be done
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void resetPosition(Position position) {
        //to be done
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void getWordChars(StringBuffer sbChars, Position bp1,
            Position bp2) { }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#preLoadNext
     */
    public boolean preLoadNext(int pos) {
        return false;
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getChildLMs
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
        lm.initialize();
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

    public LinkedList getNextKnuthElements(LayoutContext context,
                                           int alignment) {
        LinkedList contentList = new LinkedList();
        LinkedList returnedList;

        while (!childLM.isFinished()) {
            // get KnuthElements from childLM
            returnedList = childLM.getNextKnuthElements(context, alignment);

            if (returnedList != null) {
                // move elements to contentList, and accumulate their size
               KnuthElement contentElement;
               while (returnedList.size() > 0) {
                    contentElement = (KnuthElement)returnedList.removeFirst();
                    stackSize += contentElement.getW();
                    contentList.add(contentElement);
                }
            }
        }

        setFinished(true);
        return contentList;
    }

    public List addALetterSpaceTo(List oldList) {
        return oldList;
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
}

