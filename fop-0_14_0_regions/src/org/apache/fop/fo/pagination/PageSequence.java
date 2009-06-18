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
import org.apache.fop.layout.AreaTree;
import org.apache.fop.layout.Page;
import org.apache.fop.layout.PageMaster;
import org.apache.fop.apps.FOPException;                   

// Java
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

public class PageSequence extends FObj
{

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

    static final int EXPLICIT = 0;
    static final int AUTO = 1;
    static final int AUTO_EVEN = 2;
    static final int AUTO_ODD = 3;

    protected Root root;
    // protected Flow flow;
	// (001008) language in spec suggests that page sequence must have at
	// least one flow, but may have more. The contents BNF in section
	// 6.4.5 of the spec is likely incorrect.
	protected Hashtable flows;
    // protected Title title;
	
	protected Hashtable beforeStaticContents;
	protected Hashtable afterStaticContents;
    protected LayoutMasterSet layoutMasterSet;
	protected String masterName;
	
	// used for mapping regions <=> static contents in correct SPM
	protected String currentPageMasterName;
	
	protected Hashtable flowNames;
	
    protected Page currentPage;
    protected int currentPageNumber = 0;
    protected static int runningPageNumberCounter = 0;  //keeps count of page number from previous PageSequence
    protected int pageNumberType;  // specifies page numbering type (auto|auto-even|auto-odd|explicit)
    protected boolean thisIsFirstPage; // used to determine whether to calculate auto, auto-even, auto-odd

    protected PageSequence(FObj parent, PropertyList propertyList)
    throws FOPException {
        super(parent, propertyList);
        this.name = "fo:page-sequence";

		// region support
		flowNames = new Hashtable();	// all 'flow-name's in this page sequence
		beforeStaticContents = new Hashtable();
		afterStaticContents = new Hashtable();
		flows = new Hashtable();	// all Flow's in this page sequence
		
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
		
        thisIsFirstPage=true; // we are now on the first page of the page sequence
        InitialPageNumber ipn = (InitialPageNumber) this.properties.get("initial-page-number");
        String ipnValue=ipn.getString();

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

        masterName = ((MasterName) this.properties.get("master-name")).getString();
    }

