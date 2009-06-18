/*-- $Id$ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 
 */

package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.messaging.MessageHandler;
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
public class PageSequence extends FObj
{
    //
    // Factory methods
    //
    public static class Maker extends FObj.Maker
    {
        public FObj make(FObj parent, PropertyList propertyList)
        throws FOPException {
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
    /** The parent root object */
    private Root root;

    /** the set of layout masters (provided by the root object) */
    private LayoutMasterSet layoutMasterSet;

    // There doesn't seem to be anything in the spec requiring flows
    // to be in the order given, only that they map to the regions
    // defined in the page sequence, so all we need is this one hashtable
    // the set of flows includes StaticContent flows also

    /** Map of flows to their flow name (flow-name, Flow) */
    private Hashtable _flowMap;
        
    /** the "master-name" attribute */
    private String masterName;
	
	// according to communication from Paul Grosso (XSL-List,
	// 001228, Number 406), confusion in spec section 6.4.5 about
	// multiplicity of fo:flow in XSL 1.0 is cleared up - one (1)
	// fo:flow per fo:page-sequence only.
	private boolean isFlowSet = false;
	
    //
    // state attributes used during layout
    //

    private Page currentPage;

    private int currentPageNumber = 0;

    /** keeps count of page number from previous PageSequence */
    private static int runningPageNumberCounter = 0; 

    /** specifies page numbering type (auto|auto-even|auto-odd|explicit) */
    private int pageNumberType;  

    /** used to determine whether to calculate auto, auto-even, auto-odd */
    private boolean thisIsFirstPage; 

    /** the current subsequence while formatting a given page sequence */
    private SubSequenceSpecifier currentSubsequence;

    /** the current index in the subsequence list */
    private int currentSubsequenceNumber = -1; // starting case is -1 so that first getNext increments to 0
    
    /** the name of the current page master */
    private String currentPageMasterName;


    protected PageSequence(FObj parent, PropertyList propertyList)
    throws FOPException {
        super(parent, propertyList);
        this.name = "fo:page-sequence";

        if ( parent.getName().equals("fo:root") )
        {
            this.runningPageNumberCounter=0; //else not initialized correctly
            this.root = (Root) parent;
            this.root.addPageSequence(this);
        }
        else
        {
            throw
            new FOPException("page-sequence must be child of root, not "
            + parent.getName());
        }
	
        layoutMasterSet = root.getLayoutMasterSet();

	// best time to run some checks on LayoutMasterSet
        layoutMasterSet.checkRegionNames();

	_flowMap = new Hashtable();

        thisIsFirstPage=true; // we are now on the first page of the page sequence
        String ipnValue= this.properties.get("initial-page-number").getString();

        if ( ipnValue.equals("auto") )
        {
            pageNumberType=AUTO;            
        }
        else if ( ipnValue.equals("auto-even") )
        {
            pageNumberType=AUTO_EVEN;            
        }
        else if ( ipnValue.equals("auto-odd") )
        {
            pageNumberType=AUTO_ODD;            
        }
        else
        {
            pageNumberType=EXPLICIT;            
            try
            {
                int pageStart = new Integer(ipnValue).intValue();
                this.currentPageNumber = (pageStart > 0) ? pageStart - 1 : 0;
            }
            catch ( NumberFormatException nfe )
            {
                throw new FOPException("\""+ipnValue+"\" is not a valid value for initial-page-number");
            }
        }

        masterName = this.properties.get("master-name").getString();
	

    }
    
    public void addFlow(Flow flow) 
	throws FOPException
    {
	if (_flowMap.containsKey(flow.getFlowName())) {
	    throw new FOPException("flow-names must be unique within an fo:page-sequence");
	}
	if (!this.layoutMasterSet.regionNameExists(flow.getFlowName())) {
	    MessageHandler.errorln("WARNING: region-name '"+flow.getFlowName()+"' doesn't exist in the layout-master-set.");
	}
	_flowMap.put(flow.getFlowName(), flow);
	setIsFlowSet(true);
    }
    

    /**
     * Runs the formatting of this page sequence into the given area tree
     */
    public void format(AreaTree areaTree) throws FOPException {
        Status status = new Status(Status.OK);

		this.layoutMasterSet.resetPageMasters();

        do
        {
	    // makePage() moved to after the page-number computations,
	    // but store the page-number at this point for that method,
	    // since we want the 'current' current page-number...
	    int firstAvailPageNumber = this.runningPageNumberCounter;
	    boolean tempIsFirstPage = false;
         
            if ( thisIsFirstPage )
            {
				tempIsFirstPage = thisIsFirstPage;
                if ( pageNumberType==AUTO )
                {
                    this.currentPageNumber=this.runningPageNumberCounter;
                }
                else if ( pageNumberType==AUTO_ODD )
                {
                    this.currentPageNumber=this.runningPageNumberCounter;
                    if ( this.currentPageNumber % 2== 1 )
                    {
                        this.currentPageNumber++;
                    }
                }
                else if ( pageNumberType==AUTO_EVEN )
                {
                    this.currentPageNumber=this.runningPageNumberCounter;
                    if ( this.currentPageNumber % 2 == 0 )
                    {
                        this.currentPageNumber++;
                    }
                }
                thisIsFirstPage=false;
            }

	    this.currentPageNumber++;
  
	    // deliberately moved down here so page-number calculations
	    // are complete;
 	    // compute flag for 'blank-or-not-blank'
	    boolean isEmptyPage = false;
	    
	    if ( (status.getCode() == Status.FORCE_PAGE_BREAK_EVEN) &&
		 ((currentPageNumber % 2) == 1) ) {
		isEmptyPage = true;
	    }
	    else if ( (status.getCode() == Status.FORCE_PAGE_BREAK_ODD) &&
		      ((currentPageNumber % 2) == 0) ) {
		isEmptyPage = true;
	    }
	    else {
		isEmptyPage = false;
	    }

	    currentPage = makePage(areaTree, firstAvailPageNumber,
				   tempIsFirstPage, isEmptyPage);
	    	    
	    currentPage.setNumber(this.currentPageNumber);
	    this.runningPageNumberCounter=this.currentPageNumber;            

            MessageHandler.log(" [" + currentPageNumber);

	    formatStaticContent(areaTree);
	        
            if ( (status.getCode() == Status.FORCE_PAGE_BREAK_EVEN) &&
            ((currentPageNumber % 2) == 1) )
            {
            }
            else if ( (status.getCode() == Status.FORCE_PAGE_BREAK_ODD) &&
            ((currentPageNumber % 2) == 0) )
            {
            }
            else
            {
                BodyAreaContainer bodyArea = currentPage.getBody();
                bodyArea.setIDReferences(areaTree.getIDReferences());
		
		Flow flow = getCurrentFlow(RegionBody.REGION_CLASS);
		
		if (null == flow) {
	 	    MessageHandler.errorln("No flow found for region-body "
		 			   + "in page-master '" + currentPageMasterName + "'");
		    break;
		    
		}
		else {
		    status = flow.layout(bodyArea);
		}
		
            }
            MessageHandler.log("]");
            areaTree.addPage(currentPage);
	} while ( flowsAreIncomplete() );
		
        MessageHandler.errorln("");
    }

    /** 
     * Creates a new page area for the given parameters
     * @param areaTree the area tree the page should be contained in
     * @param firstAvailPageNumber the page number for this page
     * @param isFirstPage true when this is the first page in the sequence
     * @param isEmptyPage true if this page will be empty (e.g. forced even or odd break)
     * @return a Page layout object based on the page master selected from the params
     */
    private Page makePage(AreaTree areaTree, int firstAvailPageNumber,
			  boolean isFirstPage, boolean isEmptyPage)
	throws FOPException {
        // layout this page sequence
		
        // while there is still stuff in the flow, ask the
        // layoutMasterSet for a new page 

	// page number is 0-indexed
        PageMaster pageMaster = getNextPageMaster(masterName, firstAvailPageNumber, 
						  isFirstPage, isEmptyPage );

		// a legal alternative is to use the last sub-sequence
		// specification which should be handled in getNextSubsequence. That's not done here.
        if ( pageMaster == null )
        {
		throw new FOPException("page masters exhausted. Cannot recover.");
        }
        Page p = pageMaster.makePage(areaTree);
        if(currentPage != null) {
            Vector foots = currentPage.getPendingFootnotes();
            p.setPendingFootnotes(foots);
        }
        return p;
    }

    /**
     * Formats the static content of the current page
     */
    private void formatStaticContent(AreaTree areaTree) 
	throws FOPException
    {
	SimplePageMaster simpleMaster = getCurrentSimplePageMaster(); 

	if (simpleMaster.getRegion(RegionBefore.REGION_CLASS) != null && (currentPage.getBefore() != null)) {
	    Flow staticFlow = (Flow)_flowMap.get(simpleMaster.getRegion(RegionBefore.REGION_CLASS).getRegionName());
	    if (staticFlow != null) {
		AreaContainer beforeArea = currentPage.getBefore();
		beforeArea.setIDReferences(areaTree.getIDReferences());
		layoutStaticContent(staticFlow, simpleMaster.getRegion(RegionBefore.REGION_CLASS),
				    beforeArea);
	    }
	}

	if (simpleMaster.getRegion(RegionAfter.REGION_CLASS) != null && (currentPage.getAfter() != null)) {
	    Flow staticFlow = (Flow)_flowMap.get(simpleMaster.getRegion(RegionAfter.REGION_CLASS).getRegionName());
	    if (staticFlow != null) {
		AreaContainer afterArea = currentPage.getAfter();
		afterArea.setIDReferences(areaTree.getIDReferences());
		layoutStaticContent(staticFlow, simpleMaster.getRegion(RegionAfter.REGION_CLASS),
				    afterArea);
	    }

	}
	
    }
    
    private void layoutStaticContent(Flow flow, Region region,
				     AreaContainer area) 
	throws FOPException
    {
	if (flow instanceof StaticContent) {
	    AreaContainer beforeArea = currentPage.getBefore();
	    ((StaticContent)flow).layout(area, region);
	}
	else {
	    MessageHandler.errorln("WARNING: "+region.getName()+" only supports static-content flows currently. Cannot use flow named '"+flow.getFlowName()+"'");
	}
    }
    
	
    
    

    /**
     * Returns the next SubSequenceSpecifier for the given page sequence master. The result
     * is bassed on the current state of this page sequence.
     */
    // refactored from PageSequenceMaster
    private SubSequenceSpecifier getNextSubsequence(PageSequenceMaster master)
    {
	if (master.getSubSequenceSpecifierCount() > currentSubsequenceNumber + 1) {
	    
	    currentSubsequence = master.getSubSequenceSpecifier(currentSubsequenceNumber + 1);
	    currentSubsequenceNumber++;
	    return currentSubsequence;
	}
	else {
	    return null;
	}
		
    }
       
    /**
     * Returns the next simple page master for the given sequence master, page number and 
     * other state information
     */
    private SimplePageMaster getNextSimplePageMaster(PageSequenceMaster sequenceMaster, 
						     int currentPageNumber, 
						     boolean thisIsFirstPage, 
						     boolean isEmptyPage) 
    {	
	String nextPageMaster = getNextPageMasterName(sequenceMaster, currentPageNumber, thisIsFirstPage, isEmptyPage);
	return this.layoutMasterSet.getSimplePageMaster( nextPageMaster );
	
    }

    private String getNextPageMasterName(PageSequenceMaster sequenceMaster, 
					 int currentPageNumber, 
					 boolean thisIsFirstPage,
					 boolean isEmptyPage) 
    {

	if (null == currentSubsequence) {
	    currentSubsequence = getNextSubsequence(sequenceMaster);
	}
	
	String nextPageMaster = 
	    currentSubsequence.getNextPageMaster( currentPageNumber, thisIsFirstPage, isEmptyPage );


	if (null == nextPageMaster ||  isFlowForMasterNameDone(currentPageMasterName)) {
	    SubSequenceSpecifier nextSubsequence = getNextSubsequence(sequenceMaster);
	    if (nextSubsequence == null) {
		MessageHandler.errorln("\nWARNING: Page subsequences exhausted. Using previous subsequence.");
		thisIsFirstPage = true; // this becomes the first page in the new (old really) page master
		currentSubsequence.reset();
		
		// we leave currentSubsequence alone
	    }
	    else {
		currentSubsequence = nextSubsequence;
	    }
	    
	    nextPageMaster = currentSubsequence.getNextPageMaster( currentPageNumber,
								   thisIsFirstPage,
								   isEmptyPage);
	}
	currentPageMasterName = nextPageMaster;
	
	return nextPageMaster;
	
    }

    private SimplePageMaster getCurrentSimplePageMaster() 
    {
	return this.layoutMasterSet.getSimplePageMaster( currentPageMasterName );
    }

    private String getCurrentPageMasterName()
    {
	return currentPageMasterName;
    }

    // refactored from LayoutMasterSet
    private PageMaster getNextPageMaster(String pageSequenceName,
					 int currentPageNumber, 
					 boolean thisIsFirstPage, 
					 boolean isEmptyPage )
	throws FOPException
    {
	PageMaster pageMaster = null;

	// see if there is a page master sequence for this master name		
	PageSequenceMaster sequenceMaster = 
	    this.layoutMasterSet.getPageSequenceMaster( pageSequenceName );

	if (sequenceMaster != null) {
	    pageMaster = getNextSimplePageMaster(sequenceMaster, 
						 currentPageNumber,
						 thisIsFirstPage,
						 isEmptyPage).getPageMaster();

	} else { // otherwise see if there's a simple master by the given name
	    SimplePageMaster simpleMaster = 
		this.layoutMasterSet.getSimplePageMaster( pageSequenceName );
	    if (simpleMaster == null) {
		throw new FOPException( "'master-name' for 'fo:page-sequence'" +
					"matches no 'simple-page-master' or 'page-sequence-master'" );
	    }
	    currentPageMasterName = pageSequenceName;
	    
	    pageMaster = simpleMaster.getNextPageMaster();
	}
	return pageMaster;
    }


    /** 
     * Returns true when there is more flow elements left to lay out.
     */
    private boolean flowsAreIncomplete()
    {
	boolean isIncomplete = false;

	for (Enumeration e = _flowMap.elements(); e.hasMoreElements(); ) {
	    Flow flow = (Flow)e.nextElement();
	    if (flow instanceof StaticContent) {
		continue;
	    }
	    
	    Status status = flow.getStatus();
	    isIncomplete |= status.isIncomplete();
	}
	return isIncomplete;
    }

    /**
     * Returns the flow that maps to the given region class for the current
     * page master. 
     */
    private Flow getCurrentFlow(String regionClass) 
    {
	Region region = getCurrentSimplePageMaster().getRegion(regionClass);
      	if (region != null) {
	    Flow flow = (Flow)_flowMap.get(region.getRegionName());
	    return flow;
	    
	}
	else {
	    
	System.out.println("flow is null. regionClass = '"+regionClass+"' currentSPM = "+getCurrentSimplePageMaster());
	
	return null;
	}
	
    }

    private boolean isFlowForMasterNameDone( String masterName )
    {		
	// parameter is master-name of PMR; we need to locate PM
	// referenced by this, and determine whether flow(s) are OK
	if (masterName != null) {
	  
	    SimplePageMaster spm = this.layoutMasterSet.getSimplePageMaster( masterName );
	    Region region = spm.getRegion(RegionBody.REGION_CLASS);
	    
	    
	    Flow flow = (Flow)_flowMap.get( region.getRegionName() );
	    if ((null == flow) || flow.getStatus().isIncomplete())
		return false;
	    else
		return true;
	}
	return false;
    }
	
	public boolean isFlowSet()
	{
		return isFlowSet;
	}
	
	public void setIsFlowSet(boolean isFlowSet)
	{
		this.isFlowSet = isFlowSet;
	}
}
