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

package org.apache.fop.render;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.util.Service;

/**
 * This class holds references to various image handlers used by the renderers. It also
 * supports automatic discovery of additional handlers available through
 * the class path.
 */
public abstract class AbstractImageHandlerRegistry {

    /** the logger */
    private static Log log = LogFactory.getLog(AbstractImageHandlerRegistry.class);

    private static final Comparator HANDLER_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            ImageHandler h1 = (ImageHandler)o1;
            ImageHandler h2 = (ImageHandler)o2;
            return h1.getPriority() - h2.getPriority();
        }
    };

    /** Map containing image handlers for various MIME types */
    private final Map/*<Class, ImageHandler>*/ handlers
        = new java.util.HashMap/*<Class, ImageHandler>*/();

    /** List containing the same handlers as above but ordered by priority */
    private final List/*<ImageHandler>*/ handlerList
        = new java.util.LinkedList/*<ImageHandler>*/();

    /** Sorted Set of registered handlers */
    private ImageFlavor[] supportedFlavors = new ImageFlavor[0];

    private int handlerRegistrations;
    private int lastSync;

    /**
     * Default constructor.
     */
    public AbstractImageHandlerRegistry() {
        discoverHandlers();
    }

    /**
     * Add an ImageHandler. The handler itself is inspected to find out what it supports.
     * @param classname the fully qualified class name
     */
    public void addHandler(String classname) {
        try {
            ImageHandler handlerInstance
                = (ImageHandler)Class.forName(classname).newInstance();
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
                                               + getHandlerClass().getName());
        }
    }

    /**
     * Add an image handler. The handler itself is inspected to find out what it supports.
     * @param handler the ImageHandler instance
     */
    public synchronized void addHandler(ImageHandler handler) {
        Class[] imageClasses = handler.getSupportedImageClasses();
        for (int i = 0; i < imageClasses.length; i++) {
            this.handlers.put(imageClasses[i], handler);
        }

        //Sorted insert
        ListIterator iter = this.handlerList.listIterator();
        while (iter.hasNext()) {
            ImageHandler h = (ImageHandler)iter.next();
            if (getHandlerComparator().compare(handler, h) < 0) {
                iter.previous();
                break;
            }
        }
        iter.add(handler);
        this.handlerRegistrations++;
    }

    /**
     * Returns an ImageHandler which handles an specific image type given the MIME type
     * of the image.
     * @param img the Image to be handled
     * @return the ImageHandler responsible for handling the image or null if none is available
     */
    public ImageHandler getHandler(Image img) {
        return getHandler(img.getClass());
    }

    /**
     * Returns an ImageHandler which handles an specific image type given the MIME type
     * of the image.
     * @param imageClass the Image subclass for which to get a handler
     * @return the ImageHandler responsible for handling the image or null if none is available
     */
    public synchronized ImageHandler getHandler(Class imageClass) {
        ImageHandler handler = null;
        Class cl = imageClass;
        while (cl != null) {
            handler = (ImageHandler)handlers.get(cl);
            if (handler != null) {
                break;
            }
            cl = cl.getSuperclass();
        }
        return handler;
    }

    /**
     * Returns the ordered array of supported image flavors.
     * @return the array of image flavors
     */
    public synchronized ImageFlavor[] getSupportedFlavors() {
        if (this.lastSync != this.handlerRegistrations) {
            //Extract all ImageFlavors into a single array
            List flavors = new java.util.ArrayList();
            Iterator iter = this.handlerList.iterator();
            while (iter.hasNext()) {
                ImageFlavor[] f = ((ImageHandler)iter.next()).getSupportedImageFlavors();
                for (int i = 0; i < f.length; i++) {
                    flavors.add(f[i]);
                }
            }
            this.supportedFlavors = (ImageFlavor[])flavors.toArray(new ImageFlavor[flavors.size()]);
            this.lastSync = this.handlerRegistrations;
        }
        return this.supportedFlavors;
    }

    /**
     * Discovers ImageHandler implementations through the classpath and dynamically
     * registers them.
     */
    private void discoverHandlers() {
        // add mappings from available services
        Class imageHandlerClass = getHandlerClass();
        Iterator providers = Service.providers(imageHandlerClass);
        if (providers != null) {
            while (providers.hasNext()) {
                ImageHandler handler = (ImageHandler)providers.next();
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Dynamically adding ImageHandler: "
                                + handler.getClass().getName());
                    }
                    addHandler(handler);
                } catch (IllegalArgumentException e) {
                    log.error("Error while adding ImageHandler", e);
                }

            }
        }
    }

    /**
     * Returns the ImageHandler comparator
     *
     * @return the ImageHandler comparator
     */
    public Comparator getHandlerComparator() {
        return HANDLER_COMPARATOR;
    }

    /**
     * Returns the ImageHandler implementing class
     *
     * @return the ImageHandler implementing class
     */
    public abstract Class getHandlerClass();
}
