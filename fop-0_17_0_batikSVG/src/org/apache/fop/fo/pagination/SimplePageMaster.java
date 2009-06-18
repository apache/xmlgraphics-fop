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
 
 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
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
import org.apache.fop.layout.PageMaster;
import org.apache.fop.layout.BodyRegionArea;
import org.apache.fop.apps.FOPException;				   

import java.util.*;

public class SimplePageMaster extends FObj {
	
    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new SimplePageMaster(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new SimplePageMaster.Maker();
    }

    /** Page regions (regionClass, Region) */
    private Hashtable _regions;
    

    LayoutMasterSet layoutMasterSet;
    PageMaster pageMaster;
    String masterName;
    
    protected SimplePageMaster(FObj parent, PropertyList propertyList)
	throws FOPException {
	super(parent, propertyList);
	this.name = "fo:simple-page-master";

	if (parent.getName().equals("fo:layout-master-set")) {
	    this.layoutMasterSet = (LayoutMasterSet) parent;
	    masterName = this.properties.get("master-name").getString();
	    if (masterName == null) {
		MessageHandler.errorln("WARNING: simple-page-master does not have "
				   + "a master-name and so is being ignored");
	    } else {
		this.layoutMasterSet.addSimplePageMaster(this);
	    }
	} else {
	    throw new FOPException("fo:simple-page-master must be child "
				   + "of fo:layout-master-set, not " 
				   + parent.getName());
	}
	_regions = new Hashtable();
	
    }
	
    protected void end() {
	int pageWidth = this.properties.get("page-width").getLength().mvalue();
	int pageHeight = this.properties.get("page-height").getLength().mvalue();

	int marginTop = this.properties.get("margin-top").getLength().mvalue();
	int marginBottom = this.properties.get("margin-bottom").getLength().mvalue();
	int marginLeft = this.properties.get("margin-left").getLength().mvalue();
	int marginRight = this.properties.get("margin-right").getLength().mvalue();

	int contentRectangleXPosition = marginLeft;
	int contentRectangleYPosition = pageHeight - marginTop;
	int contentRectangleWidth = pageWidth - marginLeft - marginRight;
	int contentRectangleHeight = pageHeight - marginTop - marginBottom;
		
	this.pageMaster = new PageMaster(pageWidth, pageHeight);
	if (getRegion(RegionBody.REGION_CLASS) != null) {
	    this.pageMaster.addBody((BodyRegionArea)getRegion(RegionBody.REGION_CLASS).makeRegionArea(contentRectangleXPosition,contentRectangleYPosition,contentRectangleWidth,contentRectangleHeight));
	}
	else {
	    MessageHandler.errorln("ERROR: simple-page-master must have a region of class "+RegionBody.REGION_CLASS);
	}
	
	if (getRegion(RegionBefore.REGION_CLASS) != null) {
	    this.pageMaster.addBefore(getRegion(RegionBefore.REGION_CLASS).makeRegionArea(contentRectangleXPosition,contentRectangleYPosition,contentRectangleWidth,contentRectangleHeight));
	}
	
	if (getRegion(RegionAfter.REGION_CLASS) != null) {
	    this.pageMaster.addAfter(getRegion(RegionAfter.REGION_CLASS).makeRegionArea(contentRectangleXPosition,contentRectangleYPosition,contentRectangleWidth,contentRectangleHeight));
	}
	
    }

    public PageMaster getPageMaster() {
	return this.pageMaster;
    }

    public PageMaster getNextPageMaster() {
	return this.pageMaster;
    }

    public String getMasterName() 
    {
	return masterName;
    }
    

    protected void addRegion(Region region) 
	throws FOPException
    {
	if (_regions.containsKey(region.getRegionClass())) {
	    throw new FOPException("Only one region of class "+region.getRegionClass()+" allowed within a simple-page-master.");
	}
	else {
	    _regions.put(region.getRegionClass(), region);
	}
    }
    
    protected Region getRegion(String regionClass) 
    {
	return (Region)_regions.get(regionClass);
    }

    protected Hashtable getRegions() 
    {
	return _regions;
    }
    
    protected boolean regionNameExists(String regionName) 
    {
	for (Enumeration regenum = _regions.elements(); regenum.hasMoreElements() ;) {
	    Region r = (Region)regenum.nextElement();
	    if (r.getRegionName().equals(regionName)) {
		return true;
	    }
	}
	return false;
	
    }
    
}
