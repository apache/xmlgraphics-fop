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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.Image;

import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFConformanceException;
import org.apache.fop.pdf.PDFDeviceColorSpace;
import org.apache.fop.pdf.PDFDictionary;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFICCBasedColorSpace;
import org.apache.fop.pdf.PDFICCStream;
import org.apache.fop.pdf.PDFImage;
import org.apache.fop.pdf.PDFReference;
import org.apache.fop.util.ColorProfileUtil;

/**
 * Abstract PDFImage implementation for the PDF renderer.
 */
public abstract class AbstractImageAdapter implements PDFImage {

    /** logging instance */
    private static Log log = LogFactory.getLog(AbstractImageAdapter.class);

    private String key;
    /** the image */
    protected Image image;

    private PDFICCStream pdfICCStream = null;

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
            if (cs == null && desc.startsWith("sRGB")) {
                //It's the default sRGB profile which we mapped to DefaultRGB in PDFRenderer
                cs = doc.getResources().getColorSpace("DefaultRGB");
            }
            if (cs == null) {
                // sRGB hasn't been set up for the PDF document
                // so install but don't set to DefaultRGB
                cs = PDFICCBasedColorSpace.setupsRGBColorSpace(doc);
            }            
            pdfICCStream = cs.getICCStream();
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

}

