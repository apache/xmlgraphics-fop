/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
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