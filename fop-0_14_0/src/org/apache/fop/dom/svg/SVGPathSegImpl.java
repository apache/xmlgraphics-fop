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

public class SVGPathSegImpl implements SVGPathSeg {
	float[] values;
	short commandType;
	public SVGPathSegImpl(short type, float[] vals)
	{
		commandType = type;
		values = vals;
	}

	public float[] getValues()
	{
		return values;
	}

	public short getPathSegType( )
	{
		return commandType;
	}

	public void setPathSegType( short pathSegType )
	{
	}

	public String getPathSegTypeAsLetter( )
	{
		return null;
	}

	public void setPathSegTypeAsLetter( String pathSegTypeAsLetter )
	{
	}

/*	public float getX( )
	{
		return 0;
	}

	public void setX( float x )
	{
	}

	public float getY( )
	{
		return 0;
	}

	public void setY( float y )
	{
	}

	public float getX1( )
	{
		return 0;
	}

	public void setX1( float x1 )
	{
	}

	public float getY1( )
	{
		return 0;
	}

	public void setY1( float y1 )
	{
	}

	public float getX2( )
	{
		return 0;
	}

	public void setX2( float x2 )
	{
	}

	public float getY2( )
	{
		return 0;
	}

	public void setY2( float y2 )
	{
	}

	public float getR1( )
	{
		return 0;
	}

	public void setR1( float r1 )
	{
	}

	public float getR2( )
	{
		return 0;
	}

	public void setR2( float r2 )
	{
	}

	public float getAngle( )
	{
		return 0;
	}

	public void setAngle( float angle )
	{
	}

	public boolean getLargeArcFlag( )
	{
		return false;
	}

	public void setLargeArcFlag( boolean largeArcFlag )
	{
	}

	public boolean getSweepFlag( )
	{
		return false;
	}

	public void setSweepFlag( boolean sweepFlag )
	{
	}*/
}
