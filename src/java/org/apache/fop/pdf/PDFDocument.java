/*
 * Copyright 1999-2004,2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.pdf;

// Java
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/* image support modified from work of BoBoGi */
/* font support based on work by Takayuki Takeuchi */

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
 *
 * Modified by Mark Lillywhite, mark-fop@inomial.com. The changes
 * involve: ability to output pages one-at-a-time in a streaming
 * fashion (rather than storing them all for output at the end);
 * ability to write the /Pages object after writing the rest
 * of the document; ability to write to a stream and flush
 * the object list; enhanced trailer output; cleanups.
 *
 */
public class PDFDocument {

    private static final Integer LOCATION_PLACEHOLDER = new Integer(0);
    
    /** Integer constant to represent PDF 1.3 */
    public static final int PDF_VERSION_1_3 = 3;

    /** Integer constant to represent PDF 1.4 */
    public static final int PDF_VERSION_1_4 = 4;
    
    /**
     * the encoding to use when converting strings to PDF commandos.
     */
    public static final String ENCODING = "ISO-8859-1";

    private Log log = LogFactory.getLog("org.apache.fop.pdf");

    /**
     * the current character position
     */
    protected int position = 0;

    /**
     * the character position of each object
     */
    protected List location = new java.util.ArrayList();

    /** List of objects to write in the trailer */
    private List trailerObjects = new java.util.ArrayList();

    /**
     * the counter for object numbering
     */
    protected int objectcount = 0;

    /**
     * the objects themselves
     */
    protected List objects = new java.util.LinkedList();

    /**
     * character position of xref table
     */
    protected int xref;

    /** Indicates what PDF version is active */
    protected int pdfVersion = PDF_VERSION_1_4;
    
    /**
     * Indicates the PDF/A-1 mode currently active. Defaults to "no restrictions", i.e. 
     * PDF/A-1 not active.
     */
    protected PDFAMode pdfAMode = PDFAMode.DISABLED;
    
    /**
     * the /Root object
     */
    protected PDFRoot root;

    /** The root outline object */
    private PDFOutline outlineRoot = null;

    /** The /Pages object (mark-fop@inomial.com) */
    private PDFPages pages;

    /**
     * the /Info object
     */
    protected PDFInfo info;

    /**
     * the /Resources object
     */
    protected PDFResources resources;

    /**
     * the documents encryption, if exists
     */
    protected PDFEncryption encryption;

    /**
     * the colorspace (0=RGB, 1=CMYK)
     */
    protected PDFColorSpace colorspace =
        new PDFColorSpace(PDFColorSpace.DEVICE_RGB);

    /**
     * the counter for Pattern name numbering (e.g. 'Pattern1')
     */
    protected int patternCount = 0;

    /**
     * the counter for Shading name numbering
     */
    protected int shadingCount = 0;

    /**
     * the counter for XObject numbering
     */
    protected int xObjectCount = 0;

    /**
     * the XObjects Map.
     * Should be modified (works only for image subtype)
     */
    protected Map xObjectsMap = new java.util.HashMap();

    /**
     * the Font Map.
     */
    protected Map fontMap = new java.util.HashMap();

    /**
     * The filter map.
     */
    protected Map filterMap = new java.util.HashMap();

    /**
     * List of PDFGState objects.
     */
    protected List gstates = new java.util.ArrayList();

    /**
     * List of functions.
     */
    protected List functions = new java.util.ArrayList();

    /**
     * List of shadings.
     */
    protected List shadings = new java.util.ArrayList();

    /**
     * List of patterns.
     */
    protected List patterns = new java.util.ArrayList();

    /**
     * List of Links.
     */
    protected List links = new java.util.ArrayList();

    /**
     * List of FileSpecs.
     */
    protected List filespecs = new java.util.ArrayList();

    /**
     * List of GoToRemotes.
     */
    protected List gotoremotes = new java.util.ArrayList();

    /**
     * List of GoTos.
     */
    protected List gotos = new java.util.ArrayList();

    private PDFFactory factory;

    private boolean encodingOnTheFly = true;

