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

package org.apache.fop.svg;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.FontState;
import org.apache.fop.apps.FOPException;

import org.apache.fop.dom.svg.*;
import org.apache.fop.dom.svg.SVGArea;
/**
 *
 */
public class TRef extends FObj implements TextElement {

	/**
	 * inner class for making Line objects.
	 */
	public static class Maker extends FObj.Maker {

		/**
		 * make a Line object.
		 *
		 * @param parent the parent formatting object
		 * @param propertyList the explicit properties of this object
		 *
		 * @return the Line object
		 */
		public FObj make(FObj parent, PropertyList propertyList) throws FOPException
		{
			return new TRef(parent, propertyList);
		}
	}

	/**
	 * returns the maker for this object.
	 *
	 * @return the maker for TRef objects
	 */
	public static FObj.Maker maker() {
		return new TRef.Maker();
	}

	/**
	 * constructs a TRef object (called by Maker).
	 *
	 * @param parent the parent formatting object
	 * @param propertyList the explicit properties of this object
	 */
	protected TRef(FObj parent, PropertyList propertyList) {
		super(parent, propertyList);
		this.name = "svg:tref";
	}

	public GraphicImpl createTextElement()
	{
		SVGTRefElementImpl tref = new SVGTRefElementImpl();
		tref.setStyle(((SVGStyle)this.properties.get("style")).getStyle());
//		tref.dx = ((SVGLengthProperty)this.properties.get("dx")).getSVGLength().mvalue();
//		tref.dy = ((SVGLengthProperty)this.properties.get("dy")).getSVGLength().mvalue();
//		tref.x = ((SVGLengthProperty)this.properties.get("x")).getSVGLength().mvalue();
//		tref.y = ((SVGLengthProperty)this.properties.get("y")).getSVGLength().mvalue();
		Property prop;
		prop = this.properties.get("x");
		// bit of a hack, but otherwise the svg:text x element could be
		// returned which is not a list
		if(prop instanceof SVGLengthListProperty)
			tref.xlist = ((SVGLengthListProperty)prop).getSVGLengthList();
		prop = this.properties.get("y");
		if(prop instanceof SVGLengthListProperty)
			tref.ylist = ((SVGLengthListProperty)prop).getSVGLengthList();
		prop = this.properties.get("dx");
		if(prop instanceof SVGLengthListProperty)
			tref.dxlist = ((SVGLengthListProperty)prop).getSVGLengthList();
		prop = this.properties.get("dy");
		if(prop instanceof SVGLengthListProperty)
			tref.dylist = ((SVGLengthListProperty)prop).getSVGLengthList();
//		tref.dxlist = ((SVGLengthProperty)this.properties.get("dx")).getSVGLength().valueList();
		tref.ref = this.properties.get("xlink:href").getString();
		tref.setId(this.properties.get("id").getString());
		return tref;
	}

	/**
	 * layout this formatting object.
	 *
	 * @param area the area to layout the object into
	 *
	 * @return the status of the layout
	 */
	public Status layout(Area area) throws FOPException {
		
		/* if the area this is being put into is an SVGArea */
		if (area instanceof SVGArea) {
			/* add a line to the SVGArea */
		} else {
			/* otherwise generate a warning */
			System.err.println("WARNING: svg:tref outside svg:svg");
		}

		/* return status */
		return new Status(Status.OK);
	}
}
