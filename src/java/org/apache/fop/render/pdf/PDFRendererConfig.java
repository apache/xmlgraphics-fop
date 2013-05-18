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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.fonts.DefaultFontConfig;
import org.apache.fop.fonts.DefaultFontConfig.DefaultFontConfigParser;
import org.apache.fop.pdf.PDFEncryptionParams;
import org.apache.fop.pdf.PDFFilterList;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.render.RendererConfigOption;
import org.apache.fop.util.LogUtil;

import static org.apache.fop.render.pdf.PDFEncryptionOption.ENCRYPTION_LENGTH;
import static org.apache.fop.render.pdf.PDFEncryptionOption.ENCRYPTION_PARAMS;
import static org.apache.fop.render.pdf.PDFEncryptionOption.ENCRYPT_METADATA;
import static org.apache.fop.render.pdf.PDFEncryptionOption.NO_ACCESSCONTENT;
import static org.apache.fop.render.pdf.PDFEncryptionOption.NO_ANNOTATIONS;
import static org.apache.fop.render.pdf.PDFEncryptionOption.NO_ASSEMBLEDOC;
import static org.apache.fop.render.pdf.PDFEncryptionOption.NO_COPY_CONTENT;
import static org.apache.fop.render.pdf.PDFEncryptionOption.NO_EDIT_CONTENT;
import static org.apache.fop.render.pdf.PDFEncryptionOption.NO_FILLINFORMS;
import static org.apache.fop.render.pdf.PDFEncryptionOption.NO_PRINT;
import static org.apache.fop.render.pdf.PDFEncryptionOption.NO_PRINTHQ;
import static org.apache.fop.render.pdf.PDFEncryptionOption.OWNER_PASSWORD;
import static org.apache.fop.render.pdf.PDFEncryptionOption.USER_PASSWORD;
import static org.apache.fop.render.pdf.PDFRendererOption.DISABLE_SRGB_COLORSPACE;
import static org.apache.fop.render.pdf.PDFRendererOption.FILTER_LIST;
import static org.apache.fop.render.pdf.PDFRendererOption.OUTPUT_PROFILE;
import static org.apache.fop.render.pdf.PDFRendererOption.PDF_A_MODE;
import static org.apache.fop.render.pdf.PDFRendererOption.PDF_X_MODE;
import static org.apache.fop.render.pdf.PDFRendererOption.VERSION;

/**
 * The PDF renderer configuration data object.
 */
public final class PDFRendererConfig implements RendererConfig {

    private static final Log LOG = LogFactory.getLog(PDFRendererConfig.class);

    private final PDFRendererOptionsConfig configOption;

    private final DefaultFontConfig fontConfig;

    private PDFRendererConfig(DefaultFontConfig fontConfig, PDFRendererOptionsConfig config) {
        this.fontConfig = fontConfig;
        this.configOption = config;
    }

    public PDFRendererOptionsConfig getConfigOptions() {
        return configOption;
    }

    public DefaultFontConfig getFontInfoConfig() {
        return fontConfig;
    }

    /**
     * The PDF renderer configuration data parser.
     */
    public static final class PDFRendererConfigParser implements RendererConfigParser {

        public PDFRendererConfig build(FOUserAgent userAgent, Configuration cfg) throws FOPException {
            boolean strict = userAgent != null ? userAgent.validateUserConfigStrictly() : false;
            return new ParserHelper(cfg, userAgent, strict).pdfConfig;
        }

        public String getMimeType() {
            return MimeConstants.MIME_PDF;
        }
    }

    private static final class ParserHelper {

        private final Map<PDFRendererOption, Object> configOptions
        = new EnumMap<PDFRendererOption, Object>(PDFRendererOption.class);

        private PDFEncryptionParams encryptionConfig;

        private PDFRendererConfig pdfConfig;

        private ParserHelper(Configuration cfg, FOUserAgent userAgent, boolean strict) throws FOPException {
            if (cfg != null) {
                configure(cfg, userAgent, strict);
            }
            pdfConfig = new PDFRendererConfig(new DefaultFontConfigParser().parse(cfg, strict),
                    new PDFRendererOptionsConfig(configOptions, encryptionConfig));
        }

        private void parseAndPut(PDFRendererOption option, Configuration cfg) {
            put(option, option.parse(parseConfig(cfg, option)));
        }

        private void put(PDFRendererOption option, Object value) {
            if (value != null && !value.equals(option.getDefaultValue())) {
                configOptions.put(option, value);
            }
        }

        private void configure(Configuration cfg, FOUserAgent userAgent, boolean strict) throws FOPException {
            try {
                buildFilterMapFromConfiguration(cfg);
                parseAndPut(PDF_A_MODE, cfg);
                parseAndPut(PDF_X_MODE, cfg);
                configureEncryptionParams(cfg, userAgent, strict);
                parseAndPut(OUTPUT_PROFILE, cfg);
                parseAndPut(DISABLE_SRGB_COLORSPACE, cfg);

                parseAndPut(VERSION, cfg);
            } catch (ConfigurationException e) {
                LogUtil.handleException(LOG, e, strict);
            }
        }