    protected Page makePage(AreaTree areaTree, int firstAvailPageNumber,
		boolean isFirstPage, boolean isEmptyPage)
		throws FOPException {
        // layout this page sequence
		
        // while there is still stuff in the flow, ask the
        // layoutMasterSet for a new page 

		// page number is 0-indexed
        PageMaster pageMaster =
			this.layoutMasterSet.getNextPageMaster(
			masterName, firstAvailPageNumber, isFirstPage, isEmptyPage );
			
		// store the current 'master-name' for access by format()
		currentPageMasterName = this.layoutMasterSet.getCurrentPageMasterName();
		
		// a legal alternative is to use the last sub-sequence
		// specification. That's not done here.
        if ( pageMaster == null )
        {
		throw new FOPException("page masters exhausted");
        }
        return pageMaster.makePage(areaTree);
    }

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
            ((currentPageNumber % 2) == 1) )
            {
				isEmptyPage = true;
            }
            else if ( (status.getCode() == Status.FORCE_PAGE_BREAK_ODD) &&
            ((currentPageNumber % 2) == 0) )
            {
				isEmptyPage = true;
            }
            else
            {
				isEmptyPage = false;
			}
			
            currentPage = makePage(areaTree, firstAvailPageNumber, tempIsFirstPage, isEmptyPage);            
			
            currentPage.setNumber(this.currentPageNumber);
            this.runningPageNumberCounter=this.currentPageNumber;            

            MessageHandler.log(" [" + currentPageNumber);
            if ( (!this.beforeStaticContents.isEmpty()) &&
            (currentPage.getBefore() != null) )
            {
                AreaContainer beforeArea = currentPage.getBefore();
                beforeArea.setIDReferences(areaTree.getIDReferences());
				// locate the correct fo:static-content from the "beforeStaticContents"
				// Hashtable, using the "currentPageMasterName" as the key.
				StaticContent before =
					(StaticContent)this.beforeStaticContents.get(currentPageMasterName);
				if (null == before)
					MessageHandler.errorln("No static-content found for region-before "
					+ "in page-master '" + currentPageMasterName + "'");
				else
                	before.layout(beforeArea);
            }
            if ( (!this.beforeStaticContents.isEmpty()) &&
            (currentPage.getAfter() != null) )
            {
                AreaContainer afterArea = currentPage.getAfter();
                afterArea.setIDReferences(areaTree.getIDReferences());
				// locate the correct fo:static-content from the "afterStaticContents"
				// Hashtable, using the "currentPageMasterName" as the key.
				StaticContent after =
					(StaticContent)this.afterStaticContents.get(currentPageMasterName);
				if (null == after)
					MessageHandler.errorln("No static-content found for region-after "
					+ "in page-master '" + currentPageMasterName + "'");
				else
                	after.layout(afterArea);
            }
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
                AreaContainer bodyArea = currentPage.getBody();
                bodyArea.setIDReferences(areaTree.getIDReferences());
				// locate the correct fo:flow from the "flows"
				// Hashtable, using the "currentPageMasterName" as the key.
				Flow flow =
					(Flow)this.flows.get(currentPageMasterName);
				if (null == flow)
					MessageHandler.errorln("No flow found for region-body "
					+ "in page-master '" + currentPageMasterName + "'");
				else
				{
                	status = flow.layout(bodyArea);
					flow.setCurrentStatus(status);
				}
            }
            MessageHandler.log("]");
            areaTree.addPage(currentPage);
        } while ( flowsAreIncomplete() );
        MessageHandler.errorln("");
    }

    public void setFlow(String name, Flow flow)
		throws FOPException {

		if (flowNames.containsKey(name))
		{
        	throw new FOPException("flow-names must be unique within an fo:page-sequence");
		}
		
		// store the Flow against the SimplePageMaster(s) that contain(s) it
		Vector pageMasterNames = this.layoutMasterSet.findPageMasterNames(name);
		if (pageMasterNames.isEmpty())
		{
        	MessageHandler.errorln("flow-name maps to no region(s) in page-masters");
		}
		else
		{
			for (Enumeration e = pageMasterNames.elements(); e.hasMoreElements(); )
			{
				String pageMasterName = (String)e.nextElement();
				if (this.layoutMasterSet.regionNameMapsTo(pageMasterName,name).equals("fo:region-body"))
				{
					flows.put(pageMasterName,flow);
					flowNames.put(name,"body");
				}
				else
				{
					MessageHandler.errorln("Flow flow-name does not map to fo:region-body");
					flowNames.put(name,"unsupported");
				}
			}
		}
    }

    public String setStaticContent(String name, StaticContent staticContent)
		throws FOPException {

		// region class that static content maps to
		String regionClass = null;
		
		// store the flow name
		if (flowNames.containsKey(name))
		{
        	throw new FOPException("flow-names must be unique within an fo:page-sequence");
		}
		
		// store the StaticContent against the SimplePageMaster(s) that contain(s) it
		Vector pageMasterNames = this.layoutMasterSet.findPageMasterNames(name);
		if (pageMasterNames.isEmpty())
		{
        	MessageHandler.errorln("flow-name maps to no region(s) in page-masters");
		}
		else
		{
			for (Enumeration e = pageMasterNames.elements(); e.hasMoreElements(); )
			{
				String pageMasterName = (String)e.nextElement();
				if (this.layoutMasterSet.regionNameMapsTo(pageMasterName,name).equals("fo:region-before"))
				{
					beforeStaticContents.put(pageMasterName,staticContent);
					regionClass = "before";
				}
				else if (this.layoutMasterSet.regionNameMapsTo(pageMasterName,name).equals("fo:region-after"))
				{
					afterStaticContents.put(pageMasterName,staticContent);
					regionClass = "after";
				}
				else
				{
					MessageHandler.errorln("StaticContent flow-name maps to unsupported region");
					regionClass = "unsupported";
				}
				flowNames.put(name,regionClass);
			}
		}
		return regionClass;
    }
	
	public boolean flowsAreIncomplete()
	{
		boolean isIncomplete = false;
    	for (Enumeration e = flows.elements(); e.hasMoreElements(); )
		{
			Flow flow = (Flow)e.nextElement();
			Status status = flow.getCurrentStatus();
			isIncomplete |= status.isIncomplete();
		}

		return isIncomplete;
	}
	
	public Flow getFlow( String flowName )
	{
		return (Flow)flows.get( flowName );
	}
}
