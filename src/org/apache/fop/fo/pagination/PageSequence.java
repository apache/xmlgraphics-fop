/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
/*
 Modified by Mark Lillywhite mark-fop@inomial.com. Does not add
 itself to the root any more. Does not hang onto currentPage
 pointer, which caused GC issues.
 */

package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.fo.flow.Flow;
import org.apache.fop.fo.flow.StaticContent;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.AreaContainer;
import org.apache.fop.layout.BodyAreaContainer;
import org.apache.fop.layout.AreaTree;
import org.apache.fop.layout.Page;
import org.apache.fop.layout.PageMaster;
import org.apache.fop.apps.FOPException;

// Java
import java.util.*;


/**
 * This provides pagination of flows onto pages. Much of the logic for paginating
 * flows is contained in this class. The main entry point is the format method.
 */
public class PageSequence extends FObj {
    //
    // Factory methods
    //
    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new PageSequence(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new PageSequence.Maker();
    }

    //
    // intial-page-number types
    //
    private static final int EXPLICIT = 0;
    private static final int AUTO = 1;
    private static final int AUTO_EVEN = 2;
    private static final int AUTO_ODD = 3;

    //
    // associations
    //

    /**
     * The parent root object
     */
    private Root root;

    /**
     * the set of layout masters (provided by the root object)
     */
    private LayoutMasterSet layoutMasterSet;

    // There doesn't seem to be anything in the spec requiring flows
    // to be in the order given, only that they map to the regions
    // defined in the page sequence, so all we need is this one hashtable
    // the set of flows includes StaticContent flows also

    /**
     * Map of flows to their flow name (flow-name, Flow)
     */
    private Hashtable _flowMap;

    /**
     * the "master-reference" attribute,
     * which specifies the name of the page-sequence-master or
     * page-master to be used to create pages in the sequence
     */
    private String masterName;

    // according to communication from Paul Grosso (XSL-List,
    // 001228, Number 406), confusion in spec section 6.4.5 about
    // multiplicity of fo:flow in XSL 1.0 is cleared up - one (1)
    // fo:flow per fo:page-sequence only.
    private boolean isFlowSet = false;

    //
    // state attributes used during layout
    //

    // page number and related formatting variables
    private int firstPageNumber = 0;
    private PageNumberGenerator pageNumberGenerator;


    private int forcePageCount;
    private int pageCount = 0;
    private int currentPageNumber;

    /**
     * specifies page numbering type (auto|auto-even|auto-odd|explicit)
     */
    private int pageNumberType;

    /**
     * the current page master
     */
    private SimplePageMaster currentSimplePageMaster;
    private PageSequenceMaster pageSequenceMaster;

    protected PageSequence(FObj parent,
                           PropertyList propertyList) throws FOPException {
        super(parent, propertyList);

        if (parent.getName().equals("fo:root")) {
            this.root = (Root)parent;
        }
        else {
            throw new FOPException("page-sequence must be child of root, not "
                                   + parent.getName());
        }

        layoutMasterSet = root.getLayoutMasterSet();

        // best time to run some checks on LayoutMasterSet
        layoutMasterSet.checkRegionNames();

        _flowMap = new Hashtable();

        String ipnValue = this.properties.get("initial-page-number").getString();

        if (ipnValue.equals("auto")) {
            pageNumberType = AUTO;
            this.firstPageNumber = 1;
        } else if (ipnValue.equals("auto-even")) {
            pageNumberType = AUTO_EVEN;
            this.firstPageNumber = 2;
        } else if (ipnValue.equals("auto-odd")) {
            pageNumberType = AUTO_ODD;
            this.firstPageNumber = 1;
        } else {
            pageNumberType = EXPLICIT;
            try {
                int pageStart = new Integer(ipnValue).intValue();
                this.firstPageNumber = (pageStart > 0) ? pageStart  : 1;
            } catch (NumberFormatException nfe) {
                throw new FOPException("The value '" + ipnValue
                                       + "' is not valid for initial-page-number");
            }
        }

        masterName = this.properties.get("master-reference").getString();

        // get the 'format' properties
        this.pageNumberGenerator =
            new PageNumberGenerator(this.properties.get("format").getString(),
                                    this.properties.get("grouping-separator").getCharacter(),
                                    this.properties.get("grouping-size").getNumber().intValue(),
                                    this.properties.get("letter-value").getEnum());

        this.forcePageCount =
            this.properties.get("force-page-count").getEnum();

        // this.properties.get("country");
        // this.properties.get("language");
        // this.properties.get("id");
    }

    public String getName() {
        return "fo:page-sequence";
    }

    public void addFlow(Flow flow) throws FOPException {
        if (_flowMap.containsKey(flow.getFlowName())) {
            throw new FOPException("flow-names must be unique within an fo:page-sequence");
        }
        if (!this.layoutMasterSet.regionNameExists(flow.getFlowName())) {
            log.error("region-name '"
                      + flow.getFlowName()
                      + "' doesn't exist in the layout-master-set.");
        }
        _flowMap.put(flow.getFlowName(), flow);
        setIsFlowSet(true);
    }


