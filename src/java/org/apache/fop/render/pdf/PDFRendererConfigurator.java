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

import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.pdf.PDFAMode;
import org.apache.fop.pdf.PDFEncryptionParams;
import org.apache.fop.pdf.PDFFilterList;
import org.apache.fop.pdf.PDFXMode;
import org.apache.fop.render.PrintRendererConfigurator;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.util.LogUtil;

/**
 * PDF renderer configurator.
 */
public class PDFRendererConfigurator extends PrintRendererConfigurator {

    /**
     * Default constructor
     *
     * @param userAgent user agent
     */
    public PDFRendererConfigurator(FOUserAgent userAgent) {
        super(userAgent);
    }

    /**
     * Throws an UnsupportedOperationException.
     *
     * @param renderer not used
     */
    public void configure(Renderer renderer) {
        throw new UnsupportedOperationException();
    }

    private void configure(Configuration cfg, PDFRenderingUtil pdfUtil) throws FOPException {
        //PDF filters
        try {
            Map filterMap = buildFilterMapFromConfiguration(cfg);
            if (filterMap != null) {
                pdfUtil.setFilterMap(filterMap);
            }
        } catch (ConfigurationException e) {
            LogUtil.handleException(log, e, false);
        }

        String s = cfg.getChild(PDFConfigurationConstants.PDF_A_MODE, true).getValue(null);
        if (s != null) {
            pdfUtil.setAMode(PDFAMode.valueOf(s));
        }
        s = cfg.getChild(PDFConfigurationConstants.PDF_X_MODE, true).getValue(null);
        if (s != null) {
            pdfUtil.setXMode(PDFXMode.valueOf(s));
        }
            Configuration encryptionParamsConfig
                = cfg.getChild(PDFConfigurationConstants.ENCRYPTION_PARAMS, false);
        if (encryptionParamsConfig != null) {
            PDFEncryptionParams encryptionParams = pdfUtil.getEncryptionParams();
            Configuration ownerPasswordConfig = encryptionParamsConfig.getChild(
                    PDFConfigurationConstants.OWNER_PASSWORD, false);
            if (ownerPasswordConfig != null) {
                String ownerPassword = ownerPasswordConfig.getValue(null);
                if (ownerPassword != null) {
                    encryptionParams.setOwnerPassword(ownerPassword);
                }
            }
            Configuration userPasswordConfig = encryptionParamsConfig.getChild(
                    PDFConfigurationConstants.USER_PASSWORD, false);
            if (userPasswordConfig != null) {
                String userPassword = userPasswordConfig.getValue(null);
                if (userPassword != null) {
                    encryptionParams.setUserPassword(userPassword);
                }
            }
            Configuration noPrintConfig = encryptionParamsConfig.getChild(
                    PDFConfigurationConstants.NO_PRINT, false);
            if (noPrintConfig != null) {
                encryptionParams.setAllowPrint(false);
            }
            Configuration noCopyContentConfig = encryptionParamsConfig.getChild(
                    PDFConfigurationConstants.NO_COPY_CONTENT, false);
            if (noCopyContentConfig != null) {
                encryptionParams.setAllowCopyContent(false);
            }
            Configuration noEditContentConfig = encryptionParamsConfig.getChild(
                    PDFConfigurationConstants.NO_EDIT_CONTENT, false);
            if (noEditContentConfig != null) {
                encryptionParams.setAllowEditContent(false);
            }
            Configuration noAnnotationsConfig = encryptionParamsConfig.getChild(
                    PDFConfigurationConstants.NO_ANNOTATIONS, false);
            if (noAnnotationsConfig != null) {
                encryptionParams.setAllowEditAnnotations(false);
            }
            Configuration noFillInForms = encryptionParamsConfig.getChild(
                    PDFConfigurationConstants.NO_FILLINFORMS, false);
            if (noFillInForms != null) {
                encryptionParams.setAllowFillInForms(false);
            }
            Configuration noAccessContentConfig = encryptionParamsConfig.getChild(
                    PDFConfigurationConstants.NO_ACCESSCONTENT, false);
            if (noAccessContentConfig != null) {
                encryptionParams.setAllowAccessContent(false);
            }
            Configuration noAssembleDocConfig = encryptionParamsConfig.getChild(
                    PDFConfigurationConstants.NO_ASSEMBLEDOC, false);
            if (noAssembleDocConfig != null) {
                encryptionParams.setAllowAssembleDocument(false);
            }
            Configuration noPrintHqConfig = encryptionParamsConfig.getChild(
                    PDFConfigurationConstants.NO_PRINTHQ, false);
            if (noPrintHqConfig != null) {
                encryptionParams.setAllowPrintHq(false);
            }
            Configuration encryptionLengthConfig = encryptionParamsConfig.getChild(
                    PDFConfigurationConstants.ENCRYPTION_LENGTH, false);
            if (encryptionLengthConfig != null) {
                int encryptionLength = checkEncryptionLength(
                        Integer.parseInt(encryptionLengthConfig.getValue(null)));
                encryptionParams.setEncryptionLengthInBits(encryptionLength);
            }
        }

        s = cfg.getChild(PDFConfigurationConstants.KEY_OUTPUT_PROFILE, true).getValue(null);
        if (s != null) {
            pdfUtil.setOutputProfileURI(s);
        }
            Configuration disableColorSpaceConfig = cfg.getChild(
                    PDFConfigurationConstants.KEY_DISABLE_SRGB_COLORSPACE, false);
        if (disableColorSpaceConfig != null) {
            pdfUtil.setDisableSRGBColorSpace(
                    disableColorSpaceConfig.getValueAsBoolean(false));
        }
    }

