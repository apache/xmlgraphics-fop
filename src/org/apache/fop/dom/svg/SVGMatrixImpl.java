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

import org.w3c.dom.svg.*;

/**
 *
 */
public class SVGMatrixImpl implements SVGMatrix {
	float a = 1.0f;
	float b = 0.0f;
	float c = 0.0f;
	float d = 1.0f;
	float e = 0.0f;
	float f = 0.0f;

	public SVGMatrixImpl()
	{
	}

	public float getA( )
	{
		return a;
	}

	public void setA( float a )
	{
		this.a = a;
	}

	public float getB( )
	{
		return b;
	}

	public void setB( float b )
	{
		this.b = b;
	}

	public float getC( )
	{
		return c;
	}

	public void setC( float c )
	{
		this.c = c;
	}

	public float getD( )
	{
		return d;
	}

	public void setD( float d )
	{
		this.d = d;
	}

	public float getE( )
	{
		return e;
	}

	public void setE( float e )
	{
		this.e = e;
	}

	public float getF( )
	{
		return f;
	}

	public void setF( float f )
	{
		this.f = f;
	}

	public SVGMatrix multiply ( SVGMatrix secondMatrix )
	              throws SVGException
	{
		SVGMatrix mat = new SVGMatrixImpl();
		mat.setA(a * secondMatrix.getA() + b * secondMatrix.getC());
		mat.setB(a * secondMatrix.getB() + b * secondMatrix.getD());
		mat.setC(c * secondMatrix.getA() + d * secondMatrix.getC());
		mat.setD(c * secondMatrix.getB() + d * secondMatrix.getD());
		// e,f?
		mat.setE(e + secondMatrix.getE());
		mat.setF(f + secondMatrix.getF());
		return mat;
	}

	public SVGMatrix inverse (  )
	              throws SVGException
	{
		SVGMatrix mat = new SVGMatrixImpl();
		double det = Math.abs(a * d - b * c);
		mat.setA((float)(d / det));
		mat.setB((float)(b / det));
		mat.setC((float)(-c / det));
		mat.setD((float)(a / det));
		// e,f?
		mat.setE(-e);
		mat.setF(-f);
		return mat;
	}

/*			switch(t.getType()) {
				case SVGTransform.SVG_TRANSFORM_SCALE:
					{
						// to scale around the svg origin
						// we need to translate before and after
//						Float len = (Float)t.getValue();
//						float val = len.floatValue();
//						if(Math.abs(val) < 0.001)
//							val = 0;
//						currentStream.add("1 0 0 1 " + -posx / 1000f + " " + posy / 1000f + " cm\n");
//						currentStream.add(val + " 0 0 " + val + " 0 0 cm\n");
//						currentStream.add("1 0 0 1 " + posx / 1000f + " " + -posy / 1000f + " cm\n");
					}
				break;
				case SVGTransform.SVG_TRANSFORM_ROTATE:
					{
						// to rotate around the svg origin
						// we need to translate before and after
//						SVGLength len = (SVGLength)t.getValue();
//						float val = len.getValue();
//						float cosval = (float)Math.cos(val * Math.PI / 90f);
//						if(Math.abs(cosval) < 0.001)
//							cosval = 0;
//						float sinval = (float)Math.sin(val * Math.PI / 90f);
//						if(Math.abs(sinval) < 0.001)
//							sinval = 0;
//						currentStream.add("1 0 0 1 " + -posx / 1000f + " " + posy / 1000f + " cm\n");
//						currentStream.add(cosval
//										+ " " + sinval
//										+ " " + -sinval
//										+ " " + cosval
//										+ " 0 0 cm\n");
//						currentStream.add("1 0 0 1 " + posx / 1000f + " " + -posy / 1000f + " cm\n");
					}
				break;
			}*/

	public SVGMatrix translate ( float x, float y )
	              throws SVGException
	{
		SVGMatrix matrix = new SVGMatrixImpl();
		matrix.setA(a);
		matrix.setB(b);
		matrix.setC(c);
		matrix.setD(d);
		matrix.setE(e + x);
		matrix.setF(f + y);
		return matrix;
	}

	public SVGMatrix scale(float scaleFactor)
	              throws SVGException
	{
		SVGMatrix matrix = new SVGMatrixImpl();
		matrix.setA(a * scaleFactor);
		matrix.setB(b);
		matrix.setC(c);
		matrix.setD(d * scaleFactor);
		matrix.setE(e);
		matrix.setF(f);
		return matrix;
	}

	public SVGMatrix scaleNonUniform(float scaleFactorX, float scaleFactorY)
	              throws SVGException
	{
		SVGMatrix matrix = new SVGMatrixImpl();
		matrix.setA(a * scaleFactorX);
		matrix.setB(b);
		matrix.setC(c);
		matrix.setD(d * scaleFactorY);
		matrix.setE(e);
		matrix.setF(f);
		return matrix;
	}

	public SVGMatrix rotate ( float angle )
	              throws SVGException
	{
	    angle = (float)(angle * Math.PI / 180f);
		SVGMatrix matrix = new SVGMatrixImpl();
		matrix.setA((float)Math.cos(angle));
		matrix.setB((float)Math.sin(angle));
		matrix.setC((float)-Math.sin(angle));
		matrix.setD((float)Math.cos(angle));
		return multiply(matrix);
	}

	public SVGMatrix rotateFromVector(float x, float y) throws SVGException
	{
		return null;
	}

	public SVGMatrix flipX()
	{
		return null;
	}

	public SVGMatrix flipY()
	{
		return null;
	}

	public SVGMatrix skewX(float angle) throws SVGException
	{
		SVGMatrix matrix = new SVGMatrixImpl();
		matrix.setA(1);
		matrix.setB(0);
		matrix.setC((float)Math.sin(angle));
		matrix.setD(1);
		return multiply(matrix);
	}

	public SVGMatrix skewY(float angle) throws SVGException
	{
		SVGMatrix matrix = new SVGMatrixImpl();
		matrix.setA(1);
		matrix.setB((float)Math.sin(angle));
		matrix.setC(0);
		matrix.setD(1);
		return multiply(matrix);
	}

	public String toString()
	{
		return "[" + getA() + " " + getB() + " " + getC()
					+ " " + getD() + " " + getE() + " " + getF() + "]";
	}
}
