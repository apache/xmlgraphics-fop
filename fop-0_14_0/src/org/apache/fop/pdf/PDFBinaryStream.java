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
//Description:  Encode a binary stream using PDF filters

package org.apache.fop.pdf;

import org.apache.fop.datatypes.ColorSpace;

// Java
import java.util.Vector;
import java.lang.reflect.Array;

import java.io.*;

// compression
import java.util.zip.Deflater;

public class PDFBinaryStream {

	private byte[] m_data = null;
	private int m_dataSize = 0;
	private Vector m_filters = null;

	public PDFBinaryStream() {
		m_filters = new Vector();
	}

	public void setData(byte[] data) {
		this.m_data = data;
		this.m_dataSize = java.lang.reflect.Array.getLength(this.m_data);
	}

	public void encode(PDFFilter filter) throws PDFFilterException {
		if (this.m_data == null) throw new PDFFilterException("no data to encode");

		int filterType = filter.getType();
		if (filterType == PDFFilter.FLATE_DECODE) {
			Deflater compressor = new Deflater();

			// PDFFilter properties ?
			compressor.setLevel(Deflater.DEFAULT_COMPRESSION);
			compressor.setStrategy(Deflater.DEFAULT_STRATEGY);

			compressor.setInput(m_data);
			compressor.finish();
			byte[] compMap = new byte[this.m_dataSize];
			int compSize = compressor.deflate(compMap);
			compressor.end();

			this.m_data = new byte[compSize];
			for (int i = 0; i < compSize; i++) {
				this.m_data[i] = compMap[i];
			}
			this.m_dataSize = compSize;
//			this.m_data = compMap;
//			this.m_dataSize = java.lang.reflect.Array.getLength(this.m_data);
//		} else if (filterType == PDFFilter.LZW_DECODE) {
		} else if (filterType == PDFFilter.ASCII_HEX_DECODE) {
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < this.m_dataSize; i++) {
				int val = (int) (this.m_data[i] & 0xFF);
				if (val < 16) buffer.append("0");
				buffer.append(Integer.toHexString(val));
//// TEST ////
/*
buffer.append(" ");
if (i % 75 == 0) buffer.append("\n");
*/
//// TEST ////
			}
			this.m_data = buffer.toString().getBytes();
			this.m_dataSize = java.lang.reflect.Array.getLength(this.m_data);
		} else {
			throw new PDFFilterException("filter not supported");
		}

		this.m_filters.add(filter);
	}

	public String toPDF() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<<\n/Length ");
		buffer.append(this.m_dataSize);
		buffer.append("\n");
		buffer.append("/Filter [");
		for (int i = this.m_filters.size(); i > 0; i--) {
			PDFFilter filter = (PDFFilter) this.m_filters.get(i - 1);
			buffer.append(filter.toPDF());
			if (i > 1) buffer.append(" ");
		}
		buffer.append("]\n");
		buffer.append(">>\n");

		buffer.append("stream\n");
		buffer.append(this.m_data);
		buffer.append("\nendstream\n");

		return buffer.toString();
	}

	public String getPDFDictionary() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("/Length ");
//// TEST ////
		buffer.append(this.m_dataSize);
//		buffer.append(this.m_dataSize + 1);
//// TEST ////
		buffer.append("\n");
		if (this.m_filters.size() > 0) {
			buffer.append("/Filter [");
			for (int i = this.m_filters.size(); i > 0; i--) {
				PDFFilter filter = (PDFFilter) this.m_filters.get(i - 1);
				buffer.append(filter.toPDF());
				if (i > 1) buffer.append(" ");
			}
			buffer.append("]\n");
		}

		return buffer.toString();
	}

	public String getPDFStream() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("stream\n");
		buffer.append(this.m_data);
//// TEST ////
//		buffer.append(">");
//// TEST ////
		buffer.append("\nendstream\n");

		return buffer.toString();
	}

	public int outputPDFStream(PrintWriter writer) throws IOException {
		int length = 0;
		String p;

		p = new String("stream\n");
		writer.write(p);
		length += p.length();

/*
		for (int i = 0; i < this.m_dataSize; i++) {
			writer.write(Byte.toString(this.m_data[i]));
		}
*/
		writer.write(new String(this.m_data));
		length += this.m_dataSize;
//// TEST ////
//		writer.write(">");
//		length += (new String(">")).length();
//// TEST ////

		p = new String("\nendstream\n");
		writer.write(p);
		length += p.length();

		return length;
	}

}
