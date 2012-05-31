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

import org.apache.fop.apps.FopConfBuilder.RendererConfBuilder;

import static org.apache.fop.render.ps.PSRendererConfigurationOptions.AUTO_ROTATE_LANDSCAPE;
import static org.apache.fop.render.ps.PSRendererConfigurationOptions.DSC_COMPLIANT;
import static org.apache.fop.render.ps.PSRendererConfigurationOptions.LANGUAGE_LEVEL;
import static org.apache.fop.render.ps.PSRendererConfigurationOptions.OPTIMIZE_RESOURCES;
import static org.apache.fop.render.ps.PSRendererConfigurationOptions.SAFE_SET_PAGE_DEVICE;

/**
 * A fop conf builder specific to a particular renderer for Postscript.
 */
public final class PSRendererConfBuilder extends RendererConfBuilder {

    public PSRendererConfBuilder() {
        super(MimeConstants.MIME_POSTSCRIPT);
    }

    public PSRendererConfBuilder setAutoRotateLandscape(boolean value) {
        createTextElement(AUTO_ROTATE_LANDSCAPE, String.valueOf(value));
        return this;
    }

    public PSRendererConfBuilder setSafeSetPageDevice(boolean value) {
        createTextElement(SAFE_SET_PAGE_DEVICE, String.valueOf(value));
        return this;
    }

    public PSRendererConfBuilder setDscCompliant(boolean value) {
        createTextElement(DSC_COMPLIANT, String.valueOf(value));
        return this;
    }

    public PSRendererConfBuilder setLanguageLevel(int value) {
        createTextElement(LANGUAGE_LEVEL, String.valueOf(value));
        return this;
    }

    public PSRendererConfBuilder setOptimizeResources(boolean value) {
        createTextElement(OPTIMIZE_RESOURCES, String.valueOf(value));
        return this;
    }
}
