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
public class PDFFunction extends PDFObject {
	//Guts common to all function types
	/** Required: The Type of function (0,2,3,4) default is 0.*/
	protected int functionType =0; //Default
	
	/**
	 * Required: 2 * m Array of Double numbers which are possible inputs to the function
	 */
	protected Vector domain = null;

	/**
	 * Required: 2 * n Array of Double numbers which are possible outputs to the function
	 */	 
	protected Vector range = null;
	
	/* ********************TYPE 0***************************** */
	//FunctionType 0 specific function guts
	/**
	 * Required: Array containing the Integer size of the Domain and Range, respectively.
	 * Note: This is really more like two seperate integers, sizeDomain, and sizeRange,
	 * but since they're expressed as an array in PDF, my implementation reflects that.
	 */ 
	protected Vector size = null;
	
	/** Required for Type 0: Number of Bits used to represent each sample value. Limited to 1,2,4,8,12,16,24, or 32 */
	protected int bitsPerSample = 1;	
	/** Optional for Type 0: order of interpolation between samples. Limited to linear (1) or cubic (3). Default is 1 */
	protected int order = 1;
	/**
	 * Optional for Type 0: A 2 * m array of Doubles which provides a linear mapping of input values to the domain.
	 * 
	 * Required for Type 3: A 2 * k array of Doubles that, taken in pairs, map each subset of the domain defined by Domain and the Bounds array to the domain of the corresponding function.
	 * Should be two values per function, usually (0,1), as in [0 1 0 1] for 2 functions.
	 */
	protected Vector encode = null;
	/**
	 * Optinoal for Type 0: A 2 * n array of Doubles which provides a linear mapping of sample values to the range. Defaults to Range.
	 */
	protected Vector decode = null;
	/** Optional For Type 0: A stream of sample values */
	/** Required For Type 4: Postscript Calculator function composed of arithmetic, boolean, and stack operators + boolean constants */
	protected StringBuffer functionDataStream = null;
	/**
	 * Required (?) For Type 0: A vector of Strings for the various filters to be used to decode the stream.
	 * These are how the string is compressed. Flate, LZW, etc.
	 */
	protected Vector filter = null;
	/* *************************TYPE 2************************** */
	/**
	 * Required For Type 2: An Array of n Doubles defining the function result when x=0. Default is [0].
	 */
	protected Vector cZero = null;
	/**
	 * Required For Type 2: An Array of n Doubles defining the function result when x=1. Default is [1].
	 */
	protected Vector cOne = null;
	/**
	 * Required for Type 2: The interpolation exponent.
	 * Each value x will return n results.
	 * Must be greater than 0.
	 */
	protected double interpolationExponentN = 1;

	/* *************************TYPE 3************************** */
	/** Required for Type 3: An vector of PDFFunctions which form an array of k single input functions making up the stitching function. */
	protected Vector functions = null;
	/**
	 * Optional for Type 3: An array of (k-1) Doubles that, in combination with Domain, define the intervals to which each function from the Functions array apply. Bounds elements must be in order of increasing magnitude, and each value must be within the value of Domain.
	 * k is the number of functions.
	 * If you pass null, it will output (1/k) in an array of k-1 elements.
	 * This makes each function responsible for an equal amount of the stitching function.
	 * It makes the gradient even.
	 */
	protected Vector bounds = null;
   // See encode above, as it's also part of Type 3 Functions.
   
	/* *************************TYPE 4************************** */
	//See 'data' above.
	private PDFNumber pdfNumber = new PDFNumber();
	
