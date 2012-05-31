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

package org.apache.fop.render.pcl;


import org.apache.fop.apps.FopConfBuilder.RendererConfBuilder;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.render.pcl.PCLRendererConfig.Options;

import static org.apache.fop.render.pcl.PCLRendererConfig.Options.DISABLE_PJL;
import static org.apache.fop.render.pcl.PCLRendererConfig.Options.RENDERING_MODE;
import static org.apache.fop.render.pcl.PCLRendererConfig.Options.TEXT_RENDERING;
/**
 * A config builder specific to a particular renderer for specific MIME type.
 */
public final class PCLRendererConfBuilder extends RendererConfBuilder {

    public PCLRendererConfBuilder() {
        super(MimeConstants.MIME_PCL);
    }

    public PCLRendererConfBuilder setRenderingMode(PCLRenderingMode mode) {
        return setRenderingMode(mode.getName());
    }

    public PCLRendererConfBuilder setRenderingMode(String value) {
        return createTextElement(RENDERING_MODE, value);
    }

    public PCLRendererConfBuilder setTextRendering(String value) {
        return createTextElement(TEXT_RENDERING, value);
    }

    public PCLRendererConfBuilder setDisablePjl(boolean value) {
        return createTextElement(DISABLE_PJL, String.valueOf(value));
    }

    private PCLRendererConfBuilder createTextElement(Options option, String value) {
        createTextElement(option.getName(), value);
        return this;
    }

}
