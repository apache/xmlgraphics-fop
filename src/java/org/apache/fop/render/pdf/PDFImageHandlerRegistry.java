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

package org.apache.fop.render.pdf;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlgraphics.util.Service;

/**
 * This class holds references to various image handlers used by the PDF renderer. It also
 * supports automatic discovery of additional handlers available through
 * the class path.
 */
public class PDFImageHandlerRegistry {

    /** the logger */
    private static Log log = LogFactory.getLog(PDFImageHandlerRegistry.class);
    
    /** Map containing PDF image handlers for various MIME types */
    private Map handlers = new java.util.HashMap();
    
    /**
     * Default constructor.
     */
    public PDFImageHandlerRegistry() {
        discoverHandlers();
    }
    
    /**
     * Add an PDFImageHandler. The handler itself is inspected to find out what it supports.
     * @param classname the fully qualified class name
     */
    public void addHandler(String classname) {
        try {
            PDFImageHandler handlerInstance
                = (PDFImageHandler)Class.forName(classname).newInstance();
            addHandler(handlerInstance);
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
                                               + " is not an " 
                                               + PDFImageHandler.class.getName());
        }
    }
    
    /**
     * Add an image handler. The handler itself is inspected to find out what it supports.
     * @param handler the PDFImageHandler instance
     */
    public void addHandler(PDFImageHandler handler) {
        String mime = handler.getSupportedMimeType();
        handlers.put(mime, handler);
    }
    
    /**
     * Returns an PDFImageHandler which handles an specific image type given the MIME type
     * of the image.
     * @param mime the requested MIME type
     * @return the PDFImageHandler responsible for handling the image or null if none is available
     */
    public PDFImageHandler getHandler(String mime) {
        PDFImageHandler handler;

        handler = (PDFImageHandler)handlers.get(mime);
        return handler;
    }

    /**
     * Discovers PDFImageHandler implementations through the classpath and dynamically
     * registers them.
     */
    private void discoverHandlers() {
        // add mappings from available services
        Iterator providers = Service.providers(PDFImageHandler.class);
        if (providers != null) {
            while (providers.hasNext()) {
                PDFImageHandler handler = (PDFImageHandler)providers.next();
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Dynamically adding PDFImageHandler: " 
                                + handler.getClass().getName());
                    }
                    addHandler(handler);
                } catch (IllegalArgumentException e) {
                    log.error("Error while adding PDFImageHandler", e);
                }

            }
        }
    }
}
