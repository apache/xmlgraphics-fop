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
import org.apache.fop.layout.PageMaster;
import org.apache.fop.area.AreaTree;
import org.apache.fop.area.PageViewport;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.StreamRenderer;

import org.apache.fop.layoutmgr.PageLayoutManager;

// Java
import java.util.HashMap;

import org.xml.sax.Attributes;

/**
 * This provides pagination of flows onto pages. Much of the
 * logic for paginating flows is contained in this class.
 * The main entry point is the format method.
 */
public class PageSequence extends FObj {
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
     * Use to layout and render the page sequence.
     */
    private StreamRenderer streamRenderer;

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
    // defined in the page sequence, so all we need is this one hashmap
    // the set of flows includes StaticContent flows also

    /**
     * Map of flows to their flow name (flow-name, Flow)
     */
    private HashMap _flowMap;

    /**
     * the "master-reference" attribute
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

    private PageViewport currentPage;

    // page number and related formatting variables
    private String ipnValue;
    private int currentPageNumber = 0;
    private int explicitFirstNumber = 0; // explicitly specified
    private int firstPageNumber = 0; // actual
    private PageNumberGenerator pageNumberGenerator;

    private int forcePageCount = 0;
    private int pageCount = 0;
    private boolean isForcing = false;

    /**
     * specifies page numbering type (auto|auto-even|auto-odd|explicit)
     */
    private int pageNumberType;

    /**
     * used to determine whether to calculate auto, auto-even, auto-odd
     */
    private boolean thisIsFirstPage;

    /**
     * the current subsequence while formatting a given page sequence
     */
    private SubSequenceSpecifier currentSubsequence;

    /**
     * the current index in the subsequence list
     */
    private int currentSubsequenceNumber =
        -1;    // starting case is -1 so that first getNext increments to 0

    /**
     * the name of the current page master
     */
    private String currentPageMasterName;

    /**
     * The main content flow for this page-sequence.
     */
    private Flow mainFlow=null;

    /**
     * The fo:title object for this page-sequence.
     */
    private FObj titleFO;


    public PageSequence(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);

        if (parent.getName().equals("fo:root")) {
            this.root = (Root)parent;
            // this.root.addPageSequence(this);
        }
        else {
            throw new FOPException("page-sequence must be child of root, not "
                                   + parent.getName());
        }

        layoutMasterSet = root.getLayoutMasterSet();

        // best time to run some checks on LayoutMasterSet
        layoutMasterSet.checkRegionNames();

        _flowMap = new HashMap();

        thisIsFirstPage =
            true;    // we are now on the first page of the page sequence
        ipnValue = this.properties.get("initial-page-number").getString();

        if (ipnValue.equals("auto")) {
            pageNumberType = AUTO;
        } else if (ipnValue.equals("auto-even")) {
            pageNumberType = AUTO_EVEN;
        } else if (ipnValue.equals("auto-odd")) {
            pageNumberType = AUTO_ODD;
        } else {
            pageNumberType = EXPLICIT;
            try {
                int pageStart = new Integer(ipnValue).intValue();
                this.explicitFirstNumber = (pageStart > 0) ? pageStart - 1 : 0;
            } catch (NumberFormatException nfe) {
                throw new FOPException("\"" + ipnValue
                                       + "\" is not a valid value for initial-page-number");
            }
        }


        masterName = this.properties.get("master-reference").getString();
	// TODO: Add code here to set a reference to the PageSequenceMaster
	// if the masterName names a page-sequence-master, else get a
	// reference to the SimplePageMaster. Throw an exception if neither?

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


    /**
     * Add a flow or static content, mapped by its flow-name.
     * The flow-name is used to associate the flow with a region on a page,
     * based on the names given to the regions in the page-master used to
     * generate that page.
     */
    private void addFlow(Flow flow) throws FOPException {
        if (_flowMap.containsKey(flow.getFlowName())) {
            throw new FOPException("flow-names must be unique within an fo:page-sequence");
        }
        if (!this.layoutMasterSet.regionNameExists(flow.getFlowName())) {
            log.error("region-name '"
                                   + flow.getFlowName()
                                   + "' doesn't exist in the layout-master-set.");
        }
        _flowMap.put(flow.getFlowName(), flow);
        //setIsFlowSet(true);
    }


