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

package org.apache.fop.apps;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.xml.sax.SAXException;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.spi.ImageImplRegistry;
import org.apache.xmlgraphics.image.loader.util.Penalty;
import org.apache.xmlgraphics.io.ResourceResolver;

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.fonts.FontManagerConfigurator;
import org.apache.fop.hyphenation.HyphenationTreeCache;
import org.apache.fop.util.LogUtil;

/**
 * Parses the FOP configuration file and returns a {@link FopFactoryBuilder} which builds a
 * {@link FopFactory}.
 */
public class FopConfParser {

    private static final String PREFER_RENDERER = "prefer-renderer";

    private final Log log = LogFactory.getLog(FopConfParser.class);

    private final FopFactoryBuilder fopFactoryBuilder;

    /**
     * Constructor that takes the FOP conf in the form of an {@link InputStream}. A default base URI
     * must be given as a fall-back mechanism for URI resolution.
     *
     * @param fopConfStream the fop conf input stream
     * @param enviro the profile of the FOP deployment environment
     * @throws SAXException if a SAX error was thrown parsing the FOP conf
     * @throws IOException if an I/O error is thrown while parsing the FOP conf
     */
    public FopConfParser(InputStream fopConfStream, EnvironmentProfile enviro)
            throws SAXException, IOException {
        DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
        Configuration cfg;
        try {
            cfg = cfgBuilder.build(fopConfStream);
        } catch (ConfigurationException e) {
            throw new FOPException(e);
        }
        // The default base URI is taken from the directory in which the fopConf resides
        fopFactoryBuilder = new FopFactoryBuilder(enviro).setConfiguration(cfg);
        configure(enviro.getDefaultBaseURI(), enviro.getResourceResolver(), cfg);
    }

    /**
     * Constructor that takes the FOP conf in the form of an {@link InputStream}. A default base URI
     * must be given as a fall-back mechanism for URI resolution.
     *
     * @param fopConfStream the fop conf input stream
     * @param defaultBaseURI the default base URI
     * @param resourceResolver the URI resolver
     * @throws SAXException if a SAX error was thrown parsing the FOP conf
     * @throws IOException if an I/O error is thrown while parsing the FOP conf
     */
    public FopConfParser(InputStream fopConfStream, URI defaultBaseURI,
            ResourceResolver resourceResolver) throws SAXException, IOException {
        this(fopConfStream, EnvironmentalProfileFactory.createDefault(defaultBaseURI, resourceResolver));
    }

    /**
     * Constructor that takes the FOP conf in the form of an {@link InputStream}. A default base URI
     * must be given as a fall-back mechanism for URI resolution. The default URI resolvers is used.
     *
     * @param fopConfStream the fop conf input stream
     * @param defaultBaseURI the default base URI
     * @throws SAXException if a SAX error was thrown parsing the FOP conf
     * @throws IOException if an I/O error is thrown while parsing the FOP conf
     */
    public FopConfParser(InputStream fopConfStream, URI defaultBaseURI) throws SAXException,
            IOException {
        this(fopConfStream, defaultBaseURI, ResourceResolverFactory.createDefaultResourceResolver());
    }

    /**
     * Constructor that takes the FOP conf and uses the default URI resolver.
     *
     * @param fopConfFile the FOP conf file
     * @throws SAXException if a SAX error was thrown parsing the FOP conf
     * @throws IOException if an I/O error is thrown while parsing the FOP conf
     */
    public FopConfParser(File fopConfFile) throws SAXException, IOException {
        this(fopConfFile, ResourceResolverFactory.createDefaultResourceResolver());
    }

    /**
     * Constructor that parses the FOP conf and uses the URI resolver given.
     *
     * @param fopConfFile the FOP conf file
     * @param resourceResolver the URI resolver
     * @throws SAXException if a SAX error was thrown parsing the FOP conf
     * @throws IOException if an I/O error is thrown while parsing the FOP conf
     */
    public FopConfParser(File fopConfFile, ResourceResolver resourceResolver)
            throws SAXException, IOException {
        this(new FileInputStream(fopConfFile),
                fopConfFile.getAbsoluteFile().getParentFile().toURI(), resourceResolver);
    }

