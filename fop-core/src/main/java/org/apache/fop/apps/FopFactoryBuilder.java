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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xml.sax.SAXException;

import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.impl.AbstractImageSessionContext.FallbackResolver;
import org.apache.xmlgraphics.io.ResourceResolver;

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.layoutmgr.LayoutManagerMaker;

/**
 * This is the builder class for {@link FopFactory}. Setters can be chained to
 * make building a {@link FopFactory} object more concise and intuitive e.g.
 *
 * <pre>
 * {@code
 * FopFactoryBuilder fopFactoryBuilder = new FopFactoryBuilder(<URI>)
 *                                                  .setURIResolver(<URIResolver>)
 *                                                  .setPageHeight(<String>)
 *                                                  .setPageWidth(<String>)
 *                                                  .setStrictUserConfigValidation(<boolean>)
 *                                                    ... etc ...
 * FopFactory fopFactory = fopFactoryBuilder.build();
 * }
 * </pre>
 */
public final class FopFactoryBuilder {

    private final FopFactoryConfig config;

    private FopFactoryConfigBuilder fopFactoryConfigBuilder;

    /**
     * A builder class for {@link FopFactory} which can be used for setting configuration. This is
     * a helper constructor that uses the default URI resolver implementation that FOP packages
     * provide.
     *
     * @param defaultBaseURI the default base URI for resolving URIs against
     */
    public FopFactoryBuilder(URI defaultBaseURI) {
        this(defaultBaseURI, ResourceResolverFactory.createDefaultResourceResolver());
    }

    /**
     * A builder class for {@link FopFactory} which can be used for setting configuration.
     *
     * @param defaultBaseURI the default base URI for resolving URIs against
     * @param resourceResolver the URI resolver
     */
    public FopFactoryBuilder(URI defaultBaseURI, ResourceResolver resourceResolver) {
        this(EnvironmentalProfileFactory.createDefault(defaultBaseURI, resourceResolver));
    }

    /**
     * A builder class for {@link FopFactory} which can be used for setting configuration.
     *
     * @param enviro the profile of the FOP deployment environment
     */
    public FopFactoryBuilder(EnvironmentProfile enviro) {
        config = new FopFactoryConfigImpl(enviro);
        fopFactoryConfigBuilder = new ActiveFopFactoryConfigBuilder((FopFactoryConfigImpl) config);
    }

    /**
     * Returns the {@link FopFactoryConfig} which is needed to get an instance of
     * {@link FopFactory}.
     *
     * @return build the {@link FopFactoryConfig}
     * @deprecated Exposing the {@link FopFactoryConfig} is only to maintain backwards compatibility
     */
    public FopFactoryConfig buildConfig() {
        return buildConfiguration();
    }

    /**
     * Builds the configuration object used by the FopFactory.
     *
     * @return the config for the {@link FopFactory}
     */
    // The {@link FopFactoryConfig} doesn't need to be exposed in the "public" API, this method
    // should remain package private.
    FopFactoryConfig buildConfiguration() {
        fopFactoryConfigBuilder = CompletedFopFactoryConfigBuilder.INSTANCE;
        return config;
    }

    /**
     * Builds an instance of the the {@link FopFactory}.
     *
     * @return the FopFactory instance
     */
    public FopFactory build() {
        return FopFactory.newInstance(buildConfiguration());
    }

    /**
     * Gets the base URI used to resolve all URIs within FOP.
     *
     * @return the base URI
     */
    URI getBaseURI() {
        return config.getBaseURI();
    }

    /**
     * Returns the {@link FontManager} used for managing the fonts within FOP.
     *
     * @return the font managing object
     */
    public FontManager getFontManager() {
        return config.getFontManager();
    }

    /**
     * Return the {@link ImageManager} used for handling images through out FOP.
     *
     * @return the image manager
     */
    public ImageManager getImageManager() {
        return config.getImageManager();
    }

    /**
     * Sets whether to include accessibility features in document creation.
     *
     * @param enableAccessibility true to set accessibility on
     * @return <code>this</code>
     */
    public FopFactoryBuilder setAccessibility(boolean enableAccessibility) {
        fopFactoryConfigBuilder.setAccessibility(enableAccessibility);
        return this;
    }

