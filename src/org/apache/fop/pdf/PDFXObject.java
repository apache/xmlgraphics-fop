/* modified by JKT to integrate with 0.12.0 */

package org.apache.xml.fop.pdf;

import java.io.IOException;
import java.io.PrintWriter;

// shouldn't have to do this
import org.apache.xml.fop.image.*;

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
