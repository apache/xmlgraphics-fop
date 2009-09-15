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
 * An abstract configurator
 */
public abstract class AbstractConfigurator {
    /** logger instance */
    protected static Log log = LogFactory.getLog(AbstractConfigurator.class);

    private static final String MIME = "mime";
    
    /** fop factory configuration */
    protected FOUserAgent userAgent = null;

    /**
     * Default constructor
     * @param userAgent user agent
     */
    public AbstractConfigurator(FOUserAgent userAgent) {
        super();
        this.userAgent = userAgent;
    }

    /**
     * Returns the configuration subtree for a specific renderer.
     * @param mimeType the MIME type of the renderer
     * @return the requested configuration subtree, null if there's no configuration
     */
    protected Configuration getConfig(String mimeType) {
        Configuration cfg = userAgent.getFactory().getUserConfig();
        if (cfg == null) {
            if (log.isDebugEnabled()) {
                log.debug("userconfig is null");
            }
            return null;
        }

        Configuration userConfig = null;

        String type = getType();
        Configuration[] cfgs
            = cfg.getChild(type + "s").getChildren(type);
        for (int i = 0; i < cfgs.length; ++i) {
            Configuration child = cfgs[i];
            try {
                if (child.getAttribute(MIME).equals(mimeType)) {
                    userConfig = child;
                    break;
                }
            } catch (ConfigurationException e) {
                // silently pass over configurations without mime type
            }
        }
        log.debug((userConfig == null ? "No u" : "U")
                  + "ser configuration found for MIME type " + mimeType);
        return userConfig;
    }

    /**
     * Returns the configurator type
     * @return the configurator type
     */
    public abstract String getType();
}
