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

package org.apache.fop.layout;

// Java
import java.util.Vector;
import java.util.Enumeration;
import java.awt.Rectangle;

/**
 * a set of rectangles on a page that are linked to a common
 * destination
 */
public class LinkSet {

    /** the destination of the links */
    String externalDestination;

    /** the set of rectangles */
    Vector rects = new Vector();

    int xoffset = 0;

    int yoffset = 0;
	
    public LinkSet(String externalDest) {
	this.externalDestination = externalDest;
    }
    
    public void addLinkedRect(Rectangle r) {
	r.y = yoffset;
	rects.addElement(r);
    }
	
    public void setYOffset(int y) {
	this.yoffset = y;
    }
	
    public void applyAreaContainerOffsets(AreaContainer ac) {
	Enumeration re = rects.elements();
	while (re.hasMoreElements()) {
	    Rectangle r = (Rectangle)re.nextElement();
	    r.x += ac.getXPosition();
	    r.y = ac.getYPosition() - ac.getHeight() - r.y;
	}
    }
	
    // intermediate implementation for joining all sublinks on same line
    public void mergeLinks() {
	int numRects = rects.size();
	if (numRects == 1)
	    return;
	
	Rectangle curRect = new Rectangle((Rectangle)rects.elementAt(0));
	Vector nv = new Vector();
		
	for (int ri=1; ri < numRects; ri++) {
	    Rectangle r = (Rectangle)rects.elementAt(ri);
	    
	    if ((r.y != curRect.y) || (curRect.height != r.height)) {
		nv.addElement(curRect);
		curRect = new Rectangle(r);
	    } else {
		curRect.width = r.x + r.width - curRect.x;
	    }
	    if (ri == numRects-1)
		nv.addElement(curRect);
	}
	
	rects = nv;
    }
	
    public String getDest() {
	return this.externalDestination;
    }

    public Vector getRects() {
	return this.rects;
    }
}
