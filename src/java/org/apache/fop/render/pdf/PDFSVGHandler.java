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

import java.io.OutputStream;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.fonts.FontInfo;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.pdf.PDFResourceContext;
import org.apache.fop.render.AbstractGenericSVGHandler;
import org.apache.fop.render.ImageHandlerUtil;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.RendererContextConstants;

/**
 * PDF XML handler for SVG (uses Apache Batik).
 * This handler handles XML for foreign objects when rendering to PDF.
 * It renders SVG to the PDF document using the PDFGraphics2D.
 * The properties from the PDF renderer are subject to change.
 */
public class PDFSVGHandler extends AbstractGenericSVGHandler
            implements PDFRendererContextConstants {

    /** logging instance */
    private static Log log = LogFactory.getLog(PDFSVGHandler.class);

    /**
     * Get the pdf information from the render context.
     *
     * @param context the renderer context
     * @return the pdf information retrieved from the context
     */
    public static PDFInfo getPDFInfo(RendererContext context) {
        PDFInfo pdfi = new PDFInfo();
        pdfi.pdfDoc = (PDFDocument)context.getProperty(PDF_DOCUMENT);
        pdfi.outputStream = (OutputStream)context.getProperty(OUTPUT_STREAM);
        //pdfi.pdfState = (PDFState)context.getProperty(PDF_STATE);
        pdfi.pdfPage = (PDFPage)context.getProperty(PDF_PAGE);
        pdfi.pdfContext = (PDFResourceContext)context.getProperty(PDF_CONTEXT);
        //pdfi.currentStream = (PDFStream)context.getProperty(PDF_STREAM);
        pdfi.width = ((Integer)context.getProperty(WIDTH)).intValue();
        pdfi.height = ((Integer)context.getProperty(HEIGHT)).intValue();
        pdfi.fi = (FontInfo)context.getProperty(PDF_FONT_INFO);
        pdfi.currentFontName = (String)context.getProperty(PDF_FONT_NAME);
        pdfi.currentFontSize = ((Integer)context.getProperty(PDF_FONT_SIZE)).intValue();
        pdfi.currentXPosition = ((Integer)context.getProperty(XPOS)).intValue();
        pdfi.currentYPosition = ((Integer)context.getProperty(YPOS)).intValue();
        pdfi.cfg = (Configuration)context.getProperty(HANDLER_CONFIGURATION);
        Map foreign = (Map)context.getProperty(RendererContextConstants.FOREIGN_ATTRIBUTES);
        pdfi.paintAsBitmap = ImageHandlerUtil.isConversionModeBitmap(foreign);
        return pdfi;
    }

    /**
     * PDF information structure for drawing the XML document.
     */
    public static class PDFInfo {
        /** see PDF_DOCUMENT */
        public PDFDocument pdfDoc;                              // CSOK: VisibilityModifier
        /** see OUTPUT_STREAM */
        public OutputStream outputStream;                       // CSOK: VisibilityModifier
        /** see PDF_PAGE */
        public PDFPage pdfPage;                                 // CSOK: VisibilityModifier
        /** see PDF_CONTEXT */
        public PDFResourceContext pdfContext;                   // CSOK: VisibilityModifier
        /** see PDF_STREAM */
        //public PDFStream currentStream;
        /** see PDF_WIDTH */
        public int width;                                       // CSOK: VisibilityModifier
        /** see PDF_HEIGHT */
        public int height;                                      // CSOK: VisibilityModifier
        /** see PDF_FONT_INFO */
        public FontInfo fi;                                     // CSOK: VisibilityModifier
        /** see PDF_FONT_NAME */
        public String currentFontName;                          // CSOK: VisibilityModifier
        /** see PDF_FONT_SIZE */
        public int currentFontSize;                             // CSOK: VisibilityModifier
        /** see PDF_XPOS */
        public int currentXPosition;                            // CSOK: VisibilityModifier
        /** see PDF_YPOS */
        public int currentYPosition;                            // CSOK: VisibilityModifier
        /** see PDF_HANDLER_CONFIGURATION */
        public Configuration cfg;                               // CSOK: VisibilityModifier
        /** true if SVG should be rendered as a bitmap instead of natively */
        public boolean paintAsBitmap;                           // CSOK: VisibilityModifier
    }

    /** {@inheritDoc} */
    public boolean supportsRenderer(Renderer renderer) {
        return false;
    }
}
