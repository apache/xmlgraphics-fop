/* image support modified from work of BoBoGi */

package org.apache.xml.fop.pdf;

// images are the one place that FOP classes outside this package get
// referenced and I'd rather not do it
import org.apache.xml.fop.image.FopImage;

// Java
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;
							   
/**
 * class representing a PDF document. 
 *
 * The document is built up by calling various methods and then finally
 * output to given filehandle using output method.
 *
 * A PDF document consists of a series of numbered objects preceded by a
 * header and followed by an xref table and trailer. The xref table
 * allows for quick access to objects by listing their character
 * positions within the document. For this reason the PDF document must
 * keep track of the character position of each object.  The document
 * also keeps direct track of the /Root, /Info and /Resources objects.
 */
public class PDFDocument {

    /** the version of PDF supported */
    protected static final String pdfVersion = "1.2";

    /** the current character position */
    protected int position = 0;

    /** the character position of each object */
    protected Vector location = new Vector();

    /** the counter for object numbering */
    protected int objectcount = 0;

    /** the objects themselves */
    protected Vector objects = new Vector();

    /** character position of xref table */
    protected int xref;

    /** the /Root object */
    protected PDFRoot root;

    /** the /Info object */
    protected PDFInfo info;

    /** the /Resources object */
    protected PDFResources resources;
    
    protected int xObjectCount = 0;
    protected Vector xObjects = new Vector();

    /**
     * creates an empty PDF document
     */
    public PDFDocument() {

	/* create the /Root, /Info and /Resources objects */
	this.root = makeRoot();
	this.info = makeInfo();
	this.resources = makeResources();
    }
    
    /**
     * set the producer of the document
     *
     * @param producer string indicating application producing the PDF
     */
    public void setProducer(String producer) {
	this.info.setProducer(producer);
    }

    /**
     * make /Root object as next object
     *
     * @return the created /Root object
     */
    protected PDFRoot makeRoot() {

	/* create a PDFRoot with the next object number and add to
	   list of objects */
	PDFRoot pdfRoot = new PDFRoot(++this.objectcount);
	this.objects.addElement(pdfRoot);

	/* create a new /Pages object to be root of Pages hierarchy
	   and add to list of objects */
	PDFPages rootPages = new PDFPages(++this.objectcount);
	this.objects.addElement(rootPages);
	
	/* inform the /Root object of the /Pages root */
	pdfRoot.setRootPages(rootPages);
	return pdfRoot;
    }

    /**
     * make an /Info object
     *
     * @param producer string indicating application producing the PDF
     * @return the created /Info object
     */
    protected PDFInfo makeInfo() {

	/* create a PDFInfo with the next object number and add to
	   list of objects */
	PDFInfo pdfInfo = new PDFInfo(++this.objectcount);
	this.objects.addElement(pdfInfo);
	return pdfInfo;
    }

    /**
     * make a /Resources object
     *
     * @return the created /Resources object
     */
    private PDFResources makeResources() {

	/* create a PDFResources with the next object number and add
	   to list of objects */
	PDFResources pdfResources = new PDFResources(++this.objectcount);
	this.objects.addElement(pdfResources);
	return pdfResources;
    }

    /**
     * make a Type1 /Font object
     * 
     * @param fontname internal name to use for this font (eg "F1")
     * @param basefont name of the base font (eg "Helvetica")
     * @param encoding character encoding scheme used by the font
     * @return the created /Font object
     */
    public PDFFont makeFont(String fontname, String basefont,
			    String encoding) {

	/* create a PDFFont with the next object number and add to the
	   list of objects */
	PDFFont font = new PDFFont(++this.objectcount, fontname,
				   basefont, encoding);
	this.objects.addElement(font);
	return font;
    }

    public int addImage(FopImage img) {
	PDFXObject xObject = new PDFXObject(++this.objectcount,
					    ++this.xObjectCount, img);
	this.objects.addElement(xObject);
	this.xObjects.addElement(xObject);
	return xObjectCount;
    }