    /**
     * Creates an empty PDF document.
     *
     * The constructor creates a /Root and /Pages object to
     * track the document but does not write these objects until
     * the trailer is written. Note that the object ID of the
     * pages object is determined now, and the xref table is
     * updated later. This allows Pages to refer to their
     * Parent before we write it out.
     *
     * @param prod the name of the producer of this pdf document
     */
    public PDFDocument(String prod) {

        this.factory = new PDFFactory(this);

        /* create the /Root, /Info and /Resources objects */
        this.pages = getFactory().makePages();

        // Create the Root object
        this.root = getFactory().makeRoot(pages);

        // Create the Resources object
        this.resources = getFactory().makeResources();

        // Make the /Info record
        this.info = getFactory().makeInfo(prod);
    }

    /**
     * @return the integer representing the active PDF version (one of PDFDocument.PDF_VERSION_*)
     */
    public int getPDFVersion() {
        return this.pdfVersion;
    }
    
    /** @return the String representing the active PDF version */
    public String getPDFVersionString() {
        switch (getPDFVersion()) {
        case PDF_VERSION_1_3:
            return "1.3";
        case PDF_VERSION_1_4:
            return "1.4";
        default:
            throw new IllegalStateException("Unsupported PDF version selected");
        }
    }

    /** @return the PDF/A mode currently active. */
    public PDFAMode getPDFAMode() {
        return this.pdfAMode;
    }
    
    /**
     * Sets the active PDF/A mode. This must be set immediately after calling the constructor so
     * the checks will be activated.
     * @param mode one of the PDFAMode constants
     */
    public void setPDFAMode(PDFAMode mode) {
        if (mode == null) {
            throw new NullPointerException("mode must not be null");
        }
        if (mode == PDFAMode.PDFA_1A) {
            throw new UnsupportedOperationException("PDF/A-1a is not implemented, yet");
        } else if (mode == PDFAMode.PDFA_1B) {
            //you got the green light!
        }
        this.pdfAMode = mode;
    }
    
    /**
     * Returns the factory for PDF objects.
     * @return PDFFactory the factory
     */
    public PDFFactory getFactory() {
        return this.factory;
    }

    /**
     * Indicates whether stream encoding on-the-fly is enabled. If enabled
     * stream can be serialized without the need for a buffer to merely
     * calculate the stream length.
     * @return boolean true if on-the-fly encoding is enabled
     */
    public boolean isEncodingOnTheFly() {
        return this.encodingOnTheFly;
    }