    private int checkEncryptionLength(int encryptionLength) {
        int correctEncryptionLength = encryptionLength;
        if (encryptionLength < 40) {
            correctEncryptionLength = 40;
        } else if (encryptionLength > 128) {
            correctEncryptionLength = 128;
        } else if (encryptionLength % 8 != 0) {
            correctEncryptionLength = ((int) Math.round(encryptionLength / 8.0f)) * 8;
        }
        if (correctEncryptionLength != encryptionLength) {
            PDFEventProducer.Provider.get(userAgent.getEventBroadcaster())
                    .incorrectEncryptionLength(this, encryptionLength, correctEncryptionLength);
        }
        return correctEncryptionLength;
    }

    /**
     * Builds a filter map from an Avalon Configuration object.
     *
     * @param cfg the Configuration object
     * @return Map the newly built filter map
     * @throws ConfigurationException if a filter list is defined twice
     */
    public static Map buildFilterMapFromConfiguration(Configuration cfg)
                throws ConfigurationException {
        Map filterMap = new java.util.HashMap();
        Configuration[] filterLists = cfg.getChildren("filterList");
        for (int i = 0; i < filterLists.length; i++) {
            Configuration filters = filterLists[i];
            String type = filters.getAttribute("type", null);
            Configuration[] filt = filters.getChildren("value");
            List filterList = new java.util.ArrayList();
            for (int j = 0; j < filt.length; j++) {
                String name = filt[j].getValue();
                filterList.add(name);
            }

            if (type == null) {
                type = PDFFilterList.DEFAULT_FILTER;
            }

            if (!filterList.isEmpty() && log.isDebugEnabled()) {
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
                log.debug(debug.toString());
            }

            if (filterMap.get(type) != null) {
                throw new ConfigurationException("A filterList of type '"
                    + type + "' has already been defined");
            }
            filterMap.put(type, filterList);
        }
        return filterMap;
    }

    // ---=== IFDocumentHandler configuration ===---

    /** {@inheritDoc} */
    public void configure(IFDocumentHandler documentHandler) throws FOPException {
        Configuration cfg = super.getRendererConfig(documentHandler.getMimeType());
        if (cfg != null) {
            PDFDocumentHandler pdfDocumentHandler = (PDFDocumentHandler)documentHandler;
            PDFRenderingUtil pdfUtil = pdfDocumentHandler.getPDFUtil();
            configure(cfg, pdfUtil);
        }
    }

}
