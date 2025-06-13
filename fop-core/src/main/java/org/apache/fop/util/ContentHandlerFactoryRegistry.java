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

package org.apache.fop.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.util.Service;

/**
 * This class holds references to various XML handlers used by FOP. It also
 * supports automatic discovery of additional XML handlers available through
 * the class path.
 */
public class ContentHandlerFactoryRegistry {

    /** the logger */
    private static Log log = LogFactory.getLog(ContentHandlerFactoryRegistry.class);

    /** Map from namespace URIs to ContentHandlerFactories */
    private Map factories = new java.util.HashMap();

    /**
     * Default constructor.
     */
    public ContentHandlerFactoryRegistry() {
        discover();
    }

    /**
     * Add an XML handler. The handler itself is inspected to find out what it supports.
     * @param classname the fully qualified class name
     */
    public void addContentHandlerFactory(String classname) {
        try {
            ContentHandlerFactory factory
                = (ContentHandlerFactory)Class.forName(classname).getDeclaredConstructor().newInstance();
            addContentHandlerFactory(factory);
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
                                               + ContentHandlerFactory.class.getName());
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Add an ContentHandlerFactory. The instance is inspected to find out what it supports.
     * @param factory the ContentHandlerFactory instance
     */
    public void addContentHandlerFactory(ContentHandlerFactory factory) {
        String[] ns = factory.getSupportedNamespaces();
        for (String n : ns) {
            factories.put(n, factory);
        }
    }

    /**
     * Retrieves a ContentHandlerFactory instance of a given namespace URI.
     * @param namespaceURI the namespace to be handled.
     * @return the ContentHandlerFactory or null, if no suitable instance is available.
     */
    public ContentHandlerFactory getFactory(String namespaceURI) {
        return (ContentHandlerFactory)factories.get(namespaceURI);
    }

    /**
     * Discovers ContentHandlerFactory implementations through the classpath and dynamically
     * registers them.
     */
    private void discover() {
        // add mappings from available services
        Iterator providers = Service.providers(ContentHandlerFactory.class);
        if (providers != null) {
            while (providers.hasNext()) {
                ContentHandlerFactory factory = (ContentHandlerFactory)providers.next();
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Dynamically adding ContentHandlerFactory: "
                                + factory.getClass().getName());
                    }
                    addContentHandlerFactory(factory);
                } catch (IllegalArgumentException e) {
                    log.error("Error while adding ContentHandlerFactory", e);
                }

            }
        }
    }
}
