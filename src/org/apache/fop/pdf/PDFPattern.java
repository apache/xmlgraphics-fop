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

//Java...
import java.util.Vector;

//FOP...
import org.apache.fop.datatypes.ColorSpace;

/**
 * class representing a PDF Function.
 *
 * PDF Functions represent parameterized mathematical formulas and sampled representations with
 * arbitrary resolution. Functions are used in two areas: device-dependent
 * rasterization information for halftoning and transfer
 * functions, and color specification for smooth shading (a PDF 1.3 feature).
 *
 * All PDF Functions have a FunctionType (0,2,3, or 4), a Domain, and a Range.
 */
public class PDFPattern extends PDFPathPaint {
	private PDFNumber pdfNumber = new PDFNumber();
	
	/**
	 * The resources associated with this pattern
	 */
	//Guts common to all function types
	
	protected PDFResources resources = null;

	/**
	 * Either one (1) for tiling, or two (2) for shading.
	 */
	protected int patternType = 2; //Default

	/**
	 * The name of the pattern such as "Pa1" or "Pattern1"
	 */
	protected String patternName = null;

	/**
	 * 1 for colored pattern, 2 for uncolored
	 */
	protected int paintType = 2;

	/**
	 * 1 for constant spacing, 2 for no distortion, and 3 for fast rendering
	 */
	protected int tilingType = 1;

	/**
	 * Vector of Doubles representing the Bounding box rectangle
	 */
	protected Vector bBox = null;

	/**
	 * Horizontal spacing
	 */
	protected double xStep = -1;

	/**
	 * Vertical spacing
	 */
	protected double yStep = -1;

	/**
	 * The Shading object comprising the Type 2 pattern
	 */
	protected PDFShading shading=null;

	/**
	 * Vector of Integers represetning the Extended unique Identifier
	 */
	protected Vector xUID=null;

	/**
	 * String representing the extended Graphics state.
	 * Probably will never be used like this.
	 */
	protected StringBuffer extGState = null; //eventually, need a PDFExtGSState object... but not now.

	/**
	 * Vector of Doubles representing the Transformation matrix.
	 */
	protected Vector matrix=null;

	/**
	 * The stream of a pattern
	 */
	protected StringBuffer patternDataStream = null;
	
	
	/**
	 * Create a tiling pattern (type 1).
	 * 
	 * @param theNumber The object number of this PDF Object
	 * @param thePatternName The name of the pattern such as "Pa1" or "Pattern1"
	 * @param theResources the resources associated with this pattern
	 * @param thePatternType the type of pattern, which is 1 for tiling. 
	 * @param thePaintType 1 or 2, colored or uncolored.
	 * @param theTilingType 1, 2, or 3, constant spacing, no distortion, or faster tiling
	 * @param theBBox Vector of Doubles: The pattern cell bounding box
	 * @param theXStep horizontal spacing
	 * @param theYStep vertical spacing
	 * @param theMatrix Optional Vector of Doubles transformation matrix
	 * @param theXUID Optional vector of Integers that uniquely identify the pattern
	 * @param thePatternDataStream The stream of pattern data to be tiled.
	 */
	public PDFPattern(int theNumber, String thePatternName,
			PDFResources theResources, int thePatternType, //1
			int thePaintType, int theTilingType,
			Vector theBBox, double theXStep, double theYStep,
			Vector theMatrix, Vector theXUID, StringBuffer thePatternDataStream)
	{
		super(theNumber);
		this.patternName = thePatternName;

		this.resources = theResources;
		//This next parameter is implicit to all constructors, and is
		//not directly passed.
		
		this.patternType = 1; //thePatternType;
		this.paintType = thePaintType;
		this.tilingType = theTilingType;
		this.bBox = theBBox;
		this.xStep = theXStep;
		this.yStep = theYStep;
		this.matrix = theMatrix;
		this.xUID = theXUID;
		this.patternDataStream = thePatternDataStream;
	}

	/**
	 * Create a type 2 pattern (smooth shading)
	 * 
	 * @param theNumber the object number of this PDF object
	 * @param thePatternName the name of the pattern
	 * @param thePatternType the type of the pattern, which is 2, smooth shading
	 * @param theShading the PDF Shading object that comprises this pattern
	 * @param theXUID optional:the extended unique Identifier if used.
	 * @param theExtGState optional: the extended graphics state, if used.
	 * @param theMatrix Optional:Vector of Doubles that specify the matrix.
	 */
	public PDFPattern(int theNumber, String thePatternName,
		int thePatternType, PDFShading theShading, Vector theXUID,
		StringBuffer theExtGState,Vector theMatrix)
	{
		super(theNumber);
	
		this.patternName = thePatternName;
		
		this.patternType = 2; //thePatternType;
		this.shading = theShading;
		this.xUID = theXUID;
		//this isn't really implemented, so it should always be null.
		//I just don't want to have to add a new parameter once it is implemented.
		this.extGState = theExtGState; // always null
		this.matrix = theMatrix;
	}
	
