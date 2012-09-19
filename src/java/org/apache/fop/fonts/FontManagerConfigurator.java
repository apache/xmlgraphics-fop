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

package org.apache.fop.fonts;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.fonts.substitute.FontSubstitutions;
import org.apache.fop.fonts.substitute.FontSubstitutionsConfigurator;
import org.apache.fop.util.LogUtil;

/**
 * Configurator of the FontManager
 */
public class FontManagerConfigurator {

    /** logger instance */
    private static Log log = LogFactory.getLog(FontManagerConfigurator.class);

    private final Configuration cfg;

    private final URI defaultBaseUri;

    private final ResourceResolver resourceResolver;

    /**
     * Main constructor
     * @param cfg the font manager configuration object
     * @param defaultBaseUri the default URI base to use for URI resolution
     * @param resourceResolver the resource resolver
     */
    public FontManagerConfigurator(Configuration cfg, URI defaultBaseUri,
            ResourceResolver resourceResolver) {
        this.cfg = cfg;
        this.defaultBaseUri = defaultBaseUri;
        this.resourceResolver = resourceResolver;
    }

    /**
     * Initializes font settings from the user configuration
     * @param fontManager a font manager
     * @param strict true if strict checking of the configuration is enabled
     * @throws FOPException if an exception occurs while processing the configuration
     */
    public void configure(FontManager fontManager, boolean strict) throws FOPException {
        if (cfg.getChild("font-base", false) != null) {
            try {
                URI fontBase = InternalResourceResolver.getBaseURI(cfg.getChild("font-base")
                                                                      .getValue(null));
                fontManager.setResourceResolver(ResourceResolverFactory.createInternalResourceResolver(
                        defaultBaseUri.resolve(fontBase), resourceResolver));
            } catch (URISyntaxException use) {
                LogUtil.handleException(log, use, true);
            }
        } else {
            fontManager.setResourceResolver(ResourceResolverFactory.createInternalResourceResolver(
                    defaultBaseUri, resourceResolver));
        }
        // caching (fonts)
        if (cfg.getChild("use-cache", false) != null) {
            try {
                if (!cfg.getChild("use-cache").getValueAsBoolean()) {
                    fontManager.disableFontCache();
                } else {
                    if (cfg.getChild("cache-file", false) != null) {

                        fontManager.setCacheFile(URI.create(cfg.getChild("cache-file").getValue()));
                    }
                }
            } catch (ConfigurationException mfue) {
                LogUtil.handleException(log, mfue, true);
            }
        }
        // [GA] permit configuration control over base14 kerning; without this,
        // there is no way for a user to enable base14 kerning other than by
        // programmatic API;
        if (cfg.getChild("base14-kerning", false) != null) {
            try {
                fontManager
                    .setBase14KerningEnabled(cfg.getChild("base14-kerning").getValueAsBoolean());
            } catch (ConfigurationException e) {
                LogUtil.handleException(log, e, true);
            }
        }

        // global font configuration
        Configuration fontsCfg = cfg.getChild("fonts", false);
        if (fontsCfg != null) {
            // font substitution
            Configuration substitutionsCfg = fontsCfg.getChild("substitutions", false);
            if (substitutionsCfg != null) {
                FontSubstitutions substitutions = new FontSubstitutions();
                new FontSubstitutionsConfigurator(substitutionsCfg).configure(substitutions);
                fontManager.setFontSubstitutions(substitutions);
            }
            // referenced fonts (fonts which are not to be embedded)
            Configuration referencedFontsCfg = fontsCfg.getChild("referenced-fonts", false);
            if (referencedFontsCfg != null) {
                FontTriplet.Matcher matcher = createFontsMatcher(
                        referencedFontsCfg, strict);
                fontManager.setReferencedFontsMatcher(matcher);
            }
        }
    }

    /**
     * Creates a font triplet matcher from a configuration object.
     * @param cfg the configuration object
     * @param strict true for strict configuraton error handling
     * @return the font matcher
     * @throws FOPException if an error occurs while building the matcher
     */
    public static FontTriplet.Matcher createFontsMatcher(
            Configuration cfg, boolean strict) throws FOPException {
        List<FontTriplet.Matcher> matcherList = new java.util.ArrayList<FontTriplet.Matcher>();
        Configuration[] matches = cfg.getChildren("match");
        for (int i = 0; i < matches.length; i++) {
            try {
                matcherList.add(new FontFamilyRegExFontTripletMatcher(
                        matches[i].getAttribute("font-family")));
            } catch (ConfigurationException ce) {
                LogUtil.handleException(log, ce, strict);
                continue;
            }
        }
        FontTriplet.Matcher orMatcher = new OrFontTripletMatcher(
                matcherList.toArray(new FontTriplet.Matcher[matcherList.size()]));
        return orMatcher;
    }

    /**
     * Creates a font triplet matcher from a configuration object.
     * @param fontFamilies the list of font families
     * @param strict true for strict configuraton error handling
     * @return the font matcher
     * @throws FOPException if an error occurs while building the matcher
     */
    public static FontTriplet.Matcher createFontsMatcher(
            List<String> fontFamilies, boolean strict) throws FOPException {
        List<FontTriplet.Matcher> matcherList = new java.util.ArrayList<FontTriplet.Matcher>();
        for (String fontFamily : fontFamilies) {
                matcherList.add(new FontFamilyRegExFontTripletMatcher(fontFamily));
        }
        FontTriplet.Matcher orMatcher = new OrFontTripletMatcher(
                matcherList.toArray(new FontTriplet.Matcher[matcherList.size()]));
        return orMatcher;
    }

    private static class OrFontTripletMatcher implements FontTriplet.Matcher {

        private final FontTriplet.Matcher[] matchers;

        public OrFontTripletMatcher(FontTriplet.Matcher[] matchers) {
            this.matchers = matchers;
        }

        /** {@inheritDoc} */
        public boolean matches(FontTriplet triplet) {
            for (int i = 0, c = matchers.length; i < c; i++) {
                if (matchers[i].matches(triplet)) {
                    return true;
                }
            }
            return false;
        }

    }

    private static class FontFamilyRegExFontTripletMatcher implements FontTriplet.Matcher {

        private final Pattern regex;

        public FontFamilyRegExFontTripletMatcher(String regex) {
            this.regex = Pattern.compile(regex);
        }

        /** {@inheritDoc} */
        public boolean matches(FontTriplet triplet) {
            return regex.matcher(triplet.getName()).matches();
        }

    }

}
