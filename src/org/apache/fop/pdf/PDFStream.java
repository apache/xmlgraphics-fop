package org.apache.xml.fop.pdf;

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
