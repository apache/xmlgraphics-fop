/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.tools;

import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * This utility class is used to build URLs from Strings. The String can be
 * normal URLs but also just filenames. The filenames get converted to a
 * file URL.
 *
 * @author Jeremias Maerki
 */
public class URLBuilder {

    /**
     * Build an URL based on a String. The String can be a normal URL or a
     * filename. Filenames get automatically converted to to URLs.
     *
     * @param spec  A URL or a filename
     * @return      The requested URL
     * @throws MalformedURLException If spec cannot be converted to a URL.
     */
    public static URL buildURL(String spec) throws MalformedURLException {
        if (spec == null) throw new NullPointerException("spec must not be null");
        File f = new File(spec);
        if (f.exists()) {
            return f.toURL();
        } else {
            URL u1 = new URL(spec);
            return u1;
        }
    }


    /**
     * Build an URL based on a String. The String can be a normal URL or a
     * filename. Filenames get automatically converted to to URLs.
     *
     * @param baseURL   Base URL for relative paths
     * @param spec  A URL or a filename
     * @return      The requested URL
     * @throws MalformedURLException If spec cannot be converted to a URL.
     */
    public static URL buildURL(URL baseURL, String spec) throws MalformedURLException {
        if (spec == null) throw new NullPointerException("spec must not be null");
        try {
            URL u1 = buildURL(spec);
            return u1;
        } catch (MalformedURLException mfue) {
            if (baseURL == null) throw mfue;
            URL u2 = new URL(baseURL, spec);
            return u2;
        }
    }

}