    /**
     * Validate the child being added and initialize internal variables.
     * XSL content model for page-sequence:
     * <pre>(title?,static-content*,flow)</pre>
     * Note: title isn't currently implemented.
     * @param child The flow object child to be added to the PageSequence.
     */
    public void addChild(FONode child) {
	try {
	    String childName = child.getName();
	    if (childName.equals("fo:title")) {
		if (this._flowMap.size()>0) {
		    log.warn("fo:title should be first in page-sequence");
		}
		this.titleFO = (FObj)child;
	    }
	    else if (childName.equals("fo:flow")) {
		if (this.mainFlow != null) {
		    throw new FOPException("Only a single fo:flow permitted"
			      + " per fo:page-sequence");
		}
		else {
		    this.mainFlow = (Flow)child;
		    addFlow(mainFlow);
		    super.addChild(child); // For getChildren
		}
	    } 
	    else if (childName.equals("fo:static-content")) {
		if (this.mainFlow != null) {
		    throw new FOPException(childName +
					   " must precede fo:flow; ignoring");
		}
		else {
		    addFlow((Flow)child);
		}
	    }
	    else {
		// Ignore it!
		log.warn("FO '" + childName +
			 "' not a legal page-sequence child.");
		return;
	    }
	} catch (FOPException fopex) {
	    log.error("Error in PageSequence.addChild(): " +
		      fopex.getMessage());
	}
    }


    public void setStreamRenderer(StreamRenderer st) {
	this.streamRenderer = st;
    }

    public void end() {
	try {
	    this.streamRenderer.render(this);
	} catch (FOPException fopex) {
	    log.error("Error in PageSequence.end(): " +
		      fopex.getMessage());
	}
    }


//     /**
//      * Return children for layout. Only the main flow is laid out directly.
//      */
//     public ListIterator getChildren() {
// 	return new ListIterator() {
// 		boolean bFirst=true;
// 		public boolean hasNext() {
// 		    return (bFirst==true && mainFlow != null);
// 		}
// 		public Object next() {
// 		    if (bFirst==true && mainFlow != null) {
// 			bFirst=false;
// 			return mainFlow;
// 		    }
// 		    else throw new NoSuchElementException();
// 		}
// 		public void remove() {
// 		    throw new UnsupportedOperationException();
// 		}
// 	    };
//     }

    /**
     * Runs the formatting of this page sequence into the given area tree
     */
    public void format(AreaTree areaTree) throws FOPException {
	// Make a new PageLayoutManager and a FlowLayoutManager
	// Run the PLM in a thread
	// Wait for them to finish.
	
	// If no main flow, nothing to layout!
	if (this.mainFlow == null) return;

	areaTree.startPageSequence(null);

	// Initialize if already used?
        this.layoutMasterSet.resetPageMasters();

        int firstAvailPageNumber = 0;

	// This will layout pages and add them to the area tree
 	PageLayoutManager pageLM = new PageLayoutManager(areaTree, this);
	// For now, skip the threading and just call run directly.
	pageLM.run();

// 	Thread layoutThread = new Thread(pageLM);
//  	layoutThread.start();
// 	log.debug("Layout thread started");

// 	// wait on both managers
// 	try {
// 	    layoutThread.join();
// 	    log.debug("Layout thread done");
// 	} catch (InterruptedException ie) {
// 	    log.error("PageSequence.format() interrupted waiting on layout");
// 	}
	// Tell the root the last page number we created.
	this.root.setRunningPageNumberCounter(this.currentPageNumber);
    }

