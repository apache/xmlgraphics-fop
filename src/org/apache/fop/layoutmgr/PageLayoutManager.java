/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

import org.apache.fop.apps.FOPException;
import org.apache.fop.area.*;
import org.apache.fop.fo.flow.StaticContent;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.Region;
import org.apache.fop.fo.pagination.SimplePageMaster;
import org.apache.fop.fo.properties.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * LayoutManager for a PageSequence and its flow.
 * It manages all page-related layout.
 */
public class PageLayoutManager extends AbstractLayoutManager implements Runnable {

    private static class BlockBreakPosition extends LeafPosition {
        BreakPoss breakps;

        BlockBreakPosition(LayoutManager lm, BreakPoss bp) {
            super(lm, 0);
            breakps = bp;
        }
    }

    /** True if haven't yet laid out any pages.*/
    private boolean bFirstPage;
    /** Current page being worked on. */
    private PageViewport curPage;

    /** Body region of the current page */
    private BodyRegion curBody;

    /** Current span being filled */
    private Span curSpan;

    /** Number of columns in current span area. */
    private int curSpanColumns;

    /** Current flow-reference-area (column) being filled. */
    private Flow curFlow;

    private int flowBPD = 0;
    private int flowIPD = 0; 

    /** Manager which handles a queue of all pages which are completely
     * laid out and ready for rendering, except for resolution of ID
     * references?
     */
    private AreaTree areaTree;
    private PageSequence pageSequence;

    private int pageCount = 0;

    /**
     * This is the top level layout manager.
     * It is created by the PageSequence FO.
     */
    public PageLayoutManager(AreaTree areaTree, PageSequence pageseq) {
        super(pageseq);
        this.areaTree = areaTree;
        pageSequence = pageseq;
    }


    /**
     * The layout process is designed to be able to be run in a thread.
     * In theory it can run at the same
     * time as FO tree generation, once the layout-master-set has been read.
     * We can arrange it so that the iterator over the fobj children waits
     * until the next child is available.
     * As it produces pages, it adds them to the AreaTree, where the
     * rendering process can also run in a parallel thread.
     */
    public void run() {
        doLayout();
        flush();
    }

    public void doLayout() {

        // this should be done another way
        makeNewPage(false, false);
        createBodyMainReferenceArea();
        createSpan(1);
        flowIPD = curFlow.getIPD();

        BreakPoss bp;
        LayoutContext childLC = new LayoutContext(0);
        while (!isFinished()) {
            pageCount++;
            if ((bp = getNextBreakPoss(childLC)) != null) {
                addAreas((BlockBreakPosition)bp.getPosition());
                // add static areas and resolve any new id areas

                // finish page and add to area tree
                finishPage();
            }
        }

    }


    public BreakPoss getNextBreakPoss(LayoutContext context) {

        LayoutManager curLM ; // currently active LM

        while ((curLM = getChildLM()) != null) {
            BreakPoss bp = null;

            LayoutContext childLC = new LayoutContext(0);
            childLC.setStackLimit(new MinOptMax(flowBPD));
            childLC.setRefIPD(flowIPD);

            if (!curLM.isFinished()) {
                bp = curLM.getNextBreakPoss(childLC);
            }
            if (bp != null) {
                return new BreakPoss(
                         new BlockBreakPosition(curLM, bp));
            }
        }
        setFinished(true);
        return null;
    }

    public String getCurrentPageNumber() {
        return "" + pageCount;
    }

    public PageViewport resolveRefID(String ref) {
        List list = areaTree.getIDReferences(ref);
        if (list != null && list.size() > 0) {
            return (PageViewport)list.get(0);
        }
        return null;
    }

    public void addAreas(BlockBreakPosition bbp) {
        List list = new ArrayList();
        list.add(bbp.breakps);
        bbp.getLM().addAreas( new BreakPossPosIter(list, 0,
                              1), null);
    }

    public void addIDToPage(String id) {
        areaTree.addIDRef(id, curPage);
    }

    public void addUnresolvedArea(String id, Resolveable res) {
        // add unresolved to tree
        // adds to the page viewport so it can serialize
        curPage.addUnresolvedID(id, res);
        areaTree.addUnresolvedID(id, curPage);
    }

    /**
     * For now, only handle normal flow areas.
     */
    public boolean addChild(Area childArea) {
        if (childArea == null)
            return false;
        if (childArea.getAreaClass() == Area.CLASS_NORMAL) {
            return placeFlowRefArea(childArea);
        } else
            ; // TODO: all the others!
        return false;
    }

