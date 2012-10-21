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
import java.awt.color.ICC_Profile;
import java.awt.image.IndexColorModel;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.java2d.color.profile.ColorProfileUtil;

import org.apache.fop.pdf.PDFArray;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFConformanceException;
import org.apache.fop.pdf.PDFDeviceColorSpace;
import org.apache.fop.pdf.PDFDictionary;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFICCBasedColorSpace;
import org.apache.fop.pdf.PDFICCStream;
import org.apache.fop.pdf.PDFImage;
import org.apache.fop.pdf.PDFName;
import org.apache.fop.pdf.PDFReference;

/**
 * Abstract PDFImage implementation for the PDF renderer.
 */
public abstract class AbstractImageAdapter implements PDFImage {

    /** logging instance */
    private static Log log = LogFactory.getLog(AbstractImageAdapter.class);

    private String key;
    /** the image */
    protected Image image;

    private PDFICCStream pdfICCStream;

    private static final int MAX_HIVAL = 255;

    private boolean multipleFiltersAllowed = true;

    /**
     * Creates a new PDFImage from an Image instance.
     * @param image the image
     * @param key XObject key
     */
    public AbstractImageAdapter(Image image, String key) {
        this.image = image;
        this.key = key;
        if (log.isDebugEnabled()) {
            log.debug("New ImageAdapter created for key: " + key);
        }
    }

    /** {@inheritDoc} */
    public String getKey() {
        // key to look up XObject
        return this.key;
    }

    /**
     * Returns the image's color space.
     * @return the color space
     */
    protected ColorSpace getImageColorSpace() {
        return image.getColorSpace();
    }

    /** {@inheritDoc} */
    public void setup(PDFDocument doc) {
        ICC_Profile prof = getEffectiveICCProfile();
        PDFDeviceColorSpace pdfCS = toPDFColorSpace(getImageColorSpace());
        if (prof != null) {
            pdfICCStream = setupColorProfile(doc, prof, pdfCS);
        } else if (issRGB()) {
          pdfICCStream = setupsRGBColorProfile(doc);
        }
        if (doc.getProfile().getPDFAMode().isPDFA1LevelB()) {
            if (pdfCS != null
                    && pdfCS.getColorSpace() != PDFDeviceColorSpace.DEVICE_RGB
                    && pdfCS.getColorSpace() != PDFDeviceColorSpace.DEVICE_GRAY
                    && prof == null) {
                //See PDF/A-1, ISO 19005:1:2005(E), 6.2.3.3
                //FOP is currently restricted to DeviceRGB if PDF/A-1 is active.
                throw new PDFConformanceException(
                        "PDF/A-1 does not allow mixing DeviceRGB and DeviceCMYK: "
                            + image.getInfo());
            }
        }
    }

    /**
     * Returns the effective ICC profile for the image.
     * @return an ICC profile or null
     */
    protected ICC_Profile getEffectiveICCProfile() {
        return image.getICCProfile();
    }

    protected boolean issRGB() {
        return false;
    }

    private static PDFICCStream getDefaultsRGBICCStream(PDFICCBasedColorSpace cs, PDFDocument doc, 
            String profileDesc) {
        if (cs == null) {
            if (profileDesc == null || !profileDesc.startsWith("sRGB")) {
                log.warn("The default sRGB profile was indicated,"
                    + " but the profile description does not match what was expected: "
                    + profileDesc);
            }
            //It's the default sRGB profile which we mapped to DefaultRGB in PDFRenderer
            cs = (PDFICCBasedColorSpace)doc.getResources().getColorSpace(new PDFName("DefaultRGB"));
        }
        if (cs == null) {
            // sRGB hasn't been set up for the PDF document
            // so install but don't set to DefaultRGB
            cs = PDFICCBasedColorSpace.setupsRGBColorSpace(doc);
        }
        return cs.getICCStream();
    }

    private static PDFICCStream setupsRGBColorProfile(PDFDocument doc) {
        PDFICCBasedColorSpace cs = doc.getResources().getICCColorSpaceByProfileName("sRGB");
        return getDefaultsRGBICCStream(cs, doc, "sRGB");
    }

    private static PDFICCStream setupColorProfile(PDFDocument doc,
                ICC_Profile prof, PDFDeviceColorSpace pdfCS) {
        boolean defaultsRGB = ColorProfileUtil.isDefaultsRGB(prof);
        String desc = ColorProfileUtil.getICCProfileDescription(prof);
        if (log.isDebugEnabled()) {
            log.debug("Image returns ICC profile: " + desc + ", default sRGB=" + defaultsRGB);
        }
        PDFICCBasedColorSpace cs = doc.getResources().getICCColorSpaceByProfileName(desc);
        PDFICCStream pdfICCStream;
        if (!defaultsRGB) {
            if (cs == null) {
                pdfICCStream = doc.getFactory().makePDFICCStream();
                pdfICCStream.setColorSpace(prof, pdfCS);
                cs = doc.getFactory().makeICCBasedColorSpace(null, null, pdfICCStream);
            } else {
                pdfICCStream = cs.getICCStream();
            }
        } else {
            pdfICCStream = getDefaultsRGBICCStream(cs, doc, desc);
        }
        return pdfICCStream;
    }

