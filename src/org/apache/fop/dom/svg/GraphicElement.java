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
package org.apache.fop.dom.svg;

import java.util.Enumeration;

import org.apache.fop.datatypes.*;

import org.w3c.dom.svg.*;
import org.w3c.dom.*;
import org.w3c.dom.events.*;

/**
 * base class for SVG graphic objects.
 *
 * Graphic objects include rectangles, lines and text
 *
 */
public abstract class GraphicElement extends SVGElementImpl implements SVGTransformable, SVGLangSpace, SVGTests, EventTarget {
	protected SVGList reqFeatures;
	protected SVGList reqExtensions;
	protected SVGList sysLanguage;
	SVGAnimatedTransformList transform;
	String xmlspace = "default";

	public SVGElement getNearestViewportElement( )
	{
		return null;
	}

	public SVGElement getFarthestViewportElement( )
	{
		return null;
	}

	public SVGAnimatedTransformList getTransform()
	{
		if(transform != null) {
			return transform;
		}
		SVGTransformList stl = new SVGTransformListImpl();
		SVGTransform transform = new SVGTransformImpl();
		stl.appendItem(transform);
		SVGAnimatedTransformList atl = new SVGAnimatedTransformListImpl();
		atl.setBaseVal(stl);
		return atl;
	}

	public void setTransform(SVGAnimatedTransformList transform)
	{
		this.transform = transform;
	}

	public SVGRect getBBox()
	{
		return null;
	}

	public SVGMatrix getCTM()
	{
		return null;
	}

	public SVGMatrix getScreenCTM()
	{
		return null;
	}

	public SVGMatrix getTransformToElement(SVGElement element)
									throws SVGException
	{
		return null;
	}

	public String getXMLlang()
	{
		return null;
	}

	public void setXMLlang(String xmllang)
	{
	}

	public String getXMLspace()
	{
		return xmlspace;
	}

	public void setXMLspace(String xmlspace)
	{
		this.xmlspace = xmlspace;
	}

	public SVGList getRequiredFeatures( )
	{
		return reqFeatures;
	}

	public void setRequiredFeatures( SVGList requiredFeatures )
                       throws DOMException
	{
	    reqFeatures = requiredFeatures;
	}

	public SVGList getRequiredExtensions( )
	{
		return reqExtensions;
	}

	public void setRequiredExtensions( SVGList requiredExtensions )
                       throws DOMException
	{
	    reqExtensions = requiredExtensions;
	}

	public boolean hasExtension ( String extension )
	{
		return false;
	}

	public SVGList getSystemLanguage()
	{
		return sysLanguage;
	}

	public void setSystemLanguage(SVGList systemLanguage)
	{
	    sysLanguage = systemLanguage;
	}

	public void addEventListener(String type, 
										EventListener listener, 
										boolean useCapture)
	{
	}

	public void removeEventListener(String type, 
									EventListener listener, 
									boolean useCapture)
	{
	}

	public boolean dispatchEvent(Event evt)
//								throws EventException
	{
		return false;
	}

	/**
	 * Convenience method for implementations of SVGTransformable
	 * that have children that represents the bounding box
	 */
	protected SVGRect getChildrenBBox()
	{
		float minX = 10000000; // a big number
		float maxX = -10000000; // a low number
		float minY = 10000000; // a big number
		float maxY = -10000000; // a low number
		NodeList nl = getChildNodes();
		// can width and height be negative??
		for(int count = 0; count < nl.getLength(); count++) {
			Node n = nl.item(count);
			if(n instanceof SVGTransformable) {
				SVGRect r = ((SVGTransformable)n).getBBox();
				if(r != null) {
					if(minX > r.getX())
					    minX = r.getX();
					if(minY > r.getY())
					    minY = r.getY();
					if(maxX < r.getX() + r.getWidth())
					    maxX = r.getX() + r.getWidth();
					if(maxY > r.getY() + r.getHeight())
					    maxY = r.getY() + r.getHeight();
				}
			}
		}
		SVGRect rect = new SVGRectImpl();
		rect.setX(minX);
		rect.setY(minY);
		rect.setWidth(maxX - minX);
		rect.setHeight(maxY - minY);
		return rect;
	}
}
