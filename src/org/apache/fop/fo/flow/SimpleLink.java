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

/* this class contributed by Arved Sandstrom with minor modifications
   by James Tauber */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.datatypes.ColorType;

// Java
import java.util.Enumeration;
import java.awt.Rectangle;

public class SimpleLink extends FObjMixed {

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException { 
	    return new SimpleLink(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new SimpleLink.Maker();
    }
    
    public SimpleLink(FObj parent, PropertyList propertyList)
	throws FOPException {
	super(parent, propertyList);
	this.name = "fo:simple-link";
		
	if (parent.getName().equals("fo:flow")) {
	    throw new FOPException("simple-link can't be directly"
				   + " under flow"); 
	}
    }

    public Status layout(Area area) throws FOPException {
        String destination;
        int linkType;
        
        if ( !(destination = this.properties.get("internal-destination").getString()).equals(""))
        {             
            linkType=LinkSet.INTERNAL;
        }
        else if ( !(destination = this.properties.get("external-destination").getString()).equals("") )	
        { 
            linkType=LinkSet.EXTERNAL;
        }
        else
        {
            throw new FOPException("internal-destination or external-destination must be specified in simple-link");
        }

	if (this.marker == START) {
	    // initialize id                       
            String id = this.properties.get("id").getString();            
            area.getIDReferences().initializeID(id,area);                                
	    this.marker = 0;
	}
	
	// new LinkedArea to gather up inlines
        LinkSet ls = new LinkSet(destination, area, linkType);
		
	Page p = area.getPage();

	// assumption - AS
	// should be able to retrieve this from somewhere - JT
	AreaContainer ac = p.getBody();
		
	int numChildren = this.children.size();
	for (int i = this.marker; i < numChildren; i++) {
	    FONode fo = (FONode) children.elementAt(i);
	    fo.setLinkSet(ls);
	    
	    Status status;
	    if ((status = fo.layout(area)).isIncomplete()) {
		this.marker = i;
		return status;
	    }
	}
		
	ls.applyAreaContainerOffsets(ac, area);
		
	// pass on command line
	String mergeLinks = System.getProperty( "links.merge" );
	if ((null != mergeLinks) && !mergeLinks.equalsIgnoreCase("no")) {
	    ls.mergeLinks();
	}
		
	p.addLinkSet(ls);

	return new Status(Status.OK);
    }
}
