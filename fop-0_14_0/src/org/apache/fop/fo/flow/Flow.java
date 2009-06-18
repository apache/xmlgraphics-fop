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
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.layout.Area;
import org.apache.fop.apps.FOPException;

// Java
import java.util.Hashtable;
import java.util.Enumeration;

public class Flow extends FObj {
	
    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new Flow(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new Flow.Maker();
    }

    PageSequence pageSequence;

    protected Flow(FObj parent, PropertyList propertyList)
	throws FOPException {
	super(parent, propertyList);
	this.name =  "fo:flow";

	if (parent.getName().equals("fo:page-sequence")) {
	    this.pageSequence = (PageSequence) parent;
	} else {
	    throw new FOPException("flow must be child of "
				   + "page-sequence, not "
				   + parent.getName());
	}
	pageSequence.setFlow(this);
    }
	
    public Status layout(Area area) throws FOPException {
	if (this.marker == START) {
	    this.marker = 0;
	}

	boolean prevChildMustKeepWithNext = false;

	int numChildren = this.children.size();
	for (int i = this.marker; i < numChildren; i++) {
	    FObj fo = (FObj) children.elementAt(i);
	    Status status;
	    if ((status = fo.layout(area)).isIncomplete()) {
		if ((prevChildMustKeepWithNext) && (status.laidOutNone())) {
		    this.marker = i - 1;
		    FObj prevChild = (FObj) children.elementAt(this.marker);
		    prevChild.removeAreas();
		    prevChild.resetMarker();
		    prevChild.removeID(area.getIDReferences());
		    return new Status(Status.AREA_FULL_SOME);
		    // should probably return AREA_FULL_NONE if first
		    // or perhaps an entirely new status code
		} else {
		    this.marker = i;
		    return status;
		}
	    }
	    if (status.getCode() == Status.KEEP_WITH_NEXT) {
		prevChildMustKeepWithNext = true;
	    }
	    else {
	    	prevChildMustKeepWithNext = false;
	    }
	    
	}
	return new Status(Status.OK);
    }
}
