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
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOUserAgent;

/**
 * Abstract base classes for renderer-related configurator classes. This class basically just
 * provides an accessor to the specific renderer configuration object.
 */
public abstract class AbstractRendererConfigurator {

    /** logger instance */
    protected static Log log = LogFactory.getLog(AbstractRendererConfigurator.class);

    /** fop factory configuration */
    protected FOUserAgent userAgent = null;

    /**
     * Default constructor
     * @param userAgent user agent
     */
    public AbstractRendererConfigurator(FOUserAgent userAgent) {
        super();
        this.userAgent = userAgent;
    }

    /**
     * Returns the configuration subtree for a specific renderer.
     * @param renderer the renderer
     * @return the requested configuration subtree, null if there's no configuration
     */
    protected Configuration getRendererConfig(Renderer renderer) {
        String mimeType = renderer.getMimeType();
        if (mimeType == null) {
            if (log.isInfoEnabled()) {
                log.info("renderer mimeType is null");
            }
            return null;
        }

        return getRendererConfig(mimeType);
    }

    /**
     * Returns the configuration subtree for a specific renderer.
     * @param mimeType the MIME type of the renderer
     * @return the requested configuration subtree, null if there's no configuration
     */
    private Configuration getRendererConfig(String mimeType) {
        Configuration cfg = userAgent.getFactory().getUserConfig();
        if (cfg == null) {
            if (log.isDebugEnabled()) {
                log.debug("userconfig is null");
            }
            return null;
        }

        Configuration userRendererConfig = null;

        Configuration[] cfgs
            = cfg.getChild("renderers").getChildren("renderer");
        for (int i = 0; i < cfgs.length; ++i) {
            Configuration child = cfgs[i];
            try {
                if (child.getAttribute("mime").equals(mimeType)) {
                    userRendererConfig = child;
                    break;
                }
            } catch (ConfigurationException e) {
                // silently pass over configurations without mime type
            }
        }
        log.debug((userRendererConfig == null ? "No u" : "U")
                  + "ser configuration found for MIME type " + mimeType);
        return userRendererConfig;
    }
}