    public FopFactoryBuilder setStaticRegionsPerPageForAccessibility(boolean staticRegionsPerPageForAccessibility) {
        fopFactoryConfigBuilder.setStaticRegionsPerPageForAccessibility(staticRegionsPerPageForAccessibility);
        return this;
    }

    public FopFactoryBuilder setKeepEmptyTags(boolean b) {
        fopFactoryConfigBuilder.setKeepEmptyTags(b);
        return this;
    }

    /**
     * Sets the {@link LayoutManagerMaker} so that users can configure how FOP creates
     * {@link org.apache.fop.layoutmgr.LayoutManager}s.
     *
     * @param lmMaker he layout manager maker
     * @return <code>this</code>
     */
    public FopFactoryBuilder setLayoutManagerMakerOverride(LayoutManagerMaker lmMaker) {
        fopFactoryConfigBuilder.setLayoutManagerMakerOverride(lmMaker);
        return this;
    }

    /**
     * Sets the base URI, this will be used for resolving all URIs given to FOP.
     *
     * @param baseURI the base URI
     * @return <code>this</code>
     */
    public FopFactoryBuilder setBaseURI(URI baseURI) {
        fopFactoryConfigBuilder.setBaseURI(baseURI);
        return this;
    }

    public FopFactoryBuilder setHyphenBaseResourceResolver(InternalResourceResolver hyphenationResourceResolver) {
        fopFactoryConfigBuilder.setHyphenationResourceResolver(hyphenationResourceResolver);
        return this;
    }

    /**
     * Sets whether to perform strict validation on the FO used.
     *
     * @param validateStrictly true if the FO is to be strictly validated
     * @return <code>this</code>
     */
    public FopFactoryBuilder setStrictFOValidation(boolean validateStrictly) {
        fopFactoryConfigBuilder.setStrictFOValidation(validateStrictly);
        return this;
    }

    /**
     * Sets whether to perform strict alidation on the user-configuration.
     *
     * @param validateStrictly true if the fop conf is to be strictly validated
     * @return <code>this</code>
     */
    public FopFactoryBuilder setStrictUserConfigValidation(
            boolean validateStrictly) {
        fopFactoryConfigBuilder.setStrictUserConfigValidation(validateStrictly);
        return this;
    }

    /**
     * Sets whether the indent inheritance should be broken when crossing reference area boundaries.
     *
     * @param value true to break inheritance when crossing reference area boundaries
     * @return <code>this</code>
     */
    public FopFactoryBuilder setBreakIndentInheritanceOnReferenceAreaBoundary(
            boolean value) {
        fopFactoryConfigBuilder.setBreakIndentInheritanceOnReferenceAreaBoundary(value);
        return this;
    }

    /**
     * Sets the resolution of resolution-dependent input.
     *
     * @param dpi the source resolution
     * @return <code>this</code>
     */
    public FopFactoryBuilder setSourceResolution(float dpi) {
        fopFactoryConfigBuilder.setSourceResolution(dpi);
        return this;
    }

    /**
     * Sets the resolution of resolution-dependent output.
     *
     * @param dpi the target resolution
     * @return <code>this</code>
     */
    public FopFactoryBuilder setTargetResolution(float dpi) {
        fopFactoryConfigBuilder.setTargetResolution(dpi);
        return this;
    }

    /**
     * Sets the page height of the paginated output.
     *
     * @param pageHeight the page height
     * @return <code>this</code>
     */
    public FopFactoryBuilder setPageHeight(String pageHeight) {
        fopFactoryConfigBuilder.setPageHeight(pageHeight);
        return this;
    }

    /**
     * Sets the page width of the paginated output.
     *
     * @param pageWidth the page width
     * @return <code>this</code>
     */
    public FopFactoryBuilder setPageWidth(String pageWidth) {
        fopFactoryConfigBuilder.setPageWidth(pageWidth);
        return this;
    }

    /**
     * FOP will ignore the specified XML element namespace.
     *
     * @param namespaceURI the namespace URI to ignore
     * @return <code>this</code>
     */
    public FopFactoryBuilder ignoreNamespace(String namespaceURI) {
        fopFactoryConfigBuilder.ignoreNamespace(namespaceURI);
        return this;
    }

