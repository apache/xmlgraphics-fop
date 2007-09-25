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

/* $Id: $ */

package org.apache.fop.render;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;

/**
 * Configurator for XMLHandler objects. 
 */
public class XMLHandlerConfigurator extends AbstractRendererConfigurator {

    /** logger instance */
    protected static Log log = LogFactory.getLog(XMLHandlerConfigurator.class);
    
    /**
     * Default constructor
     * @param userAgent the user agent
     */
    public XMLHandlerConfigurator(FOUserAgent userAgent) {
        super(userAgent);
    }

    /**
     * Returns the configuration subtree for a specific renderer.
     * @param cfg the renderer configuration
     * @param namespace the namespace (i.e. the XMLHandler) for which the configuration should
     *                  be returned
     * @return the requested configuration subtree, null if there's no configuration
     */
    private Configuration getHandlerConfig(Configuration cfg, String namespace) {
        if (cfg == null || namespace == null) {
            return null;
        }
        Configuration handlerConfig = null;

        Configuration[] children = cfg.getChildren("xml-handler");
        for (int i = 0; i < children.length; ++i) {
            try {
                if (children[i].getAttribute("namespace").equals(namespace)) {
                    handlerConfig = children[i];
                    break;
                }
            } catch (ConfigurationException e) {
                // silently pass over configurations without namespace
            }
        }
        if (log.isDebugEnabled()) {
            log.debug((handlerConfig == null ? "No" : "")
                    + "XML handler configuration found for namespace " + namespace);
        }
        return handlerConfig;
    }

    /**
     * Configures renderer context by setting the handler configuration on it.
     * @param context the RendererContext (contains the user agent)
     * @param ns the Namespace of the foreign object
     * @throws FOPException if configuring the target objects fails
     */
    public void configure(RendererContext context, String ns) throws FOPException {
        //Optional XML handler configuration
        Configuration cfg = getRendererConfig(context.getRenderer());
        if (cfg != null) {
            cfg = getHandlerConfig(cfg, ns);
            if (cfg != null) {
                context.setProperty(RendererContextConstants.HANDLER_CONFIGURATION, cfg);
            }
        }
    }
}
