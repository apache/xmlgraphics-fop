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

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.pdf.PDFEncryptionParams;
import org.apache.fop.render.DefaultRendererConfigurator;
import org.apache.fop.render.RendererConfig.RendererConfigParser;
import org.apache.fop.render.intermediate.IFDocumentHandler;

/**
 * PDF renderer configurator.
 */
public class PDFRendererConfigurator extends DefaultRendererConfigurator {

    /**
     * Default constructor
     *
     * @param userAgent user agent
     */
    public PDFRendererConfigurator(FOUserAgent userAgent, RendererConfigParser rendererConfigParser) {
        super(userAgent, rendererConfigParser);
    }

    // ---=== IFDocumentHandler configuration ===---

    /** {@inheritDoc} */
    public void configure(IFDocumentHandler documentHandler) throws FOPException {
        final PDFRendererConfig pdfConfig = (PDFRendererConfig) getRendererConfig(documentHandler);
        if (pdfConfig != null) {
            PDFDocumentHandler pdfDocumentHandler = (PDFDocumentHandler) documentHandler;
            PDFRenderingUtil pdfUtil = pdfDocumentHandler.getPDFUtil();
            if (pdfConfig.getFilterMap() != null) {
                pdfUtil.setFilterMap(pdfConfig.getFilterMap());
            }
            if (pdfConfig.getPDFAMode() != null) {
                pdfUtil.setAMode(pdfConfig.getPDFAMode());
            }
            if (pdfConfig.getPDFXMode() != null) {
                pdfUtil.setXMode(pdfConfig.getPDFXMode());
            }
            if (pdfConfig.getOutputProfileURI() != null) {
                pdfUtil.setOutputProfileURI(pdfConfig.getOutputProfileURI());
            }
            if (pdfConfig.getPDFVersion() != null) {
                pdfUtil.setPDFVersion(pdfConfig.getPDFVersion());
            }
            if (pdfConfig.getDisableSRGBColorSpace() != null) {
                pdfUtil.setDisableSRGBColorSpace(pdfConfig.getDisableSRGBColorSpace());
            }

            PDFEncryptionParams config = pdfConfig.getEncryptionParameters();
            if (config != null) {
                PDFEncryptionParams utilParams = pdfUtil.getEncryptionParams();
                if (config.getUserPassword() != null) {
                    utilParams.setUserPassword(config.getUserPassword());
                }
                if (config.getOwnerPassword() != null) {
                    utilParams.setOwnerPassword(config.getOwnerPassword());
                }
                utilParams.setAllowPrint(config.isAllowPrint());
                utilParams.setAllowCopyContent(config.isAllowCopyContent());
                utilParams.setAllowEditContent(config.isAllowEditContent());
                utilParams.setAllowAssembleDocument(config.isAllowAssembleDocument());
                utilParams.setAllowAccessContent(config.isAllowAccessContent());
                utilParams.setAllowFillInForms(config.isAllowFillInForms());
                utilParams.setAllowPrintHq(config.isAllowPrintHq());
                utilParams.setEncryptionLengthInBits(config.getEncryptionLengthInBits());
            }
        }
    }

}
