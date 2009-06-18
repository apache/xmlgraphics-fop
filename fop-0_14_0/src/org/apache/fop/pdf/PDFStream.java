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
package org.apache.fop.pdf;

/**
 * class representing a PDF stream.
 * 
 * A derivative of the PDF Object, a PDF Stream has not only a dictionary
 * but a stream of PDF commands. The stream of commands is where the real
 * work is done, the dictionary just provides information like the stream
 * length.
 */
public class PDFStream extends PDFObject {

    /** the stream of PDF commands */
    protected StringBuffer data = new StringBuffer();

    /**
     * create an empty stream object
     *
     * @param number the object's number
     */
    public PDFStream(int number) {
	super(number);
    }

    /**
     * append data to the stream
     *
     * @param s the string of PDF to add
     */
    public void add(String s) {
	this.data = this.data.append(s);
    }

    /**
     * append an array of xRGB pixels, ASCII Hex Encoding it first
     *
     * @param pixels the area of pixels
     * @param width the width of the image in pixels
     * @param height the height of the image in pixels
     */
    public void addImageArray(int[] pixels, int width, int height) {
	for (int i = 0; i < height; i++) {
	    for (int j = 0; j < width; j++) {
		int p = pixels[i * width + j];
		int r = (p >> 16) & 0xFF;
		int g = (p >>  8) & 0xFF;
		int b = (p      ) & 0xFF;
		if (r < 16) {
		    this.data = this.data.append(0);
		}
		this.data = this.data.append(Integer.toHexString(r));
		if (g < 16) {
		    this.data = this.data.append(0);
		}
		this.data = this.data.append(Integer.toHexString(g));
		if (b < 16) {
		    this.data = this.data.append(0);
		}
		this.data = this.data.append(Integer.toHexString(b));
		this.data = this.data.append(" ");
	    }
	}
	this.data = this.data.append(">\n");
    }

    /**
     * represent as PDF.
     *
     * @return the PDF string.
     */ 
    public String toPDF() {
	String p = this.number + " " + this.generation
	    + " obj\n<< /Length " + (this.data.length()+1)
	    + " >>\nstream\n" + this.data + "\nendstream\nendobj\n";
	return p;
    }
}
