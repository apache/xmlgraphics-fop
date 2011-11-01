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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.fop.afp.AFPResourceLevel;
import org.apache.fop.afp.AFPResourceLevelDefaults;
import org.apache.fop.afp.fonts.AFPFont;
import org.apache.fop.afp.fonts.AFPFontCollection;
import org.apache.fop.afp.fonts.AFPFontInfo;
import org.apache.fop.afp.fonts.CharacterSet;
import org.apache.fop.afp.fonts.CharacterSetBuilder;
import org.apache.fop.afp.fonts.DoubleByteFont;
import org.apache.fop.afp.fonts.OutlineFont;
import org.apache.fop.afp.fonts.RasterFont;
import org.apache.fop.afp.util.DefaultFOPResourceAccessor;
import org.apache.fop.afp.util.ResourceAccessor;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.fonts.FontManagerConfigurator;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.FontUtil;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.render.PrintRendererConfigurator;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;
import org.apache.fop.util.LogUtil;

/**
 * AFP Renderer configurator
 */
public class AFPRendererConfigurator extends PrintRendererConfigurator
        implements IFDocumentHandlerConfigurator {

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

        FontManager fontManager = this.userAgent.getFactory().getFontManager();

        Configuration[] triple = fontCfg.getChildren("font-triplet");
        List<FontTriplet> tripletList = new java.util.ArrayList<FontTriplet>();
        if (triple.length == 0) {
            log.error("Mandatory font configuration element '<font-triplet...' is missing");
            return null;
        }
        for (Configuration config : triple) {
            int weight = FontUtil.parseCSS2FontWeight(config.getAttribute("weight"));
            FontTriplet triplet = new FontTriplet(config.getAttribute("name"),
                    config.getAttribute("style"),
                    weight);
            tripletList.add(triplet);
        }

        //build the fonts
        Configuration afpFontCfg = fontCfg.getChild("afp-font");
        if (afpFontCfg == null) {
            log.error("Mandatory font configuration element '<afp-font...' is missing");
            return null;
        }

        URI baseURI = null;
        String uri = afpFontCfg.getAttribute("base-uri", fontPath);
        if (uri == null) {
            //Fallback for old attribute which only supports local filenames
            String path = afpFontCfg.getAttribute("path", fontPath);
            if (path != null) {
                File f = new File(path);
                baseURI = f.toURI();
            }
        } else {
            try {
                baseURI = new URI(uri);
            } catch (URISyntaxException e) {
                log.error("Invalid URI: " + e.getMessage());
                return null;
            }
        }
        ResourceAccessor accessor = new DefaultFOPResourceAccessor(
                this.userAgent,
                fontManager.getFontBaseURL(),
                baseURI);

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

        AFPFont font = fontFromType(type, codepage, encoding, accessor, afpFontCfg);

        return font != null ? new AFPFontInfo(font, tripletList) : null;
    }


    /**
     * Create the AFPFont based on type and type-dependent configuration.
     *
     * @param type font type e.g. 'raster', 'outline'
     * @param codepage codepage file
     * @param encoding character encoding e.g. 'Cp500', 'UnicodeBigUnmarked'
     * @param accessor
     * @param afpFontCfg
     * @return the created AFPFont
     * @throws ConfigurationException
     */
    private AFPFont fontFromType(String type, String codepage, String encoding,
            ResourceAccessor accessor, Configuration afpFontCfg)
    throws ConfigurationException {

        if ("raster".equalsIgnoreCase(type)) {

            String name = afpFontCfg.getAttribute("name", "Unknown");

            // Create a new font object
            RasterFont font = new RasterFont(name);

            Configuration[] rasters = afpFontCfg.getChildren("afp-raster-font");
            if (rasters.length == 0) {
                log.error("Mandatory font configuration elements '<afp-raster-font...'"
                        + " are missing at " + afpFontCfg.getLocation());
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
                float size = rasterCfg.getAttributeAsFloat("size");
                int sizeMpt = (int)(size * 1000);
                String base14 = rasterCfg.getAttribute("base14-font", null);

                if (base14 != null) {
                    try {
                        Class<? extends Typeface> clazz = Class.forName(
                                "org.apache.fop.fonts.base14." + base14).asSubclass(Typeface.class);
                        try {
                            Typeface tf = clazz.newInstance();
                            font.addCharacterSet(sizeMpt,
                                    CharacterSetBuilder.getInstance()
                                        .build(characterset, codepage, encoding, tf));
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
                    try {
                        font.addCharacterSet(sizeMpt, CharacterSetBuilder.getInstance()
                                .build(characterset, codepage, encoding, accessor));
                    } catch (IOException ioe) {
                        toConfigurationException(codepage, characterset, ioe);
                    }
                }
            }
            return font;

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
                    Class<? extends Typeface> clazz = Class.forName("org.apache.fop.fonts.base14."
                            + base14).asSubclass(Typeface.class);
                    try {
                        Typeface tf = clazz.newInstance();
                        characterSet = CharacterSetBuilder.getInstance()
                                        .build(characterset, codepage, encoding, tf);
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
                try {
                    characterSet = CharacterSetBuilder.getInstance().build(
                            characterset, codepage, encoding, accessor);
                } catch (IOException ioe) {
                    toConfigurationException(codepage, characterset, ioe);
                }
            }
            // Return new font object
            return new OutlineFont(name, characterSet);

        } else if ("CIDKeyed".equalsIgnoreCase(type)) {
            String characterset = afpFontCfg.getAttribute("characterset");
            if (characterset == null) {
                log.error("Mandatory afp-font configuration attribute 'characterset=' is missing");
                return null;
            }
            String name = afpFontCfg.getAttribute("name", characterset);
            CharacterSet characterSet = null;
            boolean ebcdicDBCS = afpFontCfg.getAttributeAsBoolean("ebcdic-dbcs", false);

            try {
                characterSet = CharacterSetBuilder.getDoubleByteInstance().buildDBCS(characterset,
                        codepage, encoding, ebcdicDBCS, accessor);
            } catch (IOException ioe) {
                toConfigurationException(codepage, characterset, ioe);
            }

            // Create a new font object
            DoubleByteFont font = new DoubleByteFont(name, characterSet);
            return font;

        } else {
            log.error("No or incorrect type attribute: " + type);
        }

        return null;
    }

    private void toConfigurationException(String codepage, String characterset, IOException ioe)
            throws ConfigurationException {
        String msg = "Failed to load the character set metrics " + characterset
            + " with code page " + codepage
            + ". I/O error: " + ioe.getMessage();
        throw new ConfigurationException(msg, ioe);
    }

    /**
     * Builds a list of AFPFontInfo objects for use with the setup() method.
     *
     * @param cfg Configuration object
     * @return List the newly created list of fonts
     * @throws ConfigurationException if something's wrong with the config data
     */
    private List<AFPFontInfo> buildFontListFromConfiguration(Configuration cfg)
    throws FOPException, ConfigurationException {

        Configuration fonts = cfg.getChild("fonts");
        FontManager fontManager = this.userAgent.getFactory().getFontManager();

        // General matcher
        FontTriplet.Matcher referencedFontsMatcher = fontManager.getReferencedFontsMatcher();
        // Renderer-specific matcher
        FontTriplet.Matcher localMatcher = null;

        // Renderer-specific referenced fonts
        Configuration referencedFontsCfg = fonts.getChild("referenced-fonts", false);
        if (referencedFontsCfg != null) {
            localMatcher = FontManagerConfigurator.createFontsMatcher(
                    referencedFontsCfg, this.userAgent.getFactory().validateUserConfigStrictly());
        }

        List<AFPFontInfo> fontList = new java.util.ArrayList<AFPFontInfo>();
        Configuration[] font = fonts.getChildren("font");
        final String fontPath = null;
        for (int i = 0; i < font.length; i++) {
            AFPFontInfo afi = buildFont(font[i], fontPath);
            if (afi != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Adding font " + afi.getAFPFont().getFontName());
                }
                List<FontTriplet> fontTriplets = afi.getFontTriplets();
                for (int j = 0; j < fontTriplets.size(); ++j) {
                    FontTriplet triplet = (FontTriplet) fontTriplets.get(j);
                    if (log.isDebugEnabled()) {
                        log.debug("  Font triplet "
                                + triplet.getName() + ", "
                                + triplet.getStyle() + ", "
                                + triplet.getWeight());
                    }

                    if ((referencedFontsMatcher != null && referencedFontsMatcher.matches(triplet))
                            || (localMatcher != null && localMatcher.matches(triplet))) {
                        afi.getAFPFont().setEmbeddable(false);
                        break;
                    }
                }

                fontList.add(afi);
            }
        }
        return fontList;
    }

    /** images are converted to grayscale bitmapped IOCA */
    private static final String IMAGES_MODE_GRAYSCALE = "b+w";

    /** images are converted to color bitmapped IOCA */
    private static final String IMAGES_MODE_COLOR = "color";

    /**
     * Throws an UnsupportedOperationException.
     *
     * @param renderer not used
     */
    @Override
    public void configure(Renderer renderer) {
        throw new UnsupportedOperationException();
    }

    private void configure(AFPCustomizable customizable, Configuration cfg) throws FOPException {

        // image information
        Configuration imagesCfg = cfg.getChild("images");

        // default to grayscale images
        String imagesMode = imagesCfg.getAttribute("mode", IMAGES_MODE_GRAYSCALE);
        if (IMAGES_MODE_COLOR.equals(imagesMode)) {
            customizable.setColorImages(true);

            boolean cmyk = imagesCfg.getAttributeAsBoolean("cmyk", false);
            customizable.setCMYKImagesSupported(cmyk);
        } else {
            customizable.setColorImages(false);
            // default to 8 bits per pixel
            int bitsPerPixel = imagesCfg.getAttributeAsInteger("bits-per-pixel", 8);
            customizable.setBitsPerPixel(bitsPerPixel);
        }

        String dithering = imagesCfg.getAttribute("dithering-quality", "medium");
        float dq = 0.5f;
        if (dithering.startsWith("min")) {
            dq = 0.0f;
        } else if (dithering.startsWith("max")) {
            dq = 1.0f;
        } else {
            try {
                dq = Float.parseFloat(dithering);
            } catch (NumberFormatException nfe) {
                //ignore and leave the default above
            }
        }
        customizable.setDitheringQuality(dq);

        // native image support
        boolean nativeImageSupport = imagesCfg.getAttributeAsBoolean("native", false);
        customizable.setNativeImagesSupported(nativeImageSupport);

        Configuration jpegConfig = imagesCfg.getChild("jpeg");
        boolean allowEmbedding = false;
        float ieq = 1.0f;
        if (jpegConfig != null) {
            allowEmbedding = jpegConfig.getAttributeAsBoolean("allow-embedding", false);
            String bitmapEncodingQuality = jpegConfig.getAttribute("bitmap-encoding-quality", null);

            if (bitmapEncodingQuality != null) {
                try {
                    ieq = Float.parseFloat(bitmapEncodingQuality);
                } catch (NumberFormatException nfe) {
                    //ignore and leave the default above
                }
            }
        }
        customizable.canEmbedJpeg(allowEmbedding);
        customizable.setBitmapEncodingQuality(ieq);

        // shading (filled rectangles)
        Configuration shadingCfg = cfg.getChild("shading");
        AFPShadingMode shadingMode = AFPShadingMode.valueOf(
                shadingCfg.getValue(AFPShadingMode.COLOR.getName()));
        customizable.setShadingMode(shadingMode);

        // GOCA Support
        Configuration gocaCfg = cfg.getChild("goca");
        boolean gocaEnabled = gocaCfg.getAttributeAsBoolean(
                "enabled", customizable.isGOCAEnabled());
        customizable.setGOCAEnabled(gocaEnabled);
        String gocaText = gocaCfg.getAttribute(
                "text", customizable.isStrokeGOCAText() ? "stroke" : "default");
        customizable.setStrokeGOCAText("stroke".equalsIgnoreCase(gocaText)
                || "shapes".equalsIgnoreCase(gocaText));

        // renderer resolution
        Configuration rendererResolutionCfg = cfg.getChild("renderer-resolution", false);
        if (rendererResolutionCfg != null) {
            customizable.setResolution(rendererResolutionCfg.getValueAsInteger(240));
        }

        // a default external resource group file setting
        Configuration resourceGroupFileCfg
        = cfg.getChild("resource-group-file", false);
        if (resourceGroupFileCfg != null) {
            String resourceGroupDest = null;
            try {
                resourceGroupDest = resourceGroupFileCfg.getValue();
                if (resourceGroupDest != null) {
                    File resourceGroupFile = new File(resourceGroupDest);
                    boolean created = resourceGroupFile.createNewFile();
                    if (created && resourceGroupFile.canWrite()) {
                        customizable.setDefaultResourceGroupFilePath(resourceGroupDest);
                    } else {
                        log.warn("Unable to write to default external resource group file '"
                                + resourceGroupDest + "'");
                    }
                }
            } catch (ConfigurationException e) {
                LogUtil.handleException(log, e,
                        userAgent.getFactory().validateUserConfigStrictly());
            } catch (IOException ioe) {
                throw new FOPException("Could not create default external resource group file"
                            , ioe);
            }
        }

        Configuration defaultResourceLevelCfg = cfg.getChild("default-resource-levels", false);
        if (defaultResourceLevelCfg != null) {
            AFPResourceLevelDefaults defaults = new AFPResourceLevelDefaults();
            String[] types = defaultResourceLevelCfg.getAttributeNames();
            for (int i = 0, c = types.length; i < c; i++) {
                String type = types[i];
                try {
                    String level = defaultResourceLevelCfg.getAttribute(type);
                    defaults.setDefaultResourceLevel(type, AFPResourceLevel.valueOf(level));
                } catch (IllegalArgumentException iae) {
                    LogUtil.handleException(log, iae,
                            userAgent.getFactory().validateUserConfigStrictly());
                } catch (ConfigurationException e) {
                    LogUtil.handleException(log, e,
                            userAgent.getFactory().validateUserConfigStrictly());
                }
            }
            customizable.setResourceLevelDefaults(defaults);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void configure(IFDocumentHandler documentHandler) throws FOPException {
        Configuration cfg = super.getRendererConfig(documentHandler.getMimeType());
        if (cfg != null) {
            AFPDocumentHandler afpDocumentHandler = (AFPDocumentHandler)documentHandler;
            configure(afpDocumentHandler, cfg);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setupFontInfo(IFDocumentHandler documentHandler, FontInfo fontInfo)
            throws FOPException {
        FontManager fontManager = userAgent.getFactory().getFontManager();
        List<AFPFontCollection> fontCollections = new ArrayList<AFPFontCollection>();

        Configuration cfg = super.getRendererConfig(documentHandler.getMimeType());
        if (cfg != null) {
            try {
                List<AFPFontInfo> fontList = buildFontListFromConfiguration(cfg);
                fontCollections.add(new AFPFontCollection(
                        userAgent.getEventBroadcaster(), fontList));
            } catch (ConfigurationException e) {
                LogUtil.handleException(log, e,
                        userAgent.getFactory().validateUserConfigStrictly());
            }
        } else {
            fontCollections.add(new AFPFontCollection(userAgent.getEventBroadcaster(), null));
        }

        fontManager.setup(fontInfo,
                fontCollections.toArray(
                        new FontCollection[fontCollections.size()]));
        documentHandler.setFontInfo(fontInfo);
    }
}