    /**
     * Runs the formatting of this page sequence into the given area tree
     */
    public void format(AreaTree areaTree) throws FOPException {
        PageSequence previousPageSequence=this.root.getPageSequence();
        if( previousPageSequence!=null ) {
            if (previousPageSequence.forcePageCount == ForcePageCount.AUTO) {
                if (pageNumberType == AUTO_ODD) {
                    if (previousPageSequence.currentPageNumber % 2 == 0) {
                        previousPageSequence.makePage(areaTree,true,null);
                    }
                    currentPageNumber = previousPageSequence.currentPageNumber;
                } else if (pageNumberType == AUTO_EVEN) {
                    if (previousPageSequence.currentPageNumber % 2 == 1) {
                        previousPageSequence.makePage(areaTree,true,null);
                    }
                    currentPageNumber = previousPageSequence.currentPageNumber;
                } else if (pageNumberType == EXPLICIT){
                    if ((previousPageSequence.currentPageNumber % 2)
                        != (firstPageNumber % 2)) {
                        previousPageSequence.makePage(areaTree,true,null);
                    }
                    currentPageNumber = firstPageNumber;
                }
            } else {
                currentPageNumber = previousPageSequence.currentPageNumber;
                if (pageNumberType == AUTO_ODD) {
                    if (currentPageNumber % 2 == 0) {
                      currentPageNumber++;
                    }
                } else if (pageNumberType == AUTO_EVEN) {
                    if (currentPageNumber % 2 == 1) {
                      currentPageNumber++;
                    }
                } else if (pageNumberType == EXPLICIT){
                    currentPageNumber = firstPageNumber;
                }
            }
        } else {
            currentPageNumber = firstPageNumber;
        }
        previousPageSequence = null;
        this.root.setPageSequence(this);
        this.currentSimplePageMaster =
          this.layoutMasterSet.getSimplePageMaster(masterName);
        if (this.currentSimplePageMaster==null) {
            this.pageSequenceMaster =
              this.layoutMasterSet.getPageSequenceMaster(masterName);
            if (this.pageSequenceMaster==null) {
                throw new FOPException("master-reference '" + masterName
                                       + "' for fo:page-sequence matches no simple-page-master or page-sequence-master");
            }
            pageSequenceMaster.reset();
        }

        // make pages and layout content
        Status status = new Status(Status.OK);
        Page lastPage = null;
        do {
            boolean isBlankPage = false;

            // for this calculation we are already on the
            // blank page
            if (status.getCode() == Status.FORCE_PAGE_BREAK_EVEN) {
                if ((currentPageNumber % 2) == 1) {
                   isBlankPage = true;
                } 
            } else if (status.getCode() == Status.FORCE_PAGE_BREAK_ODD) {
                if ((currentPageNumber % 2) == 0) {
                   isBlankPage = true;
                } 
            }
            lastPage = makePage(areaTree, isBlankPage, lastPage);
            // Hackery, should use special variable for flow
            Region region = currentSimplePageMaster
              .getRegion(RegionBody.REGION_CLASS);
            Flow flow = (Flow)_flowMap.get(region.getRegionName());
            status = flow.getStatus();
        } while (flowsAreIncomplete());

        // handle cases of 'force-page-count' which do not depend
        // on the presence of a following page sequence
        if (this.forcePageCount == ForcePageCount.EVEN) {
            if (this.pageCount % 2 != 0) {
                makePage(areaTree,true, null);
            }
        } else if (this.forcePageCount == ForcePageCount.ODD) {
            if (this.pageCount % 2 != 1) {
                makePage(areaTree,true, null);
            }
        } else if (this.forcePageCount == ForcePageCount.END_ON_EVEN) {
            if (this.currentPageNumber % 2 == 0) {
                makePage(areaTree,true, null);
            }
        } else if (this.forcePageCount == ForcePageCount.END_ON_ODD) {
            if (this.currentPageNumber % 2 == 1) {
                makePage(areaTree,true, null);
            }
        }
    }

