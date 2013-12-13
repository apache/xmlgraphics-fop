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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.afp.AFPEventProducer;
import org.apache.fop.afp.fonts.AFPFont;
import org.apache.fop.afp.fonts.AFPFontInfo;
import org.apache.fop.afp.fonts.CharacterSet;
import org.apache.fop.afp.fonts.CharacterSetBuilder;
import org.apache.fop.afp.fonts.CharacterSetType;
import org.apache.fop.afp.fonts.DoubleByteFont;
import org.apache.fop.afp.fonts.OutlineFont;
import org.apache.fop.afp.fonts.RasterFont;
import org.apache.fop.afp.util.AFPResourceAccessor;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.events.EventProducer;
import org.apache.fop.fonts.FontConfig;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.fonts.FontManagerConfigurator;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.FontTriplet.Matcher;
import org.apache.fop.fonts.FontUtil;
import org.apache.fop.fonts.Typeface;

/**
 * The config object for AFP fonts, these differ from the the more generic fonts (TTF and Type1).
 */
public final class AFPFontConfig implements FontConfig {

    private static final Log LOG = LogFactory.getLog(AFPFontConfig.class);

    private final List<AFPFontConfigData> fontsConfig;

    private AFPFontConfig() {
        fontsConfig = new ArrayList<AFPFontConfigData>();
    }

    /**
     * Returns a list of AFP font configuration data.
     * @return the AFP font config data
     */
    public List<AFPFontConfigData> getFontConfig() {
        return fontsConfig;
    }

    /**
     * The parser for AFP font data.
     */
    static final class AFPFontInfoConfigParser implements FontConfigParser {

        /** {@inheritDoc}} */
        public AFPFontConfig parse(Configuration cfg, FontManager fontManager, boolean strict,
                EventProducer eventProducer) throws FOPException {
            try {
                return new ParserHelper(cfg, fontManager, strict,
                        (AFPEventProducer) eventProducer).fontConfig;
            } catch (ConfigurationException ce) {
                throw new FOPException(ce);
            }
        }

        AFPFontConfig getEmptyConfig() {
            return new AFPFontConfig();
        }
    }

    private static final class AggregateMatcher implements Matcher {

        private final List<Matcher> matchers;

        private AggregateMatcher(Matcher... matchers) {
            this.matchers = new ArrayList<Matcher>();
            for (Matcher matcher : matchers) {
                if (matcher != null) {
                    this.matchers.add(matcher);
                }
            }
        }

        public boolean matches(FontTriplet triplet) {
            for (Matcher matcher : matchers) {
                if (matcher.matches(triplet)) {
                    return true;
                }
            }
            return false;
        }

    }

    private static final class ParserHelper {

        private static final Log LOG = LogFactory.getLog(ParserHelper.class);

        private final AFPFontConfig fontConfig;

        private final Matcher matcher;

        private ParserHelper(Configuration cfg, FontManager fontManager, boolean strict,
                AFPEventProducer eventProducer) throws FOPException, ConfigurationException {
            Configuration fonts = cfg.getChild("fonts");
            Matcher localMatcher = null;
            Configuration referencedFontsCfg = fonts.getChild("referenced-fonts", false);
            if (referencedFontsCfg != null) {
                localMatcher = FontManagerConfigurator.createFontsMatcher(referencedFontsCfg, strict);
            }
            matcher = new AggregateMatcher(fontManager.getReferencedFontsMatcher(), localMatcher);
            fontConfig = new AFPFontConfig();
            for (Configuration font : fonts.getChildren("font")) {
                buildFont(font, eventProducer);
            }
        }

