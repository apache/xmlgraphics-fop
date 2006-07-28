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
 
package org.apache.fop.render.awt.viewer;

//Java
import java.util.ResourceBundle;
import java.util.Locale;

/**
 * AWT Viewer's localization class, backed up by <code>java.util.ResourceBundle</code>.
 * Originally contributed by:
 * Stanislav.Gorkhover@jCatalog.com
 */
public class Translator {
    
    private ResourceBundle bundle;
    private static String bundleBaseName = "org/apache/fop/render/awt/viewer/resources/Viewer";

    /**
     * Default constructor, default <code>Locale</code> is used.
     */
    public Translator() {
        this(Locale.getDefault());
    }

    /**
     * Constructor for a given <code>Locale</code>.
     * @param locale Locale to use
     */
    public Translator(Locale locale) {
        bundle = ResourceBundle.getBundle(bundleBaseName, locale);
    }

    /**
     * Returns localized <code>String</code> for a given key.
     * @param key the key
     * @return the localized String
     */
    public String getString(String key) {
        return bundle.getString(key);
    }
}

