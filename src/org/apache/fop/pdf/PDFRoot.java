package org.apache.xml.fop.pdf;

// Java
import java.io.IOException;
import java.io.PrintWriter;

/**
 * class representing a Root (/Catalog) object
 */
public class PDFRoot extends PDFObject {

    /** the /Pages object that is root of the Pages hierarchy */
    protected PDFPages rootPages;

    /**
     * create a Root (/Catalog) object
     *
     * @param number the object's number
     */
    public PDFRoot(int number) {
	super(number);
    }

    /**
     * add a /Page object to the root /Pages object
     *
     * @param page the /Page object to add
     */
    public void addPage(PDFPage page) {
	this.rootPages.addPage(page);
    }

    /**
     * set the root /Pages object
     *
     * @param pages the /Pages object to set as root
     */
    public void setRootPages(PDFPages pages) {
	this.rootPages = pages;
    }

    /**
     * represent the object as PDF
     *
     * @return the PDF string
     */
    public String toPDF() {
	String p = this.number + " " + this.generation
	    + " obj\n<< /Type /Catalog\n/Pages " 
	    + this.rootPages.referencePDF() + " >>\nendobj\n";
	return p;
    }
}