        private void buildFont(Configuration fontCfg, AFPEventProducer eventProducer)
                throws ConfigurationException {
            //FontManager fontManager = this.userAgent.getFontManager();
            Configuration[] triplets = fontCfg.getChildren("font-triplet");
            List<FontTriplet> tripletList = new ArrayList<FontTriplet>();
            if (triplets.length == 0) {
                eventProducer.fontConfigMissing(this, "<font-triplet...", fontCfg.getLocation());
                return;
            }
            for (Configuration triplet : triplets) {
                int weight = FontUtil.parseCSS2FontWeight(triplet.getAttribute("weight"));
                FontTriplet fontTriplet = new FontTriplet(triplet.getAttribute("name"),
                        triplet.getAttribute("style"), weight);
                tripletList.add(fontTriplet);
            }
            //build the fonts
            Configuration[] config = fontCfg.getChildren("afp-font");
            if (config.length == 0) {
                eventProducer.fontConfigMissing(this, "<afp-font...", fontCfg.getLocation());
                return;
            }
            Configuration afpFontCfg = config[0];
            String uri = afpFontCfg.getAttribute("base-uri", null);
            try {
                String type = afpFontCfg.getAttribute("type");
                if (type == null) {
                    eventProducer.fontConfigMissing(this, "type attribute", fontCfg.getLocation());
                    return;
                }
                String codepage = afpFontCfg.getAttribute("codepage");
                if (codepage == null) {
                    eventProducer.fontConfigMissing(this, "codepage attribute",
                            fontCfg.getLocation());
                    return;
                }
                String encoding = afpFontCfg.getAttribute("encoding");
                if (encoding == null) {
                    eventProducer.fontConfigMissing(this, "encoding attribute",
                            fontCfg.getLocation());
                    return;
                }

                fontFromType(tripletList, type, codepage, encoding, afpFontCfg, eventProducer, uri);
            } catch (ConfigurationException ce) {
                eventProducer.invalidConfiguration(this, ce);
            }
        }

        private void fontFromType(List<FontTriplet> fontTriplets, String type, String codepage,
                String encoding, Configuration cfg, AFPEventProducer eventProducer, String embedURI)
                throws ConfigurationException {
            AFPFontConfigData config = null;
            if ("raster".equalsIgnoreCase(type)) {
                config = getRasterFont(fontTriplets, type, codepage, encoding, cfg, eventProducer,
                        embedURI);
            } else if ("outline".equalsIgnoreCase(type)) {
                config = getOutlineFont(fontTriplets, type, codepage, encoding, cfg, eventProducer,
                        embedURI);
            } else if ("CIDKeyed".equalsIgnoreCase(type)) {
                config = getCIDKeyedFont(fontTriplets, type, codepage, encoding, cfg,
                        eventProducer,
                        embedURI);
            } else {
                LOG.error("No or incorrect type attribute: " + type);
            }
            if (config != null) {
                fontConfig.fontsConfig.add(config);
            }
        }

        private CIDKeyedFontConfig getCIDKeyedFont(List<FontTriplet> fontTriplets, String type,
                String codepage, String encoding, Configuration cfg, AFPEventProducer eventProducer,
                String uri) throws ConfigurationException {
            String characterset = cfg.getAttribute("characterset");
            if (characterset == null) {
                eventProducer.fontConfigMissing(this, "characterset attribute",
                        cfg.getLocation());
                return null;
            }
            String name = cfg.getAttribute("name", characterset);
            CharacterSetType charsetType = cfg.getAttributeAsBoolean("ebcdic-dbcs", false)
                    ? CharacterSetType.DOUBLE_BYTE_LINE_DATA : CharacterSetType.DOUBLE_BYTE;
            return new CIDKeyedFontConfig(fontTriplets, type, codepage, encoding, characterset,
                    name, charsetType, isEmbbedable(fontTriplets), uri);
        }

        private OutlineFontConfig getOutlineFont(List<FontTriplet> fontTriplets, String type,
                String codepage, String encoding, Configuration cfg,
                AFPEventProducer eventProducer, String uri) throws ConfigurationException {
            String characterset = cfg.getAttribute("characterset");
            if (characterset == null) {
                eventProducer.fontConfigMissing(this, "characterset attribute",
                        cfg.getLocation());
                return null;
            }
            String name = cfg.getAttribute("name", characterset);
            String base14 = cfg.getAttribute("base14-font", null);
            return new OutlineFontConfig(fontTriplets, type, codepage, encoding, characterset,
                    name, base14, isEmbbedable(fontTriplets), uri);
        }

