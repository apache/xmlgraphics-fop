/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
public class PDFImageXObject extends PDFXObject {

    private PDFImage pdfimage;

    /**
     * create an XObject with the given number and name and load the
     * image in the object
     *
     * @param xnumber the pdf object X number
     * @param img the pdf image that contains the image data
     */
    public PDFImageXObject(int xnumber, PDFImage img) {
        super();
        put("Name", new PDFName("Im" + xnumber));
        pdfimage = img;
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

    /** {@inheritDoc} */
    protected void populateStreamDict(Object lengthEntry) {
        super.populateStreamDict(lengthEntry);
        if (pdfimage.isPS()) {
            populateDictionaryFromPS();
        } else {
            populateDictionaryFromImage();
        }
    }

    private void populateDictionaryFromPS() {
        getDocumentSafely().getProfile().verifyPSXObjectsAllowed();
        put("Subtype", new PDFName("PS"));
    }

    private void populateDictionaryFromImage() {
        put("Subtype", new PDFName("Image"));
        put("Width", new Integer(pdfimage.getWidth()));
        put("Height", new Integer(pdfimage.getHeight()));
        put("BitsPerComponent", new Integer(pdfimage.getBitsPerComponent()));

        PDFICCStream pdfICCStream = pdfimage.getICCStream();
        if (pdfICCStream != null) {
            put("ColorSpace", new PDFArray(this,
                    new Object[] {new PDFName("ICCBased"), pdfICCStream}));
        } else {
            PDFDeviceColorSpace cs = pdfimage.getColorSpace();
            put("ColorSpace", new PDFName(cs.getName()));
        }

        if (pdfimage.isInverted()) {
            /* PhotoShop generates CMYK values that's inverse,
             * this will invert the values - too bad if it's not
             * a PhotoShop image...
             */
            final Float zero = new Float(0.0f);
            final Float one = new Float(1.0f);
            PDFArray decode = new PDFArray(this);
            for (int i = 0, c = pdfimage.getColorSpace().getNumComponents(); i < c; i++) {
                decode.add(one);
                decode.add(zero);
            }
            put("Decode", decode);
        }

        if (pdfimage.isTransparent()) {
            PDFColor transp = pdfimage.getTransparentColor();
            PDFArray mask = new PDFArray(this);
            if (pdfimage.getColorSpace().isGrayColorSpace()) {
                mask.add(new Integer(transp.red255()));
                mask.add(new Integer(transp.red255()));
            } else {
                mask.add(new Integer(transp.red255()));
                mask.add(new Integer(transp.red255()));
                mask.add(new Integer(transp.green255()));
                mask.add(new Integer(transp.green255()));
                mask.add(new Integer(transp.blue255()));
                mask.add(new Integer(transp.blue255()));
            }
            put("Mask", mask);
        }
        PDFReference ref = pdfimage.getSoftMaskReference();
        if (ref != null) {
            put("SMask", ref);
        }
        //Important: do this at the end so previous values can be overwritten.
        pdfimage.populateXObjectDictionary(this);
    }

    /** {@inheritDoc} */
    protected void outputRawStreamData(OutputStream out) throws IOException {
        pdfimage.outputContents(out);
    }

    /** {@inheritDoc} */
    protected int getSizeHint() throws IOException {
        return 0;
    }

    /** {@inheritDoc} */
    protected void prepareImplicitFilters() {
        PDFFilter pdfFilter = pdfimage.getPDFFilter();
        if (pdfFilter != null) {
            getFilterList().ensureFilterInPlace(pdfFilter);
        }
    }

    /**
     * This sets up the default filters for XObjects. It uses the PDFImage
     * instance to determine what default filters to apply.
     * {@inheritDoc}
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
