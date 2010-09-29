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

package org.apache.fop.svg;

import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fonts.CustomFontCollection;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontEventListener;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontInfoConfigurator;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.fonts.FontManagerConfigurator;
import org.apache.fop.fonts.FontResolver;
import org.apache.fop.fonts.base14.Base14FontCollection;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.render.pdf.PDFRendererConfigurator;

/**
 * Configurator class for PDFDocumentGraphics2D.
 */
public class PDFDocumentGraphics2DConfigurator {

    /**
     * Configures a PDFDocumentGraphics2D instance using an Avalon Configuration object.
     * @param graphics the PDFDocumentGraphics2D instance
     * @param cfg the configuration
     * @throws ConfigurationException if an error occurs while configuring the object
     */
    public void configure(PDFDocumentGraphics2D graphics, Configuration cfg)
            throws ConfigurationException {
        PDFDocument pdfDoc = graphics.getPDFDocument();

        //Filter map
        pdfDoc.setFilterMap(
                PDFRendererConfigurator.buildFilterMapFromConfiguration(cfg));

        //Fonts
        try {
            FontInfo fontInfo = createFontInfo(cfg);
            graphics.setFontInfo(fontInfo);
        } catch (FOPException e) {
            throw new ConfigurationException("Error while setting up fonts", e);
        }
    }

    /**
     * Creates the {@link FontInfo} instance for the given configuration.
     * @param cfg the configuration
     * @return the font collection
     * @throws FOPException if an error occurs while setting up the fonts
     */
    public static FontInfo createFontInfo(Configuration cfg) throws FOPException {
        FontInfo fontInfo = new FontInfo();
        final boolean strict = false;
        FontResolver fontResolver = FontManager.createMinimalFontResolver();
        //TODO The following could be optimized by retaining the FontManager somewhere
        FontManager fontManager = new FontManager();
        if (cfg != null) {
            FontManagerConfigurator fmConfigurator = new FontManagerConfigurator(cfg);
            fmConfigurator.configure(fontManager, strict);
        }

        List fontCollections = new java.util.ArrayList();
        fontCollections.add(new Base14FontCollection(fontManager.isBase14KerningEnabled()));

        if (cfg != null) {
            //TODO Wire in the FontEventListener
            FontEventListener listener = null; //new FontEventAdapter(eventBroadcaster);
            FontInfoConfigurator fontInfoConfigurator
                = new FontInfoConfigurator(cfg, fontManager, fontResolver, listener, strict);
            List/*<EmbedFontInfo>*/ fontInfoList = new java.util.ArrayList/*<EmbedFontInfo>*/();
            fontInfoConfigurator.configure(fontInfoList);
            fontCollections.add(new CustomFontCollection(fontResolver, fontInfoList));
        }
        fontManager.setup(fontInfo,
                (FontCollection[])fontCollections.toArray(
                        new FontCollection[fontCollections.size()]));
        return fontInfo;
    }

}
