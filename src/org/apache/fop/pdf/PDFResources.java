package org.apache.xml.fop.pdf;

// Java
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;

/**
 * class representing a /Resources object.
 *
 * /Resources object contain a list of references to the fonts for the
 * document
 */ 
public class PDFResources extends PDFObject {

    /** /Font objects keyed by their internal name */
    protected Hashtable fonts = new Hashtable();

    protected Vector xObjects;

    /**
     * create a /Resources object.
     *
     * @param number the object's number
     */
    public PDFResources(int number) {

	/* generic creation of object */
	super(number);
    }

    /**
     * add font object to resources list.
     *
     * @param font the PDFFont to add
     */
    public void addFont(PDFFont font) {
	this.fonts.put(font.getName(),font);
    }

    public void setXObjects(Vector xObjects) {
	this.xObjects = xObjects;
    }

    /**
     * represent the object in PDF
     *
     * @return the PDF
     */
    public String toPDF() {
	StringBuffer p = new StringBuffer(this.number + " "
					  + this.generation
					  + " obj\n<< /Font << ");

	/* construct PDF dictionary of font object references */
	Enumeration fontEnumeration = fonts.keys();
	while (fontEnumeration.hasMoreElements()) {
	    String fontName = (String) fontEnumeration.nextElement();
	    p = p.append("/" + fontName + " " 
			 + ((PDFFont) fonts.get(fontName)).referencePDF()
			 + "\n");  
	}

	p = p.append(">>\n/ProcSet [ /PDF /ImageC /Text ] ");

	if (!this.xObjects.isEmpty()) {
	    p = p.append("/XObject <<");
	    for (int i = 1; i < this.xObjects.size(); i++) {
		p = p.append("/Im" + i + " " +
			     ((PDFXObject)
			      this.xObjects.elementAt(i -
						      1)).referencePDF()
			     +
			     " \n");
	    }
	}

	p = p.append(">>\nendobj\n");

	return p.toString();
    }
}