    private void configure(final URI defaultBaseURI, final ResourceResolver resourceResolver,
            Configuration cfg) throws FOPException {
        if (log.isDebugEnabled()) {
            log.debug("Initializing FopFactory Configuration");
        }

        // strict fo validation
        if (cfg.getChild("strict-validation", false) != null) {
            try {
                boolean strict = cfg.getChild("strict-validation").getValueAsBoolean();
                fopFactoryBuilder.setStrictFOValidation(strict);
            } catch (ConfigurationException e) {
                LogUtil.handleException(log, e, false);
            }
        }

        boolean strict = false;
        if (cfg.getChild("strict-configuration", false) != null) {
            try {
                strict = cfg.getChild("strict-configuration").getValueAsBoolean();
                fopFactoryBuilder.setStrictUserConfigValidation(strict);
            } catch (ConfigurationException e) {
                LogUtil.handleException(log, e, false);
            }
        }

        if (cfg.getChild("accessibility", false) != null) {
            try {
                fopFactoryBuilder.setAccessibility(cfg.getChild("accessibility").getValueAsBoolean());
            } catch (ConfigurationException e) {
                LogUtil.handleException(log, e, false);
            }
        }

        // base definitions for relative path resolution
        if (cfg.getChild("base", false) != null) {
            try {
                URI confUri = InternalResourceResolver.getBaseURI(cfg.getChild("base").getValue(null));
                fopFactoryBuilder.setBaseURI(defaultBaseURI.resolve(confUri));
            } catch (URISyntaxException use) {
                LogUtil.handleException(log, use, strict);
            }
        }

        // renderer options
        if (cfg.getChild("source-resolution", false) != null) {
            float srcRes = cfg.getChild("source-resolution").getValueAsFloat(
                    FopFactoryConfig.DEFAULT_SOURCE_RESOLUTION);
            fopFactoryBuilder.setSourceResolution(srcRes);
            if (log.isDebugEnabled()) {
                log.debug("source-resolution set to: " + srcRes + "dpi");
            }
        }
        if (cfg.getChild("target-resolution", false) != null) {
            float targetRes = cfg.getChild("target-resolution").getValueAsFloat(
                    FopFactoryConfig.DEFAULT_TARGET_RESOLUTION);
            fopFactoryBuilder.setTargetResolution(targetRes);
            if (log.isDebugEnabled()) {
                log.debug("target-resolution set to: " + targetRes + "dpi");
            }
        }
        if (cfg.getChild("break-indent-inheritance", false) != null) {
            try {
                fopFactoryBuilder.setBreakIndentInheritanceOnReferenceAreaBoundary(
                             cfg.getChild("break-indent-inheritance").getValueAsBoolean());
            } catch (ConfigurationException e) {
                LogUtil.handleException(log, e, strict);
            }
        }
        Configuration pageConfig = cfg.getChild("default-page-settings");
        if (pageConfig.getAttribute("height", null) != null) {
            String pageHeight = pageConfig.getAttribute("height",
                    FopFactoryConfig.DEFAULT_PAGE_HEIGHT);
            fopFactoryBuilder.setPageHeight(pageHeight);
            if (log.isInfoEnabled()) {
                log.info("Default page-height set to: " + pageHeight);
            }
        }
        if (pageConfig.getAttribute("width", null) != null) {
            String pageWidth = pageConfig.getAttribute("width",
                    FopFactoryConfig.DEFAULT_PAGE_WIDTH);
            fopFactoryBuilder.setPageWidth(pageWidth);
            if (log.isInfoEnabled()) {
                log.info("Default page-width set to: " + pageWidth);
            }
        }

        if (cfg.getChild("complex-scripts") != null) {
            Configuration csConfig = cfg.getChild("complex-scripts");
            fopFactoryBuilder.setComplexScriptFeatures(!csConfig.getAttributeAsBoolean("disabled",
                    false));
        }

        setHyphPatNames(cfg, fopFactoryBuilder, strict);

        // prefer Renderer over IFDocumentHandler
        if (cfg.getChild(PREFER_RENDERER, false) != null) {
            try {
                fopFactoryBuilder.setPreferRenderer(
                             cfg.getChild(PREFER_RENDERER).getValueAsBoolean());
            } catch (ConfigurationException e) {
                LogUtil.handleException(log, e, strict);
            }
        }

        // configure font manager
        new FontManagerConfigurator(cfg, fopFactoryBuilder.getBaseURI(), resourceResolver).configure(
                fopFactoryBuilder.getFontManager(), strict);

        // configure image loader framework
        configureImageLoading(cfg.getChild("image-loading", false), strict);
    }