    /**
     * Converts text to a byte array for writing to a PDF file.
     * @param text text to convert/encode
     * @return byte[] the resulting byte array
     */
    public static byte[] encode(String text) {
        try {
            return text.getBytes(ENCODING);
        } catch (UnsupportedEncodingException uee) {
            return text.getBytes();
        }
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
      * Set the creation date of the document.
      * 
      * @param date Date to be stored as creation date in the PDF.
      */
    public void setCreationDate(Date date) {
        info.setCreationDate(date);
    }

    /**
      * Set the creator of the document.
      *
      * @param creator string indicating application creating the document
      */
    public void setCreator(String creator) {
        this.info.setCreator(creator);
    }

    /**
     * Set the filter map to use for filters in this document.
     *
     * @param map the map of filter lists for each stream type
     */
    public void setFilterMap(Map map) {
        this.filterMap = map;
    }

    /**
     * Get the filter map used for filters in this document.
     *
     * @return the map of filters being used
     */
    public Map getFilterMap() {
        return this.filterMap;
    }

    /**
     * Returns the PDFPages object associated with the root object.
     * @return the PDFPages object
     */
    public PDFPages getPages() {
        return this.pages;
    }

    /**
     * Get the PDF root object.
     *
     * @return the PDFRoot object
     */
    public PDFRoot getRoot() {
        return this.root;
    }

    /**
     * Get the pdf info object for this document.
     *
     * @return the PDF Info object for this document
     */
    public PDFInfo getInfo() {
        return info;
    }

    /**
     * Registers a PDFObject in this PDF document. The PDF is assigned a new
     * object number.
     * @param obj PDFObject to add
     * @return PDFObject the PDFObject added (its object number set)
     */
    public PDFObject registerObject(PDFObject obj) {
        assignObjectNumber(obj);
        addObject(obj);
        return obj;
    }

    /**
     * Assigns the PDFObject a object number and sets the parent of the
     * PDFObject to this PDFDocument.
     * @param obj PDFObject to assign a number to
     */
    public void assignObjectNumber(PDFObject obj) {
        if (obj == null) {
            throw new NullPointerException("obj must not be null");
        }
        if (obj.hasObjectNumber()) {
            throw new IllegalStateException(
                "Error registering a PDFObject: "
                    + "PDFObject already has an object number");
        }
        PDFDocument currentParent = obj.getDocument();
        if (currentParent != null && currentParent != this) {
            throw new IllegalStateException(
                "Error registering a PDFObject: "
                    + "PDFObject already has a parent PDFDocument");
        }

        obj.setObjectNumber(++this.objectcount);

        if (currentParent == null) {
            obj.setDocument(this);
        }
    }

    /**
     * Adds an PDFObject to this document. The object must have a object number
     * assigned.
     * @param obj PDFObject to add
     */
    public void addObject(PDFObject obj) {
        if (obj == null) {
            throw new NullPointerException("obj must not be null");
        }
        if (!obj.hasObjectNumber()) {
            throw new IllegalStateException(
                "Error adding a PDFObject: "
                    + "PDFObject doesn't have an object number");
        }

        //Add object to list
        this.objects.add(obj);

        //Add object to special lists where necessary
        if (obj instanceof PDFFunction) {
            this.functions.add(obj);
        }
        if (obj instanceof PDFShading) {
            final String shadingName = "Sh" + (++this.shadingCount);
            ((PDFShading)obj).setName(shadingName);
            this.shadings.add(obj);
        }
        if (obj instanceof PDFPattern) {
            final String patternName = "Pa" + (++this.patternCount);
            ((PDFPattern)obj).setName(patternName);
            this.patterns.add(obj);
        }
        if (obj instanceof PDFFont) {
            final PDFFont font = (PDFFont)obj;
            this.fontMap.put(font.getName(), font);
        }
        if (obj instanceof PDFGState) {
            this.gstates.add(obj);
        }
        if (obj instanceof PDFPage) {
            this.pages.notifyKidRegistered((PDFPage)obj);
        }
        if (obj instanceof PDFLink) {
            this.links.add(obj);
        }
        if (obj instanceof PDFFileSpec) {
            this.filespecs.add(obj);
        }
        if (obj instanceof PDFGoToRemote) {
            this.gotoremotes.add(obj);
        }
    }

    /**
     * Add trailer object.
     * Adds an object to the list of trailer objects.
     *
     * @param obj the PDF object to add
     */
    public void addTrailerObject(PDFObject obj) {
        this.trailerObjects.add(obj);

        if (obj instanceof PDFGoTo) {
            this.gotos.add(obj);
        }
    }

    /**
     * Apply the encryption filter to a PDFStream if encryption is enabled.
     * @param stream PDFStream to encrypt
     */
    public void applyEncryption(AbstractPDFStream stream) {
        if (isEncryptionActive()) {
            this.encryption.applyFilter(stream);
        }
    }

    /**
     * Enables PDF encryption.
     * @param params The encryption parameters for the pdf file
     */
    public void setEncryption(PDFEncryptionParams params) {
        if (getPDFAMode().isPDFA1LevelB()) {
            throw new PDFConformanceException("PDF/A-1 doesn't allow encrypted PDFs");
        }
        this.encryption = PDFEncryptionManager.newInstance(++this.objectcount, params);
        ((PDFObject)this.encryption).setDocument(this);
        if (encryption != null) {
            /**@todo this cast is ugly. PDFObject should be transformed to an interface. */
            addTrailerObject((PDFObject)this.encryption);
        } else {
            log.warn(
                "PDF encryption is unavailable. PDF will be "
                    + "generated without encryption.");
        }
    }

    /**
     * Indicates whether encryption is active for this PDF or not.
     * @return boolean True if encryption is active
     */
    public boolean isEncryptionActive() {
        return this.encryption != null;
    }

    /**
     * Returns the active Encryption object.
     * @return the Encryption object
     */
    public PDFEncryption getEncryption() {
        return encryption;
    }

    private Object findPDFObject(List list, PDFObject compare) {
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Object obj = iter.next();
            if (compare.equals(obj)) {
                return obj;
            }
        }
        return null;
    }

