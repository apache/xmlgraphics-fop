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

/* $Id$ */

package org.apache.fop.render;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.util.Service;

/**
 * This class holds references to various XML handlers used by FOP. It also
 * supports automatic discovery of additional XML handlers available through
 * the class path.
 */
public class XMLHandlerRegistry {

    /** the logger */
    private static Log log = LogFactory.getLog(XMLHandlerRegistry.class);
    
    /** Map containing XML handlers for various document types */
    private Map handlers = new java.util.HashMap();
    
    
    /**
     * Default constructor.
     */
    public XMLHandlerRegistry() {
        discoverXMLHandlers();
    }
    
    /**
     * Set the default XML handler for the given MIME type.
     * @param mime MIME type
     * @param handler XMLHandler to use
     */
    private void setDefaultXMLHandler(String mime,
                                     XMLHandler handler) {
        addXMLHandler(mime, XMLHandler.HANDLE_ALL, handler);
    }
    
    /**
     * Add an XML handler. The handler itself is inspected to find out what it supports.
     * @param classname the fully qualified class name
     */
    public void addXMLHandler(String classname) {
        try {
            XMLHandler handlerInstance =
                (XMLHandler)Class.forName(classname).newInstance();
            addXMLHandler(handlerInstance);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not find "
                                               + classname);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Could not instantiate "
                                               + classname);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not access "
                                               + classname);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(classname
                                               + " is not an ElementMapping");
        }
    }
    
    /**
     * Add an XML handler. The handler itself is inspected to find out what it supports.
     * @param handler the XMLHandler instance
     */
    public void addXMLHandler(XMLHandler handler) {
        String mime = handler.getMimeType();
        String ns = handler.getNamespace();
        if (ns == null) {
            setDefaultXMLHandler(mime, handler);
        } else {
            addXMLHandler(mime, ns, handler);
        }
    }
    
    /**
     * Add an XML handler for the given MIME type and XML namespace.
     * @param mime MIME type
     * @param ns Namespace URI
     * @param handler XMLHandler to use
     */
    private void addXMLHandler(String mime, String ns,
                              XMLHandler handler) {
        Map mh = (Map)handlers.get(mime);
        if (mh == null) {
            mh = new java.util.HashMap();
            handlers.put(mime, mh);
        }
        mh.put(ns, handler);
    }
    
    /**
     * Returns an XMLHandler which handles an XML dialect of the given namespace and for
     * a specified output format defined by its MIME type.
     * @param mime the MIME type of the output format
     * @param ns the XML namespace associated with the XML to be rendered
     * @return the XMLHandler responsible for handling the XML or null if none is available
     */
    public XMLHandler getXMLHandler(String mime, String ns) {
        XMLHandler handler = null;

        Map mh = (Map)handlers.get(mime);
        if (mh != null) {
            handler = (XMLHandler)mh.get(ns);
            if (handler == null) {
                handler = (XMLHandler)mh.get(XMLHandler.HANDLE_ALL);
            }
        }
        if (handler == null) {
            mh = (Map)handlers.get(XMLHandler.HANDLE_ALL);
            if (mh != null) {
                handler = (XMLHandler)mh.get(ns);
                if (handler == null) {
                    handler = (XMLHandler)mh.get(XMLHandler.HANDLE_ALL);
                }
            }
        }
        return handler;
    }
    
    
    /**
     * Discovers XMLHandler implementations through the classpath and dynamically
     * registers them.
     */
    private void discoverXMLHandlers() {
        // add mappings from available services
        Iterator providers =
            Service.providers(XMLHandler.class);
        if (providers != null) {
            while (providers.hasNext()) {
                String str = (String)providers.next();
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Dynamically adding XMLHandler: " + str);
                    }
                    addXMLHandler(str);
                } catch (IllegalArgumentException e) {
                    log.error("Error while adding XMLHandler", e);
                }

            }
        }
    }
}
