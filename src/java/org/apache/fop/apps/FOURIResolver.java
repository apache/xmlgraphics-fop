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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.util.io.Base64EncodeStream;

import org.apache.fop.util.DataURIResolver;

/**
 * Provides FOP specific URI resolution. This is the default URIResolver
 * {@link FOUserAgent} will use unless overidden.
 * 
 * @see javax.xml.transform.URIResolver
 */
public class FOURIResolver implements javax.xml.transform.URIResolver {

    // log
    private Log log = LogFactory.getLog("FOP");

    /** URIResolver for RFC 2397 data URLs */
    private URIResolver dataURIResolver = new DataURIResolver();

    /** A user settable URI Resolver */
    private URIResolver uriResolver = null;

    /** true if exceptions are to be thrown if the URIs cannot be resolved. */
    private boolean throwExceptions = false;

    /**
     * Checks if the given base URL is acceptable. It also normalizes the URL.
     * @param base the base URL to check
     * @return the normalized URL
     * @throws MalformedURLException if there's a problem with a file URL
     */
    public String checkBaseURL(String base) throws MalformedURLException {
        if (!base.endsWith("/")) {
            // The behavior described by RFC 3986 regarding resolution of relative
            // references may be misleading for normal users:
            // file://path/to/resources + myResource.res -> file://path/to/myResource.res
            // file://path/to/resources/ + myResource.res -> file://path/to/resources/myResource.res
            // We assume that even when the ending slash is missing, users have the second
            // example in mind
            base += "/";
        }
        File dir = new File(base);
        try {
            base = (dir.isDirectory() ? dir.toURI().toURL() : new URL(base)).toExternalForm();
        } catch (MalformedURLException mfue) {
            if (throwExceptions) {
                throw mfue;
            }
            log.error(mfue.getMessage());
        }
        return base;
    }

    /**
     * Default constructor
     */
    public FOURIResolver() {
        this(false);
    }

    /**
     * Additional constructor
     * 
     * @param throwExceptions
     *            true if exceptions are to be thrown if the URIs cannot be
     *            resolved.
     */
    public FOURIResolver(boolean throwExceptions) {
        this.throwExceptions = throwExceptions;
    }

    /**
     * Handles resolve exceptions appropriately.
     * 
     * @param errorStr
     *            error string
     * @param strict
     *            strict user config
     */
    private void handleException(Exception e, String errorStr, boolean strict)
            throws TransformerException {
        if (strict) {
            throw new TransformerException(errorStr, e);
        }
        log.error(e.getMessage());
    }

