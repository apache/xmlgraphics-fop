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
import java.io.IOException;
import java.net.MalformedURLException;

import org.xml.sax.SAXException;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.fonts.FontManager;
import org.apache.fop.fonts.FontManagerConfigurator;
import org.apache.fop.util.LogUtil;

/**
 * FopFactory configurator
 */
public class FopFactoryConfigurator {

    /** Defines if FOP should use an alternative rule to determine text indents */
    public static final boolean DEFAULT_BREAK_INDENT_INHERITANCE = false;

    /** Defines if FOP should validate the user config strictly */
    public static final boolean DEFAULT_STRICT_USERCONFIG_VALIDATION = true;

    /** Defines if FOP should use strict validation for FO and user config */
    public static final boolean DEFAULT_STRICT_FO_VALIDATION = true;

    /** Defines the default page-width */
    public static final String DEFAULT_PAGE_WIDTH = "8.26in";

    /** Defines the default page-height */
    public static final String DEFAULT_PAGE_HEIGHT = "11in";

    /** Defines the default source resolution (72dpi) for FOP */
    public static final float DEFAULT_SOURCE_RESOLUTION = 72.0f; //dpi

    /** Defines the default target resolution (72dpi) for FOP */
    public static final float DEFAULT_TARGET_RESOLUTION = 72.0f; //dpi

    /** logger instance */
    private final Log log = LogFactory.getLog(FopFactoryConfigurator.class);

    /** Fop factory */
    private FopFactory factory = null;

    /** Fop factory configuration */
    private Configuration cfg = null;

    /**
     * Default constructor
     * @param factory fop factory
     */
    public FopFactoryConfigurator(FopFactory factory) {
        super();
        this.factory = factory;
    }

    /**
     * Initializes user agent settings from the user configuration
     * file, if present: baseURL, resolution, default page size,...
     * @param factory fop factory
     * @throws FOPException fop exception
     */
    public void configure(FopFactory factory) throws FOPException {
        if (log.isDebugEnabled()) {
            log.debug("Initializing FopFactory Configuration");
        }

        // strict configuration
        if (cfg.getChild("strict-configuration", false) != null) {
            try {
                factory.setStrictUserConfigValidation(
                        cfg.getChild("strict-configuration").getValueAsBoolean());
            } catch (ConfigurationException e) {
                LogUtil.handleException(log, e, false);
            }
        }
        boolean strict = factory.validateUserConfigStrictly();

        // strict fo validation
        if (cfg.getChild("strict-validation", false) != null) {
            try {
                factory.setStrictValidation(
                        cfg.getChild("strict-validation").getValueAsBoolean());
            } catch (ConfigurationException e) {
                LogUtil.handleException(log, e, strict);
            }
        }

        // base definitions for relative path resolution
        if (cfg.getChild("base", false) != null) {
            try {
                factory.setBaseURL(
                        cfg.getChild("base").getValue(null));
            } catch (MalformedURLException mfue) {
                LogUtil.handleException(log, mfue, strict);
            }
        }
        if (cfg.getChild("hyphenation-base", false) != null) {
            try {
                factory.setHyphenBaseURL(
                        cfg.getChild("hyphenation-base").getValue(null));
            } catch (MalformedURLException mfue) {
                LogUtil.handleException(log, mfue, strict);
            }
        }

        // renderer options
        if (cfg.getChild("source-resolution", false) != null) {
            factory.setSourceResolution(
                    cfg.getChild("source-resolution").getValueAsFloat(
                            FopFactoryConfigurator.DEFAULT_SOURCE_RESOLUTION));
            if (log.isDebugEnabled()) {
                log.debug("source-resolution set to: " + factory.getSourceResolution()
                    + "dpi (px2mm=" + factory.getSourcePixelUnitToMillimeter() + ")");
            }
        }
        if (cfg.getChild("target-resolution", false) != null) {
            factory.setTargetResolution(
                    cfg.getChild("target-resolution").getValueAsFloat(
                            FopFactoryConfigurator.DEFAULT_TARGET_RESOLUTION));
            if (log.isDebugEnabled()) {
                log.debug("target-resolution set to: " + factory.getTargetResolution()
                        + "dpi (px2mm=" + factory.getTargetPixelUnitToMillimeter()
                        + ")");
            }
        }
        if (cfg.getChild("break-indent-inheritance", false) != null) {
            try {
                factory.setBreakIndentInheritanceOnReferenceAreaBoundary(
                        cfg.getChild("break-indent-inheritance").getValueAsBoolean());
            } catch (ConfigurationException e) {
                LogUtil.handleException(log, e, strict);
            }
        }
        Configuration pageConfig = cfg.getChild("default-page-settings");
        if (pageConfig.getAttribute("height", null) != null) {
            factory.setPageHeight(
                    pageConfig.getAttribute("height", FopFactoryConfigurator.DEFAULT_PAGE_HEIGHT));
            if (log.isInfoEnabled()) {
                log.info("Default page-height set to: " + factory.getPageHeight());
            }
        }
        if (pageConfig.getAttribute("width", null) != null) {
            factory.setPageWidth(
                    pageConfig.getAttribute("width", FopFactoryConfigurator.DEFAULT_PAGE_WIDTH));
            if (log.isInfoEnabled()) {
                log.info("Default page-width set to: " + factory.getPageWidth());
            }
        }

        // configure font manager
        FontManager fontManager = factory.getFontManager();
        FontManagerConfigurator fontManagerConfigurator = new FontManagerConfigurator(cfg);
        fontManagerConfigurator.configure(fontManager, strict);
    }

    /**
     * Set the user configuration.
     * @param userConfigFile the configuration file
     * @throws IOException if an I/O error occurs
     * @throws SAXException if a parsing error occurs
     */
    public void setUserConfig(File userConfigFile) throws SAXException, IOException {
        try {
            DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
            setUserConfig(cfgBuilder.buildFromFile(userConfigFile));
        } catch (ConfigurationException e) {
            throw new FOPException(e);
        }
    }

    /**
     * Set the user configuration from an URI.
     * @param uri the URI to the configuration file
     * @throws IOException if an I/O error occurs
     * @throws SAXException if a parsing error occurs
     */
    public void setUserConfig(String uri) throws SAXException, IOException {
        try {
            DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
            setUserConfig(cfgBuilder.build(uri));
        } catch (ConfigurationException e) {
            throw new FOPException(e);
        }
    }

    /**
     * Set the user configuration.
     * @param cfg avalon configuration
     * @throws FOPException if a configuration problem occurs
     */
    public void setUserConfig(Configuration cfg) throws FOPException {
        this.cfg = cfg;
        configure(this.factory);
    }

    /**
     * Get the avalon user configuration.
     * @return the user configuration
     */
    public Configuration getUserConfig() {
        return this.cfg;
    }
}
