package org.apache.xml.fop.pdf;

// Java
import java.io.PrintWriter;
import java.util.Vector;

/**
 * class representing a /Page object.
 *
 * There is one of these for every page in a PDF document. The object
 * specifies the dimensions of the page and references a /Resources
 * object, a contents stream and the page's parent in the page
 * hierarchy.
 */
public class PDFPage extends PDFObject {

    /** the page's parent, a /Pages object */
    protected PDFPages parent;

    /** the page's /Resource object */
    protected PDFResources resources;

    /** the contents stream */
    protected PDFStream contents;

    /** the width of the page in points */
    protected int pagewidth;

    /** the height of the page in points */
    protected int pageheight;

    /**
     * create a /Page object
     *
     * @param number the object's number
     * @param resources the /Resources object
     * @param contents the content stream
     * @param pagewidth the page's width in points
     * @param pageheight the page's height in points
     */
    public PDFPage(int number, PDFResources resources,
		   PDFStream contents, int pagewidth,
		   int pageheight) {

	/* generic creation of object */
	super(number);

	/* set fields using parameters */
	this.resources = resources;
	this.contents = contents;
	this.pagewidth = pagewidth;
	this.pageheight = pageheight;
    }

    /**
     * set this page's parent
     *
     * @param parent the /Pages object that is this page's parent
     */
    public void setParent(PDFPages parent) {
	this.parent = parent;
    }

    /**
     * represent this object as PDF
     *
     * @return the PDF string
     */
    public String toPDF() {
	String p = this.number + " " + this.generation
	    + " obj\n<< /Type /Page\n/Parent "
	    + this.parent.referencePDF() + "\n/MediaBox [ 0 0 "
	    + this.pagewidth + " " + this.pageheight + " ]\n/Resources "
	    + this.resources.referencePDF() + "\n/Contents "
	    + this.contents.referencePDF() + " >>\nendobj\n";
	return p;
    }
}
