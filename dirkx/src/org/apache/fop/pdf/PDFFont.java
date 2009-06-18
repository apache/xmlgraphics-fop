package org.apache.xml.fop.pdf;

// Java
import java.io.IOException;
import java.io.PrintWriter;

/**
 * class representing a /Font object.
 *
 * A small object expressing the base font name and encoding of a
 * Type 1 font along with an internal name for the font used within
 * streams of content
 */
public class PDFFont extends PDFObject {

    /** the internal name for the font (eg "F1") */
    protected String fontname;

    /** the base font name (eg "Helvetica") */
    protected String basefont;

    /** the character encoding scheme used by the font (eg
	"WinAnsiEncoding") */
    protected String encoding;

    /**
     * create the /Font object
     *
     * @param the object's number
     * @param fontname the internal name for the font
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     */
    public PDFFont(int number, String fontname, String basefont,
		   String encoding) {

	/* generic creation of PDF object */
	super(number);

	/* set fields using paramaters */
	this.fontname = fontname;
	this.basefont = basefont;
	this.encoding = encoding;
    }

    /**
     * get the internal name used for this font
     *
     * @return the internal name
     */
    public String getName() {
	return this.fontname;
    }

    /**
     * produce the PDF representation for the object
     *
     * @return the PDF
     */
    public String toPDF() {
	String p = this.number + " " + this.generation
	    + " obj\n<< /Type /Font\n/Subtype /Type1\n/Name /"
	    + this.fontname + "\n/BaseFont /" + this.basefont
	    + "\n/Encoding /"+ this.encoding + " >>\nendobj\n";
	return p;
    }
}
