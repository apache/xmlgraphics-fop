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
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.ForeignObjectArea;
import org.apache.fop.layout.FontState;
import org.apache.fop.apps.FOPException;

import org.apache.fop.dom.svg.*;
import org.w3c.dom.svg.*;

import org.apache.fop.dom.svg.SVGArea;
/**
 * class representing svg:svg pseudo flow object.
 */
public class SVG extends FObj implements GraphicsCreator {

	/**
	 * inner class for making SVG objects.
	 */
	public static class Maker extends FObj.Maker {

	/**
	 * make an SVG object.
	 *
	 * @param parent the parent formatting object
	 * @param propertyList the explicit properties of this object
	 *
	 * @return the SVG object
	 */
	public FObj make(FObj parent, PropertyList propertyList)
		throws FOPException {
		return new SVG(parent, propertyList);
	}
	}

	/**
	 * returns the maker for this object.
	 *
	 * @return the maker for SVG objects
	 */
	public static FObj.Maker maker() {
	return new SVG.Maker();
	}

	FontState fs;
	float width;
	float height;

	/**
	 * constructs an SVG object (called by Maker).
	 *
	 * @param parent the parent formatting object
	 * @param propertyList the explicit properties of this object
	 */
	public SVG(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
	this.name = "svg:svg";
	}

	public SVGElement createGraphic()
	{
		SVGSVGElementImpl svgArea = null;
		SVGLength w = ((SVGLengthProperty)this.properties.get("width")).getSVGLength();
		SVGLength h = ((SVGLengthProperty)this.properties.get("height")).getSVGLength();
		svgArea = new SVGSVGElementImpl();
		SVGAnimatedLength sal;
		sal = new SVGAnimatedLengthImpl(w);
		sal.setBaseVal(w);
		svgArea.setWidth(sal);
		sal = new SVGAnimatedLengthImpl(h);
		sal.setBaseVal(h);
		svgArea.setHeight(sal);
		SVGLength x = ((SVGLengthProperty)this.properties.get("x")).getSVGLength();
		sal = new SVGAnimatedLengthImpl(x);
		sal.setBaseVal(x);
		svgArea.setX(sal);
		SVGLength y = ((SVGLengthProperty)this.properties.get("y")).getSVGLength();
		sal = new SVGAnimatedLengthImpl(y);
		sal.setBaseVal(y);
		svgArea.setY(sal);

		svgArea.setStyle(((SVGStyle)this.properties.get("style")).getStyle());
		svgArea.setTransform(((SVGTransform)this.properties.get("transform")).getTransform());
		svgArea.setId(this.properties.get("id").getString());
		int numChildren = this.children.size();
		for (int i = 0; i < numChildren; i++) {
			FONode fo = (FONode) children.elementAt(i);
			if(fo instanceof GraphicsCreator) {
				SVGElement impl = ((GraphicsCreator)fo).createGraphic();
				if(impl != null) {
					if(impl instanceof SVGElementImpl)
						((SVGElementImpl)impl).setClassName(new SVGAnimatedStringImpl(((FObj)fo).getProperty("class").getString()));
					svgArea.appendChild((org.w3c.dom.Node)impl);
				}
//			} else if(fo instanceof Defs) {
//				svgArea.addDefs(((Defs)fo).createDefs());
			}
			Status status;
		}
		return svgArea;
	}

	/**
	 * layout this formatting object.
	 *
	 * @param area the area to layout the object into
	 *
	 * @return the status of the layout
	 */
	public Status layout(Area area) throws FOPException {
	
	if (!(area instanceof ForeignObjectArea)) {
	    // this is an error
	    throw new FOPException("SVG not in fo:instream-foreign-object");
	}

	if (this.marker == BREAK_AFTER) {
		return new Status(Status.OK);
	}

	if (this.marker == START) {
		/* retrieve properties */
		String id = this.properties.get("id").getString();
		String fontFamily = this.properties.get("font-family").getString();
		String fontStyle = this.properties.get("font-style").getString();
		String fontWeight = this.properties.get("font-weight").getString();
		int fontSize = this.properties.get("font-size").getLength().mvalue();
		
		this.fs = new FontState(area.getFontInfo(), fontFamily,
					 fontStyle, fontWeight, fontSize);
		
		this.width = ((SVGLengthProperty)this.properties.get("width")).getSVGLength().getValue();
		this.height = ((SVGLengthProperty)this.properties.get("height")).getSVGLength().getValue();
		
		this.marker = 0;
	}

	/* create an SVG area */
	/* if width and height are zero, may want to get the bounds of the content. */
	SVGArea svg = new SVGArea(fs, width, height);
	SVGDocument doc = new SVGDocumentImpl();
	svg.setSVGDocument(doc);
	svg.start();

	/* add the SVG area to the containing area */
	ForeignObjectArea foa = (ForeignObjectArea)area;
	foa.setObject(svg);
	foa.setIntrinsicWidth(svg.getWidth());
	foa.setIntrinsicHeight(svg.getHeight());

	doc.appendChild((SVGSVGElement)createGraphic());

	/* finish off the SVG area */
	svg.end();

	/* return status */
	return new Status(Status.OK);
	}
}
