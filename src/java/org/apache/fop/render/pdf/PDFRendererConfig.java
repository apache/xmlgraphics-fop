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

import java.net.URI;
import java.net.URISyntaxException;
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
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.DefaultFontConfig;
import org.apache.fop.fonts.DefaultFontConfig.DefaultFontConfigParser;
import org.apache.fop.pdf.PDFAMode;
import org.apache.fop.pdf.PDFEncryptionParams;
import org.apache.fop.pdf.PDFFilterList;
import org.apache.fop.pdf.PDFXMode;
import org.apache.fop.pdf.Version;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.util.LogUtil;

import static org.apache.fop.render.pdf.PDFRendererConfigOption.DISABLE_SRGB_COLORSPACE;
import static org.apache.fop.render.pdf.PDFRendererConfigOption.ENCRYPTION_LENGTH;
import static org.apache.fop.render.pdf.PDFRendererConfigOption.ENCRYPTION_PARAMS;
import static org.apache.fop.render.pdf.PDFRendererConfigOption.FILTER_LIST;
import static org.apache.fop.render.pdf.PDFRendererConfigOption.NO_ACCESSCONTENT;
import static org.apache.fop.render.pdf.PDFRendererConfigOption.NO_ANNOTATIONS;
import static org.apache.fop.render.pdf.PDFRendererConfigOption.NO_ASSEMBLEDOC;
import static org.apache.fop.render.pdf.PDFRendererConfigOption.NO_COPY_CONTENT;
import static org.apache.fop.render.pdf.PDFRendererConfigOption.NO_EDIT_CONTENT;
import static org.apache.fop.render.pdf.PDFRendererConfigOption.NO_FILLINFORMS;
import static org.apache.fop.render.pdf.PDFRendererConfigOption.NO_PRINT;
import static org.apache.fop.render.pdf.PDFRendererConfigOption.NO_PRINTHQ;
import static org.apache.fop.render.pdf.PDFRendererConfigOption.OUTPUT_PROFILE;
import static org.apache.fop.render.pdf.PDFRendererConfigOption.OWNER_PASSWORD;
import static org.apache.fop.render.pdf.PDFRendererConfigOption.PDF_A_MODE;
import static org.apache.fop.render.pdf.PDFRendererConfigOption.PDF_X_MODE;
import static org.apache.fop.render.pdf.PDFRendererConfigOption.USER_PASSWORD;
import static org.apache.fop.render.pdf.PDFRendererConfigOption.VERSION;

/**
 * The PDF renderer configuration data object.
 */
public final class PDFRendererConfig implements RendererConfig {

    private static final Log LOG = LogFactory.getLog(PDFRendererConfig.class);

    private final Map<PDFRendererConfigOption, Object> configOptions
            = new EnumMap<PDFRendererConfigOption, Object>(PDFRendererConfigOption.class);

    private final DefaultFontConfig fontConfig;

    private PDFRendererConfig(DefaultFontConfig fontConfig) {
        this.fontConfig = fontConfig;
    }

    public DefaultFontConfig getFontInfoConfig() {
        return fontConfig;
    }

    public Map<String, List<String>> getFilterMap() {
        return (Map<String, List<String>>) configOptions.get(FILTER_LIST);
    }

    public PDFAMode getPDFAMode() {
        return (PDFAMode) configOptions.get(PDF_A_MODE);
    }

    public PDFXMode getPDFXMode() {
        return (PDFXMode) configOptions.get(PDF_X_MODE);
    }

    public PDFEncryptionParams getEncryptionParameters() {
        return (PDFEncryptionParams) configOptions.get(ENCRYPTION_PARAMS);
    }

    public URI getOutputProfileURI() {
        return (URI) configOptions.get(OUTPUT_PROFILE);
    }

    public Boolean getDisableSRGBColorSpace() {
        return (Boolean) configOptions.get(DISABLE_SRGB_COLORSPACE);
    }

