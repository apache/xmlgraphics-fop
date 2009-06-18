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

import java.util.*;

import org.w3c.dom.svg.*;

/**
 *
 */
public class SVGPathElementImpl extends GraphicElement implements SVGPathElement {

	public Vector pathElements;

	/**
	 * construct a line graphic
	 */
	public SVGPathElementImpl(Vector v)
	{
		this.pathElements = v;
	}

	public SVGAnimatedNumber getPathLength()
	{
		return null;
	}

	public void setPathLength( SVGAnimatedNumber length )
	{
	}

	public SVGList getPathSegList()
	{
		return null;
	}

	public SVGList getNormalizedPathSegList()
	{
		return null;
	}

	public float getTotalLength()
	{
		return 0;
	}

	public SVGPoint getPointAtLength(float distance)
	              throws SVGException
	{
		return null;
	}

	public int getPathSegAtLength(float distance)
	              throws SVGException
	{
		return 0;
	}

	public short getPathSegType( )
	{
		return 0;
	}

	public String getPathSegTypeAsLetter( )
	{
		return null;
	}

	public SVGPathSegClosePath createSVGPathSegClosePath (  )
	{
		return null;
	}

	public SVGPathSegMovetoAbs createSVGPathSegMovetoAbs ( float x, float y )
	{
		return null;
	}

	public SVGPathSegMovetoRel createSVGPathSegMovetoRel ( float x, float y )
	{
		return null;
	}

	public SVGPathSegLinetoAbs createSVGPathSegLinetoAbs ( float x, float y )
	{
		return null;
	}

	public SVGPathSegLinetoRel createSVGPathSegLinetoRel ( float x, float y )
	{
		return null;
	}

	public SVGPathSegCurvetoCubicAbs createSVGPathSegCurvetoCubicAbs ( float x, float y, float x1, float y1, float x2, float y2 )
	{
		return null;
	}

	public SVGPathSegCurvetoCubicRel createSVGPathSegCurvetoCubicRel ( float x, float y, float x1, float y1, float x2, float y2 )
	{
		return null;
	}

	public SVGPathSegCurvetoQuadraticAbs createSVGPathSegCurvetoQuadraticAbs ( float x, float y, float x1, float y1 )
	{
		return null;
	}

	public SVGPathSegCurvetoQuadraticRel createSVGPathSegCurvetoQuadraticRel ( float x, float y, float x1, float y1 )
	{
		return null;
	}

	public SVGPathSegArcAbs createSVGPathSegArcAbs ( float x, float y, float r1, float r2, float angle, boolean largeArcFlag, boolean sweepFlag )
	{
		return null;
	}

	public SVGPathSegArcRel createSVGPathSegArcRel ( float x, float y, float r1, float r2, float angle, boolean largeArcFlag, boolean sweepFlag )
	{
		return null;
	}

	public SVGPathSegLinetoHorizontalAbs createSVGPathSegLinetoHorizontalAbs ( float x )
	{
		return null;
	}

	public SVGPathSegLinetoHorizontalRel createSVGPathSegLinetoHorizontalRel ( float x )
	{
		return null;
	}

	public SVGPathSegLinetoVerticalAbs createSVGPathSegLinetoVerticalAbs ( float y )
	{
		return null;
	}

	public SVGPathSegLinetoVerticalRel createSVGPathSegLinetoVerticalRel ( float y )
	{
		return null;
	}

	public SVGPathSegCurvetoCubicSmoothAbs createSVGPathSegCurvetoCubicSmoothAbs ( float x, float y, float x2, float y2 )
	{
		return null;
	}

	public SVGPathSegCurvetoCubicSmoothRel createSVGPathSegCurvetoCubicSmoothRel ( float x, float y, float x2, float y2 )
	{
		return null;
	}

	public SVGPathSegCurvetoQuadraticSmoothAbs createSVGPathSegCurvetoQuadraticSmoothAbs ( float x, float y )
	{
		return null;
	}

	public SVGPathSegCurvetoQuadraticSmoothRel createSVGPathSegCurvetoQuadraticSmoothRel ( float x, float y )
	{
		return null;
	}

	public SVGList   getAnimatedPathSegList( )
	{
		return null;
	}

	public SVGList   getAnimatedNormalizedPathSegList( )
	{
		return null;
	}
}