	/**
	 * create an complete Function object of Type 0, A Sampled function.
	 * 
	 * Use null for an optional object parameter if you choose not to use it.
	 * For optional int parameters, pass the default.
	 * 
	 * @param theDomain Vector objects of Double objects.
	 * This is the domain of the function.
	 * See page 264 of the PDF 1.3 Spec.
	 * @param theRange Vector objects of Double objects.
	 * This is the Range of the function.
	 * See page 264 of the PDF 1.3 Spec.
	 * @param theSize A Vector object of Integer objects.
	 * This is the number of samples in each input dimension.
	 * I can't imagine there being more or less than two input dimensions,
	 * so maybe this should be an array of length 2.
	 * 
	 * See page 265 of the PDF 1.3 Spec.
	 * @param theBitsPerSample An int specifying the number of bits user to represent each sample value.
	 * Limited to 1,2,4,8,12,16,24 or 32.
	 * See page 265 of the 1.3 PDF Spec.
	 * @param theOrder The order of interpolation between samples. Default is 1 (one). Limited
	 * to 1 (one) or 3, which means linear or cubic-spline interpolation.
	 * 
	 * This attribute is optional.
	 * 
	 * See page 265 in the PDF 1.3 spec.
	 * @param theEncode Vector objects of Double objects.
	 * This is the linear mapping of input values intop the domain
	 * of the function's sample table. Default is hard to represent in
	 * ascii, but basically [0 (Size0 1) 0 (Size1 1)...].
	 * This attribute is optional.
	 * 
	 * See page 265 in the PDF 1.3 spec.
	 * @param theDecode Vector objects of Double objects.
	 * This is a linear mapping of sample values into the range.
	 * The default is just the range.
	 * 
	 * This attribute is optional.
	 * Read about it on page 265 of the PDF 1.3 spec.
	 * @param theFunctionDataStream The sample values that specify the function are provided in a stream.
	 * 
	 * This is optional, but is almost always used.
	 * 
	 * Page 265 of the PDF 1.3 spec has more.
	 * @param theFilter This is a vector of String objects which are the various filters that
	 * have are to be applied to the stream to make sense of it. Order matters,
	 * so watch out.
	 * 
	 * This is not documented in the Function section of the PDF 1.3 spec,
	 * it was deduced from samples that this is sometimes used, even if we may never
	 * use it in FOP. It is added for completeness sake.
	 * @param theNumber The object number of this PDF object.
	 * @param theFunctionType This is the type of function (0,2,3, or 4).
	 * It should be 0 as this is the constructor for sampled functions.
	 */
	public PDFFunction(int theNumber, int theFunctionType,
			Vector theDomain, Vector theRange,
			Vector theSize,int theBitsPerSample,
			int theOrder,Vector theEncode,Vector theDecode,
			StringBuffer theFunctionDataStream, Vector theFilter)
	{
		super(theNumber);
		
		this.functionType = 0; //dang well better be 0;
		this.size = theSize;
		this.bitsPerSample = theBitsPerSample; 
		this.order = theOrder; //int
		this.encode = theEncode;//vector of int
		this.decode = theDecode; //vector of int
		this.functionDataStream = theFunctionDataStream;
		this.filter = theFilter;//vector of Strings
		
		//the domain and range are actually two dimensional arrays.
		//so if there's not an even number of items, bad stuff
		//happens.
		this.domain =  theDomain;
		this.range =  theRange;
	}
	
	/**
	 * create an complete Function object of Type 2, an Exponential Interpolation function.
	 * 
	 * Use null for an optional object parameter if you choose not to use it.
	 * For optional int parameters, pass the default.
	 * 
	 * @param theNumber the object's number
	 * @param theDomain Vector objects of Double objects.
	 * This is the domain of the function.
	 * See page 264 of the PDF 1.3 Spec.
	 * @param theRange Vector of Doubles that is the Range of the function.
	 * See page 264 of the PDF 1.3 Spec.
	 * @param theCZero This is a vector of Double objects which defines the function result
	 * when x=0.
	 * 
	 * This attribute is optional.
	 * It's described on page 268 of the PDF 1.3 spec.
	 * @param theCOne This is a vector of Double objects which defines the function result
	 * when x=1.
	 * 
	 * This attribute is optional.
	 * It's described on page 268 of the PDF 1.3 spec.
	 * @param theInterpolationExponentN This is the inerpolation exponent.
	 * 
	 * This attribute is required.
	 * PDF Spec page 268
	 * @param theFunctionType The type of the function, which should be 2.
	 */
	public PDFFunction(int theNumber, int theFunctionType,
						Vector theDomain, Vector theRange,
						Vector theCZero, Vector theCOne,
						double theInterpolationExponentN)
	{
		super(theNumber);
		
		this.functionType = 2; //dang well better be 2;
		
		this.cZero = theCZero;
		this.cOne = theCOne;
		this.interpolationExponentN = theInterpolationExponentN;
		

		this.domain = theDomain;
		this.range = theRange;
		
	}