    private void setHyphPatNames(Configuration cfg, FopFactoryBuilder builder, boolean strict)
            throws FOPException {
        Configuration[] hyphPatConfig = cfg.getChildren("hyphenation-pattern");
        if (hyphPatConfig.length != 0) {
            Map<String, String> hyphPatNames = new HashMap<String, String>();
            for (int i = 0; i < hyphPatConfig.length; ++i) {
                String lang;
                String country;
                String filename;
                StringBuffer error = new StringBuffer();
                String location = hyphPatConfig[i].getLocation();

                lang = hyphPatConfig[i].getAttribute("lang", null);
                if (lang == null) {
                    addError("The lang attribute of a hyphenation-pattern configuration"
                            + " element must exist (" + location + ")", error);
                } else if (!lang.matches("[a-zA-Z]{2}")) {
                    addError("The lang attribute of a hyphenation-pattern configuration"
                            + " element must consist of exactly two letters ("
                            + location + ")", error);
                }
                lang = lang.toLowerCase(Locale.getDefault());

                country = hyphPatConfig[i].getAttribute("country", null);
                if ("".equals(country)) {
                    country = null;
                }
                if (country != null) {
                    if (!country.matches("[a-zA-Z]{2}")) {
                        addError("The country attribute of a hyphenation-pattern configuration"
                                + " element must consist of exactly two letters ("
                                + location + ")", error);
                    }
                    country = country.toUpperCase(Locale.getDefault());
                }

                filename = hyphPatConfig[i].getValue(null);
                if (filename == null) {
                    addError("The value of a hyphenation-pattern configuration"
                            + " element may not be empty (" + location + ")", error);
                }

                if (error.length() != 0) {
                    LogUtil.handleError(log, error.toString(), strict);
                    continue;
                }

                String llccKey = HyphenationTreeCache.constructLlccKey(lang, country);
                hyphPatNames.put(llccKey, filename);
                if (log.isDebugEnabled()) {
                    log.debug("Using hyphenation pattern filename " + filename
                            + " for lang=\"" + lang + "\""
                            + (country != null ? ", country=\"" + country + "\"" : ""));
                }
            }
            builder.setHyphPatNames(hyphPatNames);
        }
    }

    private static void addError(String message, StringBuffer error) {
        if (error.length() != 0) {
            error.append(". ");
        }
        error.append(message);
    }

    private void configureImageLoading(Configuration parent, boolean strict) throws FOPException {
        if (parent == null) {
            return;
        }
        ImageImplRegistry registry = fopFactoryBuilder.getImageManager().getRegistry();
        Configuration[] penalties = parent.getChildren("penalty");
        try {
            for (int i = 0, c = penalties.length; i < c; i++) {
                Configuration penaltyCfg = penalties[i];
                String className = penaltyCfg.getAttribute("class");
                String value = penaltyCfg.getAttribute("value");
                Penalty p = null;
                if (value.toUpperCase(Locale.getDefault()).startsWith("INF")) {
                    p = Penalty.INFINITE_PENALTY;
                } else {
                    try {
                        p = Penalty.toPenalty(Integer.parseInt(value));
                    } catch (NumberFormatException nfe) {
                        LogUtil.handleException(log, nfe, strict);
                    }
                }
                if (p != null) {
                    registry.setAdditionalPenalty(className, p);
                }
            }
        } catch (ConfigurationException e) {
            LogUtil.handleException(log, e, strict);
        }
    }

    /**
     * Returns the {@link FopFactoryBuilder}.
     *
     * @return the object for configuring the {@link FopFactory}
     */
    public FopFactoryBuilder getFopFactoryBuilder() {
        return fopFactoryBuilder;
    }
}
