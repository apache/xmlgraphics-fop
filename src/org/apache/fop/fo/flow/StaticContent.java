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
package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.fo.pagination.*;
import org.apache.fop.layout.Area;
import org.apache.fop.apps.FOPException;				   

// Java
import java.util.Enumeration;

public class StaticContent extends Flow {

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new StaticContent(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new StaticContent.Maker();
    }

    protected StaticContent(FObj parent, PropertyList propertyList)
	throws FOPException {
	super(parent, propertyList);
	((PageSequence)parent).setIsFlowSet(false);	// hacquery of sorts
    }
    
    public Status layout(Area area) throws FOPException {
	return layout(area, null);
    }
    

    public Status layout(Area area, Region region) throws FOPException {

	int numChildren = this.children.size();
	// Set area absolute height so that link rectangles will be drawn correctly in xsl-before and xsl-after
	String regionClass = "none";
	if (region != null) {
	    regionClass = region.getRegionClass();
	}
	else {
	    if(getFlowName().equals("xsl-region-before")) {
		regionClass = RegionBefore.REGION_CLASS;
	    }
	    else if(getFlowName().equals("xsl-region-after")) {
		regionClass = RegionAfter.REGION_CLASS;
	    }
	    
	}
	
	if(regionClass.equals(RegionBefore.REGION_CLASS)) {
	    area.setAbsoluteHeight(-area.getMaxHeight());
	}
	else if(regionClass.equals(RegionAfter.REGION_CLASS)) {
	    area.setAbsoluteHeight(area.getPage().getBody().getMaxHeight());
	}
       
	for (int i = 0; i < numChildren; i++) {
	    FObj fo = (FObj) children.elementAt(i);
	    
		Status status;
		if ((status = fo.layout(area)).isIncomplete()) {
			this.marker = i;
			if ((i != 0) && (status.getCode() == Status.AREA_FULL_NONE)) {
			    status = new Status(Status.AREA_FULL_SOME);
			}
			return(status);
		}
	}
	resetMarker();
	return new Status(Status.OK);
    }

    protected String getElementName() 
    {
	return "fo:static-content";
    }

    // flowname checking is more stringient for static content currently
    protected void setFlowName(String name)
	throws FOPException
    {
	if (name == null || name.equals("")) {
	    throw new FOPException("A 'flow-name' is required for "+getElementName()+".");
 	}
	else {
	    super.setFlowName(name);
	}
	
    }
}
