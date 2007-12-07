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

package org.apache.fop.render.pdf;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlgraphics.image.codec.png.PNGEncodeParam;
import org.apache.xmlgraphics.image.codec.png.PNGImageEncoder;
import org.apache.xmlgraphics.ps.ImageEncodingHelper;

import org.apache.fop.image2.impl.ImageRendered;
import org.apache.fop.pdf.AlphaRasterImage;
import org.apache.fop.pdf.FlateFilter;
import org.apache.fop.pdf.PDFArray;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFDeviceColorSpace;
import org.apache.fop.pdf.PDFDictionary;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFFilter;
import org.apache.fop.pdf.PDFFilterException;
import org.apache.fop.pdf.PDFFilterList;
import org.apache.fop.pdf.PDFName;
import org.apache.fop.pdf.PDFReference;
import org.apache.fop.pdf.PDFWritable;

/**
 * PDFImage implementation for the PDF renderer which handles RenderedImages.
 */
public class ImageRenderedAdapter extends AbstractImageAdapter {

    /** logging instance */
    private static Log log = LogFactory.getLog(ImageRenderedAdapter.class);

    private ImageEncodingHelper encodingHelper;
    
    private PDFFilter pdfFilter = null;
    private String maskRef;
    private PDFReference softMask;
    private boolean usePredictors = false;

    /**
     * Creates a new PDFImage from an Image instance.
     * @param image the image
     * @param key XObject key
     */
    public ImageRenderedAdapter(ImageRendered image, String key) {
        super(image, key);
        this.encodingHelper = new ImageEncodingHelper(image.getRenderedImage());
    }

    /**
     * Returns the ImageRendered instance for this adapter.
     * @return the ImageRendered instance
     */
    public ImageRendered getImage() {
        return ((ImageRendered)this.image);
    }
    
    private ColorModel getEffectiveColorModel() {
        return encodingHelper.getEncodedColorModel();
    }
    
    /** {@inheritDoc} */
    protected ColorSpace getImageColorSpace() {
        return getEffectiveColorModel().getColorSpace();
    }

    /** {@inheritDoc} */
    public void setup(PDFDocument doc) {
        RenderedImage ri = getImage().getRenderedImage();
        ColorModel cm = getEffectiveColorModel();

        if (usePredictors) {
            //Experiment hasn't worked, yet, so it's disabled
            FlateFilter flate;
            try {
                flate = new FlateFilter();
                flate.setApplied(true);
                flate.setPredictor(FlateFilter.PREDICTION_PNG_OPT);
                flate.setColors(cm.getNumComponents());
                flate.setColumns(ri.getWidth());
                flate.setBitsPerComponent(getBitsPerComponent());
            } catch (PDFFilterException e) {
                throw new RuntimeException("Unexpected bug while configuring FlateFilter", e);
            }
            this.pdfFilter = flate;
        }

        super.setup(doc);
        
        //Handle transparency mask if applicable
        ColorModel orgcm = ri.getColorModel(); 
        if (orgcm.hasAlpha() && orgcm.getTransparency() == ColorModel.TRANSLUCENT) {
            doc.getProfile().verifyTransparencyAllowed(image.getInfo().getOriginalURI());
            //TODO Implement code to combine image with background color if transparency is not
            //allowed (need BufferedImage support for that)
            
            AlphaRasterImage alphaImage = new AlphaRasterImage("Mask:" + getKey(), ri);
            this.softMask = doc.addImage(null, alphaImage).makeReference();

        }
    }

    /** {@inheritDoc} */
    public PDFDeviceColorSpace getColorSpace() {
        // DeviceGray, DeviceRGB, or DeviceCMYK
        return toPDFColorSpace(getEffectiveColorModel().getColorSpace());
    }

