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

import org.apache.fop.dom.stylesheets.StyleSheetListImpl;
import org.apache.fop.fo.Property;

import java.util.*;

import org.w3c.dom.events.Event;
import org.w3c.dom.Element;
import org.w3c.dom.css.RGBColor;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.stylesheets.StyleSheetList;
import org.w3c.dom.stylesheets.StyleSheet;
import org.w3c.dom.NodeList;
import org.w3c.dom.views.DocumentView;
import org.w3c.dom.svg.*;
import org.w3c.dom.css.*;
import org.w3c.dom.*;

/**
 *
 */
public class SVGSVGElementImpl extends GraphicElement implements SVGSVGElement {
	SVGAnimatedLength x;
	SVGAnimatedLength y;
	SVGAnimatedLength width;
	SVGAnimatedLength height;

	public SVGSVGElementImpl()
	{
	}

	public SVGAnimatedLength getX( )
	{
		return x;
	}

	public SVGAnimatedLength getY( )
	{
		return y;
	}

	public SVGAnimatedLength getWidth( )
	{
		return width;
	}

	public SVGAnimatedLength getHeight( )
	{
		return height;
	}

	public void setWidth(SVGAnimatedLength w)
	{
		width = w;
	}

	public void setHeight(SVGAnimatedLength h)
	{
		height = h;
	}

	public void setX(SVGAnimatedLength x)
	{
		this.x = x;
	}

	public void setY(SVGAnimatedLength y)
	{
		this.y = y;
	}

	public SVGRect getViewport( )
	{
		return null;
	}

    public SVGRect getBBox()
    {
        return getChildrenBBox();
    }

	public String getContentScriptType( )
	{
		return null;
	}

	public void setContentScriptType( String contentScriptType )
	{
	}

	public String getContentStyleType( )
	{
		return null;
	}

	public void setContentStyleType( String contentStyleType )
	{
	}

/*	public CSSValue getPresentationAttribute ( String name )
	{
		CSSStyleDeclaration style;
		style = getStyle();
		CSSValue val;
		val = style.getPropertyCSSValue(name);
		if(val == null) {
			// get "style" element style for this
		}
		if(val == null) {
			// get element parents style
			Node par = getParentNode();
			if(par instanceof SVGStylable) {
				val = ((SVGStylable)par).getPresentationAttribute(name);
			}
		}
		return val;
	}*/

	public SVGPoint getCurrentTranslate( )
	{
		return null;
	}

	public void setCurrentTranslate( SVGPoint currentTranslate )
	{
	}

	public SVGViewSpec getCurrentView( )
	{
		return null;
	}

	public void deSelectAll()
	{
	}

	public NodeList getIntersectionList ( SVGRect rect, SVGElement referenceElement )
	{
		return null;
	}

	public NodeList getEnclosureList ( SVGRect rect, SVGElement referenceElement )
	{
		return null;
	}

	public boolean checkIntersection ( SVGElement element, SVGRect rect )
	{
		return false;
	}

	public boolean checkEnclosure ( SVGElement element, SVGRect rect )
	{
		return false;
	}

	public float getPixelUnitToMillimeterX( )
	{
		return 0;
	}

	public float getPixelUnitToMillimeterY( )
	{
		return 0;
	}

	public float getScreenPixelToMillimeterX( )
	{
		return 0;
	}

	public float getScreenPixelToMillimeterY( )
	{
		return 0;
	}

	public boolean getUseCurrentView( )
	{
		return true;
	}

	public void setUseCurrentView( boolean useCurrentView )
	{
	}

	public float getCurrentScale( )
	{
		return 0;
	}

	public void setCurrentScale( float currentScale )
	{
	}

	public int suspendRedraw ( int max_wait_milliseconds )
	{
		return 0;
	}

	public void unsuspendRedraw ( int suspend_handle_id )
	{
	}

	public void unsuspendRedrawAll (  )
	{
	}

	public void forceRedraw (  )
	{
	}

	public void pauseAnimations (  )
	{
	}

	public void unpauseAnimations (  )
	{
	}

	public boolean animationsPaused ()
	{
		return true;
	}

	public float getCurrentTime()
	{
		return 0;
	}

	public void setCurrentTime ( float seconds )
	{
	}

	public SVGLength createSVGLength (  )
	{
		return new SVGLengthImpl();
	}

	public SVGAngle createSVGAngle (  )
	{
		return new SVGAngleImpl();
	}

	public SVGPoint createSVGPoint (  )
	{
		return null;
	}

	public SVGMatrix createSVGMatrix (  )
	{
		return new SVGMatrixImpl();
	}

	public SVGRect createSVGRect (  )
	{
		return new SVGRectImpl();
	}

	public SVGTransform createSVGTransform (  )
	{
		return new SVGTransformImpl();
	}

	public SVGTransform createSVGTransformFromMatrix ( SVGMatrix matrix )
	{
		SVGTransform trans = new SVGTransformImpl();
		trans.setMatrix(matrix);
		return trans;
	}

	public RGBColor createRGBColor (  )
	{
		return null;
	}

	public SVGICCColor createSVGICCColor (  )
	{
		return null;
	}

	public Element getElementById ( String elementId )
	{
		return null;
	}

	public short getZoomAndPan( )
	{
		return 0;
	}

	public void setZoomAndPan( short zoomAndPan )
	{
	}

	public SVGAnimatedRect getViewBox()
	{
		return null;
	}

	public SVGAnimatedPreserveAspectRatio getPreserveAspectRatio( )
	{
		return null;
	}

	public CSSStyleDeclaration getComputedStyle(Element el, String str)
	{
		return null;
	}

	public CSSStyleDeclaration getOverrideStyle(Element el, String str)
	{
		return null;
	}

	public StyleSheetList getStyleSheets()
	{
		NodeList nl = getElementsByTagName("style");
		Vector shs = new Vector();
		for(int count = 0; count < nl.getLength(); count++) {
			Node el = (Node)nl.item(count);
			SVGStyleElementImpl sse = (SVGStyleElementImpl)el;
			StyleSheet sheet = sse.getStyleSheet();
			shs.addElement(sheet);
		}
		return new StyleSheetListImpl(shs);
	}

	public Event createEvent(String str)
	{
		return null;
	}

	public DocumentView getDocument()
	{
		return null;
	}

    public Node appendChild(Node newChild)
                                    throws DOMException
	{
		Node nChild = super.appendChild(newChild);
		if(newChild instanceof SVGElementImpl) {
			SVGElementImpl ele = (SVGElementImpl)newChild;
			ele.setOwnerSVG(this);
		}
		setOwnerSVG(this);
		return nChild;
	}

    public void setOwnerSVG(SVGSVGElement owner)
	{
		ownerSvg = owner;
		NodeList nl = getChildNodes();
		for(int count = 0; count < nl.getLength(); count++) {
			Node n = nl.item(count);
			if(n instanceof SVGElementImpl) {
				((SVGElementImpl)n).setOwnerSVG(this);
			}
		}
	}
}
