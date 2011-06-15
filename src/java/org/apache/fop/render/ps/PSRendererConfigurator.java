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

import java.util.Locale;

import org.apache.avalon.framework.configuration.Configuration;

import org.apache.xmlgraphics.ps.PSGenerator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.PrintRendererConfigurator;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;

/**
 * Postscript renderer config
 */
public class PSRendererConfigurator extends PrintRendererConfigurator
            implements IFDocumentHandlerConfigurator {

    /**
     * Default constructor
     * @param userAgent user agent
     */
    public PSRendererConfigurator(FOUserAgent userAgent) {
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

    private void configure(PSRenderingUtil psUtil, Configuration cfg) {
        psUtil.setAutoRotateLandscape(
            cfg.getChild("auto-rotate-landscape").getValueAsBoolean(false));
        Configuration child;
        child = cfg.getChild("language-level");
        if (child != null) {
            psUtil.setLanguageLevel(child.getValueAsInteger(
                    PSGenerator.DEFAULT_LANGUAGE_LEVEL));
        }
        child = cfg.getChild("optimize-resources");
        if (child != null) {
            psUtil.setOptimizeResources(child.getValueAsBoolean(false));
        }
        child = cfg.getChild("rendering");
        if (child != null) {
            psUtil.setRenderingMode(PSRenderingMode.valueOf(
                    child.getValue(psUtil.getRenderingMode().toString())
                    .toUpperCase(Locale.ENGLISH)));
        }
        psUtil.setSafeSetPageDevice(
            cfg.getChild("safe-set-page-device").getValueAsBoolean(false));
        psUtil.setDSCComplianceEnabled(
            cfg.getChild("dsc-compliant").getValueAsBoolean(true));
    }

    /** {@inheritDoc} */
    public void configure(IFDocumentHandler documentHandler) throws FOPException {
        Configuration cfg = super.getRendererConfig(documentHandler.getMimeType());
        if (cfg != null) {
            PSDocumentHandler psDocumentHandler = (PSDocumentHandler)documentHandler;
            configure(psDocumentHandler.getPSUtil(), cfg);
        }

    }

}
