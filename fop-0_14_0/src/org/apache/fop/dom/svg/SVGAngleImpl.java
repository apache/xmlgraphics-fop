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

import org.apache.fop.fo.Property;

import java.util.*;
import java.text.*;

import org.w3c.dom.svg.*;

/**
 * SVG Angle.
 *
 */
public class SVGAngleImpl implements SVGAngle {
	float value = 0;
	short unitType = SVG_ANGLETYPE_UNKNOWN;

	public SVGAngleImpl()
	{
	}

	public short getUnitType( )
	{
		return unitType;
	}

	public float getValue( )
	{
		return value;
	}

	public void setValue( float value )
	{
		this.value = value;
	}

	public float getValueInSpecifiedUnits( )
	{
		switch(unitType) {
			case SVG_ANGLETYPE_UNKNOWN:
				throw new SVGExceptionImpl(SVGException.SVG_WRONG_TYPE_ERR, "unknown unit type");
//			break;
			case SVG_ANGLETYPE_UNSPECIFIED:
				throw new SVGExceptionImpl(SVGException.SVG_WRONG_TYPE_ERR, "unknown unit type");
//			break;
			case SVG_ANGLETYPE_DEG:
			break;
			case SVG_ANGLETYPE_RAD:
			break;
			case SVG_ANGLETYPE_GRAD:
			break;
		}
		return 0;
	}

	public void setValueInSpecifiedUnits( float valueInSpecifiedUnits )
	{
		switch(unitType) {
			case SVG_ANGLETYPE_UNKNOWN:
				throw new SVGExceptionImpl(SVGException.SVG_WRONG_TYPE_ERR, "unknown unit type");
//			break;
			case SVG_ANGLETYPE_UNSPECIFIED:
				throw new SVGExceptionImpl(SVGException.SVG_WRONG_TYPE_ERR, "unknown unit type");
//			break;
			case SVG_ANGLETYPE_DEG:
			break;
			case SVG_ANGLETYPE_RAD:
			break;
			case SVG_ANGLETYPE_GRAD:
			break;
		}
	}

	public String getValueAsString( )
	{
		NumberFormat nf = NumberFormat.getInstance();
		return nf.format(value);
	}

	public void setValueAsString( String valueAsString )
	{
		NumberFormat nf = NumberFormat.getInstance();
		try {
			value = nf.parse(valueAsString).floatValue();
			value = (float)(value * Math.PI / 90f);
		} catch(ParseException pe) {
			value = 0;
		}
	}

	public float getAnimatedValue( )
	{
		return 0;
	}

	public void newValueSpecifiedUnits ( short unitType, float valueInSpecifiedUnits )
	              throws SVGException
	{
		switch(unitType) {
			case SVG_ANGLETYPE_UNKNOWN:
				throw new SVGExceptionImpl(SVGException.SVG_WRONG_TYPE_ERR, "unknown unit type");
//			break;
			case SVG_ANGLETYPE_UNSPECIFIED:
				throw new SVGExceptionImpl(SVGException.SVG_WRONG_TYPE_ERR, "unknown unit type");
//			break;
			case SVG_ANGLETYPE_DEG:
				value = (float)(valueInSpecifiedUnits * Math.PI / 90.0);
			break;
			case SVG_ANGLETYPE_RAD:
				value = valueInSpecifiedUnits;
			break;
			case SVG_ANGLETYPE_GRAD:
				value = (float)(valueInSpecifiedUnits * Math.PI / 90.0);
			break;
		}
		this.unitType = unitType;
	}

	public void convertToSpecifiedUnits ( short unitType )
	              throws SVGException
	{
		switch(unitType) {
			case SVG_ANGLETYPE_UNKNOWN:
				throw new SVGExceptionImpl(SVGException.SVG_WRONG_TYPE_ERR, "unknown unit type");
//			break;
			case SVG_ANGLETYPE_UNSPECIFIED:
				throw new SVGExceptionImpl(SVGException.SVG_WRONG_TYPE_ERR, "unknown unit type");
//			break;
			case SVG_ANGLETYPE_DEG:
			break;
			case SVG_ANGLETYPE_RAD:
			break;
			case SVG_ANGLETYPE_GRAD:
			break;
		}
		this.unitType = unitType;
	}
}