	/**
	 * Get the name of the pattern
	 * 
	 * @return String representing the name of the pattern.
	 */
	public String getName()
	{
		return (this.patternName);
	}
	
	public String getColorSpaceOut(boolean fillNotStroke)
	{
		if(fillNotStroke)
		{ //fill but no stroke
			return("/Pattern cs /"+this.getName()+" scn \n");
		}
		else
		{ //stroke (or border)
			return("/Pattern CS /"+this.getName()+" SCN \n");
		}
	}
 
	
	 /**
	  * represent as PDF. Whatever the FunctionType is, the correct
	  * representation spits out. The sets of required and optional
	  * attributes are different for each type, but if a required
	  * attribute's object was constructed as null, then no error
	  * is raised. Instead, the malformed PDF that was requested
	  * by the construction is dutifully output.
	  * This policy should be reviewed.
	  *
	  * @return the PDF string.
	  */
	public byte[] toPDF() {


		int vectorSize=0;
		int tempInt=0;
		StringBuffer p = new StringBuffer();
		p.append(this.number + " " +this.generation
			+ " obj\n<< \n/Type /Pattern \n");

		if(this.resources != null)
		{
			p.append("/Resources "+this.resources.referencePDF()+" \n");
		}

		p.append("/PatternType "+this.patternType+" \n");

		if(this.patternType == 1)
		{
			p.append("/PaintType "+this.paintType+" \n");
			p.append("/TilingType "+this.tilingType+" \n");
			
			if(this.bBox != null)
			{
				vectorSize = this.bBox.size();
				p.append("/BBox [ ");
				for (tempInt =0; tempInt < vectorSize; tempInt++)
				{
					p.append(
					pdfNumber.doubleOut((Double)this.bBox.elementAt(tempInt)));				
					p.append(" ");
				}
				p.append("] \n");
			}
			p.append("/XStep "+pdfNumber.doubleOut(new Double(this.xStep))+" \n");
			p.append("/YStep "+pdfNumber.doubleOut(new Double(this.yStep))+" \n");
			
			if(this.matrix != null)
			{
				vectorSize = this.matrix.size();
				p.append("/Matrix [ ");
				for (tempInt =0; tempInt < vectorSize; tempInt++)
				{
					p.append(pdfNumber.doubleOut(
						(Double)this.matrix.elementAt(tempInt)));
					p.append(" ");
				}
				p.append("] \n");
			}
			
			if(this.xUID != null)
			{
				vectorSize = this.xUID.size();
				p.append("/XUID [ ");
				for (tempInt =0; tempInt < vectorSize; tempInt++)
				{
					p.append(((Integer)this.xUID.elementAt(tempInt))+" ");				
				}
				p.append("] \n");
			}
			//don't forget the length of the stream.
			if(this.patternDataStream != null)
			{
				p.append("/Length "+(this.patternDataStream.length()+1)
					+ " \n");
			}
				
		}
		else //if (this.patternType ==2)
		{//Smooth Shading...
			if(this.shading != null)
			{
				p.append("/Shading "+this.shading.referencePDF()+" \n");
			}
			
			if(this.xUID != null)
			{
				vectorSize = this.xUID.size();
				p.append("/XUID [ ");
				for (tempInt =0; tempInt < vectorSize; tempInt++)
				{
					p.append(((Integer)this.xUID.elementAt(tempInt))+" ");
				}
				p.append("] \n");
			}			
			
			if(this.extGState != null)
			{//will probably have to change this if it's used.
				p.append("/ExtGState "+this.extGState+" \n");
			}
			
			if(this.matrix != null)
			{
				vectorSize = this.matrix.size();
				p.append("/Matrix [ ");
				for (tempInt =0; tempInt < vectorSize; tempInt++)
				{
					p.append(pdfNumber.doubleOut(
					(Double)this.matrix.elementAt(tempInt)));
					p.append(" ");
				}
				p.append("] \n");
			}
		}//end of if patterntype =1...else 2.

		p.append(">> \n");

		//stream representing the function
		if(this.patternDataStream != null)
		{
			p.append("stream\n"+this.patternDataStream +"\nendstream\n");
		}

		p.append("endobj\n");

		

		return (p.toString().getBytes());

	}
}
