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
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.*;

// Java
import java.util.Hashtable;
import java.util.Enumeration;

public class BlockContainer extends FObj {
	
    FontState fs;
    ColorType backgroundColor;
    int paddingTop;
    int paddingBottom;
    int paddingLeft;
    int paddingRight;
    int position;
    
    int top;
    int bottom;
    int left;
    int right;
    int width;
    int height;
    
    ColorType borderColor;
    int borderWidth;
    int borderStyle;
    
    AreaContainer areaContainer;

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new BlockContainer(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new BlockContainer.Maker();
    }

    PageSequence pageSequence;

    protected BlockContainer(FObj parent, PropertyList propertyList)
	throws FOPException {
	super(parent, propertyList);
        this.name =  "fo:block-container";
    }
	
    public Status layout(Area area) throws FOPException {
	if (this.marker == START) {
	    this.marker = 0;
            
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
	    this.backgroundColor =
		this.properties.get("background-color").getColorType();
	    this.paddingTop =
		this.properties.get("padding").getLength().mvalue();
            this.paddingLeft = this.paddingTop;
            this.paddingRight = this.paddingTop;
            this.paddingBottom = this.paddingTop;
            if (this.paddingTop == 0) {
	      this.paddingTop =
		  this.properties.get("padding-top").getLength().mvalue();
	      this.paddingLeft =
		  this.properties.get("padding-left").getLength().mvalue();
	      this.paddingBottom =
		  this.properties.get("padding-bottom").getLength().mvalue();
	      this.paddingRight =
		  this.properties.get("padding-right").getLength().mvalue();
            }
            
	    this.position =
		this.properties.get("position").getEnum();
	    this.top =
		this.properties.get("top").getLength().mvalue();
	    this.bottom =
		this.properties.get("bottom").getLength().mvalue();
	    this.left =
		this.properties.get("left").getLength().mvalue();
	    this.right =
		this.properties.get("right").getLength().mvalue();
	    this.width =
		this.properties.get("width").getLength().mvalue();
	    this.height =
		this.properties.get("height").getLength().mvalue();
            
	    this.borderColor =
		this.properties.get("border-color").getColorType();
	    this.borderWidth =
		this.properties.get("border-width").getLength().mvalue();
	    this.borderStyle =
		this.properties.get("border-style").getEnum();

            // initialize id                       
            String id = this.properties.get("id").getString();            
            area.getIDReferences().initializeID(id,area);  
	}        

	boolean prevChildMustKeepWithNext = false;
        
        AreaContainer container = (AreaContainer)area;
        if ((this.width == 0) && (this.height == 0)) {
          width = right - left;
          height = bottom - top;
        }
	this.areaContainer =
          new AreaContainer(fs, container.getXPosition() + left, container.getYPosition() - top, width, height,
                            position);

	areaContainer.setPage(area.getPage());
	areaContainer.setBackgroundColor(backgroundColor);
	areaContainer.setPadding(paddingTop, paddingLeft, paddingBottom,
			     paddingRight);
        areaContainer.setBorderStyle(borderStyle, borderStyle, borderStyle, borderStyle); 
        areaContainer.setBorderWidth(borderWidth, borderWidth, borderWidth, borderWidth); 
        areaContainer.setBorderColor(borderColor, borderColor, borderColor, borderColor); 
	areaContainer.start();
        
        areaContainer.setAbsoluteHeight(area.getAbsoluteHeight());
        areaContainer.setIDReferences(area.getIDReferences());
        
	int numChildren = this.children.size();
	for (int i = this.marker; i < numChildren; i++) {
	    FObj fo = (FObj) children.elementAt(i);
	    Status status;
	    if ((status = fo.layout(areaContainer)).isIncomplete()) {
              /*
		if ((prevChildMustKeepWithNext) && (status.laidOutNone())) {
		    this.marker = i - 1;
		    FObj prevChild = (FObj) children.elementAt(this.marker);
		    prevChild.removeAreas();
		    prevChild.resetMarker();
		    return new Status(Status.AREA_FULL_SOME);
		    // should probably return AREA_FULL_NONE if first
		    // or perhaps an entirely new status code
		} else {
		    this.marker = i;
		    return status;
		}
              */
	    }
	    if (status.getCode() == Status.KEEP_WITH_NEXT) {
		prevChildMustKeepWithNext = true;
	    }
	}
        area.setAbsoluteHeight(areaContainer.getAbsoluteHeight());

	areaContainer.end();
        if (position == Position.ABSOLUTE)
          areaContainer.setHeight(height);
	area.addChild(areaContainer);
        
	return new Status(Status.OK);
    }
}