	/**
	 * create an complete Function object of Type 3, a Stitching function.
	 * 
	 * Use null for an optional object parameter if you choose not to use it.
	 * For optional int parameters, pass the default.
	 * 
	 * @param theNumber the object's number
	 * @param theDomain Vector objects of Double objects.
	 * This is the domain of the function.
	 * See page 264 of the PDF 1.3 Spec.
	 * @param theRange Vector objects of Double objects.
	 * This is the Range of the function.
	 * See page 264 of the PDF 1.3 Spec.
	 * @param theFunctions A Vector of the PDFFunction objects that the stitching function stitches.
	 * 
	 * This attributed is required.
	 * It is described on page 269 of the PDF spec.
	 * @param theBounds This is a vector of Doubles representing the numbers that,
	 * in conjunction with Domain define the intervals to which each function from
	 * the 'functions' object applies. It must be in order of increasing magnitude,
	 * and each must be within Domain.
	 * 
	 * It basically sets how much of the gradient each function handles.
	 * 
	 * This attributed is required.
	 * It's described on page 269 of the PDF 1.3 spec.
	 * @param theEncode Vector objects of Double objects.
	 * This is the linear mapping of input values intop the domain
	 * of the function's sample table. Default is hard to represent in
	 * ascii, but basically [0 (Size0 1) 0 (Size1 1)...].
	 * This attribute is required.
	 * 
	 * See page 270 in the PDF 1.3 spec.
	 * @param theFunctionType This is the function type. It should be 3,
	 * for a stitching function.
	 */
	public PDFFunction(int theNumber, int theFunctionType,
			Vector theDomain, Vector theRange,
			Vector theFunctions, Vector theBounds,
			Vector theEncode)
	{
		super(theNumber);
		
		this.functionType = 3; //dang well better be 3;
				
		this.functions = theFunctions;
		this.bounds = theBounds;
		this.encode = theEncode;
		this.domain = theDomain;
		this.range =  theRange;

	}
	