        private void configureEncryptionParams(Configuration cfg, FOUserAgent userAgent, boolean strict) {
            Configuration encryptCfg = cfg.getChild(ENCRYPTION_PARAMS, false);
            if (encryptCfg != null) {
                encryptionConfig = new PDFEncryptionParams();
                encryptionConfig.setOwnerPassword(parseConfig(encryptCfg, OWNER_PASSWORD));
                encryptionConfig.setUserPassword(parseConfig(encryptCfg, USER_PASSWORD));
                encryptionConfig.setAllowPrint(!doesValueExist(encryptCfg, NO_PRINT));
                encryptionConfig.setAllowCopyContent(!doesValueExist(encryptCfg, NO_COPY_CONTENT));
                encryptionConfig.setAllowEditContent(!doesValueExist(encryptCfg, NO_EDIT_CONTENT));
                encryptionConfig.setAllowEditAnnotations(!doesValueExist(encryptCfg, NO_ANNOTATIONS));
                encryptionConfig.setAllowFillInForms(!doesValueExist(encryptCfg, NO_FILLINFORMS));
                encryptionConfig.setAllowAccessContent(!doesValueExist(encryptCfg, NO_ACCESSCONTENT));
                encryptionConfig.setAllowAssembleDocument(!doesValueExist(encryptCfg, NO_ASSEMBLEDOC));
                encryptionConfig.setAllowPrintHq(!doesValueExist(encryptCfg, NO_PRINTHQ));
                encryptionConfig.setEncryptMetadata(getConfigValue(encryptCfg, ENCRYPT_METADATA, true));
                String encryptionLength = parseConfig(encryptCfg, ENCRYPTION_LENGTH);
                if (encryptionLength != null) {
                    int validatedLength = checkEncryptionLength(Integer.parseInt(encryptionLength), userAgent);
                    encryptionConfig.setEncryptionLengthInBits(validatedLength);
                }
            }
        }

        private void buildFilterMapFromConfiguration(Configuration cfg) throws ConfigurationException,
        FOPException {
            Configuration[] filterLists = cfg.getChildren(FILTER_LIST.getName());
            Map<String, List<String>> filterMap = new HashMap<String, List<String>>();
            for (Configuration filters : filterLists) {
                String type = filters.getAttribute("type", PDFFilterList.DEFAULT_FILTER);
                List<String> filterList = new ArrayList<String>();
                for (Configuration nameCfg : filters.getChildren("value")) {
                    filterList.add(nameCfg.getValue());
                }
                if (!filterList.isEmpty() && LOG.isDebugEnabled()) {
                    StringBuffer debug = new StringBuffer("Adding PDF filter");
                    if (filterList.size() != 1) {
                        debug.append("s");
                    }
                    debug.append(" for type ").append(type).append(": ");
                    for (int j = 0; j < filterList.size(); j++) {
                        if (j != 0) {
                            debug.append(", ");
                        }
                        debug.append(filterList.get(j));
                    }
                    LOG.debug(debug.toString());
                }

                if (filterMap.get(type) != null) {
                    throw new ConfigurationException("A filterList of type '"
                            + type + "' has already been defined");
                }
                filterMap.put(type, filterList);
            }
            put(FILTER_LIST, filterMap);
        }

        private String parseConfig(Configuration cfg, RendererConfigOption option) {
            Configuration child = cfg.getChild(option.getName());
            return child.getValue(null);
        }

        private boolean doesValueExist(Configuration cfg, RendererConfigOption option) {
            return cfg.getChild(option.getName(), false) != null;
        }

        private boolean getConfigValue(Configuration cfg, RendererConfigOption option, boolean defaultTo) {
            if (cfg.getChild(option.getName(), false) != null) {
                Configuration child = cfg.getChild(option.getName());
                try {
                    return child.getValueAsBoolean();
                } catch (ConfigurationException e) {
                    return defaultTo;
                }
            } else {
                return defaultTo;
            }
        }

        private int checkEncryptionLength(int encryptionLength, FOUserAgent userAgent) {
            int correctEncryptionLength = encryptionLength;
            if (encryptionLength < 40) {
                correctEncryptionLength = 40;
            } else if (encryptionLength > 256) {
                correctEncryptionLength = 256;
            } else if (encryptionLength > 128 && encryptionLength < 256) {
                correctEncryptionLength = 128;
            } else if (encryptionLength % 8 != 0) {
                correctEncryptionLength = Math.round(encryptionLength / 8.0f) * 8;
            }
            if (correctEncryptionLength != encryptionLength && userAgent != null) {
                PDFEventProducer.Provider.get(userAgent.getEventBroadcaster())
                .incorrectEncryptionLength(this, encryptionLength,
                        correctEncryptionLength);
            }
            return correctEncryptionLength;
        }
    }

}