    private void initPageNumber() {
	this.currentPageNumber = this.root.getRunningPageNumberCounter() + 1;

	if (this.pageNumberType == AUTO_ODD) {
	    // Next page but force odd. May force empty page creation!
	    // Whose master is used for this??? Assume no. 
	    // Use force-page-count=auto
	    // on preceding page-sequence to make sure that there is no gap!
	    if (currentPageNumber % 2 == 0) {
		this.currentPageNumber++;
	    }
	} else if (pageNumberType == AUTO_EVEN) {
	    if (currentPageNumber % 2 == 1) {
		this.currentPageNumber++;
	    }
	}
	else if (pageNumberType == EXPLICIT) {
	    this.currentPageNumber = this.explicitFirstNumber;
	}
	this.firstPageNumber = this.currentPageNumber;
    }

    /**
     * Called by PageLayoutManager when it needs a new page on which to
     * place content. The PageSequence manages the page number (odd/even),
     * but the PLM tells it if the page is blank or is the last page.
     * @param bIsBlank If true, use a master for a blank page.
     * @param bIsLast If true, use the master for the last page in the sequence.
     */
    public PageViewport createPage(boolean bIsBlank, boolean bIsLast) 
	throws FOPException
    {

	    // Set even/odd flag and first flag based on current state
	// Should do it this way, but fix it later....
	/*boolean bEvenPage = ((this.currentPageNumber %2)==0);
	  currentPage = makePage(bEvenPage, */
	currentPage = makePage(this.currentPageNumber,
			       this.currentPageNumber==this.firstPageNumber,
			       bIsLast, bIsBlank);
	return currentPage;
	    // The page will have a viewport/reference area pair defined
	    // for each region in the master.
	    // Set up the page itself
	//            currentPage.setNumber(this.currentPageNumber);
// SKIP ALL THIS FOR NOW!!!
//             String formattedPageNumber =
//                 pageNumberGenerator.makeFormattedPageNumber(this.currentPageNumber);
//             currentPage.setFormattedNumber(formattedPageNumber);
// 	    this.currentPageNumber++;
//             //this.root.setRunningPageNumberCounter(this.currentPageNumber);

//                 BodyAreaContainer bodyArea = currentPage.getBody();
//                 bodyArea.setIDReferences(areaTree.getIDReferences());

//             // because of markers, do after fo:flow (likely also
//             // justifiable because of spec)
//             currentPage.setPageSequence(this);
//             formatStaticContent(areaTree);

//             //log.info("]");
//             areaTree.addPage(currentPage);
//             this.pageCount++;    // used for 'force-page-count' calculations

        // handle the 'force-page-count'
	    //forcePage(areaTree, firstAvailPageNumber);
    }

    /**
     * Creates a new page area for the given parameters
     * @param areaTree the area tree the page should be contained in
     * @param firstAvailPageNumber the page number for this page
     * @param isFirstPage true when this is the first page in the sequence
     * @param isEmptyPage true if this page will be empty
     * (e.g. forced even or odd break)
     * @return a Page layout object based on the page master selected 
     * from the params
     * TODO: modify the other methods to use even/odd flag and bIsLast
     */
    private PageViewport makePage(int firstAvailPageNumber,
		      boolean isFirstPage, boolean bIsLast,
		      boolean isEmptyPage) throws FOPException {
        // layout this page sequence

        // while there is still stuff in the flow, ask the
        // layoutMasterSet for a new page

        // page number is 0-indexed
        PageMaster pageMaster = getNextPageMaster(masterName,
                                firstAvailPageNumber,
                                isFirstPage, isEmptyPage);

        // a legal alternative is to use the last sub-sequence
        // specification which should be handled in getNextSubsequence.
	// That's not done here.
        if (pageMaster == null) {
            throw new FOPException("page masters exhausted. Cannot recover.");
        }
        PageViewport p = pageMaster.makePage();
//         if (currentPage != null) {
//             Vector foots = currentPage.getPendingFootnotes();
//             p.setPendingFootnotes(foots);
//         }
        return p;
    }