    public Version getPDFVersion() {
        String pdfVersion = (String) configOptions.get(VERSION);
        return pdfVersion == null ? null : Version.getValueOf(pdfVersion);
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

        private PDFRendererConfig pdfConfig;

        private ParserHelper(Configuration cfg, FOUserAgent userAgent, boolean strict) throws FOPException {
            pdfConfig = new PDFRendererConfig(new DefaultFontConfigParser().parse(cfg, strict));
            if (cfg != null) {
                configure(cfg, userAgent, strict);
            }
        }

        private void put(PDFRendererConfigOption option, Object value) {
            if (value != null && !value.equals(option.getDefaultValue())) {
                pdfConfig.configOptions.put(option, value);
            }
        }

        private void configure(Configuration cfg, FOUserAgent userAgent, boolean strict)
                throws FOPException {
            try {
                buildFilterMapFromConfiguration(cfg);
                put(PDF_A_MODE, PDFAMode.getValueOf(parseConfig(cfg, PDF_A_MODE)));
                put(PDF_X_MODE, PDFXMode.getValueOf(parseConfig(cfg, PDF_X_MODE)));
                Configuration encryptCfg = cfg.getChild(ENCRYPTION_PARAMS.getName(), false);
                if (encryptCfg != null) {
                    PDFEncryptionParams encryptionConfig = new PDFEncryptionParams();
                    encryptionConfig.setOwnerPassword(parseConfig(encryptCfg, OWNER_PASSWORD));
                    encryptionConfig.setUserPassword(parseConfig(encryptCfg, USER_PASSWORD));
                    encryptionConfig.setAllowPrint(!doesValueExist(encryptCfg, NO_PRINT));
                    encryptionConfig.setAllowCopyContent(!doesValueExist(encryptCfg, NO_COPY_CONTENT));
                    encryptionConfig.setAllowEditContent(!doesValueExist(encryptCfg, NO_EDIT_CONTENT));
                    encryptionConfig.setAllowEditAnnotations(!doesValueExist(encryptCfg, NO_ANNOTATIONS));
                    encryptionConfig.setAllowFillInForms(!doesValueExist(encryptCfg, NO_FILLINFORMS));
                    encryptionConfig.setAllowAccessContent(!doesValueExist(encryptCfg, NO_ACCESSCONTENT));
                    encryptionConfig.setAllowAssembleDocument(!doesValueExist(encryptCfg,
                            NO_ASSEMBLEDOC));
                    encryptionConfig.setAllowPrintHq(!doesValueExist(encryptCfg, NO_PRINTHQ));
                    String encryptionLength = parseConfig(encryptCfg, ENCRYPTION_LENGTH);
                    if (encryptionLength != null) {
                        int validatedLength = checkEncryptionLength(Integer.parseInt(encryptionLength),
                                userAgent);
                        encryptionConfig.setEncryptionLengthInBits(validatedLength);
                    }
                    put(ENCRYPTION_PARAMS, encryptionConfig);
                }
                put(OUTPUT_PROFILE, InternalResourceResolver.cleanURI(parseConfig(cfg, OUTPUT_PROFILE)));
                put(DISABLE_SRGB_COLORSPACE, Boolean.valueOf(parseConfig(cfg, DISABLE_SRGB_COLORSPACE)));
                put(VERSION, getPDFDocVersion(cfg));
            } catch (ConfigurationException e) {
                LogUtil.handleException(LOG, e, strict);
            } catch (URISyntaxException use) {
                LogUtil.handleException(LOG, use, strict);
            }
        }

        private void buildFilterMapFromConfiguration(Configuration cfg)
                throws ConfigurationException, FOPException {
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
                    LogUtil.handleError(LOG, debug.toString(), true);
                }

                if (filterMap.get(type) != null) {
                    throw new ConfigurationException("A filterList of type '"
                            + type + "' has already been defined");
                }
                filterMap.put(type, filterList);
            }
            put(FILTER_LIST, filterMap);
        }

        private String parseConfig(Configuration cfg, PDFRendererConfigOption option) {
            Configuration child = cfg.getChild(option.getName());
            return child.getValue(null);
        }

        private boolean doesValueExist(Configuration cfg, PDFRendererConfigOption option) {
            return cfg.getChild(option.getName(), false) != null;
        }

        private String getPDFDocVersion(Configuration cfg) throws FOPException {
            Configuration pdfVersion = cfg.getChild(VERSION.getName(), false);
            if (pdfVersion != null) {
                String version = pdfVersion.getValue(null);
                if (version != null && version.length() != 0) {
                    return version;
                } else {
                    throw new FOPException("The PDF version has not been set.");
                }
            }
            return null;
        }

        private int checkEncryptionLength(int encryptionLength, FOUserAgent userAgent) {
            int correctEncryptionLength = encryptionLength;
            if (encryptionLength < 40) {
                correctEncryptionLength = 40;
            } else if (encryptionLength > 128) {
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