    /**
     * Place a FlowReferenceArea into the current span. The FlowLM is
     * responsible for making sure that it will actually fit in the
     * current span area. In fact the area has already been added to the
     * current span, so we are just checking to see if the span is "full",
     * possibly moving to the next column or to the next page.
     */
    protected boolean placeFlowRefArea(Area area) {
        // assert (curSpan != null);
        // assert (area == curFlow);
        // assert (curFlow == curSpan.getFlow(curSpan.getColumnCount()-1));
        // assert (area.getBPD().min < curSpan.getHeight());
        // Last column on this page is filled
        // See if the flow is full. The Flow LM can add an area before
        // it's full in the case of a break or a span.
        // Also in the case of a float to be placed. In that case, there
        // may be further material added later.
        // The Flow LM sets the "finished" flag on the Flow Area if it has
        // completely filled it. In this case, if on the last column
        // end the page.
        getParentArea(area);
        // Alternatively the child LM indicates to parent that it's full?
        //System.out.println("size: " + area.getAllocationBPD().max +
        //                   ":" + curSpan.getMaxBPD().min);
        if (area.getAllocationBPD().max >= curSpan.getMaxBPD().min) {
            // Consider it filled
            if (curSpan.getColumnCount() == curSpanColumns) {
                finishPage();
                return true;
            } else
                curFlow = null; // Create new flow on next getParentArea()
        }
        return false;
    }


    protected void placeAbsoluteArea(Area area) {
    }


    protected void placeBeforeFloat(Area area) {
    }


    protected void placeSideFloat(Area area) {
    }

    protected void placeFootnote(Area area) {
        // After doing this, reduce available space on the curSpan.
        // This has to be propagated to the curFlow (FlowLM) so that
        // it can adjust its limit for composition (or it just asks
        // curSpan for BPD before doing the break?)
        // If multi-column, we may have to balance to find more space
        // for a float. When?
    }

    private PageViewport makeNewPage(boolean bIsBlank, boolean bIsLast) {
        finishPage();
        try {
            curPage = pageSequence.createPage(bIsBlank, bIsLast);
        } catch (FOPException fopex) { /* ???? */
            fopex.printStackTrace();
        }

        curPage.setPageNumber(getCurrentPageNumber());
        RegionViewport reg = curPage.getPage().getRegion(
                    RegionReference.BODY);
        curBody = (BodyRegion) reg.getRegion();
        flowBPD = (int)reg.getViewArea().getHeight();
        return curPage;
    }

    private void layoutStaticContent(Region region, int regionClass) {
        if (region != null ) {
            StaticContent flow = pageSequence
              .getStaticContent(region.getRegionName());
            if (flow != null) {
                RegionViewport reg = curPage.getPage()
                  .getRegion(regionClass);
                reg.getRegion().setIPD((int)reg.getViewArea().getWidth());
                if (reg == null ) {
                    System.out.println("no region viewport: shouldn't happen");
                }
                StaticContentLayoutManager lm = flow.getLayoutManager();
                lm.init();
                lm.setRegionReference(reg.getRegion());
                lm.setParentLM(this);
                LayoutContext childLC = new LayoutContext(0);
                childLC.setStackLimit(new MinOptMax((int)curPage.getViewArea().getHeight()));
                childLC.setRefIPD((int)reg.getViewArea().getWidth());

                while (!lm.isFinished()) {
                    BreakPoss bp = lm.getNextBreakPoss(childLC);
                    if (bp != null) {
                        ArrayList vecBreakPoss = new ArrayList();
                        vecBreakPoss.add(bp);
                        lm.addAreas( new BreakPossPosIter(vecBreakPoss, 0,
                                                          vecBreakPoss.size()), null);
                    } else {
                      System.out.println("bp==null  cls="+regionClass);
                    }
                }
                //lm.flush();
                lm.reset(null);
            }
        }
    }
  
    private void finishPage() {
        if (curPage != null) {
            // Layout static content into the regions
            // Need help from pageseq for this
            SimplePageMaster spm = pageSequence.getCurrentSimplePageMaster();
            layoutStaticContent(spm.getRegion(Region.BEFORE), RegionReference.BEFORE);
            layoutStaticContent(spm.getRegion(Region.AFTER), RegionReference.AFTER);
            layoutStaticContent(spm.getRegion(Region.START), RegionReference.START);
            layoutStaticContent(spm.getRegion(Region.END), RegionReference.END);
            // Queue for ID resolution and rendering
            areaTree.addPage(curPage);
            curPage = null;
            curBody = null;
            curSpan = null;
            curFlow = null;
        }
    }

