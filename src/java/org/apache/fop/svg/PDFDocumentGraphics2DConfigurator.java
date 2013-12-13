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

import java.io.File;
import java.net.URI;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.fonts.DefaultFontConfig;
import org.apache.fop.fonts.DefaultFontConfigurator;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.FontCacheManagerFactory;
import org.apache.fop.fonts.FontDetectorFactory;
import org.apache.fop.fonts.FontEventListener;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.fonts.FontSetup;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.render.pdf.PDFRendererConfig;
import org.apache.fop.render.pdf.PDFRendererConfig.PDFRendererConfigParser;

/**
 * Configurator class for PDFDocumentGraphics2D.
 */
public class PDFDocumentGraphics2DConfigurator {

    /**
     * Configures a PDFDocumentGraphics2D instance using an Avalon Configuration object.
     * @param graphics the PDFDocumentGraphics2D instance
     * @param cfg the configuration
     * @param useComplexScriptFeatures true if complex script features enabled
     * @throws ConfigurationException if an error occurs while configuring the object
     */
    public void configure(PDFDocumentGraphics2D graphics, Configuration cfg,
                          boolean useComplexScriptFeatures)
            throws ConfigurationException {
        PDFDocument pdfDoc = graphics.getPDFDocument();
        try {
            //Filter map
            PDFRendererConfig pdfConfig = new PDFRendererConfigParser().build(null, cfg);
            pdfDoc.setFilterMap(pdfConfig.getConfigOptions().getFilterMap());
        } catch (FOPException e) {
            throw new RuntimeException(e);
        }

        //Fonts
        try {
            FontInfo fontInfo = createFontInfo(cfg, useComplexScriptFeatures);
            graphics.setFontInfo(fontInfo);
        } catch (FOPException e) {
            throw new ConfigurationException("Error while setting up fonts", e);
        }
    }

    /**
     * Creates the {@link FontInfo} instance for the given configuration.
     * @param cfg the configuration
     * @param useComplexScriptFeatures true if complex script features enabled
     * @return the font collection
     * @throws FOPException if an error occurs while setting up the fonts
     */
    public static FontInfo createFontInfo(Configuration cfg, boolean useComplexScriptFeatures)
        throws FOPException {
        FontInfo fontInfo = new FontInfo();
        final boolean strict = false;
        if (cfg != null) {
            URI thisUri = new File(".").getAbsoluteFile().toURI();
            InternalResourceResolver resourceResolver
                    = ResourceResolverFactory.createDefaultInternalResourceResolver(thisUri);
            //TODO The following could be optimized by retaining the FontManager somewhere
            FontManager fontManager = new FontManager(resourceResolver, FontDetectorFactory.createDefault(),
                    FontCacheManagerFactory.createDefault());

            //TODO Make use of fontBaseURL, font substitution and referencing configuration
            //Requires a change to the expected configuration layout

            final FontEventListener listener = null;
            DefaultFontConfig.DefaultFontConfigParser parser
                    = new DefaultFontConfig.DefaultFontConfigParser();
            DefaultFontConfig fontInfoConfig = parser.parse(cfg, strict);
            DefaultFontConfigurator fontInfoConfigurator
                    = new DefaultFontConfigurator(fontManager, listener, strict);
            List<EmbedFontInfo> fontInfoList = fontInfoConfigurator.configure(fontInfoConfig);
            fontManager.saveCache();
            FontSetup.setup(fontInfo, fontInfoList, resourceResolver, useComplexScriptFeatures);
        } else {
            FontSetup.setup(fontInfo, useComplexScriptFeatures);
        }
        return fontInfo;
    }

}
