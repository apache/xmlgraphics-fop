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

package org.apache.fop.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

/**
 * This class is a URIResolver implementation that provides access to resources in the WEB-INF
 * directory of a web application using "servlet-content:" URIs.
 */
public class ServletContextURIResolver implements URIResolver {

    /** The protocol name for the servlet context URIs. */
    public static final String SERVLET_CONTEXT_PROTOCOL = "servlet-context:";
    
    private ServletContext servletContext;
    
    /**
     * Main constructor
     * @param servletContext the servlet context to access the resources through
     */
    public ServletContextURIResolver(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    /** @see javax.xml.transform.URIResolver#resolve(java.lang.String, java.lang.String) */
    public Source resolve(String href, String base) throws TransformerException {
        if (href.startsWith(SERVLET_CONTEXT_PROTOCOL)) {
            return resolveServletContextURI(href.substring(SERVLET_CONTEXT_PROTOCOL.length()));
        } else {
            if (base != null 
                    && base.startsWith(SERVLET_CONTEXT_PROTOCOL) 
                    && (href.indexOf(':') < 0)) {
                String abs = base + href;
                return resolveServletContextURI(
                        abs.substring(SERVLET_CONTEXT_PROTOCOL.length()));
            } else {
                return null;
            }
        }
    }

    /**
     * Resolves the "servlet-context:" URI.
     * @param path the path part after the protocol (should start with a "/")
     * @return the resolved Source or null if the resource was not found
     * @throws TransformerException if no URL can be constructed from the path
     */
    protected Source resolveServletContextURI(String path) throws TransformerException {
        while (path.startsWith("//")) {
            path = path.substring(1);
        }
        try {
            URL url = this.servletContext.getResource(path);
            InputStream in = this.servletContext.getResourceAsStream(path);
            if (in != null) {
                if (url != null) {
                    return new StreamSource(in, url.toExternalForm());
                } else {
                    return new StreamSource(in);
                }
            } else {
                throw new TransformerException("Resource does not exist. \"" + path
                        + "\" is not accessible through the servlet context.");
            }
        } catch (MalformedURLException mfue) {
            throw new TransformerException(
                    "Error accessing resource using servlet context: " + path, mfue);
        }
    }
}