    /**
     * Creates a new page area for the given parameters
     * @param areaTree the area tree the page should be contained in
     * @param isBlankPage true if this page will be empty (e.g. forced even or odd break, or forced page count)
     * @return a Page layout object based on the page master selected from the params
     */
    private Page makePage(AreaTree areaTree,
                          boolean isBlankPage,
                          Page lastPage)
      throws FOPException {
        if (this.pageSequenceMaster!=null) {
            this.currentSimplePageMaster=this.pageSequenceMaster
              .getNextSimplePageMaster(((this.currentPageNumber % 2)==1),
                                       isBlankPage);
        }
        Page newPage = this.currentSimplePageMaster.getPageMaster()
          .makePage(areaTree);
        if (lastPage != null) {
            Vector foots = lastPage.getPendingFootnotes();
            newPage.setPendingFootnotes(foots);
        }
        newPage.setNumber(this.currentPageNumber);
        String formattedPageNumber =
          pageNumberGenerator.makeFormattedPageNumber(this.currentPageNumber);
        newPage.setFormattedNumber(formattedPageNumber);
        newPage.setPageSequence(this);
        log.info("[" + currentPageNumber + (isBlankPage?"(forced)]":"]"));
        if (!isBlankPage) {
            BodyAreaContainer bodyArea = newPage.getBody();
            bodyArea.setIDReferences(areaTree.getIDReferences());

            Region region = currentSimplePageMaster
              .getRegion(RegionBody.REGION_CLASS);
            Flow flow = (Flow)_flowMap.get(region.getRegionName());
            if (flow != null) {
                flow.layout(bodyArea);
            } else {
                throw new FOPException("No flow found for region-body in page-master '"
                                       + currentSimplePageMaster.getMasterName() + "'");
            }
        }
        // because of markers, do after fo:flow (likely also
        // justifiable because of spec)
        formatStaticContent(areaTree, newPage);
        areaTree.addPage(newPage);
        this.currentPageNumber++;
        this.pageCount++;
        return newPage;
    }

    /**
     * Formats the static content of the current page
     */
    private void formatStaticContent(AreaTree areaTree, Page page)
      throws FOPException {
        SimplePageMaster simpleMaster = currentSimplePageMaster;

        if (simpleMaster.getRegion(RegionBefore.REGION_CLASS) != null
                && (page.getBefore() != null)) {
            Flow staticFlow =
                (Flow)_flowMap.get(simpleMaster.getRegion(RegionBefore.REGION_CLASS).getRegionName());
            if (staticFlow != null) {
                AreaContainer beforeArea = page.getBefore();
                beforeArea.setIDReferences(areaTree.getIDReferences());
                layoutStaticContent(staticFlow,
                                    simpleMaster.getRegion(RegionBefore.REGION_CLASS),
                                    beforeArea);
            }
        }

        if (simpleMaster.getRegion(RegionAfter.REGION_CLASS) != null
                && (page.getAfter() != null)) {
            Flow staticFlow =
                (Flow)_flowMap.get(simpleMaster.getRegion(RegionAfter.REGION_CLASS).getRegionName());
            if (staticFlow != null) {
                AreaContainer afterArea = page.getAfter();
                afterArea.setIDReferences(areaTree.getIDReferences());
                layoutStaticContent(staticFlow,
                                    simpleMaster.getRegion(RegionAfter.REGION_CLASS),
                                    afterArea);
            }
        }

        if (simpleMaster.getRegion(RegionStart.REGION_CLASS) != null
                && (page.getStart() != null)) {
            Flow staticFlow =
                (Flow)_flowMap.get(simpleMaster.getRegion(RegionStart.REGION_CLASS).getRegionName());
            if (staticFlow != null) {
                AreaContainer startArea = page.getStart();
                startArea.setIDReferences(areaTree.getIDReferences());
                layoutStaticContent(staticFlow,
                                    simpleMaster.getRegion(RegionStart.REGION_CLASS),
                                    startArea);
            }
        }

        if (simpleMaster.getRegion(RegionEnd.REGION_CLASS) != null
                && (page.getEnd() != null)) {
            Flow staticFlow =
                (Flow)_flowMap.get(simpleMaster.getRegion(RegionEnd.REGION_CLASS).getRegionName());
            if (staticFlow != null) {
                AreaContainer endArea = page.getEnd();
                endArea.setIDReferences(areaTree.getIDReferences());
                layoutStaticContent(staticFlow,
                                    simpleMaster.getRegion(RegionEnd.REGION_CLASS),
                                    endArea);
            }
        }

    }

    private void layoutStaticContent(Flow flow, Region region,
                                     AreaContainer area) throws FOPException {
        if (flow instanceof StaticContent) {
            ((StaticContent)flow).layout(area, region);
        } else {
            log.error("The region '" + region.getRegionName()
                      + "' only supports static-content. Cannot use flow named '"
                      + flow.getFlowName() + "'");
        }
    }

    /**
     * Returns true when there are more flow elements left to lay out.
     */
    private boolean flowsAreIncomplete() {
        for (Enumeration e = _flowMap.elements(); e.hasMoreElements(); ) {
            Flow flow = (Flow)e.nextElement();
            if (flow instanceof StaticContent) {
                continue;
            }
            if (flow.getStatus().isIncomplete()) {
                return true;
            }
        }
        return false;
    }

    public boolean isFlowSet() {
        return isFlowSet;
    }

    public void setIsFlowSet(boolean isFlowSet) {
        this.isFlowSet = isFlowSet;
    }

    public int getCurrentPageNumber() {
        return currentPageNumber; 
    }

    public int getPageCount() {
    	return pageCount;
    }

}
