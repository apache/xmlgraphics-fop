/*
 * Copyright 1999-2006 The Apache Software Foundation.
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

package org.apache.fop.render.pdf;
import org.apache.fop.pdf.PDFConformanceException;
import org.apache.fop.pdf.PDFFilterList;
import org.apache.fop.pdf.PDFImage;
import org.apache.fop.pdf.PDFFilter;
import org.apache.fop.pdf.PDFICCStream;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.DCTFilter;
import org.apache.fop.pdf.CCFFilter;
import org.apache.fop.pdf.PDFColorSpace;
import org.apache.fop.pdf.PDFXObject;
import org.apache.fop.pdf.BitmapImage;

import org.apache.fop.image.FopImage;
import org.apache.fop.image.EPSImage;
import org.apache.fop.image.TIFFImage;

import java.io.IOException;
import java.io.OutputStream;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;

/**
 * PDFImage implementation for the PDF renderer.
 */
public class FopPDFImage implements PDFImage {

    private FopImage fopImage;
    private PDFICCStream pdfICCStream = null;
    private PDFFilter pdfFilter = null;
    private String maskRef;
    private String softMaskRef;
    private boolean isPS = false;
    private boolean isCCF = false;
    private boolean isDCT = false;
    private String key;

    /**
     * Creates a new PDFImage from a FopImage
     * @param image Image
     * @param key XObject key
     */
    public FopPDFImage(FopImage image, String key) {
        fopImage = image;
        this.key = key;
        isPS = (fopImage instanceof EPSImage);
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#getKey()
     */
    public String getKey() {
        // key to look up XObject
        return this.key;
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#setup(PDFDocument)
     */
    public void setup(PDFDocument doc) {
        if ("image/jpeg".equals(fopImage.getMimeType())) {
            pdfFilter = new DCTFilter();
            pdfFilter.setApplied(true);
            isDCT = true;

        } else if ("image/tiff".equals(fopImage.getMimeType())
                    && fopImage instanceof TIFFImage) {
            TIFFImage tiffImage = (TIFFImage) fopImage;
            if (tiffImage.getStripCount() == 1) {
                int comp = tiffImage.getCompression();
                if (comp == 1) {
                    // Nothing to do
                } else if (comp == 3) {
                    pdfFilter = new CCFFilter();
                    pdfFilter.setApplied(true);
                    isCCF = true;
                } else if (comp == 4) {
                    pdfFilter = new CCFFilter();
                    pdfFilter.setApplied(true);
                    ((CCFFilter)pdfFilter).setDecodeParms("<< /K -1 /Columns " 
                        + tiffImage.getWidth() + " >>");
                    isCCF = true;
                } else if (comp == 6) {
                    pdfFilter = new DCTFilter();
                    pdfFilter.setApplied(true);
                    isDCT = true;
                }
            }
        }
        if (isPS || isDCT || isCCF) {
            fopImage.load(FopImage.ORIGINAL_DATA);
        } else {
            fopImage.load(FopImage.BITMAP);
        }
        ICC_Profile prof = fopImage.getICCProfile();
        PDFColorSpace pdfCS = toPDFColorSpace(fopImage.getColorSpace());
        if (prof != null) {
            pdfICCStream = doc.getFactory().makePDFICCStream();
            pdfICCStream.setColorSpace(prof, pdfCS);
        }
        //Handle transparency mask if applicable
        if (fopImage.hasSoftMask()) {
            byte [] softMask = fopImage.getSoftMask();
            if (softMask == null) {
                return;
            }
            BitmapImage fopimg = new BitmapImage
                ("Mask:" + key, fopImage.getWidth(), fopImage.getHeight(), 
                 softMask, null);
            fopimg.setColorSpace(new PDFColorSpace(PDFColorSpace.DEVICE_GRAY));
            PDFXObject xobj = doc.addImage(null, fopimg);
            softMaskRef = xobj.referencePDF();
        }
        if (doc.getPDFAMode().isPDFA1LevelB()) {
            if (pdfCS != null
                    && pdfCS.getColorSpace() != PDFColorSpace.DEVICE_RGB 
                    && pdfCS.getColorSpace() != PDFColorSpace.DEVICE_GRAY
                    && prof == null) {
                //See PDF/A-1, ISO 19005:1:2005(E), 6.2.3.3
                //FOP is currently restricted to DeviceRGB if PDF/A-1 is active.
                throw new PDFConformanceException(
                        "PDF/A-1 does not allow mixing DeviceRGB and DeviceCMYK.");
            }
        }
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#getWidth()
     */
    public int getWidth() {
        return fopImage.getWidth();
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#getHeight()
     */
    public int getHeight() {
        return fopImage.getHeight();
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#getColorSpace()
     */
    public PDFColorSpace getColorSpace() {
        // DeviceGray, DeviceRGB, or DeviceCMYK
        if (isCCF || isDCT || isPS) {
            return toPDFColorSpace(fopImage.getColorSpace());
        } else {
            return toPDFColorSpace(ColorSpace.getInstance(ColorSpace.CS_sRGB));
        }
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#getBitsPerPixel()
     */
    public int getBitsPerPixel() {
        if (isCCF) {
            return fopImage.getBitsPerPixel();
        } else {
            return 8; //TODO This is suboptimal, handling everything as RGB
            //The image wrappers can mostly only return RGB bitmaps right now. This should
            //be improved so the renderers can deal directly with RenderedImage instances.
        }
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#isTransparent()
     */
    public boolean isTransparent() {
        return fopImage.isTransparent();
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#getTransparentColor()
     */
    public PDFColor getTransparentColor() {
        return new PDFColor(fopImage.getTransparentColor().getRed(),
                            fopImage.getTransparentColor().getGreen(),
                            fopImage.getTransparentColor().getBlue());
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#getMask()
     */
    public String getMask() {
        return maskRef;
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#getSoftMask()
     */
    public String getSoftMask() {
        return softMaskRef;
    }

    /** @return true for CMYK images generated by Adobe Photoshop */
    public boolean isInverted() {
        return fopImage.isInverted();
    }
    
    /**
     * @see org.apache.fop.pdf.PDFImage#isPS()
     */
    public boolean isPS() {
        return isPS;
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#getPDFFilter()
     */
    public PDFFilter getPDFFilter() {
        return pdfFilter;
    }
    
    /**
     * @see org.apache.fop.pdf.PDFImage#outputContents(OutputStream)
     */
    public void outputContents(OutputStream out) throws IOException {
        if (isPS) {
            outputPostScriptContents(out);
        } else {
            if (fopImage.getBitmapsSize() > 0) {
                out.write(fopImage.getBitmaps());
            } else {
                out.write(fopImage.getRessourceBytes());
            }
        }
    }

    /**
     * Serializes an EPS image to an OutputStream.
     * @param out OutputStream to write to
     * @throws IOException in case of an I/O problem
     */
    protected void outputPostScriptContents(OutputStream out) throws IOException {
        EPSImage epsImage = (EPSImage) fopImage;
        int[] bbox = epsImage.getBBox();
        int bboxw = bbox[2] - bbox[0];
        int bboxh = bbox[3] - bbox[1];

        // delegate the stream work to PDFStream
        //PDFStream imgStream = new PDFStream(0);

        StringBuffer preamble = new StringBuffer();
        preamble.append("%%BeginDocument: " + epsImage.getDocName() + "\n");

        preamble.append("userdict begin                 % Push userdict on dict stack\n");
        preamble.append("/PreEPS_state save def\n");
        preamble.append("/dict_stack countdictstack def\n");
        preamble.append("/ops_count count 1 sub def\n");
        preamble.append("/showpage {} def\n");


        preamble.append((double)(1f / (double) bboxw) + " "
                      + (double)(1f / (double) bboxh) + " scale\n");
        preamble.append(-bbox[0] + " " + (-bbox[1]) + " translate\n");
        preamble.append(bbox[0] + " " + bbox[1] + " "
                        + bboxw + " " + bboxh + " rectclip\n");
        preamble.append("newpath\n");

        StringBuffer post = new StringBuffer();
        post.append("%%EndDocument\n");
        post.append("count ops_count sub {pop} repeat\n");
        post.append("countdictstack dict_stack sub {end} repeat\n");
        post.append("PreEPS_state restore\n");
        post.append("end % userdict\n");

        //Write Preamble
        out.write(PDFDocument.encode(preamble.toString()));
        //Write EPS contents
        out.write(((EPSImage)fopImage).getEPSImage());
        //Writing trailer
        out.write(PDFDocument.encode(post.toString()));
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#getICCStream()
     */
    public PDFICCStream getICCStream() {
        return pdfICCStream;
    }

    /**
     * Converts a ColorSpace object to a PDFColorSpace object.
     * @param cs ColorSpace instance
     * @return PDFColorSpace new converted object
     */
    public static PDFColorSpace toPDFColorSpace(ColorSpace cs) {
        if (cs == null) {
            return null;
        }

        PDFColorSpace pdfCS = new PDFColorSpace(0);
        switch(cs.getType()) {
            case ColorSpace.TYPE_CMYK:
                pdfCS.setColorSpace(PDFColorSpace.DEVICE_CMYK);
            break;
            case ColorSpace.TYPE_RGB:
                pdfCS.setColorSpace(PDFColorSpace.DEVICE_RGB);
            break;
            case ColorSpace.TYPE_GRAY:
                pdfCS.setColorSpace(PDFColorSpace.DEVICE_GRAY);
            break;
        }
        return pdfCS;
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#getFilterHint()
     */
    public String getFilterHint() {
        if (isPS) {
            return PDFFilterList.CONTENT_FILTER;
        } else if (isDCT) {
            return PDFFilterList.JPEG_FILTER;
        } else if (isCCF) {
            return PDFFilterList.TIFF_FILTER;
        } else {
            return PDFFilterList.IMAGE_FILTER;
        }
    }

}

