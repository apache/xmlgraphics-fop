/*-- $Id$ -- 

 ============================================================================
						 The Apache Software License, Version 1.1
 ============================================================================
 
	 Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of	source code must	retain the above copyright  notice,
	 this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
	 this list of conditions and the following disclaimer in the documentation
	 and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
	 include  the following  acknowledgment:	"This product includes	software
	 developed	by the  Apache Software Foundation	(http://www.apache.org/)."
	 Alternately, this  acknowledgment may  appear in the software itself,	if
	 and wherever such third-party acknowledgments normally appear.
 
 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
	 endorse  or promote  products derived  from this	software without	prior
	 written permission. For written permission, please contact
	 apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
	 "Apache" appear	in their name,  without prior written permission  of the
	 Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.	IN NO  EVENT SHALL  THE
 APACHE SOFTWARE	FOUNDATION	OR ITS CONTRIBUTORS	BE LIABLE FOR	ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,	EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY	THEORY OF LIABILITY,  WHETHER  IN CONTRACT,	STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN	ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software	Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 
 */
package org.apache.fop.pdf;

// Java
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.fop.datatypes.ColorType;
import java.util.Vector;

public class PDFColor extends PDFPathPaint {

	protected double red  = -1.0;
	protected double green= -1.0;
	protected double blue = -1.0;
	
	protected double cyan   = -1.0;
	protected double magenta= -1.0;
	protected double yellow = -1.0;
	protected double black  = -1.0;

	
	public PDFColor(int theNumber, org.apache.fop.datatypes.ColorType theColor)
	{
		this.colorspace = 0;
		//super(theNumber)
		this.red  =  (double)theColor.red();
		this.green = (double)theColor.green();
		this.blue  = (double)theColor.blue();
		
	}

	public PDFColor(int theNumber, double theRed, double theGreen, double theBlue) {
		//super(theNumber);
		
		this.colorspace=0;

		this.red = theRed;
		this.green = theGreen;
		this.blue = theBlue;
	}
	
	public PDFColor(int theNumber, double theCyan, double theMagenta, double theYellow, double theBlack) {
		//super(theNumber);//?
		
		this.colorspace = 1;
		
		this.cyan = theCyan;
		this.magenta = theMagenta;
		this.yellow = theYellow;
		this.black = theBlack;
	}
	
	public int setColorspace()
	{
		return(this.colorspace);
	}
	
	public Vector getVector()
	{//return a vector representation of the color
	//in the appropriate colorspace.
		Vector theColorVector= new Vector();
		if (this.colorspace == 0)
		{//RGB				
			theColorVector.addElement(new Double(this.red));
			theColorVector.addElement(new Double(this.green));
			theColorVector.addElement(new Double(this.blue));
		}
		else
		{//CMYK
			theColorVector.addElement(new Double(this.cyan));
			theColorVector.addElement(new Double(this.magenta));
			theColorVector.addElement(new Double(this.yellow));
			theColorVector.addElement(new Double(this.black));

		}
		return(theColorVector);
	}
	
	public double red()
	{
		return(this.red);
	}
	public double green()
	{
		return(this.green);
	}
	public double blue()
	{
		return(this.blue);
	}
	public double cyan()
	{
		return(this.cyan);
	}
	public double magenta()
	{
		return(this.magenta);
	}
	public double yellow()
	{
		return(this.yellow);
	}
	public double black()
	{
		return(this.black);
	}
	
	public String getColorspaceOut(boolean fillNotStroke)
	{
		StringBuffer p = new StringBuffer("");

		double tempDouble;
		
		//if the color was constructed in a different colorspace,
		//then we need to convert it.
		
		if(this.colorspace==0)
		{//colorspace is RGB
			if((this.red < 0)
				|| (this.green < 0)
				|| (this.blue < 0))
			{
				this.convertCMYKtoRGB();
			} //end of convert from CMYK
				
			//output RGB
			if(fillNotStroke)
			{ //fill
				p.append(this.doubleOut(this.red)+" "
					+this.doubleOut(this.green)+" "
					+this.doubleOut(this.blue)+" "
					+" rg \n");
			} 
			else
			{//stroke/border
				p.append(this.doubleOut(this.red)+" "
					+this.doubleOut(this.green)+" "
					+this.doubleOut(this.blue)+" "
					+" RG \n");
			}
		}//end of output RGB
		else
		{//colorspace is CMYK
			if((this.cyan < 0)
				|| (this.magenta < 0)
				|| (this.yellow < 0)
				|| (this.black < 0))
			{
				this.convertRGBtoCMYK();
			}//end of if convert from RGB
			
			if(fillNotStroke)
			{ //fill
				p.append(this.doubleOut(this.cyan) + " "
					+ this.doubleOut(this.magenta) + " "
					+ this.doubleOut(this.yellow) + " "
					+ this.doubleOut(this.black) + " k \n");
			}
			else
			{ //fill
				p.append(this.doubleOut(this.cyan) + " "
					+ this.doubleOut(this.magenta) + " "
					+ this.doubleOut(this.yellow) + " "
					+ this.doubleOut(this.black) + " K \n");
			}
			
		}//end of if CMYK
		return(p.toString());
	}

	
	
	public String doubleOut(double doubleDown)
	{
		StringBuffer p = new StringBuffer();
		double trouble = doubleDown % 1;
		
		if(trouble > 0.950)
		{
			p.append((int)doubleDown+1);
		}
		else if (trouble < 0.050)
		{
			p.append((int)doubleDown);
		}
		else
		{
			String doubleString = new String(doubleDown+"");
			int decimal = doubleString.indexOf(".");
			p.append(doubleString.substring(0, decimal));

			if ((doubleString.length() - decimal) > 6)
			{
				p.append(doubleString.substring(decimal,decimal+6));
			}
			else
			{
				p.append(doubleString.substring(decimal));
			}
		}
		return(p.toString());
	}
	
	protected void convertCMYKtoRGB()
	{
		//convert CMYK to RGB
		this.red = 1.0 - this.cyan;
		this.green = 1.0 - this.green;
		this.blue= 1.0 - this.yellow;
				
		this.red   = (this.black / 2.0)+ this.red;
		this.green = (this.black / 2.0)+ this.green;
		this.blue  = (this.blue / 2.0) + this.blue;
		
	}

	protected void convertRGBtoCMYK()
	{
		//convert RGB to CMYK
		this.cyan   = 1.0 - this.red;
		this.magenta= 1.0 - this.green;
		this.yellow = 1.0 - this.blue;
				
		this.black = 0.0;
		/* If you want to calculate black, uncomment this
		//pick the lowest color
		tempDouble = this.red;
				
		if (this.green < tempDouble)
			tempDouble = this.green;
				
		if (this.blue < tempDouble)
			tempDouble = this.blue;
					
		this.black = tempDouble / 2.0; // 3.0???
		*/
	}
	
	

	
	String toPDF()
	{
		return ("");

	} //end of toPDF
}