    /** {@inheritDoc} */
    public int getBitsPerComponent() {
        ColorModel cm = getEffectiveColorModel();
        if (cm instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel)cm;
            return icm.getComponentSize(0);
        } else {
            return cm.getComponentSize(0);
        }
    }

    /** {@inheritDoc} */
    public boolean isTransparent() {
        ColorModel cm = getEffectiveColorModel();
        if (cm instanceof IndexColorModel) {
            if (cm.getTransparency() == IndexColorModel.TRANSLUCENT) {
                return true;
            }
        }
        return (getImage().getTransparentColor() != null);
    }
    
    private static Integer getIndexOfFirstTransparentColorInPalette(RenderedImage image) {
        ColorModel cm = image.getColorModel();
        if (cm instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel)cm;
            //Identify the transparent color in the palette
            byte[] alphas = new byte[icm.getMapSize()];
            byte[] reds = new byte[icm.getMapSize()];
            byte[] greens = new byte[icm.getMapSize()];
            byte[] blues = new byte[icm.getMapSize()];
            icm.getAlphas(alphas);
            icm.getReds(reds);
            icm.getGreens(greens);
            icm.getBlues(blues);
            for (int i = 0;
                    i < ((IndexColorModel) cm).getMapSize();
                    i++) {
                if ((alphas[i] & 0xFF) == 0) {
                    /*
                    Color transparentColor = new Color(
                            (int)(reds[i] & 0xFF),
                            (int)(greens[i] & 0xFF),
                            (int)(blues[i] & 0xFF));
                    */
                    return new Integer(i);
                }
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    public PDFColor getTransparentColor() {
        ColorModel cm = getEffectiveColorModel();
        if (cm instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel)cm;
            if (cm.getTransparency() == IndexColorModel.TRANSLUCENT) {
                int transPixel = icm.getTransparentPixel();
                return new PDFColor(
                        icm.getRed(transPixel),
                        icm.getGreen(transPixel),
                        icm.getBlue(transPixel));
            }
        }
        return new PDFColor(getImage().getTransparentColor());
    }

    /** {@inheritDoc} */
    public String getMask() {
        return maskRef;
    }

    /** {@inheritDoc} */
    public String getSoftMask() {
        return softMask.toInlinePDFString();
    }

    /** {@inheritDoc} */
    public PDFReference getSoftMaskReference() {
        return softMask;
    }
    
    /** {@inheritDoc} */
    public PDFFilter getPDFFilter() {
        return pdfFilter;
    }
    
    /** {@inheritDoc} */
    public void outputContents(OutputStream out) throws IOException {
        if (usePredictors) {
            PNGEncodeParam param = PNGEncodeParam.getDefaultEncodeParam(
                    getImage().getRenderedImage());
            EmbeddedPNGEncoder encoder = new EmbeddedPNGEncoder(out, param);
            encoder.encode(getImage().getRenderedImage());
        } else {
            encodingHelper.encode(out);
        }
    }

    private static final int MAX_HIVAL = 255;
    
    /** {@inheritDoc} */
    public void populateXObjectDictionary(PDFDictionary dict) {
        ColorModel cm = getEffectiveColorModel();
        if (cm instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel)cm;
            PDFArray indexed = new PDFArray();
            indexed.add(new PDFName("Indexed"));
            
            if (icm.getColorSpace().getType() != ColorSpace.TYPE_RGB) {
                log.warn("Indexed color space is not using RGB as base color space."
                        + " The image may not be handled correctly."
                        + " Base color space: " + icm.getColorSpace() 
                        + " Image: " + image.getInfo());
            }
            indexed.add(new PDFName(toPDFColorSpace(icm.getColorSpace()).getName()));
            int c = icm.getMapSize();
            int hival = c - 1;
            if (hival > MAX_HIVAL) {
                throw new UnsupportedOperationException("hival must not go beyond " + MAX_HIVAL);
            }
            indexed.add(new Integer(hival));
            final StringBuffer sb = new StringBuffer("<");
            int[] palette = new int[c];
            icm.getRGBs(palette);
            for (int i = 0; i < c; i++) {
                if (i > 0) {
                    sb.append(" ");
                }
                //TODO Probably doesn't work for non RGB based color spaces
                rgb2Hex(palette[i], sb);
            }
            sb.append(">");
            indexed.add(new PDFWritable() {
                public String toInlinePDFString() {
                    //Work-around String escaping. Maybe a little hacky.
                    return sb.toString();
                }
            });

            dict.put("ColorSpace", indexed);
            dict.put("BitsPerComponent", icm.getPixelSize());
            
            Integer index = getIndexOfFirstTransparentColorInPalette(getImage().getRenderedImage());
            if (index != null) {
                PDFArray mask = new PDFArray();
                mask.add(index);
                mask.add(index);
                dict.put("Mask", mask);
            }
        }
    }
    
    private static final char[] HEX = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    private static void rgb2Hex(int rgb, StringBuffer sb) {
        for (int i = 5; i >= 0; i--) {
            int shift = i * 4;
            int n = (rgb & (15 << shift)) >> shift;
            sb.append(HEX[n % 16]);
        }
    }
    
    /** {@inheritDoc} */
    public String getFilterHint() {
        if (usePredictors) {
            return PDFFilterList.PRECOMPRESSED_FILTER;
        } else {
            return PDFFilterList.IMAGE_FILTER;
        }
    }

    private static class EmbeddedPNGEncoder extends PNGImageEncoder {

        public EmbeddedPNGEncoder(OutputStream output, PNGEncodeParam param) {
            super(output, param);
        }
        
        public void encode(RenderedImage image) throws IOException {
            prepareEncoding(image);
            
            DeflaterOutputStream dos
                = new DeflaterOutputStream(getOutputStream(),
                        new Deflater(Deflater.DEFAULT_COMPRESSION));

            // Future work - don't convert entire image to a Raster It
            // might seem that you could just call image.getData() but
            // 'BufferedImage.subImage' doesn't appear to set the Width
            // and height properly of the Child Raster, so the Raster
            // you get back here appears larger than it should.
            // This solves that problem by bounding the raster to the
            // image's bounds...
            /*
            Raster ras = image.getData(new Rectangle(image.getMinX(), 
                                                     image.getMinY(),
                                                     image.getWidth(),
                                                     image.getHeight()));
            */
            Raster ras = image.getData();

            boolean skipAlpha = image.getColorModel().hasAlpha();
            if (skipAlpha) {
                int numBands = ras.getNumBands() - 1;
                int[] bandList = new int[numBands];
                for (int i = 0; i < numBands; i++) {
                    bandList[i] = i;
                }
                ras = ras.createChild(0, 0,
                                      ras.getWidth(), ras.getHeight(),
                                      0, 0,
                                      bandList);
            }

            encodePass(dos, ras, 0, 0, 1, 1);

            dos.finish();
        }
    }

}