    /**
     * This is called from FlowLayoutManager when it needs to start
     * a new flow container (while generating areas).
     * @param area The area for which a container is needed. It must be
     * some kind of block-level area. It must have area-class, break-before
     * and span properties set.
     */
    public Area getParentArea(Area childArea) {
        int aclass = childArea.getAreaClass();
        if (aclass == Area.CLASS_NORMAL) {
            // TODO: how to get properties from the Area???
            // Need span, break
            int breakVal = Constants.AUTO; // childArea.getBreakBefore();
            if (breakVal != Constants.AUTO) {
                // We may be forced to make new page
                handleBreak(breakVal);
            } else if (curPage == null) {
                makeNewPage(false, false);
            }
            // Now we should be on the right kind of page
            boolean bNeedSpan = false;
            int span = Constants.NONE; // childArea.getSpan()
            int numCols = 1;
            if (span == Constants.ALL) {
                // Assume the number of columns is stored on the curBody object.
                //numCols = curBody.getProperty(NUMBER_OF_COLUMNS);
            }
            if (curSpan == null) {
                createBodyMainReferenceArea();
                bNeedSpan = true;
            } else if (numCols != curSpanColumns) {
                // TODO: BALANCE EXISTING COLUMNS
                if (curSpanColumns > 1) {
                    // balanceColumns();
                }
                bNeedSpan = true;
            }
            if (bNeedSpan) {
                // Make a new span and the first flow
                createSpan(numCols);
            } else if (curFlow == null) {
                createFlow();
            }
            return curFlow;
        } else {
            if (curPage == null) {
                makeNewPage(false, false);
            }
            // Now handle different kinds of areas
            if (aclass == Area.CLASS_BEFORE_FLOAT) {
                BeforeFloat bf = curBody.getBeforeFloat();
                if (bf == null) {
                    bf = new BeforeFloat();
                    curBody.setBeforeFloat(bf);
                }
                return bf;
            } else if (aclass == Area.CLASS_FOOTNOTE) {
                Footnote fn = curBody.getFootnote();
                if (fn == null) {
                    fn = new Footnote();
                    curBody.setFootnote(fn);
                }
                return fn;
            }
            // TODO!!! other area classes (side-float, absolute, fixed)
            return null;
        }
    }


    /**
     * Depending on the kind of break condition, make new column
     * or page. May need to make an empty page if next page would
     * not have the desired "handedness".
     */
    protected void handleBreak(int breakVal) {
        if (breakVal == Constants.COLUMN) {
            if (curSpan != null &&
                    curSpan.getColumnCount() != curSpanColumns) {
                // Move to next column
                createFlow();
                return;
            }
            // else need new page
            breakVal = Constants.PAGE;
        }
        if (needEmptyPage(breakVal)) {
            curPage = makeNewPage(true, false);
        }
        if (needNewPage(breakVal)) {
            curPage = makeNewPage(false, false);
        }
    }


    /**
     * If we have already started to layout content on a page,
     * and there is a forced break, see if we need to generate
     * an empty page.
     * Note that if not all content is placed, we aren't sure whether
     * it will flow onto another page or not, so we'd probably better
     * block until the queue of layoutable stuff is empty!
     */
    private boolean needEmptyPage(int breakValue) {
        return false;
        // 	if (breakValue == Constants.PAGE || curPage.isEmpty()) {
        // 	    // any page is OK or we already have an empty page
        // 	    return false;
        // 	}
        // 	else {
        // 	    /* IF we are on the kind of page we need, we'll need a new page. */
        // 	    if (curPage.getPageNumber()%2 != 0) {
        // 		// Current page is odd
        // 		return (breakValue == Constants.ODD_PAGE);
        // 	    }
        // 	    else {
        // 		return (breakValue == Constants.EVEN_PAGE);
        // 	    }
        // 	}
    }

    /**
     * See if need to generate a new page for a forced break condition.
     * TODO: methods to see if the current page is empty and to get
     * its number.
     */
    private boolean needNewPage(int breakValue) {
        return false;
        //if (curPage.isEmpty()) {
        //if (breakValue == Constants.PAGE) {
        //return false;
        //}
        //else if (curPage.getPageNumber()%2 != 0) {
        //// Current page is odd
        //return (breakValue == Constants.EVEN_PAGE);
        //}
        //else {
        //return (breakValue == Constants.ODD_PAGE);
        //}
        //}
        //else {
        //    return true;
        //}
    }


    private void createBodyMainReferenceArea() {
        curBody.setMainReference(new MainReference());
    }

    private Flow createFlow() {
        curFlow = new Flow();
        curFlow.setIPD(curSpan.getIPD()); // adjust for columns
        //curFlow.setBPD(100000);
        // Set IPD and max BPD on the curFlow from curBody
        curSpan.addFlow(curFlow);
        return curFlow;
    }

    private void createSpan(int numCols) {
        // check number of columns (= all in Body or 1)
        // If already have a span, get its size and position (as MinMaxOpt)
        // This determines the position of the new span area
        // Attention: space calculation between the span areas.

        //MinOptMax newpos ;
        //if (curSpan != null) {
        //newpos = curSpan.getPosition(BPD);
        //newpos.add(curSpan.getDimension(BPD));
        //}
        //else newpos = new MinOptMax();
        curSpan = new Span(numCols);
        // get Width or Height as IPD for span
        curSpan.setIPD((int) curPage.getPage().getRegion(
                          RegionReference.BODY).getViewArea().getWidth());

        //curSpan.setPosition(BPD, newpos);
        curBody.getMainReference().addSpan(curSpan);
        createFlow();
    }

    // See finishPage...
    protected boolean flush() {
        finishPage();
        return false;
    }

}
