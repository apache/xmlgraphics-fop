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
 
 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
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

import org.w3c.dom.svg.*;
import org.w3c.dom.*;
import org.w3c.dom.events.*;

/**
 * class representing a rectangle in an SVG Area
 */
public class SVGRectElementImpl extends GraphicElement implements SVGRectElement {

	/** x-coordinate of corner */
	private SVGAnimatedLength x;

	/** y-coordinate of corner */
	private SVGAnimatedLength y;
	private SVGAnimatedLength rx;
	private SVGAnimatedLength ry;

	/** width of rectangle */
	private SVGAnimatedLength width;

	/** height of rectangle */
	private SVGAnimatedLength height;

	/**
	 * construct a rectangle graphic.
	 *
	 * @param x x-coordinate of corner
	 * @param y y-coordinate of corner
	 * @param width width of rectangle
	 * @param height height of rectangle
	 */
	public SVGRectElementImpl() {
	}

	public SVGRect getBBox()
	{
		SVGRect rect = new SVGRectImpl();
		rect.setX(x.getBaseVal().getValue());
		rect.setY(y.getBaseVal().getValue());
		rect.setWidth(width.getBaseVal().getValue());
		rect.setHeight(height.getBaseVal().getValue());
		return rect;
	}

	public SVGAnimatedLength getX( )
	{
		return x;
	}

	public void setX( SVGAnimatedLength x )
	{
		this.x = x;
	}

	public SVGAnimatedLength getY( )
	{
		return y;
	}

	public void setY( SVGAnimatedLength y )
	{
		this.y = y;
	}

	public SVGAnimatedLength getWidth( )
	{
		return width;
	}

	public void setWidth( SVGAnimatedLength width )
	{
		this.width = width;
	}

	public SVGAnimatedLength getHeight( )
	{
		return height;
	}

	public void setHeight( SVGAnimatedLength height )
	{
		this.height = height;
	}

	public SVGAnimatedLength getRx( )
	{
		return rx;
	}

	public void setRx( SVGAnimatedLength rx )
	{
	    this.rx = rx;
	}

	public SVGAnimatedLength getRy( )
	{
		return ry;
	}

	public void setRy( SVGAnimatedLength ry )
	{
	    this.ry = ry;
	}

    public String getTagName()
	{
		return "rect";
	}
}