    /**
     * Looks through the registered functions to see if one that is equal to
     * a reference object exists
     * @param compare reference object
     * @return the function if it was found, null otherwise
     */
    protected PDFFunction findFunction(PDFFunction compare) {
        return (PDFFunction)findPDFObject(functions, compare);
    }

    /**
     * Looks through the registered shadings to see if one that is equal to
     * a reference object exists
     * @param compare reference object
     * @return the shading if it was found, null otherwise
     */
    protected PDFShading findShading(PDFShading compare) {
        return (PDFShading)findPDFObject(shadings, compare);
    }

    /**
     * Find a previous pattern.
     * The problem with this is for tiling patterns the pattern
     * data stream is stored and may use up memory, usually this
     * would only be a small amount of data.
     * @param compare reference object
     * @return the shading if it was found, null otherwise
     */
    protected PDFPattern findPattern(PDFPattern compare) {
        return (PDFPattern)findPDFObject(patterns, compare);
    }

    /**
     * Finds a font.
     * @param fontname name of the font
     * @return PDFFont the requested font, null if it wasn't found
     */
    protected PDFFont findFont(String fontname) {
        return (PDFFont)fontMap.get(fontname);
    }

    /**
     * Finds a link.
     * @param compare reference object to use as search template
     * @return the link if found, null otherwise
     */
    protected PDFLink findLink(PDFLink compare) {
        return (PDFLink)findPDFObject(links, compare);
    }

    /**
     * Finds a file spec.
     * @param compare reference object to use as search template
     * @return the file spec if found, null otherwise
     */
    protected PDFFileSpec findFileSpec(PDFFileSpec compare) {
        return (PDFFileSpec)findPDFObject(filespecs, compare);
    }

    /**
     * Finds a goto remote.
     * @param compare reference object to use as search template
     * @return the goto remote if found, null otherwise
     */
    protected PDFGoToRemote findGoToRemote(PDFGoToRemote compare) {
        return (PDFGoToRemote)findPDFObject(gotoremotes, compare);
    }

    /**
     * Finds a goto.
     * @param compare reference object to use as search template
     * @return the goto if found, null otherwise
     */
    protected PDFGoTo findGoTo(PDFGoTo compare) {
        return (PDFGoTo)findPDFObject(gotos, compare);
    }

    /**
     * Looks for an existing GState to use
     * @param wanted requested features
     * @param current currently active features
     * @return PDFGState the GState if found, null otherwise
     */
    protected PDFGState findGState(PDFGState wanted, PDFGState current) {
        PDFGState poss;
        Iterator iter = gstates.iterator();
        while (iter.hasNext()) {
            PDFGState avail = (PDFGState)iter.next();
            poss = new PDFGState();
            poss.addValues(current);
            poss.addValues(avail);
            if (poss.equals(wanted)) {
                return avail;
            }
        }
        return null;
    }

    /**
     * Get the PDF color space object.
     *
     * @return the color space
     */
    public PDFColorSpace getPDFColorSpace() {
        return this.colorspace;
    }

    /**
     * Get the color space.
     *
     * @return the color space
     */
    public int getColorSpace() {
        return getPDFColorSpace().getColorSpace();
    }

    /**
     * Set the color space.
     * This is used when creating gradients.
     *
     * @param theColorspace the new color space
     */
    public void setColorSpace(int theColorspace) {
        this.colorspace.setColorSpace(theColorspace);
        return;
    }

    /**
     * Get the font map for this document.
     *
     * @return the map of fonts used in this document
     */
    public Map getFontMap() {
        return fontMap;
    }