        private RasterFontConfig getRasterFont(List<FontTriplet> triplets, String type,
                String codepage, String encoding, Configuration cfg,
                AFPEventProducer eventProducer, String uri)
                throws ConfigurationException {
            String name = cfg.getAttribute("name", "Unknown");
            // Create a new font object
            Configuration[] rasters = cfg.getChildren("afp-raster-font");
            if (rasters.length == 0) {
                eventProducer.fontConfigMissing(this, "<afp-raster-font...",
                        cfg.getLocation());
                return null;
            }
            List<RasterCharactersetData> charsetData = new ArrayList<RasterCharactersetData>();
            for (Configuration rasterCfg : rasters) {
                String characterset = rasterCfg.getAttribute("characterset");
                if (characterset == null) {
                    eventProducer.fontConfigMissing(this, "characterset attribute",
                            cfg.getLocation());
                    return null;
                }
                float size = rasterCfg.getAttributeAsFloat("size");
                int sizeMpt = (int) (size * 1000);
                String base14 = rasterCfg.getAttribute("base14-font", null);
                charsetData.add(new RasterCharactersetData(characterset, sizeMpt, base14));
            }
            return new RasterFontConfig(triplets, type, codepage, encoding, null, name, uri, charsetData,
                    isEmbbedable(triplets));
        }

        private boolean isEmbbedable(List<FontTriplet> triplets) {
            for (FontTriplet triplet : triplets) {
                if (matcher.matches(triplet)) {
                    return false;
                }
            }
            return true;
        }

    }

    abstract static class AFPFontConfigData {
        private final List<FontTriplet> triplets;
        private final String codePage;
        private final String encoding;
        private final String name;
        private final boolean embeddable;
        private final String uri;

        AFPFontConfigData(List<FontTriplet> triplets, String type, String codePage,
                String encoding, String name, boolean embeddable, String uri) {
            this.triplets = Collections.unmodifiableList(triplets);
            this.codePage = codePage;
            this.encoding = encoding;
            this.name = name;
            this.embeddable = embeddable;
            this.uri = uri;
        }

        static AFPFontInfo getFontInfo(AFPFont font, AFPFontConfigData config) {
            return font != null ? new AFPFontInfo(font, config.triplets) : null;
        }

        abstract AFPFontInfo getFontInfo(InternalResourceResolver resourceResolver,
                AFPEventProducer eventProducer) throws IOException;

        AFPResourceAccessor getAccessor(InternalResourceResolver resourceResolver) {
            return new AFPResourceAccessor(resourceResolver, uri);
        }
    }

    static final class CIDKeyedFontConfig extends AFPFontConfigData {

        private final CharacterSetType charsetType;

        private final String characterset;

        private CIDKeyedFontConfig(List<FontTriplet> triplets, String type, String codePage, String encoding,
                String characterset, String name, CharacterSetType charsetType, boolean embeddable, String uri) {
            super(triplets, type, codePage, encoding, name, embeddable, uri);
            this.characterset = characterset;
            this.charsetType = charsetType;
        }

        @Override
        AFPFontInfo getFontInfo(InternalResourceResolver resourceResolver, AFPEventProducer eventProducer)
                throws IOException {
            AFPResourceAccessor accessor = getAccessor(resourceResolver);
            CharacterSet characterSet = CharacterSetBuilder.getDoubleByteInstance().buildDBCS(
                    characterset, super.codePage, super.encoding, charsetType, accessor, eventProducer);
            return getFontInfo(new DoubleByteFont(super.codePage, super.embeddable, characterSet,
                    eventProducer), this);
        }
    }

    static final class OutlineFontConfig extends AFPFontConfigData {
        private final String base14;
        private final String characterset;

