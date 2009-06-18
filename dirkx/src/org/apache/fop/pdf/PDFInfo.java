package org.apache.xml.fop.pdf;

// Java
import java.io.IOException;
import java.io.PrintWriter;

/**
 * class representing an /Info object
 */
public class PDFInfo extends PDFObject {

    /** the application producing the PDF */
    protected String producer;

    /**
     * create an Info object
     *
     * @param number the object's number
     */
    public PDFInfo(int number) {
	super(number);
    }

    /**
     * set the producer string
     *
     * @param producer the producer string
     */
    public void setProducer(String producer) {
	this.producer = producer;
    }

    /**
     * produce the PDF representation of the object
     *
     * @return the PDF
     */
    public String toPDF() {
	String p = this.number + " " + this.generation
	    + " obj\n<< /Type /Info\n/Producer (" + this.producer
	    + ") >>\nendobj\n";
	return p;
    }
}