    /**
     * Called by the processor through {@link FOUserAgent} when it encounters an
     * uri in an external-graphic element. (see also
     * {@link javax.xml.transform.URIResolver#resolve(String, String)} This
     * resolver will allow URLs without a scheme, i.e. it assumes 'file:' as the
     * default scheme. It also allows relative URLs with scheme, e.g.
     * file:../../abc.jpg which is not strictly RFC compliant as long as the
     * scheme is the same as the scheme of the base URL. If the base URL is null
     * a 'file:' URL referencing the current directory is used as the base URL.
     * If the method is successful it will return a Source of type
     * {@link javax.xml.transform.stream.StreamSource} with its SystemID set to
     * the resolved URL used to open the underlying InputStream.
     * 
     * @param href
     *            An href attribute, which may be relative or absolute.
     * @param base
     *            The base URI against which the first argument will be made
     *            absolute if the absolute URI is required.
     * @return A {@link javax.xml.transform.Source} object, or null if the href
     *         cannot be resolved.
     * @throws javax.xml.transform.TransformerException
     *             Never thrown by this implementation.
     * @see javax.xml.transform.URIResolver#resolve(String, String)
     */
    public Source resolve(String href, String base) throws TransformerException {
        Source source = null;

        // data URLs can be quite long so evaluate early and don't try to build a File
        // (can lead to problems)
        source = dataURIResolver.resolve(href, base);

        // Custom uri resolution
        if (source == null && uriResolver != null) {
            source = uriResolver.resolve(href, base);
        }

        // Fallback to default resolution mechanism
        if (source == null) {
            URL absoluteURL = null;
            int hashPos = href.indexOf('#');
            String fileURL, fragment;
            if (hashPos >= 0) {
                fileURL = href.substring(0, hashPos);
                fragment = href.substring(hashPos);
            } else {
                fileURL = href;
                fragment = null;
            }
            File file = new File(fileURL);
            if (file.canRead() && file.isFile()) {
                try {
                    if (fragment != null) {
                        absoluteURL = new URL(file.toURI().toURL().toExternalForm() + fragment);
                    } else {
                        absoluteURL = file.toURI().toURL();
                    }
                } catch (MalformedURLException mfue) {
                    handleException(mfue, "Could not convert filename '" + href
                            + "' to URL", throwExceptions);
                }
            } else {
                // no base provided
                if (base == null) {
                    // We don't have a valid file protocol based URL
                    try {
                        absoluteURL = new URL(href);
                    } catch (MalformedURLException mue) {
                        try {
                            // the above failed, we give it another go in case
                            // the href contains only a path then file: is
                            // assumed
                            absoluteURL = new URL("file:" + href);
                        } catch (MalformedURLException mfue) {
                            handleException(mfue, "Error with URL '" + href
                                    + "'", throwExceptions);
                        }
                    }

                    // try and resolve from context of base
                } else {
                    URL baseURL = null;
                    try {
                        baseURL = new URL(base);
                    } catch (MalformedURLException mfue) {
                        handleException(mfue, "Error with base URL '" + base
                                + "'", throwExceptions);
                    }

                    /*
                     * This piece of code is based on the following statement in
                     * RFC2396 section 5.2:
                     * 
                     * 3) If the scheme component is defined, indicating that
                     * the reference starts with a scheme name, then the
                     * reference is interpreted as an absolute URI and we are
                     * done. Otherwise, the reference URI's scheme is inherited
                     * from the base URI's scheme component.
                     * 
                     * Due to a loophole in prior specifications [RFC1630], some
                     * parsers allow the scheme name to be present in a relative
                     * URI if it is the same as the base URI scheme.
                     * Unfortunately, this can conflict with the correct parsing
                     * of non-hierarchical URI. For backwards compatibility, an
                     * implementation may work around such references by
                     * removing the scheme if it matches that of the base URI
                     * and the scheme is known to always use the <hier_part>
                     * syntax.
                     * 
                     * The URL class does not implement this work around, so we
                     * do.
                     */
                    String scheme = baseURL.getProtocol() + ":";
                    if (href.startsWith(scheme)) {
                        href = href.substring(scheme.length());
                        if ("file:".equals(scheme)) {
                            int colonPos = href.indexOf(':');
                            int slashPos = href.indexOf('/');
                            if (slashPos >= 0 && colonPos >= 0
                                    && colonPos < slashPos) {
                                href = "/" + href; // Absolute file URL doesn't
                                // have a leading slash
                            }
                        }
                    }
                    try {
                        absoluteURL = new URL(baseURL, href);
                    } catch (MalformedURLException mfue) {
                        handleException(mfue, "Error with URL; base '" + base
                                + "' " + "href '" + href + "'", throwExceptions);
                    }
                }
            }

            if (absoluteURL != null) {
                String effURL = absoluteURL.toExternalForm();
                try {
                    URLConnection connection = absoluteURL.openConnection();
                    connection.setAllowUserInteraction(false);
                    connection.setDoInput(true);
                    updateURLConnection(connection, href);
                    connection.connect();
                    return new StreamSource(connection.getInputStream(), effURL);
                } catch (FileNotFoundException fnfe) {
                    // Note: This is on "debug" level since the caller is
                    // supposed to handle this
                    log.debug("File not found: " + effURL);
                } catch (java.io.IOException ioe) {
                    log.error("Error with opening URL '" + effURL + "': "
                            + ioe.getMessage());
                }
            }
        }
        return source;
    }

    /**
     * This method allows you to set special values on a URLConnection just
     * before the connect() method is called. Subclass FOURIResolver and
     * override this method to do things like adding the user name and password
     * for HTTP basic authentication.
     * 
     * @param connection
     *            the URLConnection instance
     * @param href
     *            the original URI
     */
    protected void updateURLConnection(URLConnection connection, String href) {
        // nop
    }

    /**
     * This is a convenience method for users who want to override
     * updateURLConnection for HTTP basic authentication. Simply call it using
     * the right username and password.
     * 
     * @param connection
     *            the URLConnection to set up for HTTP basic authentication
     * @param username
     *            the username
     * @param password
     *            the password
     */
    protected void applyHttpBasicAuthentication(URLConnection connection,
            String username, String password) {
        String combined = username + ":" + password;
        try {
            ByteArrayOutputStream baout = new ByteArrayOutputStream(combined
                    .length() * 2);
            Base64EncodeStream base64 = new Base64EncodeStream(baout);
            // TODO Not sure what charset/encoding can be used with basic
            // authentication
            base64.write(combined.getBytes("UTF-8"));
            base64.close();
            connection.setRequestProperty("Authorization", "Basic "
                    + new String(baout.toByteArray(), "UTF-8"));
        } catch (IOException e) {
            // won't happen. We're operating in-memory.
            throw new RuntimeException(
                    "Error during base64 encodation of username/password");
        }
    }

    /**
     * Sets the custom URI Resolver. It is used for resolving factory-level URIs like
     * hyphenation patterns and as backup for URI resolution performed during a
     * rendering run.
     * 
     * @param resolver
     *            the new URI resolver
     */
    public void setCustomURIResolver(URIResolver resolver) {
        this.uriResolver = resolver;
    }

    /**
     * Returns the custom URI Resolver.
     * 
     * @return the URI Resolver or null, if none is set
     */
    public URIResolver getCustomURIResolver() {
        return this.uriResolver;
    }

    /**
     * @param throwExceptions
     *            Whether or not to throw exceptions on resolution error
     */
    public void setThrowExceptions(boolean throwExceptions) {
        this.throwExceptions = throwExceptions;
    }
}
