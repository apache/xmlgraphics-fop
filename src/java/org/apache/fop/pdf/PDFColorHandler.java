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

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.text.DecimalFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.java2d.color.ColorExt;
import org.apache.xmlgraphics.java2d.color.ColorUtil;
import org.apache.xmlgraphics.java2d.color.DeviceCMYKColorSpace;
import org.apache.xmlgraphics.java2d.color.NamedColorSpace;

import org.apache.fop.util.ColorProfileUtil;
import org.apache.fop.util.DecimalFormatCache;

/**
 * This class handles the registration of color spaces and the generation of PDF code to select
 * the right colors given a {@link Color} instance.
 */
public class PDFColorHandler {

    private Log log = LogFactory.getLog(PDFColorHandler.class);

    private PDFResources resources;

    public PDFColorHandler(PDFResources resources) {
        this.resources = resources;
    }

    private PDFDocument getDocument() {
        return this.resources.getDocumentSafely();
    }

    /**
     * Generates code to select the given color and handles the registration of color spaces in
     * PDF where necessary.
     * @param codeBuffer the target buffer to receive the color selection code
     * @param color the color
     * @param fill true for fill color, false for stroke color
     */
    public void establishColor(StringBuffer codeBuffer, Color color, boolean fill) {
        if (color instanceof ColorExt) {
            ColorExt colExt = (ColorExt)color;
            //Alternate colors have priority
            Color[] alt = colExt.getAlternateColors();
            for (int i = 0, c = alt.length; i < c; i++) {
                Color col = alt[i];
                boolean established = establishColorFromColor(codeBuffer, col, fill);
                if (established) {
                    return;
                }
            }
        }

        //Fallback
        establishColorFromColor(codeBuffer, color, fill);
    }

    private boolean establishColorFromColor(StringBuffer codeBuffer, Color color, boolean fill) {
        ColorSpace cs = color.getColorSpace();
        if (cs instanceof DeviceCMYKColorSpace) {
            establishDeviceCMYK(codeBuffer, color, fill);
            return true;
        } else if (!cs.isCS_sRGB()) {
            if (cs instanceof ICC_ColorSpace) {
                PDFICCBasedColorSpace pdfcs = getICCBasedColorSpace((ICC_ColorSpace)cs);
                establishColor(codeBuffer, pdfcs, color, fill);
                return true;
            } else if (cs instanceof NamedColorSpace) {
                PDFSeparationColorSpace sepcs = getSeparationColorSpace((NamedColorSpace)cs);
                establishColor(codeBuffer, sepcs, color, fill);
                return true;
            }
        }
        //Fallback (RGB) Color
        establishDeviceRGB(codeBuffer, color, fill);
        return true;
    }

    private PDFICCBasedColorSpace getICCBasedColorSpace(ICC_ColorSpace cs) {
        ICC_Profile profile = cs.getProfile();
        String desc = ColorProfileUtil.getICCProfileDescription(profile);
        if (log.isDebugEnabled()) {
            log.trace("ICC profile encountered: " + desc);
        }
        PDFICCBasedColorSpace pdfcs = this.resources.getICCColorSpaceByProfileName(desc);
        if (pdfcs == null) {
            //color space is not in the PDF, yet
            PDFFactory factory = getDocument().getFactory();
            PDFICCStream pdfICCStream = factory.makePDFICCStream();
            PDFDeviceColorSpace altSpace = PDFDeviceColorSpace.toPDFColorSpace(cs);
            pdfICCStream.setColorSpace(profile, altSpace);
            pdfcs = factory.makeICCBasedColorSpace(null, desc, pdfICCStream);
        }
        return pdfcs;
    }

    private PDFSeparationColorSpace getSeparationColorSpace(NamedColorSpace cs) {
        PDFName colorName = new PDFName(cs.getColorName());
        PDFSeparationColorSpace sepcs = (PDFSeparationColorSpace)this.resources.getColorSpace(
                colorName);
        if (sepcs == null) {
            //color space is not in the PDF, yet
            PDFFactory factory = getDocument().getFactory();
            sepcs = factory.makeSeparationColorSpace(null, cs);
        }
        return sepcs;
    }

    private void establishColor(StringBuffer codeBuffer,
            PDFColorSpace pdfcs, Color color, boolean fill) {
        codeBuffer.append(new PDFName(pdfcs.getName()));
        if (fill)  {
            codeBuffer.append(" cs ");
        } else {
            codeBuffer.append(" CS ");
        }
        writeColor(codeBuffer, color, pdfcs.getNumComponents(), (fill ? "sc" : "SC"));
    }

    private void establishDeviceRGB(StringBuffer codeBuffer, Color color, boolean fill) {
        float[] comps;
        if (color.getColorSpace().isCS_sRGB()) {
            comps = color.getColorComponents(null);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Converting color to sRGB as a fallback: " + color);
            }
            ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            comps = color.getColorComponents(sRGB, null);
        }
        if (ColorUtil.isGray(color)) {
            comps = new float[] {comps[0]}; //assuming that all components are the same
            writeColor(codeBuffer, comps, 1, (fill ? "g" : "G"));
        } else {
            writeColor(codeBuffer, comps, 3, (fill ? "rg" : "RG"));
        }
    }

    private void establishDeviceCMYK(StringBuffer codeBuffer, Color color, boolean fill) {
        writeColor(codeBuffer, color, 4, (fill ? "k" : "K"));
    }

    private void writeColor(StringBuffer codeBuffer, Color color, int componentCount,
            String command) {
        float[] comps = color.getColorComponents(null);
        writeColor(codeBuffer, comps, componentCount, command);
    }

    private void writeColor(StringBuffer codeBuffer, float[] comps, int componentCount,
            String command) {
        if (comps.length != componentCount) {
            throw new IllegalStateException("Color with unexpected component count encountered");
        }
        DecimalFormat df = DecimalFormatCache.getDecimalFormat(4);
        for (int i = 0, c = comps.length; i < c; i++) {
            codeBuffer.append(df.format(comps[i])).append(" ");
        }
        codeBuffer.append(command).append("\n");
    }

}
