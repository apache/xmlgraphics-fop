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
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configuration;

import org.apache.xmlgraphics.image.loader.ImageManager;

import org.apache.fop.apps.io.ResourceResolver;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.layoutmgr.LayoutManagerMaker;

/**
 * The configuration data for a {@link FopFactory} instance.
 */
public interface FopFactoryConfig {

    /** Defines if FOP should use an alternative rule to determine text indents */
    boolean DEFAULT_BREAK_INDENT_INHERITANCE = false;

    /** Defines if FOP should validate the user config strictly */
    boolean DEFAULT_STRICT_USERCONFIG_VALIDATION = true;

    /** Defines if FOP should use strict validation for FO and user config */
    boolean DEFAULT_STRICT_FO_VALIDATION = true;

    /** Defines the default page-width */
    String DEFAULT_PAGE_WIDTH = "8.26in";

    /** Defines the default page-height */
    String DEFAULT_PAGE_HEIGHT = "11in";

    /** Defines the default source resolution (72dpi) for FOP */
    float DEFAULT_SOURCE_RESOLUTION = 72.0f; //dpi

    /** Defines the default target resolution (72dpi) for FOP */
    float DEFAULT_TARGET_RESOLUTION = 72.0f; //dpi

    /**
     * Whether accessibility features are switched on.
     *
     * @return true if accessibility features have been requested
     */
    boolean isAccessibilityEnabled();

    /** @see {@link FopFactory#getLayoutManagerMakerOverride()} */
    LayoutManagerMaker getLayoutManagerMakerOverride();

    /**
     * The URI resolver used through-out FOP for controlling all file access.
     *
     * @return the URI resolver
     */
    ResourceResolver getNewURIResolver();

    /**
     * The base URI from which URIs are resolved against.
     *
     * @return the base URI
     */
    URI getBaseURI();

    /** @see {@link FopFactory#validateStrictly()} */
    boolean validateStrictly();

    /** @see {@link FopFactory#validateUserConfigStrictly()} */
    boolean validateUserConfigStrictly();

    /** @see {@link FopFactory#isBreakIndentInheritanceOnReferenceAreaBoundary()} */
    boolean isBreakIndentInheritanceOnReferenceAreaBoundary();

    /** @see {@link FopFactory#getSourceResolution()} */
    float getSourceResolution();

    /** @see {@link FopFactory#getTargetResolution()} */
    float getTargetResolution();

    /** @see {@link FopFactory#getPageHeight()} */
    String getPageHeight();

    /** @see {@link FopFactory#getPageWidth()} */
    String getPageWidth();

    /** @see {@link FopFactory#getIgnoredNamespace()} */
    Set<String> getIgnoredNamespaces();

    /** @see {@link FopFactory#isNamespaceIgnored(String)} */
    boolean isNamespaceIgnored(String namespace);

    /**
     * Returns the Avalon {@link Configuration} object.
     *
     * @return the Avalon config object
     */
    Configuration getUserConfig();

    /** @see {@link org.apache.fop.render.RendererFactory#isRendererPreferred()} */
    boolean preferRenderer();

    /** @see {@link FopFactory#getFontManager()} */
    FontManager getFontManager();

    /** @see {@link FopFactory#getImageManager()} */
    ImageManager getImageManager();

    boolean isComplexScriptFeaturesEnabled();

    Map<String, String> getHyphPatNames();
}
