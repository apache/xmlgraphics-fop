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
/* modified by JKT to integrate with 0.12.0 */

package org.apache.fop.pdf;

import java.io.IOException;
import java.io.PrintWriter;

// shouldn't have to do this
import org.apache.fop.image.*;

/**
 * PDF XObject
 *
 * A derivative of the PDF Object, is a PDF Stream that has not only a
 * dictionary but a stream of image data.
 * the dictionary just provides information like the stream length
 */
public class PDFXObject extends PDFObject {

    FopImage fopimage;
    int[] map;
    int Xnum;


    /**
     * create an Xobject with the given number and name and load the
     * image in the object 
     */
    public PDFXObject(int number,int Xnumber,FopImage img) {
	super(number);
	this.Xnum=Xnumber;
	if (img == null)
	    System.err.println("FISH");
	this.map = img.getimagemap();
	fopimage=img;
    }

    /**
     * represent as PDF
     */
    protected int output(PrintWriter writer) throws IOException {
	int length=0;
	int i=0;
	int x,y;
	int ncc=(fopimage.getcolor()? 3 : 1);//Number of Color Channels
	int size=(fopimage.getpixelwidth())*(fopimage.getpixelheight()*ncc);
	String p;
	String pdf = this.toPDF();
	// push the pdf dictionary on the writer
	writer.write(pdf);
	length +=pdf.length();
	p = (size*2+1) + " >>\n";
	p = p + "stream\n";
	writer.write(p);
	length +=p.length();
	// push all the image data on  the writer and takes care of length for trailer
	for (y=fopimage.getpixelheight()-1;y>=0;y--)
	    {
		for (x=0;x<fopimage.getpixelwidth()*ncc;x++)
		    {
			i=y*fopimage.getpixelwidth()*ncc+x;
			if (this.map[i]<16)
			    {
				writer.write("0");                         
				writer.write(Integer.toHexString(this.map[i]));
				length++;
				length++;
			    }else
				{
				    writer.write(Integer.toHexString(this.map[i]));
				    length++;
				    length++;
				}
		    }
	    }
	// close the object
	p = ">";
	p += "\nendstream\nendobj\n";
	writer.write(p);
	length +=p.length();
	return length;
    }
    
    String toPDF() {
	String p = this.number + " " + this.generation + " obj\n";
	p = p + "<</Type /XObject\n";
	p = p + "/Subtype /Image\n";
	p = p + "/Name /Im"+Xnum+"\n";
	p = p + "/Width "+fopimage.getpixelwidth()+"\n";
	p = p + "/Height "+fopimage.getpixelheight()+"\n";
	p = p + "/BitsPerComponent 8\n";
	if (fopimage.getcolor())
	    p = p + "/ColorSpace /DeviceRGB\n";
	else
	    p = p + "/ColorSpace /DeviceGray\n";
	p = p + "/Filter /ASCIIHexDecode\n";
	p = p + "/Length ";
	return p;
    }
}