    /**
     * Formats the static content of the current page
     */
//     private void formatStaticContent(AreaTree areaTree) throws FOPException {
//         SimplePageMaster simpleMaster = getCurrentSimplePageMaster();

//         if (simpleMaster.getRegion(Region.BODY) != null
//                 && (currentPage.getBefore() != null)) {
//             Flow staticFlow =
//                 (Flow)_flowMap.get(simpleMaster.getRegion(RegionBefore.REGION_CLASS).getRegionName());
//             if (staticFlow != null) {
//                 AreaContainer beforeArea = currentPage.getBefore();
//                 beforeArea.setIDReferences(areaTree.getIDReferences());
//                 layoutStaticContent(staticFlow,
//                                     simpleMaster.getRegion(RegionBefore.REGION_CLASS),
//                                     beforeArea);
//             }
//         }

//         if (simpleMaster.getRegion(RegionAfter.REGION_CLASS) != null
//                 && (currentPage.getAfter() != null)) {
//             Flow staticFlow =
//                 (Flow)_flowMap.get(simpleMaster.getRegion(RegionAfter.REGION_CLASS).getRegionName());
//             if (staticFlow != null) {
//                 AreaContainer afterArea = currentPage.getAfter();
//                 afterArea.setIDReferences(areaTree.getIDReferences());
//                 layoutStaticContent(staticFlow,
//                                     simpleMaster.getRegion(RegionAfter.REGION_CLASS),
//                                     afterArea);
//             }
//         }

//         if (simpleMaster.getRegion(RegionStart.REGION_CLASS) != null
//                 && (currentPage.getStart() != null)) {
//             Flow staticFlow =
//                 (Flow)_flowMap.get(simpleMaster.getRegion(RegionStart.REGION_CLASS).getRegionName());
//             if (staticFlow != null) {
//                 AreaContainer startArea = currentPage.getStart();
//                 startArea.setIDReferences(areaTree.getIDReferences());
//                 layoutStaticContent(staticFlow,
//                                     simpleMaster.getRegion(RegionStart.REGION_CLASS),
//                                     startArea);
//             }
//         }

//         if (simpleMaster.getRegion(RegionEnd.REGION_CLASS) != null
//                 && (currentPage.getEnd() != null)) {
//             Flow staticFlow =
//                 (Flow)_flowMap.get(simpleMaster.getRegion(RegionEnd.REGION_CLASS).getRegionName());
//             if (staticFlow != null) {
//                 AreaContainer endArea = currentPage.getEnd();
//                 endArea.setIDReferences(areaTree.getIDReferences());
//                 layoutStaticContent(staticFlow,
//                                     simpleMaster.getRegion(RegionEnd.REGION_CLASS),
//                                     endArea);
//             }
//         }

//     }
//
//     private void layoutStaticContent(Flow flow, Region region,
//                                      AreaContainer area) throws FOPException {
//         if (flow instanceof StaticContent) {
//             AreaContainer beforeArea = currentPage.getBefore();
//             ((StaticContent)flow).layout(area, region);
//         } else {
//             log.error("" + region.getName()
//                                    + " only supports static-content flows currently. Cannot use flow named '"
//                                    + flow.getFlowName() + "'");
//         }
//     }

    /**
     * Returns the next SubSequenceSpecifier for the given page sequence master.
     * The result is bassed on the current state of this page sequence.
     */
    private SubSequenceSpecifier getNextSubsequence(PageSequenceMaster master) {
        if (master.getSubSequenceSpecifierCount()
                > currentSubsequenceNumber + 1) {

            currentSubsequence =
                master.getSubSequenceSpecifier(currentSubsequenceNumber + 1);
            currentSubsequenceNumber++;
            return currentSubsequence;
        } else {
            return null;
        }
    }

    /**
     * Returns the next simple page master for the given sequence master, page number and
     * other state information
     */
    private SimplePageMaster getNextSimplePageMaster(PageSequenceMaster sequenceMaster,
            int currentPageNumber, boolean thisIsFirstPage,
            boolean isEmptyPage) {
        // handle forcing
        if (isForcing) {
            String nextPageMaster = getNextPageMasterName(sequenceMaster,
                                    currentPageNumber, false, true);
            return this.layoutMasterSet.getSimplePageMaster(nextPageMaster);
        }
        String nextPageMaster = getNextPageMasterName(sequenceMaster,
                                currentPageNumber, thisIsFirstPage, isEmptyPage);
        return this.layoutMasterSet.getSimplePageMaster(nextPageMaster);

    }

