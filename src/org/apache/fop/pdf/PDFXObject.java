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
/* modified by Eric SCHAEFFER to integrate with 0.13.0 */

package org.apache.fop.pdf;

// Java
import java.io.IOException;
import org.apache.fop.messaging.MessageHandler;
import java.io.OutputStream;

// FOP
import org.apache.fop.datatypes.ColorSpace;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.FopImageException;

/**
 * PDF XObject
 *
 * A derivative of the PDF Object, is a PDF Stream that has not only a
 * dictionary but a stream of image data.
 * the dictionary just provides information like the stream length
 */
public class PDFXObject extends PDFObject {

    FopImage fopimage;
    int Xnum;


    /**
     * create an Xobject with the given number and name and load the
     * image in the object 
     */
    public PDFXObject(int number,int Xnumber,FopImage img) {
	super(number);
	this.Xnum=Xnumber;
	if (img == null)
	    MessageHandler.errorln("FISH");
	fopimage=img;
    }

	/**
	 * @return the PDF XObject number
	 */
	public int getXNumber() {
		return this.Xnum;
	}

    /**
     * represent as PDF
     */
    protected int output(OutputStream stream) throws IOException {
	int length=0;
	int i=0;
	int x,y;

	try {
	    // delegate the stream work to PDFStream
	    PDFStream imgStream = new PDFStream(0);
	    
		imgStream.setData(fopimage.getBitmaps());
		//	imgStream.addFilter(new FlateFilter());
		imgStream.addDefaultFilters();
		
		String dictEntries = imgStream.applyFilters();

		String p = this.number + " " + this.generation + " obj\n";
		p = p + "<</Type /XObject\n";
		p = p + "/Subtype /Image\n";
		p = p + "/Name /Im" + Xnum + "\n";
		p = p + "/Length " + imgStream.getDataLength();
		p = p + "/Width " + fopimage.getWidth() + "\n";
		p = p + "/Height " + fopimage.getHeight() + "\n";
		p = p + "/BitsPerComponent " + fopimage.getBitsPerPixel() + "\n";
		ColorSpace cs = fopimage.getColorSpace();
		p = p + "/ColorSpace /" + cs.getColorSpacePDFString() + "\n";
		if (fopimage.isTransparent()) {
			PDFColor transp = fopimage.getTransparentColor();
			p = p + "/Mask [" + transp.red255() + " " + transp.red255() + " " + transp.green255() + " " + transp.green255() + " " + transp.blue255() + " " + transp.blue255() + "]\n";
		}
		p = p + dictEntries;
		p = p + ">>\n";

		// don't know if it's the good place (other objects can have references to it)
		fopimage.close();

		// push the pdf dictionary on the writer
		byte[] pdfBytes = p.getBytes();
		stream.write(pdfBytes);
		length += pdfBytes.length;
		// push all the image data on  the writer and takes care of length for trailer
		length += imgStream.outputStreamData(stream);

		pdfBytes = ("endobj\n").getBytes();
		stream.write(pdfBytes);
		length += pdfBytes.length;
	} catch (FopImageException imgex) {
	    MessageHandler.errorln("Error in XObject : " + imgex.getMessage());
	}

	return length;
    }
    
    byte[] toPDF() {
/* Not used any more
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
*/
	return null;
    }
}
