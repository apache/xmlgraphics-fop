/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
import java.io.OutputStream;

/* modified by JKT to integrate with 0.12.0 */
/* modified by Eric SCHAEFFER to integrate with 0.13.0 */

/**
 * PDF XObject
 *
 * A derivative of the PDF Object, is a PDF Stream that has not only a
 * dictionary but a stream of image data.
 * The dictionary just provides information like the stream length.
 * This outputs the image dictionary and the image data.
 * This is used as a reference for inserting the same image in the
 * document in another place.
 */
public class PDFXObject extends AbstractPDFStream {
    
    private PDFImage pdfimage;
    private int xnum;

    /**
     * create an XObject with the given number and name and load the
     * image in the object
     *
     * @param xnumber the pdf object X number
     * @param img the pdf image that contains the image data
     */
    public PDFXObject(int xnumber, PDFImage img) {
        super();
        this.xnum = xnumber;
        pdfimage = img;
    }

    /**
     * Get the xnumber for this pdf object.
     *
     * @return the PDF XObject number
     */
    public int getXNumber() {
        return this.xnum;
    }

    /**
     * Output the image as PDF.
     * This sets up the image dictionary and adds the image data stream.
     *
     * @param stream the output stream to write the data
     * @throws IOException if there is an error writing the data
     * @return the length of the data written
     */
    protected int output(OutputStream stream) throws IOException {
        int length = super.output(stream);
        
        // let it gc
        // this object is retained as a reference to inserting
        // the same image but the image data is no longer needed
        pdfimage = null;
        return length;
    }

    /**
     * @see org.apache.fop.pdf.AbstractPDFStream#buildStreamDict(String)
     */
    protected String buildStreamDict(String lengthEntry) {
        String dictEntries = getFilterList().buildFilterDictEntries();
        if (pdfimage.isPS()) {
            return buildDictionaryFromPS(lengthEntry, dictEntries);
        } else {
            return buildDictionaryFromImage(lengthEntry, dictEntries);
        }
    }
    
    private String buildDictionaryFromPS(String lengthEntry, 
                                         String dictEntries) {
        StringBuffer sb = new StringBuffer(128);
        sb.append(getObjectID());
        sb.append("<</Type /XObject\n");
        sb.append("/Subtype /PS\n");
        sb.append("/Length " + lengthEntry);

        sb.append(dictEntries);
        sb.append("\n>>\n");
        return sb.toString();
    }

    private String buildDictionaryFromImage(String lengthEntry,
                                            String dictEntries) {
        StringBuffer sb = new StringBuffer(128);
        sb.append(getObjectID());
        sb.append("<</Type /XObject\n");
        sb.append("/Subtype /Image\n");
        sb.append("/Name /Im" + xnum + "\n");
        sb.append("/Length " + lengthEntry + "\n");
        sb.append("/Width " + pdfimage.getWidth() + "\n");
        sb.append("/Height " + pdfimage.getHeight() + "\n");
        sb.append("/BitsPerComponent " + pdfimage.getBitsPerPixel() + "\n");

        PDFICCStream pdfICCStream = pdfimage.getICCStream();
        if (pdfICCStream != null) {
            sb.append("/ColorSpace [/ICCBased "
                + pdfICCStream.referencePDF() + "]\n");
        } else {
            PDFColorSpace cs = pdfimage.getColorSpace();
            sb.append("/ColorSpace /" + cs.getColorSpacePDFString()
                  + "\n");
        }

        /* PhotoShop generates CMYK values that's inverse,
           this will invert the values - too bad if it's not
           a PhotoShop image...
         */
        if (pdfimage.getColorSpace().getColorSpace()
                == PDFColorSpace.DEVICE_CMYK) {
            sb.append("/Decode [ 1.0 0.0 1.0 0.0 1.0 0.0 1.1 0.0 ]\n");
        }

        if (pdfimage.isTransparent()) {
            PDFColor transp = pdfimage.getTransparentColor();
            sb.append("/Mask [" 
                + transp.red255() + " "
                + transp.red255() + " " 
                + transp.green255() + " " 
                + transp.green255() + " "
                + transp.blue255() + " " 
                + transp.blue255() + "]\n");
        }
        String ref = pdfimage.getSoftMask();
        if (ref != null) {
            sb.append("/SMask " + ref + "\n");
        }

        sb.append(dictEntries);
        sb.append("\n>>\n");
        return sb.toString();
    }
    
    /**
     * @see org.apache.fop.pdf.PDFStream#outputRawStreamData(OutputStream)
     */
    protected void outputRawStreamData(OutputStream out) throws IOException {
        pdfimage.outputContents(out);
    }

    /**
     * @see org.apache.fop.pdf.AbstractPDFStream#getSizeHint()
     */
    protected int getSizeHint() throws IOException {
        return 0;
    }

    /**
     * @see org.apache.fop.pdf.AbstractPDFStream#prepareImplicitFilters()
     */
    protected void prepareImplicitFilters() {
        PDFFilter pdfFilter = pdfimage.getPDFFilter();
        if (pdfFilter != null) {
            getFilterList().ensureFilterInPlace(pdfFilter);
        }
    }
    
    /**
     * This sets up the default filters for XObjects. It uses the PDFImage
     * instance to determine what default filters to apply.
     * @see org.apache.fop.pdf.AbstractPDFStream#setupFilterList()
     */
    protected void setupFilterList() {
        if (!getFilterList().isInitialized()) {
            getFilterList().addDefaultFilters(
                getDocumentSafely().getFilterMap(), 
                pdfimage.getFilterHint());
        }
        super.setupFilterList();
    }
    

}