    /**
     * Get the next page master name.
     * This gets the name of the next page master. If the sequence
     * is exhausted then an error is indicated and the last page
     * master name is used.
     */
    private String getNextPageMasterName(PageSequenceMaster sequenceMaster,
                                         int currentPageNumber,
                                         boolean thisIsFirstPage,
                                         boolean isEmptyPage) {

        if (null == currentSubsequence) {
            currentSubsequence = getNextSubsequence(sequenceMaster);
        }

        String nextPageMaster =
            currentSubsequence.getNextPageMaster(currentPageNumber,
                                                 thisIsFirstPage,
                                                 isEmptyPage);


        if (null == nextPageMaster
                || isFlowForMasterNameDone(currentPageMasterName)) {
            SubSequenceSpecifier nextSubsequence =
                getNextSubsequence(sequenceMaster);
            if (nextSubsequence == null) {
                log.error("Page subsequences exhausted. Using previous subsequence.");
                thisIsFirstPage =
                    true;    // this becomes the first page in the new (old really) page master
                currentSubsequence.reset();

                // we leave currentSubsequence alone
            }
            else {
                currentSubsequence = nextSubsequence;
            }

            nextPageMaster =
                currentSubsequence.getNextPageMaster(currentPageNumber,
                                                     thisIsFirstPage,
                                                     isEmptyPage);
        }
        currentPageMasterName = nextPageMaster;

        return nextPageMaster;

    }

    private SimplePageMaster getCurrentSimplePageMaster() {
        return this.layoutMasterSet.getSimplePageMaster(currentPageMasterName);
    }

    private String getCurrentPageMasterName() {
        return currentPageMasterName;
    }

    // refactored from LayoutMasterSet
    private PageMaster getNextPageMaster(String pageSequenceName,
                                         int currentPageNumber,
                                         boolean thisIsFirstPage,
                                         boolean isEmptyPage) throws FOPException {
        PageMaster pageMaster = null;

        // see if there is a page master sequence for this master name
        PageSequenceMaster sequenceMaster =
            this.layoutMasterSet.getPageSequenceMaster(pageSequenceName);

        if (sequenceMaster != null) {
            pageMaster = getNextSimplePageMaster(sequenceMaster,
                                                 currentPageNumber,
                                                 thisIsFirstPage,
                                                 isEmptyPage).getPageMaster();

        } else {    // otherwise see if there's a simple master by the given name
            SimplePageMaster simpleMaster =
                this.layoutMasterSet.getSimplePageMaster(pageSequenceName);
            if (simpleMaster == null) {
                throw new FOPException("'master-reference' for 'fo:page-sequence'"
                                       + "matches no 'simple-page-master' or 'page-sequence-master'");
            }
            currentPageMasterName = pageSequenceName;

            pageMaster = simpleMaster.getNextPageMaster();
        }
        return pageMaster;
    }


//     /**
//      * Returns true when there is more flow elements left to lay out.
//      */
//     private boolean flowsAreIncomplete() {
//         boolean isIncomplete = false;

//         for (Iterator e = _flowMap.values().iterator(); e.hasNext(); ) {
//             Flow flow = (Flow)e.next();
//             if (flow instanceof StaticContent) {
//                 continue;
//             }

//             Status status = flow.getStatus();
//             isIncomplete |= status.isIncomplete();
//         }
//         return isIncomplete;
//     }

//     /**
//      * Returns the flow that maps to the given region class for the current
//      * page master.
//      */
//     private Flow getCurrentFlow(String regionClass) {
//         Region region = getCurrentSimplePageMaster().getRegion(regionClass);
//         if (region != null) {
//             Flow flow = (Flow)_flowMap.get(region.getRegionName());
//             return flow;

//         } else {

//             System.out.println("flow is null. regionClass = '" + regionClass
//                                + "' currentSPM = "
//                                + getCurrentSimplePageMaster());

//             return null;
//         }

//     }

