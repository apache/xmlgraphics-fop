/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: $ */

package org.apache.fop.apps;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

// commons logging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides FOP specific URI resolution.
 * This is the default URIResolver {@link FOUserAgent} will use unless overidden.
 * @see javax.xml.transform.URIResolver
 */
public class FOURIResolver
    implements javax.xml.transform.URIResolver {
    
    private Log log = LogFactory.getLog("FOP");
    
    /**
     * Called by the processor through {@link FOUserAgent} when it encounters an 
     * uri in an external-graphic element.
     * (see also {@link javax.xml.transform.URIResolver#resolve(String, String)}
     * This resolver will allow URLs without a scheme, i.e. it assumes 'file:' as
     * the default scheme. It also allows relative URLs with scheme, 
     * e.g. file:../../abc.jpg which is not strictly RFC compliant as long as the 
     * scheme is the same as the scheme of the base URL. If the base URL is null 
     * a 'file:' URL referencing the current directory is used as the base URL.
     * If the method is successful it will return a Source of type 
     * {@link javax.xml.transform.stream.StreamSource} with its SystemID set to 
     * the resolved URL used to open the underlying InputStream.
     * 
     * @param href An href attribute, which may be relative or absolute.
     * @param base The base URI against which the first argument will be made 
     * absolute if the absolute URI is required. 
     * @return A {@link javax.xml.transform.Source} object, or null if the href 
     * cannot be resolved. 
     * @throws javax.xml.transform.TransformerException Never thrown by this implementation.
     * @see javax.xml.transform.URIResolver#resolve(String, String)
     */
    public Source resolve(String href, String base)
        throws javax.xml.transform.TransformerException {
        
        URL absoluteURL = null;
        File f = new File(href);
        if (f.exists()) {
            try {
                absoluteURL = f.toURL();
            } catch (MalformedURLException mfue) {
                log.error("Could not convert filename to URL: " + mfue.getMessage(), mfue); 
            }
        } else {
            URL baseURL = toBaseURL(base);
            if (baseURL == null) {
                // We don't have a valid baseURL just use the URL as given
                try {
                    absoluteURL = new URL(href);
                } catch (MalformedURLException mue) {
                    try {
                        // the above failed, we give it another go in case
                        // the href contains only a path then file: is assumed
                        absoluteURL = new URL("file:" + href);
                    } catch (MalformedURLException mfue) {
                        log.error("Error with URL '" + href + "': " + mue.getMessage(), mue);
                        return null;
                    }
                }
            } else {
                try {
                    /*
                        This piece of code is based on the following statement in RFC2396 section 5.2:

                        3) If the scheme component is defined, indicating that the reference
                           starts with a scheme name, then the reference is interpreted as an
                           absolute URI and we are done.  Otherwise, the reference URI's
                           scheme is inherited from the base URI's scheme component.

                           Due to a loophole in prior specifications [RFC1630], some parsers
                           allow the scheme name to be present in a relative URI if it is the
                           same as the base URI scheme.  Unfortunately, this can conflict
                           with the correct parsing of non-hierarchical URI.  For backwards
                           compatibility, an implementation may work around such references
                           by removing the scheme if it matches that of the base URI and the
                           scheme is known to always use the <hier_part> syntax.

                        The URL class does not implement this work around, so we do.
                    */

                    String scheme = baseURL.getProtocol() + ":";
                    if (href.startsWith(scheme)) {
                        href = href.substring(scheme.length());
                        if ("file:".equals(scheme) && href.indexOf(':') >= 0) {
                            href = "/" + href; //Absolute file URL doesn't have a leading slash
                        }
                    }
                    absoluteURL = new URL(baseURL, href);
                } catch (MalformedURLException mfue) {
                    log.error("Error with URL '" + href + "': " + mfue.getMessage(), mfue);
                    return null;
                }
            }
        }
        
        try {
            return new StreamSource(absoluteURL.openStream(), absoluteURL.toExternalForm());
        } catch (java.io.IOException ioe) {
            log.error("Error with opening URL '" + href + "': " + ioe.getMessage(), ioe);
        }
        return null;
    }

    /**
     * Returns the base URL as a java.net.URL.
     * If the base URL is not set a default URL pointing to the
     * current directory is returned.
     * @param baseURL the base URL
     * @returns the base URL as java.net.URL
     */
    private URL toBaseURL(String baseURL) {
        try {
            return new URL(baseURL == null 
                            ? new java.io.File("").toURL().toExternalForm() 
                            : baseURL);
        } catch (MalformedURLException mfue) {
            log.error("Error with base URL: " + mfue.getMessage(), mfue);
        }
        return null;
    }
    
}
