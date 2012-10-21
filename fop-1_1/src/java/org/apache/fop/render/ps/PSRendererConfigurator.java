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

package org.apache.fop.render.ps;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.DefaultRendererConfigurator;
import org.apache.fop.render.RendererConfig.RendererConfigParser;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;


/**
 * Postscript renderer config
 */
public class PSRendererConfigurator extends DefaultRendererConfigurator
        implements IFDocumentHandlerConfigurator {

    /**
     * Default constructor
     * @param userAgent user agent
     */
    public PSRendererConfigurator(FOUserAgent userAgent, RendererConfigParser rendererConfigParser) {
        super(userAgent, rendererConfigParser);
    }

    private void configure(PSRenderingUtil psUtil, PSRendererConfig psConfig) {
        if (psConfig.isAutoRotateLandscape() != null) {
            psUtil.setAutoRotateLandscape(psConfig.isAutoRotateLandscape());
        }
        if (psConfig.getLanguageLevel() != null) {
            psUtil.setLanguageLevel(psConfig.getLanguageLevel());
        }
        if (psConfig.isOptimizeResources() != null) {
            psUtil.setOptimizeResources(psConfig.isOptimizeResources());
        }
        if (psConfig.isSafeSetPageDevice() != null) {
            psUtil.setSafeSetPageDevice(psConfig.isSafeSetPageDevice());
        }
        if (psConfig.isDscComplianceEnabled() != null) {
            psUtil.setDSCComplianceEnabled(psConfig.isDscComplianceEnabled());
        }
        if (psConfig.getRenderingMode() != null) {
            psUtil.setRenderingMode(psConfig.getRenderingMode());
        }
    }

    @Override
    public void configure(IFDocumentHandler documentHandler) throws FOPException {
        PSRendererConfig psConfig = (PSRendererConfig) getRendererConfig(documentHandler);
        if (psConfig != null) {
            PSDocumentHandler psDocumentHandler = (PSDocumentHandler) documentHandler;
            PSRenderingUtil psUtil = psDocumentHandler.getPSUtil();
            configure(psUtil, psConfig);
        }
    }
}