	/**
	 * create an complete Function object of Type 4, a postscript calculator function.
	 * 
	 * Use null for an optional object parameter if you choose not to use it.
	 * For optional int parameters, pass the default.
	 * 
	 * @param theDomain Vector object of Double objects.
	 * This is the domain of the function.
	 * See page 264 of the PDF 1.3 Spec.
	 * @param theRange Vector object of Double objects.
	 * This is the Range of the function.
	 * See page 264 of the PDF 1.3 Spec.
	 * @param theFunctionDataStream This is a stream of arithmetic, boolean, and stack operators and boolean constants.
	 * I end up enclosing it in the '{' and '}' braces for you, so don't do it
	 * yourself.
	 * 
	 * This attribute is required.
	 * It's described on page 269 of the PDF 1.3 spec.
	 * @param theNumber The object number of this PDF object.
	 * @param theFunctionType The type of function which should be 4, as this is
	 * a Postscript calculator function
	 */
	public PDFFunction(int theNumber, int theFunctionType,
			Vector theDomain, Vector theRange,
			StringBuffer theFunctionDataStream)
	{
		super(theNumber);
		
		this.functionType = 4; //dang well better be 4;
		this.functionDataStream = theFunctionDataStream;

		this.domain =  theDomain;
		
		this.range = theRange;

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
		int numberOfFunctions=0;
		int tempInt=0;
		StringBuffer p = new StringBuffer();
		p.append(this.number + " " +this.generation 
			+ " obj\n<< \n/FunctionType "+this.functionType+" \n");

		//FunctionType 0
		if(this.functionType == 0)
		{
			if(this.domain != null)
			{
				//DOMAIN
				p.append("/Domain [ ");
				vectorSize = this.domain.size();
				for(tempInt=0; tempInt < vectorSize; tempInt++)
				{
					p.append(pdfNumber.doubleOut(
						(Double)this.domain.elementAt(tempInt))
						+" ");
				}
				
				p.append("] \n");
			}			
			else
			{
				p.append("/Domain [ 0 1 ] \n");
			}

			//SIZE
			if(this.size != null)
			{
				p.append("/Size [ ");
				vectorSize = this.size.size();
				for(tempInt=0; tempInt < vectorSize; tempInt++)
				{
					p.append(pdfNumber.doubleOut(
					(Double)this.size.elementAt(tempInt)) +" ");
				}
				p.append("] \n");
			}
			//ENCODE
			if(this.encode != null)
			{
				p.append("/Encode [ ");
				vectorSize = this.encode.size();
				for(tempInt=0; tempInt < vectorSize; tempInt++)
				{
					p.append(pdfNumber.doubleOut(
					(Double)this.encode.elementAt(tempInt)) +" ");
				}
				p.append("] \n");
			}
			else
			{
				p.append("/Encode [ ");
				vectorSize = this.functions.size();
				for(tempInt=0; tempInt < vectorSize; tempInt++)
				{
					p.append("0 1 ");
				}
				p.append("] \n");
				
			}
			
			//BITSPERSAMPLE
			p.append("/BitsPerSample "+this.bitsPerSample);
			
			//ORDER (optional)
			if(this.order ==1 || this.order == 3)
			{
				p.append(" \n/Order "+this.order+" \n");
			}
			
			//RANGE
			if(this.range != null)
			{
				p.append("/Range [ ");
				vectorSize = this.range.size();
				for(tempInt=0; tempInt < vectorSize; tempInt++)
				{
					p.append(pdfNumber.doubleOut(
					(Double)this.range.elementAt(tempInt)) +" ");
				}
				
				p.append("] \n");
			}
			
			//DECODE
			if(this.decode != null)
			{
				p.append("/Decode [ ");
				vectorSize = this.decode.size();
				for(tempInt=0; tempInt < vectorSize; tempInt++)
				{
					p.append(pdfNumber.doubleOut(
					(Double)this.decode.elementAt(tempInt)) +" ");
				}
				
				p.append("] \n");
			}
			
			//LENGTH		
			if(this.functionDataStream != null)
			{
				p.append("/Length "+(this.functionDataStream.length()+1)
					+ " \n");
			}
			
			//FILTER?
			if (this.filter != null)
			{//if there's a filter
				vectorSize= this.filter.size();
				p.append("/Filter ");
				if (vectorSize == 1)
				{
					p.append("/"+((String)this.filter.elementAt(0))+" \n");
				}
				else
				{
					p.append("[ ");
					for(tempInt=0; tempInt <vectorSize; tempInt++)
					{
						p.append("/"+((String)this.filter.elementAt(0))+" ");
					}
					p.append("] \n");
				}
			}
			p.append(">> \n");
			
			//stream representing the function
			if(this.functionDataStream != null)
			{
				p.append("stream\n"+this.functionDataStream +"\nendstream\n");
			}
			
			p.append("endobj\n");
			
		}//end of if FunctionType 0
		else if(this.functionType == 2)
		{
			//DOMAIN
			if(this.domain != null)
			{
				p.append("/Domain [ ");
				vectorSize = this.domain.size();
				for(tempInt=0; tempInt < vectorSize; tempInt++)
				{
					p.append(pdfNumber.doubleOut(
					(Double)this.domain.elementAt(tempInt)) +" ");
				}
				
				p.append("] \n");
			}
			else
			{
				p.append("/Domain [ 0 1 ] \n");
			}

			
			//RANGE
			if(this.range != null)
			{
				p.append("/Range [ ");
				vectorSize = this.range.size();
				for(tempInt=0; tempInt < vectorSize; tempInt++)
				{
					p.append(pdfNumber.doubleOut(
					(Double)this.range.elementAt(tempInt)) +" ");
				}
				
				p.append("] \n");
			}
			
			//FunctionType, C0, C1, N are required in PDF
			
			//C0
			if(this.cZero != null)
			{
				p.append("/C0 [ ");
				vectorSize = this.cZero.size();
				for(tempInt = 0; tempInt < vectorSize; tempInt++)
				{
					p.append(pdfNumber.doubleOut(
					(Double)this.cZero.elementAt(tempInt))+" ");
				}
				p.append("] \n");				
			}
			
			//C1
			if(this.cOne != null)
			{
				p.append("/C1 [ ");
				vectorSize = this.cOne.size();
				for(tempInt = 0; tempInt < vectorSize; tempInt++)
				{
					p.append(pdfNumber.doubleOut(
					(Double)this.cOne.elementAt(tempInt))+" ");
				}
				p.append("] \n");
			}
			
			//N: The interpolation Exponent
			p.append("/N "
				+pdfNumber.doubleOut(
					new Double(this.interpolationExponentN))
				+" \n");
			
			p.append(">> \nendobj\n");

		}
		else if(this.functionType == 3)
		{//fix this up when my eyes uncross
			//DOMAIN
			if(this.domain != null)
			{
				p.append("/Domain [ ");
				vectorSize = this.domain.size();
				for(tempInt=0; tempInt < vectorSize; tempInt++)
				{
					p.append(pdfNumber.doubleOut(
					(Double)this.domain.elementAt(tempInt)) +" ");
				}
				p.append("] \n");
			}
			else
			{
				p.append("/Domain [ 0 1 ] \n");
			}

			//RANGE
			if(this.range != null)
			{
				p.append("/Range [ ");
				vectorSize = this.range.size();
				for(tempInt=0; tempInt < vectorSize; tempInt++)
				{
					p.append(pdfNumber.doubleOut(
					(Double)this.range.elementAt(tempInt)) +" ");
				}
				
				p.append("] \n");
			}
		
			//FUNCTIONS
			if(this.functions != null)
			{
				p.append("/Functions [ ");
				numberOfFunctions = this.functions.size();
				for(tempInt =0;tempInt < numberOfFunctions; tempInt++)
				{
					p.append( ((PDFFunction)this.functions.elementAt(tempInt)).referencePDF()+" ");
					
				}
				p.append("] \n");
			}
			
			
			//ENCODE
			if(this.encode != null)
			{
				p.append("/Encode [ ");
				vectorSize = this.encode.size();
				for(tempInt=0; tempInt < vectorSize; tempInt++)
				{
					p.append(pdfNumber.doubleOut(
					(Double)this.encode.elementAt(tempInt)) +" ");
				}
								
				p.append("] \n");
			}
			else
			{
				p.append("/Encode [ ");
				vectorSize = this.functions.size();
				for(tempInt=0; tempInt < vectorSize; tempInt++)
				{
					p.append("0 1 ");
				}
				p.append("] \n");
				
			}
			
			
			//BOUNDS, required, but can be empty
			p.append("/Bounds [ ");
			if(this.bounds != null)
			{
				
				vectorSize= this.bounds.size();
				for(tempInt = 0; tempInt < vectorSize; tempInt++)
				{
					p.append(pdfNumber.doubleOut(
					(Double)this.bounds.elementAt(tempInt))+" ");
				}
			
			}
			else
			{
				if(this.functions != null)
				{ 
					//if there are n functions,
					// there must be n-1 bounds.
					// so let each function handle an equal portion
					// of the whole. e.g. if there are 4, then [ 0.25 0.25 0.25 ]
					
					String functionsFraction = 
						pdfNumber.doubleOut(new Double(
							1.0 / ((double)numberOfFunctions)));
	
					for(tempInt =0;tempInt+1 < numberOfFunctions; tempInt++)
					{
						
						p.append( functionsFraction + " ");
					}
					functionsFraction = null; //clean reference.
					
				}
				
			}
			p.append("] \n");
			
			
			p.append(">> \nendobj\n");
		}
		else if(this.functionType == 4)
		{//fix this up when my eyes uncross
			//DOMAIN
			if(this.domain != null)
			{
				p.append("/Domain [ ");
				vectorSize = this.domain.size();
				for(tempInt=0; tempInt < vectorSize; tempInt++)
				{
					p.append(pdfNumber.doubleOut(
					(Double)this.domain.elementAt(tempInt)) +" ");
				}
				
				p.append("] \n");
			}
			else
			{
				p.append("/Domain [ 0 1 ] \n");
			}

			//RANGE
			if(this.range != null)
			{
				p.append("/Range [ ");
				vectorSize = this.range.size();
				for(tempInt=0; tempInt < vectorSize; tempInt++)
				{
					p.append(pdfNumber.doubleOut(
					(Double)this.range.elementAt(tempInt)) +" ");
				}
				
				p.append("] \n");
			}
			
			//LENGTH		
			if(this.functionDataStream != null)
			{
				p.append("/Length "+(this.functionDataStream.length()+1)
					+ " \n");
			}
			
			p.append(">> \n");
			
			//stream representing the function
			if(this.functionDataStream != null)
			{
				p.append("stream\n{ "+this.functionDataStream +" } \nendstream\n");
			}
			
			p.append("endobj\n");
			
		}
		
		return (p.toString().getBytes());
		
	}
}
