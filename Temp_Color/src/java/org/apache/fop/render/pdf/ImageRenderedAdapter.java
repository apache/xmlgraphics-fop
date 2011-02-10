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
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.impl.ImageRendered;
import org.apache.xmlgraphics.ps.ImageEncodingHelper;

import org.apache.fop.pdf.AlphaRasterImage;
import org.apache.fop.pdf.PDFArray;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFDeviceColorSpace;
import org.apache.fop.pdf.PDFDictionary;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFFilter;
import org.apache.fop.pdf.PDFFilterList;
import org.apache.fop.pdf.PDFName;
import org.apache.fop.pdf.PDFReference;

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

    /**
     * Creates a new PDFImage from an Image instance.
     * @param image the image
     * @param key XObject key
     */
    public ImageRenderedAdapter(ImageRendered image, String key) {
        super(image, key);
        this.encodingHelper = new ImageEncodingHelper(image.getRenderedImage(), true);
    }

    /**
     * Returns the ImageRendered instance for this adapter.
     * @return the ImageRendered instance
     */
    public ImageRendered getImage() {
        return ((ImageRendered)this.image);
    }

    /** {@inheritDoc} */
    public int getWidth() {
        RenderedImage ri = getImage().getRenderedImage();
        return ri.getWidth();
    }

    /** {@inheritDoc} */
    public int getHeight() {
        RenderedImage ri = getImage().getRenderedImage();
        return ri.getHeight();
    }

    private ColorModel getEffectiveColorModel() {
        return encodingHelper.getEncodedColorModel();
    }

    /** {@inheritDoc} */
    protected ColorSpace getImageColorSpace() {
        return getEffectiveColorModel().getColorSpace();
    }

    /** {@inheritDoc} */
    protected ICC_Profile getEffectiveICCProfile() {
        ColorSpace cs = getImageColorSpace();
        if (cs instanceof ICC_ColorSpace) {
            ICC_ColorSpace iccSpace = (ICC_ColorSpace)cs;
            return iccSpace.getProfile();
        } else {
            return null;
        }
    }

    /** {@inheritDoc} */
    public void setup(PDFDocument doc) {
        RenderedImage ri = getImage().getRenderedImage();

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
    public PDFReference getSoftMaskReference() {
        return softMask;
    }

    /** {@inheritDoc} */
    public PDFFilter getPDFFilter() {
        return pdfFilter;
    }

    /** {@inheritDoc} */
    public void outputContents(OutputStream out) throws IOException {
        encodingHelper.encode(out);
    }

    private static final int MAX_HIVAL = 255;

    /** {@inheritDoc} */
    public void populateXObjectDictionary(PDFDictionary dict) {
        ColorModel cm = getEffectiveColorModel();
        if (cm instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel)cm;
            PDFArray indexed = new PDFArray(dict);
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
            int[] palette = new int[c];
            icm.getRGBs(palette);
            ByteArrayOutputStream baout = new ByteArrayOutputStream();
            for (int i = 0; i < c; i++) {
                //TODO Probably doesn't work for non RGB based color spaces
                //See log warning above
                int entry = palette[i];
                baout.write((entry & 0xFF0000) >> 16);
                baout.write((entry & 0xFF00) >> 8);
                baout.write(entry & 0xFF);
            }
            indexed.add(baout.toByteArray());

            dict.put("ColorSpace", indexed);
            dict.put("BitsPerComponent", icm.getPixelSize());

            Integer index = getIndexOfFirstTransparentColorInPalette(getImage().getRenderedImage());
            if (index != null) {
                PDFArray mask = new PDFArray(dict);
                mask.add(index);
                mask.add(index);
                dict.put("Mask", mask);
            }
        }
    }

    /** {@inheritDoc} */
    public String getFilterHint() {
        return PDFFilterList.IMAGE_FILTER;
    }

}