        private OutlineFontConfig(List<FontTriplet> triplets, String type, String codePage,
                String encoding, String characterset, String name, String base14, boolean embeddable, String uri) {
            super(triplets, type, codePage, encoding, name, embeddable, uri);
            this.characterset = characterset;
            this.base14 = base14;
        }

        @Override
        AFPFontInfo getFontInfo(InternalResourceResolver resourceResolver, AFPEventProducer eventProducer)
                throws IOException {
            CharacterSet characterSet = null;
            if (base14 != null) {
                try {
                    Typeface tf = getTypeFace(base14);
                    characterSet = CharacterSetBuilder.getSingleByteInstance()
                                                      .build(characterset, super.codePage,
                                                              super.encoding, tf, eventProducer);
                } catch (ClassNotFoundException cnfe) {
                    String msg = "The base 14 font class for " + characterset
                            + " could not be found";
                    LOG.error(msg);
                }
            } else {
                AFPResourceAccessor accessor = getAccessor(resourceResolver);
                characterSet = CharacterSetBuilder.getSingleByteInstance().buildSBCS(
                        characterset, super.codePage, super.encoding, accessor, eventProducer);
            }
            return getFontInfo(new OutlineFont(super.name, super.embeddable, characterSet,
                    eventProducer), this);
        }
    }

    private static Typeface getTypeFace(String base14Name) throws ClassNotFoundException {
        try {
            Class<? extends Typeface> clazz = Class.forName("org.apache.fop.fonts.base14."
                    + base14Name).asSubclass(Typeface.class);
            return clazz.newInstance();
        } catch (IllegalAccessException iae) {
            LOG.error(iae.getMessage());
        } catch (ClassNotFoundException cnfe) {
            LOG.error(cnfe.getMessage());
        } catch (InstantiationException ie) {
            LOG.error(ie.getMessage());
        }
        throw new ClassNotFoundException("Couldn't load file for AFP font with base14 name: "
                + base14Name);
    }

    static final class RasterFontConfig extends AFPFontConfigData {
        private final List<RasterCharactersetData> charsets;

        private RasterFontConfig(List<FontTriplet> triplets, String type, String codePage,
                String encoding, String characterset, String name, String uri,
                List<RasterCharactersetData> csetData, boolean embeddable) {
            super(triplets, type, codePage, encoding, name, embeddable, uri);
            this.charsets = Collections.unmodifiableList(csetData);
        }

        @Override
        AFPFontInfo getFontInfo(InternalResourceResolver resourceResolver, AFPEventProducer eventProducer)
                throws IOException {
            RasterFont rasterFont = new RasterFont(super.name, super.embeddable);
            for (RasterCharactersetData charset : charsets) {
                if (charset.base14 != null) {
                    try {
                        Typeface tf = getTypeFace(charset.base14);
                        rasterFont.addCharacterSet(charset.size,
                                CharacterSetBuilder.getSingleByteInstance().build(
                                        charset.characterset, super.codePage, super.encoding,
                                        tf, eventProducer));

                    } catch (ClassNotFoundException cnfe) {
                        String msg = "The base 14 font class for " + charset.characterset
                                + " could not be found";
                        LOG.error(msg);
                    } catch (IOException ie) {
                        String msg = "The base 14 font class " + charset.characterset
                                + " could not be instantiated";
                        LOG.error(msg);
                    }
                } else {
                    AFPResourceAccessor accessor = getAccessor(resourceResolver);
                    rasterFont.addCharacterSet(charset.size,
                            CharacterSetBuilder.getSingleByteInstance().buildSBCS(charset.characterset,
                                    super.codePage, super.encoding, accessor, eventProducer));
                }
            }
            return getFontInfo(rasterFont, this);
        }
    }

    static final class RasterCharactersetData {
        private final String characterset;
        private final int size;
        private final String base14;

        private RasterCharactersetData(String characterset, int size, String base14) {
            this.characterset = characterset;
            this.size = size;
            this.base14 = base14;
        }
    }
}
