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

package org.apache.fop.render.afp;

import java.io.File;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.FontUtil;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.render.PrintRendererConfigurator;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.afp.fonts.AFPFontInfo;
import org.apache.fop.render.afp.fonts.CharacterSet;
import org.apache.fop.render.afp.fonts.FopCharacterSet;
import org.apache.fop.render.afp.fonts.OutlineFont;
import org.apache.fop.render.afp.fonts.RasterFont;
import org.apache.fop.render.afp.modca.AFPDataStream;
import org.apache.fop.util.LogUtil;

/**
 * AFP Renderer configurator 
 */
public class AFPRendererConfigurator extends PrintRendererConfigurator {
    
    /**
     * Default constructor
     * 
     * @param userAgent user agent
     */
    public AFPRendererConfigurator(FOUserAgent userAgent) {
        super(userAgent);
    }

    private AFPFontInfo buildFont(Configuration fontCfg, String fontPath)
        throws ConfigurationException {

        Configuration[] triple = fontCfg.getChildren("font-triplet");
        List tripleList = new java.util.ArrayList();
        if (triple.length == 0) {
            log.error("Mandatory font configuration element '<font-triplet...' is missing");
            return null;
        }
        for (int j = 0; j < triple.length; j++) {
            int weight = FontUtil.parseCSS2FontWeight(triple[j].getAttribute("weight"));
            tripleList.add(new FontTriplet(triple[j].getAttribute("name"),
                                           triple[j].getAttribute("style"),
                                           weight));
        }

        //build the fonts
        Configuration afpFontCfg = fontCfg.getChild("afp-font");
        if (afpFontCfg == null) {
            log.error("Mandatory font configuration element '<afp-font...' is missing");
            return null;
        }
        String path = afpFontCfg.getAttribute("path", fontPath);
        String type = afpFontCfg.getAttribute("type");
        if (type == null) {
            log.error("Mandatory afp-font configuration attribute 'type=' is missing");
            return null;
        }
        String codepage = afpFontCfg.getAttribute("codepage");
        if (codepage == null) {
            log.error("Mandatory afp-font configuration attribute 'code=' is missing");
            return null;
        }
        String encoding = afpFontCfg.getAttribute("encoding");
        if (encoding == null) {
            log.error("Mandatory afp-font configuration attribute 'encoding=' is missing");
            return null;
        }

        if ("raster".equalsIgnoreCase(type)) {

            String name = afpFontCfg.getAttribute("name", "Unknown");

            // Create a new font object
            RasterFont font = new RasterFont(name);

            Configuration[] rasters = afpFontCfg.getChildren("afp-raster-font");
            if (rasters.length == 0) {
                log.error(
                        "Mandatory font configuration elements '<afp-raster-font...' are missing");
                return null;
            }
            for (int j = 0; j < rasters.length; j++) {
                Configuration rasterCfg = rasters[j];

                String characterset = rasterCfg.getAttribute("characterset");
                if (characterset == null) {
                    log.error(
                    "Mandatory afp-raster-font configuration attribute 'characterset=' is missing");
                    return null;
                }
                int size = rasterCfg.getAttributeAsInteger("size");
                String base14 = rasterCfg.getAttribute("base14-font", null);

                if (base14 != null) {
                    try {
                        Class clazz = Class.forName("org.apache.fop.fonts.base14."
                            + base14);
                        try {
                            Typeface tf = (Typeface)clazz.newInstance();
                            font.addCharacterSet(size, new FopCharacterSet(
                                codepage, encoding, characterset, size, tf));
                        } catch (Exception ie) {
                            String msg = "The base 14 font class " + clazz.getName()
                                + " could not be instantiated";
                            log.error(msg);
                        }
                    } catch (ClassNotFoundException cnfe) {
                        String msg = "The base 14 font class for " + characterset
                            + " could not be found";
                        log.error(msg);
                    }
                } else {
                    font.addCharacterSet(size, new CharacterSet(
                        codepage, encoding, characterset, path));
                }
            }
            return new AFPFontInfo(font, tripleList);

        } else if ("outline".equalsIgnoreCase(type)) {
            String characterset = afpFontCfg.getAttribute("characterset");
            if (characterset == null) {
                log.error("Mandatory afp-font configuration attribute 'characterset=' is missing");
                return null;
            }
            String name = afpFontCfg.getAttribute("name", characterset);
            CharacterSet characterSet = null;
            String base14 = afpFontCfg.getAttribute("base14-font", null);
            if (base14 != null) {
                try {
                    Class clazz = Class.forName("org.apache.fop.fonts.base14."
                        + base14);
                    try {
                        Typeface tf = (Typeface)clazz.newInstance();
                        characterSet = new FopCharacterSet(
                                codepage, encoding, characterset, 1, tf);
                    } catch (Exception ie) {
                        String msg = "The base 14 font class " + clazz.getName()
                            + " could not be instantiated";
                        log.error(msg);
                    }
                } catch (ClassNotFoundException cnfe) {
                    String msg = "The base 14 font class for " + characterset
                        + " could not be found";
                    log.error(msg);
                }
            } else {
                characterSet = new CharacterSet(codepage, encoding, characterset, path);
            }
            // Create a new font object
            OutlineFont font = new OutlineFont(name, characterSet);
            return new AFPFontInfo(font, tripleList);
        } else {
            log.error("No or incorrect type attribute");
        }
        return null;
    }
    
