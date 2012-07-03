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

import java.io.File;

import org.apache.fop.apps.FOPException;

/**
 * A factory that provides the font caching manager mechanism.
 *
 */
public final class FontCacheManagerFactory {

    private FontCacheManagerFactory() {
    }

    /**
     * Create the default font caching mechanism.
     * @return the font cache manager
     */
    public static FontCacheManager createDefault() {
        return new FontCacheManagerImpl();
    }

    /**
     * Create a disabled font caching mechanism which by definition does nothing to cache fonts.
     * @return a completely restricted font cache manager
     */
    public static FontCacheManager createDisabled() {
        return new DisabledFontCacheManager();
    }

    private static final class FontCacheManagerImpl implements FontCacheManager {

        private FontCache fontCache;

        public FontCache load(File cacheFile) {
            if (fontCache == null) {
                fontCache = FontCache.loadFrom(cacheFile);
                if (fontCache == null) {
                    fontCache = new FontCache();
                }
            }
            return fontCache;
        }

        public void save(File cacheFile) throws FOPException {
            if (fontCache != null && fontCache.hasChanged()) {
                fontCache.saveTo(cacheFile);
            }
        }

        public void delete(File cacheFile) throws FOPException {
            if (!cacheFile.delete()) {
                throw new FOPException("Failed to flush the font cache file '" + cacheFile + "'.");
            }
        }
    }

    private static final class DisabledFontCacheManager implements FontCacheManager {

        public FontCache load(File cacheFile) {
            return null;
        }

        public void save(File cacheFile) throws FOPException {
            // nop
        }

        public void delete(File cacheFile) throws FOPException {
            throw new FOPException("Font Cache disabled");
        }
    }
}
