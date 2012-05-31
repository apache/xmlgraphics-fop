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

import java.net.URI;

import org.apache.fop.apps.io.ResourceResolver;
import org.apache.fop.apps.io.URIResolverWrapper;
import org.apache.fop.fonts.FontCacheManager;
import org.apache.fop.fonts.FontCacheManagerFactory;
import org.apache.fop.fonts.FontDetector;
import org.apache.fop.fonts.FontDetectorFactory;
import org.apache.fop.fonts.FontManager;

/**
 * Creates an {@link EnvironmentProfile} that sets the environment in which a FOP instance is run.
 */
public final class EnvironmentalProfileFactory {

    private EnvironmentalProfileFactory() {
    };

    /**
     * Creates the default environment that FOP is invoked in. This default profile has no
     * operational restrictions for FOP.
     *
     * @param defaultBaseUri the default base URI for resolving resource URIs
     * @param resourceResolver the resource resolver
     * @return the environment profile
     */
    public static EnvironmentProfile createDefault(URI defaultBaseUri,
            ResourceResolver resourceResolver) {
        return new Profile(defaultBaseUri, resourceResolver,
                createFontManager(defaultBaseUri, resourceResolver,
                        FontDetectorFactory.createDefault(),
                        FontCacheManagerFactory.createDefault()));
    }

    /**
     * Creates an IO-restricted environment for FOP by disabling some of the environment-specific
     * functionality within FOP.
     *
     * @param defaultBaseUri the default base URI for resolving resource URIs
     * @param resourceResolver the resource resolver
     * @return  the environment profile
     */
    public static EnvironmentProfile createRestrictedIO(URI defaultBaseUri,
            ResourceResolver resourceResolver) {
        return new Profile(defaultBaseUri, resourceResolver,
                createFontManager(defaultBaseUri, resourceResolver,
                        FontDetectorFactory.createDisabled(),
                        FontCacheManagerFactory.createDisabled()));
    }

    private static final class Profile implements EnvironmentProfile {

        private final ResourceResolver resourceResolver;

        private final FontManager fontManager;

        private final URI defaultBaseURI;

        private Profile(URI defaultBaseURI, ResourceResolver resourceResolver,
                FontManager fontManager) {
            if (defaultBaseURI == null) {
                throw new IllegalArgumentException("Default base URI must not be null");
            }
            if (resourceResolver == null) {
                throw new IllegalArgumentException("URI Resolver must not be null");
            }
            this.defaultBaseURI = defaultBaseURI;
            this.resourceResolver = resourceResolver;
            this.fontManager = fontManager;
        }

        public ResourceResolver getResourceResolver() {
            return resourceResolver;
        }

        public FontManager getFontManager() {
            return fontManager;
        }

        public URI getDefaultBaseURI() {
            return defaultBaseURI;
        }
    }

    private static FontManager createFontManager(URI defaultBaseUri, ResourceResolver resourceResolver,
            FontDetector fontDetector, FontCacheManager fontCacheManager) {
        return new FontManager(new URIResolverWrapper(defaultBaseUri, resourceResolver), fontDetector,
                fontCacheManager);
    }
}