    /**
     * Builds a list of AFPFontInfo objects for use with the setup() method.
     * 
     * @param cfg Configuration object
     * @return List the newly created list of fonts
     * @throws ConfigurationException if something's wrong with the config data
     */
    private List buildFontListFromConfiguration(Configuration cfg)
            throws ConfigurationException {
        List fontList = new java.util.ArrayList();
        Configuration[] font = cfg.getChild("fonts").getChildren("font");
        for (int i = 0; i < font.length; i++) {
            AFPFontInfo afi = buildFont(font[i], null);
            if (afi != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Adding font " + afi.getAFPFont().getFontName());
                    for (int j = 0; j < afi.getFontTriplets().size(); ++j) {
                        FontTriplet triplet = (FontTriplet) afi.getFontTriplets().get(j);
                        log.debug("  Font triplet "
                                  + triplet.getName() + ", "
                                  + triplet.getStyle() + ", "
                                  + triplet.getWeight());
                    }
                }
                fontList.add(afi);
            }
        }
        return fontList;
    }

    /**
     * Configure the AFP renderer.
     * 
     * @param renderer AFP renderer
     * @throws FOPException fop exception
     * @see org.apache.fop.render.PrintRendererConfigurator#configure(Renderer)
     */
    public void configure(Renderer renderer) throws FOPException {
        Configuration cfg = super.getRendererConfig(renderer);
        if (cfg != null) {
            AFPRenderer afpRenderer = (AFPRenderer)renderer;
            try {
                List fontList = buildFontListFromConfiguration(cfg);
                afpRenderer.setFontList(fontList);
            } catch (ConfigurationException e) {
                LogUtil.handleException(log, e,
                        userAgent.getFactory().validateUserConfigStrictly());
            }
                    
            // image information
            Configuration imagesCfg = cfg.getChild("images");
            if (!"color".equalsIgnoreCase(imagesCfg.getAttribute("mode", "b+w"))) {
                afpRenderer.setBitsPerPixel(imagesCfg.getAttributeAsInteger("bits-per-pixel", 8));
            } else {
                afpRenderer.setColorImages(true);
            }
            
            // renderer resolution
            Configuration rendererResolutionCfg = cfg.getChild("renderer-resolution", false);
            if (rendererResolutionCfg != null) {
                afpRenderer.setResolution(rendererResolutionCfg.getValueAsInteger(240));
            }

            // TODO: provide support for different MO:DCA interchange sets
            // the MO:DCA interchange set in use (defaults to MO:DCA-L)
//            Configuration modcaCfg = cfg.getChild("modca", false);
//            if (modcaCfg != null) {
//                String interchangeSetString = cfg.getAttribute(
//                        "interchange-set", InterchangeSet.MODCA_PRESENTATION_INTERCHANGE_SET_2);
//                InterchangeSet interchangeSet = InterchangeSet.valueOf(interchangeSetString);
//                afpRenderer.getAFPDataStream().setInterchangeSet(interchangeSet);
//            }

            // a default external resource group file setting
            Configuration resourceGroupFileCfg
                = cfg.getChild("resource-group-file", false);
            if (resourceGroupFileCfg != null) {
                String resourceGroupDest = null;
                try {
                    resourceGroupDest = resourceGroupFileCfg.getValue();
                } catch (ConfigurationException e) {
                    LogUtil.handleException(log, e,
                            userAgent.getFactory().validateUserConfigStrictly());
                }
                File resourceGroupFile = new File(resourceGroupDest);
                if (resourceGroupFile.canWrite()) {
                    AFPDataStream datastream = afpRenderer.getAFPDataStream();
                    datastream.setDefaultResourceGroupFilePath(resourceGroupDest);
                } else {
                    log.warn("Unable to write to default external resource group file '"
                                + resourceGroupDest + "'");
                }
            }
        }
    }
}