    /** {@inheritDoc} */
    public int getWidth() {
        return image.getSize().getWidthPx();
    }

    /** {@inheritDoc} */
    public int getHeight() {
        return image.getSize().getHeightPx();
    }

    /** {@inheritDoc} */
    public boolean isTransparent() {
        return false;
    }

    /** {@inheritDoc} */
    public PDFColor getTransparentColor() {
        return null;
    }

    /** {@inheritDoc} */
    public String getMask() {
        return null;
    }

    /** @return null (if not overridden) */
    public String getSoftMask() {
        return null;
    }

    /** {@inheritDoc} */
    public PDFReference getSoftMaskReference() {
        return null;
    }

    /** {@inheritDoc} */
    public boolean isInverted() {
        return false;
    }

    /** {@inheritDoc} */
    public boolean isPS() {
        return false;
    }

    /** {@inheritDoc} */
    public PDFICCStream getICCStream() {
        return pdfICCStream;
    }

    /** {@inheritDoc} */
    public void populateXObjectDictionary(PDFDictionary dict) {
        //nop
    }

    /**
     * This is to be used by populateXObjectDictionary() when the image is palette based.
     * @param dict the dictionary to fill in
     * @param icm the image color model
     */
    protected void populateXObjectDictionaryForIndexColorModel(PDFDictionary dict, IndexColorModel icm) {
        PDFArray indexed = new PDFArray(dict);
        indexed.add(new PDFName("Indexed"));
        if (icm.getColorSpace().getType() != ColorSpace.TYPE_RGB) {
            log.warn("Indexed color space is not using RGB as base color space."
                    + " The image may not be handled correctly." + " Base color space: "
                    + icm.getColorSpace() + " Image: " + image.getInfo());
        }
        indexed.add(new PDFName(toPDFColorSpace(icm.getColorSpace()).getName()));
        int c = icm.getMapSize();
        int hival = c - 1;
        if (hival > MAX_HIVAL) {
            throw new UnsupportedOperationException("hival must not go beyond " + MAX_HIVAL);
        }
        indexed.add(Integer.valueOf(hival));
        int[] palette = new int[c];
        icm.getRGBs(palette);
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        for (int i = 0; i < c; i++) {
            // TODO Probably doesn't work for non RGB based color spaces
            // See log warning above
            int entry = palette[i];
            baout.write((entry & 0xFF0000) >> 16);
            baout.write((entry & 0xFF00) >> 8);
            baout.write(entry & 0xFF);
        }
        indexed.add(baout.toByteArray());

        dict.put("ColorSpace", indexed);
        dict.put("BitsPerComponent", icm.getPixelSize());

        Integer index = getIndexOfFirstTransparentColorInPalette(icm);
        if (index != null) {
            PDFArray mask = new PDFArray(dict);
            mask.add(index);
            mask.add(index);
            dict.put("Mask", mask);
        }
    }

    private static Integer getIndexOfFirstTransparentColorInPalette(IndexColorModel icm) {
        byte[] alphas = new byte[icm.getMapSize()];
        byte[] reds = new byte[icm.getMapSize()];
        byte[] greens = new byte[icm.getMapSize()];
        byte[] blues = new byte[icm.getMapSize()];
        icm.getAlphas(alphas);
        icm.getReds(reds);
        icm.getGreens(greens);
        icm.getBlues(blues);
        for (int i = 0; i < icm.getMapSize(); i++) {
            if ((alphas[i] & 0xFF) == 0) {
                return Integer.valueOf(i);
            }
        }
        return null;
    }

    /**
     * Converts a ColorSpace object to a PDFColorSpace object.
     * @param cs ColorSpace instance
     * @return PDFColorSpace new converted object
     */
    public static PDFDeviceColorSpace toPDFColorSpace(ColorSpace cs) {
        if (cs == null) {
            return null;
        }

        PDFDeviceColorSpace pdfCS = new PDFDeviceColorSpace(0);
        switch (cs.getType()) {
            case ColorSpace.TYPE_CMYK:
                pdfCS.setColorSpace(PDFDeviceColorSpace.DEVICE_CMYK);
            break;
            case ColorSpace.TYPE_GRAY:
                pdfCS.setColorSpace(PDFDeviceColorSpace.DEVICE_GRAY);
                break;
            default:
                pdfCS.setColorSpace(PDFDeviceColorSpace.DEVICE_RGB);
        }
        return pdfCS;
    }

    /** {@inheritDoc} */
    public boolean multipleFiltersAllowed() {
        return multipleFiltersAllowed;
    }

    /**
     * Disallows multiple filters.
     */
    public void disallowMultipleFilters() {
        multipleFiltersAllowed = false;
    }

}