    private boolean isFlowForMasterNameDone(String masterName) {
        // parameter is master-name of PMR; we need to locate PM
        // referenced by this, and determine whether flow(s) are OK
        if (isForcing)
            return false;
        if (masterName != null) {

            SimplePageMaster spm =
                this.layoutMasterSet.getSimplePageMaster(masterName);
            Region region = spm.getRegion(Region.BODY);


            Flow flow = (Flow)_flowMap.get(region.getRegionName());
            if ((null == flow) || flow.getStatus().isIncomplete())
                return false;
            else
                return true;
        }
        return false;
    }

    public boolean isFlowSet() {
        return isFlowSet;
    }

    public void setIsFlowSet(boolean isFlowSet) {
        this.isFlowSet = isFlowSet;
    }

    public String getIpnValue() {
        return ipnValue;
    }

    public int getCurrentPageNumber() {
        return currentPageNumber;
    }

//     private void forcePage(AreaTree areaTree, int firstAvailPageNumber) {
//         boolean makePage = false;
//         if (this.forcePageCount == ForcePageCount.AUTO) {
//             PageSequence nextSequence =
//                 this.root.getSucceedingPageSequence(this);
//             if (nextSequence != null) {
//                 if (nextSequence.getIpnValue().equals("auto")) {
//                     // do nothing special
//                 }
//                 else if (nextSequence.getIpnValue().equals("auto-odd")) {
//                     if (firstAvailPageNumber % 2 == 0) {
//                         makePage = true;
//                     }
//                 } else if (nextSequence.getIpnValue().equals("auto-even")) {
//                     if (firstAvailPageNumber % 2 != 0) {
//                         makePage = true;
//                     }
//                 } else {
//                     int nextSequenceStartPageNumber =
//                         nextSequence.getCurrentPageNumber();
//                     if ((nextSequenceStartPageNumber % 2 == 0)
//                             && (firstAvailPageNumber % 2 == 0)) {
//                         makePage = true;
//                     } else if ((nextSequenceStartPageNumber % 2 != 0)
//                                && (firstAvailPageNumber % 2 != 0)) {
//                         makePage = true;
//                     }
//                 }
//             }
//         } else if ((this.forcePageCount == ForcePageCount.EVEN)
//                    && (this.pageCount % 2 != 0)) {
//             makePage = true;
//         } else if ((this.forcePageCount == ForcePageCount.ODD)
//                    && (this.pageCount % 2 == 0)) {
//             makePage = true;
//         } else if ((this.forcePageCount == ForcePageCount.END_ON_EVEN)
//                    && (firstAvailPageNumber % 2 == 0)) {
//             makePage = true;
//         } else if ((this.forcePageCount == ForcePageCount.END_ON_ODD)
//                    && (firstAvailPageNumber % 2 != 0)) {
//             makePage = true;
//         } else if (this.forcePageCount == ForcePageCount.NO_FORCE) {
//             // do nothing
//         }

//         if (makePage) {
//             try {
//                 this.isForcing = true;
//                 this.currentPageNumber++;
//                 firstAvailPageNumber = this.currentPageNumber;
//                 currentPage = makePage(areaTree, firstAvailPageNumber, false,
//                                        true);
//                 String formattedPageNumber =
//                     pageNumberGenerator.makeFormattedPageNumber(this.currentPageNumber);
//                 currentPage.setFormattedNumber(formattedPageNumber);
//                 currentPage.setPageSequence(this);
//                 formatStaticContent(areaTree);
//                 log.debug("[forced-" + firstAvailPageNumber + "]");
//                 areaTree.addPage(currentPage);
//                 this.root.setRunningPageNumberCounter(this.currentPageNumber);
//                 this.isForcing = false;
//             } catch (FOPException fopex) {
//                 log.debug("'force-page-count' failure");
//             }
//         }
//     }

}
