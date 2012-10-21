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

package org.apache.fop.render;

import org.apache.avalon.framework.configuration.Configuration;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.FontConfig;

/**
 * Implementations of this interface have all the renderer-specific configuration data found in the
 * FOP-conf. This object is just a data object that is created by the {@link RendererConfigParser}
 * when the FOP conf is parsed.
 */
public interface RendererConfig {

    /**
     * Returns the render-specific font configuration information.
     * @return the font config
     */
    FontConfig getFontInfoConfig();

    /**
     * Implementations of this interface parse the relevant renderer-specific configuration data
     * within the FOP-conf and create a {@link RendererConfig}.
     */
    public interface RendererConfigParser {

        /**
         * Builds the object that contains the renderer configuration data.
         *
         * @param userAgent the user agent
         * @param rendererConfiguration the Avalon config object for parsing the data
         * @return the configuration data object
         * @throws FOPException if an error occurs while parsing the fop conf
         */
        RendererConfig build(FOUserAgent userAgent, Configuration rendererConfiguration) throws FOPException;

        /**
         * The MIME type of the renderer.
         *
         * @return the mime type
         */
        String getMimeType();
    }
}
