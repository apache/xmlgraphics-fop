/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.viewer; 

//Java
import java.util.ResourceBundle; 
import java.util.Locale;
import java.util.MissingResourceException;

/** 
 * AWT Viewer's localization class, backed up by <code>java.util.ResourceBundle</code>.
 * Originally contributed by:
 * Stanislav.Gorkhover@jCatalog.com
 */
public class Translator {
    private ResourceBundle bundle;
    private static String bundleBaseName = "org/apache/fop/viewer/resources/Viewer";

    /**
     * Default constructor, default <code>Locale</code> is used.
     */
    public Translator() {
        this(Locale.getDefault()); 
    }
 
    /** 
     * Constructor for a given <code>Locale</code>.
     */
    public Translator(Locale locale) {
        bundle = ResourceBundle.getBundle(bundleBaseName, locale);
    }

    /**
     * Returns localized <code>String</code> for a given key.
     */
    public String getString(String key) {
        return bundle.getString(key);
    }
}

