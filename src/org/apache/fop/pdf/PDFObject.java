package org.apache.xml.fop.pdf;

// Java
import java.io.IOException;
import java.io.PrintWriter;

/**
 * generic PDF object.
 * 
 * A PDF Document is essentially a collection of these objects. A PDF
 * Object has a number and a generation (although the generation will always
 * be 0 in new documents).
 */
public abstract class PDFObject {

    /** the object's number */
    protected int number;

    /** the object's generation (0 in new documents) */
    protected int generation = 0;

    /**
     * create an empty object
     *
     * @param number the object's number
     */
    public PDFObject(int number) {
	this.number = number;
    }

    /**
     * write the PDF represention of this object
     *
     * @param writer the PrintWriter to write the PDF to
     * @return the number of characters written
     */
    protected int output(PrintWriter writer) throws IOException {
	String pdf = this.toPDF();
	writer.write(pdf);
	return pdf.length();
    }

    /**
     * the PDF representation of a reference to this object
     *
     * @return the reference string
     */
    protected String referencePDF() {
	String p = this.number + " " + this.generation + " R";
	return p;
    }

    /**
     * represent object as PDF
     *
     * @return PDF string
     */
    abstract String toPDF();
}
