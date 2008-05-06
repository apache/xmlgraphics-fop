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

package org.apache.fop.fonts;

import java.net.MalformedURLException;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fonts.substitute.FontSubstitutions;
import org.apache.fop.fonts.substitute.FontSubstitutionsConfigurator;
import org.apache.fop.util.LogUtil;

/**
 * Configurator of the FontManager
 */
public class FontManagerConfigurator {
    
    /** logger instance */
    private static Log log = LogFactory.getLog(FontManagerConfigurator.class);

    private Configuration cfg;
    
    /**
     * Main constructor
     * @param cfg the font manager configuration object
     */
    public FontManagerConfigurator(Configuration cfg) {
        this.cfg = cfg;
    }

    /**
     * Initializes font settings from the user configuration
     * @param fontManager a font manager
     * @throws FOPException fop exception
     */
    public void configure(FontManager fontManager) throws FOPException {
        // caching (fonts)
        if (cfg.getChild("use-cache", false) != null) {
            try {
                fontManager.setUseCache(
                        cfg.getChild("use-cache").getValueAsBoolean());
            } catch (ConfigurationException mfue) {
                LogUtil.handleException(log, mfue, true);
            }
        }
        if (cfg.getChild("font-base", false) != null) {
            try {
                fontManager.setFontBaseURL(
                        cfg.getChild("font-base").getValue(null));
            } catch (MalformedURLException mfue) {
                LogUtil.handleException(log, mfue, true);
            }
        }

        // global font configuration
        Configuration fontsCfg = cfg.getChild("fonts", false);
        if (fontsCfg != null) {
            // font substitution
            Configuration substitutionsCfg = fontsCfg.getChild("substitutions", false);
            if (substitutionsCfg != null) {
                FontSubstitutionsConfigurator fontSubstitutionsConfigurator
                    = new FontSubstitutionsConfigurator(substitutionsCfg);
                FontSubstitutions substitutions = new FontSubstitutions();
                fontSubstitutionsConfigurator.configure(substitutions);
                fontManager.setFontSubstitutions(substitutions);
            }
        }
    }    
}
