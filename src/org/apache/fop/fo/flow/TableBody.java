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
import org.apache.fop.datatypes.*;
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;

// Java
import java.util.Vector;

public class TableBody extends FObj {
	
    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new TableBody(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new TableBody.Maker();
    }

    FontState fs;
    int spaceBefore;
    int spaceAfter;
    ColorType backgroundColor;

    Vector columns;

    AreaContainer areaContainer;

    public TableBody(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
	this.name = "fo:table-body";
    }

    public void setColumns(Vector columns) {
	this.columns = columns;
    }

    public Status layout(Area area) throws FOPException {
	if (this.marker == BREAK_AFTER) {
	    return new Status(Status.OK);
	}

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
	    this.spaceBefore =
		this.properties.get("space-before.optimum").getLength().mvalue();  
	    this.spaceAfter =
		this.properties.get("space-after.optimum").getLength().mvalue(); 
	    this.backgroundColor =
		this.properties.get("background-color").getColorType();

	    if (area instanceof BlockArea) {
		area.end();
	    }

	    //if (this.isInListBody) {
	    //startIndent += bodyIndent + distanceBetweenStarts;
	    //}

	    this.marker = 0;

	}

	if ((spaceBefore != 0) && (this.marker ==0)) {
	    area.addDisplaySpace(spaceBefore);
	}

	this.areaContainer =
	    new AreaContainer(fs, -area.borderWidthLeft, -area.borderWidthTop, area.getAllocationWidth(), 
			  area.spaceLeft(), Position.RELATIVE);
	areaContainer.setPage(area.getPage());
	areaContainer.setBackgroundColor(backgroundColor);
	areaContainer.start();

	int numChildren = this.children.size();
	for (int i = this.marker; i < numChildren; i++) {
	    TableRow row = (TableRow) children.elementAt(i);

	    row.setColumns(columns);

	    Status status;
	    if ((status = row.layout(areaContainer)).isIncomplete()) {
		this.marker = i;
		if ((i != 0) && (status.getCode() == Status.AREA_FULL_NONE)) {
		    status = new Status(Status.AREA_FULL_SOME);
		}
    	        area.addChild(areaContainer);
		//areaContainer.end();
		area.increaseHeight(areaContainer.getHeight());
		return status;
	    }
	}
	area.addChild(areaContainer);
	areaContainer.end();

	/* should this be combined into above? */
	area.increaseHeight(areaContainer.getHeight());

	if (spaceAfter != 0) {
	    area.addDisplaySpace(spaceAfter);
	}

	if (area instanceof BlockArea) {
	    area.start();
	}

	return new Status(Status.OK);
    }

    public int getAreaHeight() {
	return areaContainer.getHeight();
    }
}