    /**
     * make a /Page object
     *
     * @param resources resources object to use
     * @param contents stream object with content
     * @param pagewidth width of the page in points
     * @param pageheight height of the page in points
     * @return the created /Page object
     */
    public PDFPage makePage(PDFResources resources,
			    PDFStream contents, int pagewidth,
			    int pageheight)  {

	/* create a PDFPage with the next object number, the given
	   resources, contents and dimensions */
	PDFPage page = new PDFPage(++this.objectcount, resources,
				   contents, pagewidth, pageheight);

	/* add it to the list of objects */
	this.objects.addElement(page);

	/* add the page to the Root */
	this.root.addPage(page);

	return page;
    }

    /**
     * make a stream object
     *
     * @return the stream object created
     */
    public PDFStream makeStream() {
    
	/* create a PDFStream with the next object number and add it
	   to the list of objects */
	PDFStream obj = new PDFStream(++this.objectcount);
	this.objects.addElement(obj);
	return obj;
    }

    /**
     * get the /Resources object for the document
     *
     * @return the /Resources object
     */
    public PDFResources getResources() {
	return this.resources;
    }

    /**
     * write the entire document out
     *
     * @param writer the PrinterWriter to output the document to
     */
    public void output(PrintWriter writer) throws IOException {

	/* output the header and increment the character position by
	   the header's length */
	this.position += outputHeader(writer);

	this.resources.setXObjects(xObjects);

	/* loop through the object numbers */
	for (int i=1; i <= this.objectcount; i++) {

	    /* add the position of this object to the list of object
	       locations */
	    this.location.addElement(new Integer(this.position));

	    /* retrieve the object with the current number */
	    PDFObject object = (PDFObject)this.objects.elementAt(i-1);

	    /* output the object and increment the character position
	       by the object's length */
	    this.position += object.output(writer);
	}

	/* output the xref table and increment the character position
	   by the table's length */
	this.position += outputXref(writer);

	/* output the trailer and flush the Writer */
	outputTrailer(writer);
	writer.flush();
    }

    /**
     * write the PDF header
     *
     * @param writer the PrintWriter to write the header to
     * @return the number of characters written
     */
    protected int outputHeader(PrintWriter writer) throws IOException {
	String pdf = "%PDF-" + this.pdfVersion + "\n";
	writer.write(pdf);
	return pdf.length();
    }

    /**
     * write the trailer
     *
     * @param writer the PrintWriter to write the trailer to
     */
    protected void outputTrailer(PrintWriter writer) throws IOException {

	/* construct the trailer */
	String pdf = "trailer\n<<\n/Size " + (this.objectcount+1)
	    + "\n/Root " + this.root.number + " " + this.root.generation
	    + " R\n/Info " + this.info.number + " " 
	    + this.info.generation + " R\n>>\nstartxref\n" + this.xref 
	    + "\n%%EOF\n";

	/* write the trailer */
	writer.write(pdf);
    }

    /**
     * write the xref table
     *
     * @param writer the PrintWriter to write the xref table to
     * @return the number of characters written
     */
    private int outputXref(PrintWriter writer) throws IOException {

	/* remember position of xref table */
	this.xref = this.position;

	/* construct initial part of xref */
	StringBuffer pdf = new StringBuffer("xref\n0 " + (this.objectcount+1) 
	    + "\n0000000000 65535 f \n");

	/* loop through object numbers */
	for (int i=1; i < this.objectcount+1; i++) {

	    /* contruct xref entry for object */
	    String padding = "0000000000";
	    String x = this.location.elementAt(i-1).toString();
	    String loc = padding.substring(x.length()) + x;

	    /* append to xref table */
	    pdf = pdf.append(loc + " 00000 n \n");
	}

	/* write the xref table and return the character length */
	writer.write(pdf.toString());
	return pdf.length();
    }
}