    /**
     * Resolve a URI.
     *
     * @param uri the uri to resolve
     * @throws java.io.FileNotFoundException if the URI could not be resolved
     * @return the InputStream from the URI.
     */
    protected InputStream resolveURI(String uri)
        throws java.io.FileNotFoundException {
        try {
            /**@todo Temporary hack to compile, improve later */
            return new java.net.URL(uri).openStream();
        } catch (Exception e) {
            throw new java.io.FileNotFoundException(
                "URI could not be resolved (" + e.getMessage() + "): " + uri);
        }
    }

    /**
     * Get an image from the image map.
     *
     * @param key the image key to look for
     * @return the image or PDFXObject for the key if found
     */
    public PDFXObject getImage(String key) {
        PDFXObject xObject = (PDFXObject)xObjectsMap.get(key);
        return xObject;
    }

    /**
     * Add an image to the PDF document.
     * This adds an image to the PDF objects.
     * If an image with the same key already exists it will return the
     * old PDFXObject.
     *
     * @param res the PDF resource context to add to, may be null
     * @param img the PDF image to add
     * @return the PDF XObject that references the PDF image data
     */
    public PDFXObject addImage(PDFResourceContext res, PDFImage img) {
        // check if already created
        String key = img.getKey();
        PDFXObject xObject = (PDFXObject)xObjectsMap.get(key);
        if (xObject != null) {
            if (res != null) {
                res.getPDFResources().addXObject(xObject);
            }
            return xObject;
        }

        // setup image
        img.setup(this);
        // create a new XObject
        xObject = new PDFXObject(++this.xObjectCount, img);
        registerObject(xObject);
        this.resources.addXObject(xObject);
        if (res != null) {
            res.getPDFResources().addXObject(xObject);
        }
        this.xObjectsMap.put(key, xObject);
        return xObject;
    }

    /**
     * Add a form XObject to the PDF document.
     * This adds a Form XObject to the PDF objects.
     * If a Form XObject with the same key already exists it will return the
     * old PDFFormXObject.
     *
     * @param res the PDF resource context to add to, may be null
     * @param cont the PDF Stream contents of the Form XObject
     * @param formres the PDF Resources for the Form XObject data
     * @param key the key for the object
     * @return the PDF Form XObject that references the PDF data
     */
    public PDFFormXObject addFormXObject(
        PDFResourceContext res,
        PDFStream cont,
        PDFResources formres,
        String key) {
        PDFFormXObject xObject;
        xObject =
            new PDFFormXObject(
                ++this.xObjectCount,
                cont,
                formres.referencePDF());
        registerObject(xObject);
        this.resources.addXObject(xObject);
        if (res != null) {
            res.getPDFResources().addXObject(xObject);
        }
        return xObject;
    }

