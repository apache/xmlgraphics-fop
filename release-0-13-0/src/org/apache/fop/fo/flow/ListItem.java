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
import org.apache.fop.layout.Area;
import org.apache.fop.layout.BlockArea;
import org.apache.fop.layout.FontState;
import org.apache.fop.apps.FOPException;

// Java
import java.util.Enumeration;

public class ListItem extends FObj {

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new ListItem(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new ListItem.Maker();
    }

    FontState fs;
    int align;
    int alignLast;
    int breakBefore;
    int breakAfter;
    int lineHeight;
    int startIndent;
    int endIndent;
    int spaceBefore;
    int spaceAfter;
	
    public ListItem(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
	this.name = "fo:list-item";
    }

    public Status layout(Area area) throws FOPException {
	if (this.marker == START) {
	    String fontFamily =
		this.properties.get("font-family").getString(); 
	    String fontStyle =
		this.properties.get("font-style").getString(); 
	    String fontWeight =
		this.properties.get("font-weight").getString(); 
	    int fontSize =
		this.properties.get("font-size").getLength().mvalue(); 
	    
	    this.fs = new FontState(area.getFontInfo(), fontFamily, 
				    fontStyle, fontWeight, fontSize); 
	    
	    this.align = this.properties.get("text-align").getEnum(); 
	    this.alignLast =
		this.properties.get("text-align-last").getEnum(); 
	    this.lineHeight =
		this.properties.get("line-height").getLength().mvalue(); 
	    this.startIndent =
		this.properties.get("start-indent").getLength().mvalue(); 
	    this.endIndent =
		this.properties.get("end-indent").getLength().mvalue(); 
	    this.spaceBefore =
		this.properties.get("space-before.optimum").getLength().mvalue(); 
	    this.spaceAfter =
		this.properties.get("space-after.optimum").getLength().mvalue(); 
	    
	    this.marker = 0;
	}

	/* not sure this is needed given we know area is from list block */
	if (area instanceof BlockArea) {
	    area.end();
	}

	if (spaceBefore != 0) {
	    area.addDisplaySpace(spaceBefore);
	}

	startIndent += this.bodyIndent;

	BlockArea blockArea =
	    new BlockArea(fs, area.getAllocationWidth(), 
			  area.spaceLeft(), startIndent, endIndent,
			  0, align, alignLast, lineHeight);
	blockArea.setPage(area.getPage());
	blockArea.start();

	int numChildren = this.children.size();
	if (numChildren != 2) {
	    throw new FOPException("list-item must have exactly two children");
	}
	ListItemLabel label = (ListItemLabel) children.elementAt(0);
	ListItemBody body = (ListItemBody) children.elementAt(1);

	label.setDistanceBetweenStarts(this.distanceBetweenStarts);
	label.setLabelSeparation(this.labelSeparation);
	label.setBodyIndent(this.bodyIndent);

	body.setDistanceBetweenStarts(this.distanceBetweenStarts);
	body.setBodyIndent(this.bodyIndent);

	/* this doesn't actually do anything */
	body.setLabelSeparation(this.labelSeparation);

	Status status;

	// what follows doesn't yet take into account whether the
	// body failed completely or only got some text in

	if (this.marker == 0) {
	    status = label.layout(blockArea);
	    if (status.isIncomplete()) {
		return status;
	    }
	}

	status = body.layout(blockArea);
	if (status.isIncomplete()) {
	    blockArea.end();
	    area.addChild(blockArea);
	    area.increaseHeight(blockArea.getHeight());
	    this.marker = 1;
	    return status;
	}

	blockArea.end();
	area.addChild(blockArea);
	area.increaseHeight(blockArea.getHeight());

	if (spaceAfter != 0) {
	    area.addDisplaySpace(spaceAfter);
	}

	/* not sure this is needed given we know area is from list block */
	if (area instanceof BlockArea) {
	    area.start();
	}
	return new Status(Status.OK);
    }
}
