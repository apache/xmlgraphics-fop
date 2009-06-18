/*

 ============================================================================
						 The Apache Software License, Version 1.1
 ============================================================================
 
	 Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of	source code must	retain the above copyright	notice,
	 this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
	 this list of conditions and the following disclaimer in the documentation
	 and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
	 include  the following  acknowledgment:	"This product includes	software
	 developed	by the	Apache Software Foundation	(http://www.apache.org/)."
	 Alternately, this	acknowledgment may	appear in the software itself,	if
	 and wherever such third-party acknowledgments normally appear.
 
 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
	 endorse  or promote  products derived	from this	software without	prior
	 written permission. For written permission, please contact
	 apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
	 "Apache" appear	in their name,	without prior written permission  of the
	 Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR	PURPOSE ARE  DISCLAIMED.	IN NO  EVENT SHALL	THE
 APACHE SOFTWARE	FOUNDATION	OR ITS CONTRIBUTORS	BE LIABLE FOR	ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,	EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT	OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)	HOWEVER CAUSED AND ON
 ANY	THEORY OF LIABILITY,  WHETHER  IN CONTRACT,	STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR	OTHERWISE) ARISING IN	ANY WAY OUT OF THE	USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software	consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software	Foundation and was	originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 
*/

//Author:       Eric SCHAEFFER
//Description:  represent a PDF filter object

package org.apache.fop.pdf;

public class PDFFilter {
	public static int ASCII_HEX_DECODE = 1;
	public static int ASCII_85_DECODE = 2;
	public static int LZW_DECODE = 3;
	public static int RUN_LENGTH_DECODE = 4;
	public static int CCITT_FAX_DECODE = 5;
	public static int DCT_DECODE = 6;
	public static int FLATE_DECODE = 7;

	// Filter type
	private int m_filterType;

	// Properties

	// LZW - Flat
	private Integer m_predictor = null;
	// LZW - Flat - CCITT
	private Integer m_columns = null;
	// LZW - Flat
	private Integer m_colors = null;
	// LZW - Flat
	private Integer m_bitsPerComponent = null;
	// LZW
	private Integer m_earlyChange = null;
	// CCITT
	private Integer m_k = null;
	// CCITT
	private Boolean m_endOfLine = null;
	// CCITT
	private Boolean m_encodedByteAlign = null;
	// CCITT
	private Integer m_rows = null;
	// CCITT
	private Boolean m_endOfBlock = null;
	// CCITT
	private Boolean m_blackls1 = null;
	// CCITT
	private Integer m_damagedRowsBeforeError = null;

	public PDFFilter(int filter) throws PDFFilterException {
		if (	(filter != ASCII_HEX_DECODE) &&
				(filter != ASCII_85_DECODE) &&
				(filter != LZW_DECODE) &&
				(filter != RUN_LENGTH_DECODE) &&
				(filter != CCITT_FAX_DECODE) &&
				(filter != DCT_DECODE) &&
				(filter != FLATE_DECODE)
				) {
			throw new PDFFilterException("Filter type not supported");
		}
		this.m_filterType = filter;
	}

	public int getType() {
		return this.m_filterType;
	}

	public void setPredictor(Integer value) throws PDFFilterException {
		if ((this.m_filterType != LZW_DECODE) && (this.m_filterType != FLATE_DECODE)) {
			throw new PDFFilterException("No Predictor property for this filter");
		}

		this.m_predictor = value;
	}

	public Integer getPredictor() throws PDFFilterException {
		if ((this.m_filterType != LZW_DECODE) && (this.m_filterType != FLATE_DECODE)) {
			throw new PDFFilterException("No Predictor property for this filter");
		}

		return this.m_predictor;
	}

// ... etc ... 

	public String toPDF() {
		String pdf = null;
/*
	public static int DCT_DECODE = 6;
*/
		if (this.m_filterType == ASCII_HEX_DECODE) {
			pdf = "/ASCIIHexDecode";
		} else if (this.m_filterType == ASCII_85_DECODE) {
			pdf = "/ASCI85Decode";
		} else if (this.m_filterType == LZW_DECODE) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("/LZWDecode");

			if (this.m_predictor != null) {
				buffer.append(" /Predictor ");
				buffer.append(this.m_predictor);
			}
			if (this.m_columns != null) {
				buffer.append(" /Columns ");
				buffer.append(this.m_columns);
			}
			if (this.m_colors != null) {
				buffer.append(" /Colors ");
				buffer.append(this.m_colors);
			}
			if (this.m_bitsPerComponent != null) {
				buffer.append(" /BitsPerComponent ");
				buffer.append(this.m_bitsPerComponent);
			}
			if (this.m_earlyChange != null) {
				buffer.append(" /EarlyChange ");
				buffer.append(this.m_earlyChange);
			}

			pdf = buffer.toString();
		} else if (this.m_filterType == FLATE_DECODE) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("/FlateDecode");

			if (this.m_predictor != null) {
				buffer.append(" /Predictor ");
				buffer.append(this.m_predictor);
			}
			if (this.m_columns != null) {
				buffer.append(" /Columns ");
				buffer.append(this.m_columns);
			}
			if (this.m_colors != null) {
				buffer.append(" /Colors ");
				buffer.append(this.m_colors);
			}
			if (this.m_bitsPerComponent != null) {
				buffer.append(" /BitsPerComponent ");
				buffer.append(this.m_bitsPerComponent);
			}

			pdf = buffer.toString();
		} else if (this.m_filterType == RUN_LENGTH_DECODE) {
			pdf = "/RunLengthDecode";
		} else if (this.m_filterType == CCITT_FAX_DECODE) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("/CCITTFaxDecode");

			if (this.m_k != null) {
				buffer.append(" /K ");
				buffer.append(this.m_k);
			}
			if (this.m_endOfLine != null) {
				buffer.append(" /EndOfLine ");
				buffer.append(this.m_endOfLine);
			}
			if (this.m_encodedByteAlign != null) {
				buffer.append(" /EncodedByteAlign ");
				buffer.append(this.m_encodedByteAlign);
			}
			if (this.m_columns != null) {
				buffer.append(" /Columns ");
				buffer.append(this.m_columns);
			}
			if (this.m_rows != null) {
				buffer.append(" /Rows ");
				buffer.append(this.m_rows);
			}
			if (this.m_endOfBlock != null) {
				buffer.append(" /EndOfBlock ");
				buffer.append(this.m_endOfBlock);
			}
			if (this.m_blackls1 != null) {
				buffer.append(" /Blackls1 ");
				buffer.append(this.m_blackls1);
			}
			if (this.m_damagedRowsBeforeError != null) {
				buffer.append(" /DamagedRowsBeforeError ");
				buffer.append(this.m_damagedRowsBeforeError);
			}

			pdf = buffer.toString();
		} else if (this.m_filterType == DCT_DECODE) {
			pdf = "/DCTDecode";
		}

		return pdf;
	}

}
