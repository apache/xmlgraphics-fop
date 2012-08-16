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
 * This is a mutable implementation of the {@link FopFactoryConfig} to be used for testing purposes.
 * This is also an example of how to make the seemingly immutable {@link FopFactory} mutable should
 * a client need to, though this is ill-advised.
 */
public final class MutableConfig implements FopFactoryConfig {

    private final FopFactoryConfig delegate;

    private boolean setBreakInheritance;
    private float sourceResolution;

    public MutableConfig(FopFactoryBuilder factoryBuilder) {
        delegate = factoryBuilder.buildConfiguration();
        setBreakInheritance = delegate.isBreakIndentInheritanceOnReferenceAreaBoundary();
        sourceResolution = delegate.getSourceResolution();
    }

    public boolean isAccessibilityEnabled() {
        return delegate.isAccessibilityEnabled();
    }

    public LayoutManagerMaker getLayoutManagerMakerOverride() {
        return delegate.getLayoutManagerMakerOverride();
    }

    public ResourceResolver getResourceResolver() {
        return delegate.getResourceResolver();
    }

    public URI getBaseURI() {
        return delegate.getBaseURI();
    }

    public boolean validateStrictly() {
        return delegate.validateStrictly();
    }

    public boolean validateUserConfigStrictly() {
        return delegate.validateUserConfigStrictly();
    }

    public boolean isBreakIndentInheritanceOnReferenceAreaBoundary() {
        return setBreakInheritance;
    }

    public void setBreakIndentInheritanceOnReferenceAreaBoundary(boolean value) {
        setBreakInheritance = value;
    }

    public float getSourceResolution() {
        return sourceResolution;
    }

    public void setSourceResolution(float srcRes) {
        sourceResolution = srcRes;
    }

    public float getTargetResolution() {
        return delegate.getTargetResolution();
    }

    public String getPageHeight() {
        return delegate.getPageHeight();
    }

    public String getPageWidth() {
        return delegate.getPageWidth();
    }

    public Set<String> getIgnoredNamespaces() {
        return delegate.getIgnoredNamespaces();
    }

    public boolean isNamespaceIgnored(String namespace) {
        return delegate.isNamespaceIgnored(namespace);
    }

    public Configuration getUserConfig() {
        return delegate.getUserConfig();
    }

    public boolean preferRenderer() {
        return delegate.preferRenderer();
    }

    public FontManager getFontManager() {
        return delegate.getFontManager();
    }

    public ImageManager getImageManager() {
        return delegate.getImageManager();
    }

    public boolean isComplexScriptFeaturesEnabled() {
        return delegate.isComplexScriptFeaturesEnabled();
    }

    public Map<String, String> getHyphenationPatternNames() {
        return delegate.getHyphenationPatternNames();
    }
}