    /**
     * FOP will ignore the colletion of XML element namespaces.
     *
     * @param namespaceURIs a collection of namespace URIs to ignore
     * @return <code>this</code>
     */
    public FopFactoryBuilder ignoreNamespaces(Collection<String> namespaceURIs) {
        fopFactoryConfigBuilder.ignoreNamespaces(namespaceURIs);
        return this;
    }

    /**
     * Sets the Avalon configuration if a FOP conf is used.
     *
     * @param cfg the fop conf configuration
     * @return <code>this</code>
     */
    public FopFactoryBuilder setConfiguration(Configuration cfg) {
        return setConfiguration(cfg, true);
    }

    protected FopFactoryBuilder setConfiguration(Configuration cfg, boolean parse) {
        fopFactoryConfigBuilder.setConfiguration(cfg);
        if (parse) {
            try {
                new FopConfParser(cfg, this);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    /**
     * Sets whether to chose a {@link org.apache.fop.render.Renderer} in preference to an
     * {@link org.apache.fop.render.intermediate.IFDocumentHandler}.
     *
     * @param preferRenderer true to prefer {@link org.apache.fop.render.Renderer}
     * @return <code>this</code>
     */
    public FopFactoryBuilder setPreferRenderer(boolean preferRenderer) {
        fopFactoryConfigBuilder.setPreferRenderer(preferRenderer);
        return this;
    }

    public FopFactoryBuilder setComplexScriptFeatures(boolean csf) {
        fopFactoryConfigBuilder.setComplexScriptFeaturesEnabled(csf);
        return this;
    }

    public FopFactoryBuilder setHyphPatNames(Map<String, String> hyphPatNames) {
        fopFactoryConfigBuilder.setHyphPatNames(hyphPatNames);
        return this;
    }

    public FopFactoryBuilder setTableBorderOverpaint(boolean b) {
        fopFactoryConfigBuilder.setTableBorderOverpaint(b);
        return this;
    }

    public FopFactoryBuilder setSimpleLineBreaking(boolean b) {
        fopFactoryConfigBuilder.setSimpleLineBreaking(b);
        return this;
    }

    public FopFactoryBuilder setSkipPagePositionOnlyAllowed(boolean b) {
        fopFactoryConfigBuilder.setSkipPagePositionOnlyAllowed(b);
        return this;
    }

    public FopFactoryBuilder setLegacySkipPagePositionOnly(boolean b) {
        fopFactoryConfigBuilder.setLegacySkipPagePositionOnly(b);
        return this;
    }

    public FopFactoryBuilder setLegacyLastPageChangeIPD(boolean b) {
        fopFactoryConfigBuilder.setLegacyLastPageChangeIPD(b);
        return this;
    }

    public FopFactoryBuilder setLegacyFoWrapper(boolean b) {
        fopFactoryConfigBuilder.setLegacyFoWrapper(b);
        return this;
    }

    public FopFactoryBuilder setLegacyInvalidBreakPosition(boolean value) {
        fopFactoryConfigBuilder.setLegacyInvalidBreakPosition(value);
        return this;
    }

    public static class FopFactoryConfigImpl implements FopFactoryConfig {

        private final EnvironmentProfile enviro;

        private final ImageManager imageManager;

        private boolean accessibility;

        private boolean staticRegionsPerPageForAccessibility;

        private boolean keepEmptyTags = true;

        private LayoutManagerMaker layoutManagerMaker;

        private URI baseURI;

        private InternalResourceResolver hyphenationResourceResolver;

        private boolean hasStrictFOValidation = true;

        private boolean hasStrictUserValidation = FopFactoryConfig.DEFAULT_STRICT_USERCONFIG_VALIDATION;

        private boolean breakIndentInheritanceOnReferenceBoundary
        = FopFactoryConfig.DEFAULT_BREAK_INDENT_INHERITANCE;

        private float sourceResolution = FopFactoryConfig.DEFAULT_SOURCE_RESOLUTION;

        private float targetResolution = FopFactoryConfig.DEFAULT_TARGET_RESOLUTION;

        private String pageHeight = FopFactoryConfig.DEFAULT_PAGE_HEIGHT;

        private String pageWidth = FopFactoryConfig.DEFAULT_PAGE_WIDTH;

        private Set<String> ignoredNamespaces = new HashSet<String>();

        private Configuration cfg;

        private boolean preferRenderer;

        private boolean isComplexScript = true;

        private Map<String, String> hyphPatNames;

        private boolean tableBorderOverpaint;
        private boolean simpleLineBreaking;

        private boolean skipPagePositionOnlyAllowed = true;

        private boolean legacySkipPagePositionOnly;

        private boolean legacyLastPageChangeIPD;

        private boolean legacyFoWrapper;

        private boolean legacyInvalidBreakPosition = FopFactoryConfig.DEFAULT_LEGACY_INVALID_BREAK_POSITION;

        private static final class ImageContextImpl implements ImageContext {

            private final FopFactoryConfig config;

            ImageContextImpl(FopFactoryConfig config) {
                this.config = config;
            }

            public float getSourceResolution() {
                return config.getSourceResolution();
            }
        }

        FopFactoryConfigImpl(EnvironmentProfile enviro) {
            this.enviro = enviro;
            this.baseURI = enviro.getDefaultBaseURI();
            this.imageManager = new ImageManager(new ImageContextImpl(this));
        }

        /** {@inheritDoc} */
        public boolean isAccessibilityEnabled() {
            return accessibility;
        }

        public boolean isStaticRegionsPerPageForAccessibility() {
            return staticRegionsPerPageForAccessibility;
        }

        public boolean isKeepEmptyTags() {
            return keepEmptyTags;
        }

        /** {@inheritDoc} */
        public LayoutManagerMaker getLayoutManagerMakerOverride() {
            return layoutManagerMaker;
        }

        /** {@inheritDoc} */
        public ResourceResolver getResourceResolver() {
            return enviro.getResourceResolver();
        }

        /** {@inheritDoc} */
        public URI getBaseURI() {
            return baseURI;
        }

        public InternalResourceResolver getHyphenationResourceResolver() {
            return hyphenationResourceResolver;
        }

        /** {@inheritDoc} */
        public boolean validateStrictly() {
            return hasStrictFOValidation;
        }

        /** {@inheritDoc} */
        public boolean validateUserConfigStrictly() {
            return hasStrictUserValidation;
        }

        /** {@inheritDoc} */
        public boolean isBreakIndentInheritanceOnReferenceAreaBoundary() {
            return breakIndentInheritanceOnReferenceBoundary;
        }

        /** {@inheritDoc} */
        public float getSourceResolution() {
            return sourceResolution;
        }

        /** {@inheritDoc} */
        public float getTargetResolution() {
            return targetResolution;
        }

        /** {@inheritDoc} */
        public String getPageHeight() {
            return pageHeight;
        }

        /** {@inheritDoc} */
        public String getPageWidth() {
            return pageWidth;
        }

        /** {@inheritDoc} */
        public Set<String> getIgnoredNamespaces() {
            return Collections.unmodifiableSet(ignoredNamespaces);
        }

        /** {@inheritDoc} */
        public boolean isNamespaceIgnored(String namespace) {
            return ignoredNamespaces.contains(namespace);
        }

        /** {@inheritDoc} */
        public Configuration getUserConfig() {
            return cfg;
        }

        /** {@inheritDoc} */
        public boolean preferRenderer() {
            return preferRenderer;
        }

        /** {@inheritDoc} */
        public FontManager getFontManager() {
            return enviro.getFontManager();
        }

        /** {@inheritDoc} */
        public ImageManager getImageManager() {
            return imageManager;
        }

        public boolean isComplexScriptFeaturesEnabled() {
            return isComplexScript;
        }

        public boolean isTableBorderOverpaint() {
            return tableBorderOverpaint;
        }

        public boolean isSimpleLineBreaking() {
            return simpleLineBreaking;
        }

        public boolean isSkipPagePositionOnlyAllowed() {
            return skipPagePositionOnlyAllowed;
        }

        public boolean isLegacySkipPagePositionOnly() {
            return legacySkipPagePositionOnly;
        }

        public boolean isLegacyLastPageChangeIPD() {
            return legacyLastPageChangeIPD;
        }

        public boolean isLegacyFoWrapper() {
            return legacyFoWrapper;
        }

        public boolean isLegacyInvalidBreakPosition() {
            return legacyInvalidBreakPosition;
        }

        public Map<String, String> getHyphenationPatternNames() {
            return hyphPatNames;
        }

        public FallbackResolver getFallbackResolver() {
            return enviro.getFallbackResolver();
        }
    }

    private interface FopFactoryConfigBuilder {

        void setAccessibility(boolean enableAccessibility);

        void setStaticRegionsPerPageForAccessibility(boolean staticRegionsPerPageForAccessibility);

        void setKeepEmptyTags(boolean b);

        void setLayoutManagerMakerOverride(LayoutManagerMaker lmMaker);

        void setBaseURI(URI baseURI);

        void setHyphenationResourceResolver(InternalResourceResolver hyphenationResourceResolver);

        void setStrictFOValidation(boolean validateStrictly);

        void setStrictUserConfigValidation(boolean validateStrictly);

        void setLegacyInvalidBreakPosition(boolean invalidBreakPosition);

        void setBreakIndentInheritanceOnReferenceAreaBoundary(boolean value);

        void setSourceResolution(float dpi);

        void setTargetResolution(float dpi);

        void setPageHeight(String pageHeight);

        void setPageWidth(String pageWidth);

        void ignoreNamespace(String namespaceURI);

        void ignoreNamespaces(Collection<String> namespaceURIs);

        void setConfiguration(Configuration cfg);

        void setPreferRenderer(boolean preferRenderer);

        void setComplexScriptFeaturesEnabled(boolean csf);

        void setHyphPatNames(Map<String, String> hyphPatNames);

        void setTableBorderOverpaint(boolean b);

        void setSimpleLineBreaking(boolean b);

        void setSkipPagePositionOnlyAllowed(boolean b);

        void setLegacySkipPagePositionOnly(boolean b);

        void setLegacyLastPageChangeIPD(boolean b);

        void setLegacyFoWrapper(boolean b);
    }

    private static final class CompletedFopFactoryConfigBuilder implements FopFactoryConfigBuilder {

        private static final CompletedFopFactoryConfigBuilder INSTANCE
        = new CompletedFopFactoryConfigBuilder();

        private void throwIllegalStateException() {
            throw new IllegalStateException("The final FOP Factory configuration has already been built");
        }

        public void setAccessibility(boolean enableAccessibility) {
            throwIllegalStateException();
        }

        public void setStaticRegionsPerPageForAccessibility(boolean staticRegionsPerPageForAccessibility) {
            throwIllegalStateException();
        }

        public void setKeepEmptyTags(boolean b) {
            throwIllegalStateException();
        }

        public void setLayoutManagerMakerOverride(LayoutManagerMaker lmMaker) {
            throwIllegalStateException();

        }

        public void setBaseURI(URI baseURI) {
            throwIllegalStateException();
        }

        public void setHyphenationResourceResolver(InternalResourceResolver hyphenationResourceResolver) {
            throwIllegalStateException();
        }

        public void setStrictFOValidation(boolean validateStrictly) {
            throwIllegalStateException();
        }

        public void setStrictUserConfigValidation(boolean validateStrictly) {
            throwIllegalStateException();
        }

        public void setLegacyInvalidBreakPosition(boolean ignoreInvalidBreakPosition) {
            throwIllegalStateException();
        }

        public void setBreakIndentInheritanceOnReferenceAreaBoundary(
                boolean value) {
            throwIllegalStateException();
        }

        public void setSourceResolution(float dpi) {
            throwIllegalStateException();
        }

        public void setTargetResolution(float dpi) {
            throwIllegalStateException();
        }

        public void setPageHeight(String pageHeight) {
            throwIllegalStateException();
        }

        public void setPageWidth(String pageWidth) {
            throwIllegalStateException();
        }

        public void ignoreNamespace(String namespaceURI) {
            throwIllegalStateException();
        }

        public void ignoreNamespaces(Collection<String> namespaceURIs) {
            throwIllegalStateException();
        }

        public void setConfiguration(Configuration cfg) {
            throwIllegalStateException();
        }

        public void setPreferRenderer(boolean preferRenderer) {
            throwIllegalStateException();
        }

        public void setComplexScriptFeaturesEnabled(boolean csf) {
            throwIllegalStateException();
        }

        public void setHyphPatNames(Map<String, String> hyphPatNames) {
            throwIllegalStateException();
        }

        public void setTableBorderOverpaint(boolean b) {
            throwIllegalStateException();
        }

        public void setSimpleLineBreaking(boolean b) {
            throwIllegalStateException();
        }

        public void setSkipPagePositionOnlyAllowed(boolean b) {
            throwIllegalStateException();
        }

        public void setLegacySkipPagePositionOnly(boolean b) {
            throwIllegalStateException();
        }

        public void setLegacyLastPageChangeIPD(boolean b) {
            throwIllegalStateException();
        }

        public void setLegacyFoWrapper(boolean b) {
            throwIllegalStateException();
        }
    }

    private static final class ActiveFopFactoryConfigBuilder implements FopFactoryConfigBuilder {

        private final FopFactoryConfigImpl config;

        private ActiveFopFactoryConfigBuilder(FopFactoryConfigImpl config) {
            this.config = config;
        }

        public void setAccessibility(boolean enableAccessibility) {
            config.accessibility = enableAccessibility;
        }

        public void setStaticRegionsPerPageForAccessibility(boolean staticRegionsPerPageForAccessibility) {
            config.staticRegionsPerPageForAccessibility = staticRegionsPerPageForAccessibility;
        }

        public void setKeepEmptyTags(boolean b) {
            config.keepEmptyTags = b;
        }

        public void setLayoutManagerMakerOverride(LayoutManagerMaker lmMaker) {
            config.layoutManagerMaker = lmMaker;
        }

        public void setBaseURI(URI baseURI) {
            config.baseURI = baseURI;
        }

        public void setHyphenationResourceResolver(InternalResourceResolver hyphenationResourceResolver) {
            config.hyphenationResourceResolver = hyphenationResourceResolver;
        }

        public void setStrictFOValidation(boolean validateStrictly) {
            config.hasStrictFOValidation = validateStrictly;
        }

        public void setStrictUserConfigValidation(
                boolean validateStrictly) {
            config.hasStrictUserValidation = validateStrictly;
        }

        public void setBreakIndentInheritanceOnReferenceAreaBoundary(boolean value) {
            config.breakIndentInheritanceOnReferenceBoundary = value;
        }

        public void setSourceResolution(float dpi) {
            config.sourceResolution = dpi;
        }

        public void setTargetResolution(float dpi) {
            config.targetResolution = dpi;
        }

        public void setPageHeight(String pageHeight) {
            config.pageHeight = pageHeight;
        }

        public void setPageWidth(String pageWidth) {
            config.pageWidth = pageWidth;
        }

        public void ignoreNamespace(String namespaceURI) {
            config.ignoredNamespaces.add(namespaceURI);
        }

        public void ignoreNamespaces(
                Collection<String> namespaceURIs) {
            config.ignoredNamespaces.addAll(namespaceURIs);
        }

        public void setConfiguration(Configuration cfg) {
            config.cfg = cfg;
        }

        public void setPreferRenderer(boolean preferRenderer) {
            config.preferRenderer = preferRenderer;
        }

        public void setComplexScriptFeaturesEnabled(boolean csf) {
            config.isComplexScript = csf;
        }

        public void setHyphPatNames(Map<String, String> hyphPatNames) {
            config.hyphPatNames = hyphPatNames;
        }

        public void setTableBorderOverpaint(boolean b) {
            config.tableBorderOverpaint = b;
        }

        public void setSimpleLineBreaking(boolean b) {
            config.simpleLineBreaking = b;
        }

        public void setSkipPagePositionOnlyAllowed(boolean b) {
            config.skipPagePositionOnlyAllowed = b;
        }

        public void setLegacySkipPagePositionOnly(boolean b) {
            config.legacySkipPagePositionOnly = b;
        }

        public void setLegacyLastPageChangeIPD(boolean b) {
            config.legacyLastPageChangeIPD = b;
        }

        public void setLegacyFoWrapper(boolean b) {
            config.legacyFoWrapper = b;
        }

        public void setLegacyInvalidBreakPosition(boolean legacyInvalidBreakPosition) {
            config.legacyInvalidBreakPosition = legacyInvalidBreakPosition;
        }
    }

}