    /**
     * Get the root Outlines object. This method does not write
     * the outline to the PDF document, it simply creates a
     * reference for later.
     *
     * @return the PDF Outline root object
     */
    public PDFOutline getOutlineRoot() {
        if (outlineRoot != null) {
            return outlineRoot;
        }

        outlineRoot = new PDFOutline(null, null, true);
        assignObjectNumber(outlineRoot);
        addTrailerObject(outlineRoot);
        root.setRootOutline(outlineRoot);
        return outlineRoot;
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
     * Ensure there is room in the locations xref for the number of
     * objects that have been created.
     */
    private void setLocation(int objidx, int position) {
        while (location.size() <= objidx) {
            location.add(LOCATION_PLACEHOLDER);
        }
        location.set(objidx, new Integer(position));
    }

    /**
     * write the entire document out
     *
     * @param stream the OutputStream to output the document to
     * @throws IOException if there is an exception writing to the output stream
     */
    public void output(OutputStream stream) throws IOException {
        //Write out objects until the list is empty. This approach (used with a
        //LinkedList) allows for output() methods to create and register objects
        //on the fly even during serialization.
        while (this.objects.size() > 0) {
            /* Retrieve first */
            PDFObject object = (PDFObject)this.objects.remove(0);
            /*
             * add the position of this object to the list of object
             * locations
             */
            setLocation(object.getObjectNumber() - 1, this.position);

            /*
             * output the object and increment the character position
             * by the object's length
             */
            this.position += object.output(stream);
        }

        //Clear all objects written to the file
        //this.objects.clear();
    }

    /**
     * Write the PDF header.
     *
     * This method must be called prior to formatting
     * and outputting AreaTrees.
     *
     * @param stream the OutputStream to write the header to
     * @throws IOException if there is an exception writing to the output stream
     */
    public void outputHeader(OutputStream stream) throws IOException {
        this.position = 0;

        if (getPDFAMode().isPDFA1LevelB() && getPDFVersion() != PDF_VERSION_1_4) {
            throw new PDFConformanceException("PDF version must be 1.4 for " + getPDFAMode());
        }
        
        byte[] pdf = ("%PDF-" + getPDFVersionString() + "\n").getBytes();
        stream.write(pdf);
        this.position += pdf.length;

        // output a binary comment as recommended by the PDF spec (3.4.1)
        byte[] bin = {
                (byte)'%',
                (byte)0xAA,
                (byte)0xAB,
                (byte)0xAC,
                (byte)0xAD,
                (byte)'\n' };
        stream.write(bin);
        this.position += bin.length;
    }

    /** @return the "ID" entry for the file trailer */
    protected String getIDEntry() {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            DateFormat df = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS");
            digest.update(df.format(new Date()).getBytes());
            //Ignoring the filename here for simplicity even though it's recommended by the PDF spec
            digest.update(String.valueOf(this.position).getBytes());
            digest.update(getInfo().toPDF());
            byte[] res = digest.digest();
            String s = PDFText.toHex(res);
            return "/ID [" + s + " " + s + "]";
        } catch (NoSuchAlgorithmException e) {
            if (getPDFAMode().isPDFA1LevelB()) {
                throw new UnsupportedOperationException("MD5 not available: " + e.getMessage());
            } else {
                return ""; //Entry is optional if PDF/A is not active
            }
        }
    }
    
    /**
     * write the trailer
     *
     * @param stream the OutputStream to write the trailer to
     * @throws IOException if there is an exception writing to the output stream
     */
    public void outputTrailer(OutputStream stream) throws IOException {
        output(stream);
        for (int count = 0; count < trailerObjects.size(); count++) {
            PDFObject o = (PDFObject)trailerObjects.get(count);
            this.location.set(
                o.getObjectNumber() - 1,
                new Integer(this.position));
            this.position += o.output(stream);
        }
        /* output the xref table and increment the character position
          by the table's length */
        this.position += outputXref(stream);

        // Determine existance of encryption dictionary
        String encryptEntry = "";
        if (this.encryption != null) {
            encryptEntry = this.encryption.getTrailerEntry();
        }

        /* construct the trailer */
        String pdf =
            "trailer\n"
                + "<<\n"
                + "/Size "
                + (this.objectcount + 1)
                + "\n"
                + "/Root "
                + this.root.referencePDF()
                + "\n"
                + "/Info "
                + this.info.referencePDF()
                + "\n"
                + getIDEntry()
                + "\n"
                + encryptEntry
                + ">>\n"
                + "startxref\n"
                + this.xref
                + "\n"
                + "%%EOF\n";

        /* write the trailer */
        stream.write(encode(pdf));
    }

    /**
     * write the xref table
     *
     * @param stream the OutputStream to write the xref table to
     * @return the number of characters written
     */
    private int outputXref(OutputStream stream) throws IOException {

        /* remember position of xref table */
        this.xref = this.position;

        /* construct initial part of xref */
        StringBuffer pdf = new StringBuffer(128);
        pdf.append(
            "xref\n0 " + (this.objectcount + 1) + "\n0000000000 65535 f \n");

        for (int count = 0; count < this.location.size(); count++) {
            String x = this.location.get(count).toString();

            /* contruct xref entry for object */
            String padding = "0000000000";
            String loc = padding.substring(x.length()) + x;

            /* append to xref table */
            pdf = pdf.append(loc + " 00000 n \n");
        }

        /* write the xref table and return the character length */
        byte[] pdfBytes = encode(pdf.toString());
        stream.write(pdfBytes);
        return pdfBytes.length;
    }

}
