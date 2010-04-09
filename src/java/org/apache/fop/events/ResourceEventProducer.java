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

package org.apache.fop.events;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.w3c.dom.Document;
import org.xml.sax.Locator;

import org.apache.xmlgraphics.image.loader.ImageException;

import org.apache.fop.events.model.AbstractEventModelFactory;
import org.apache.fop.events.model.EventModel;

/**
 * Event producer interface for resource events (missing images, fonts etc.).
 */
public interface ResourceEventProducer extends EventProducer {

    /**
     * Provider class for the event producer.
     */
    class Provider {

        /**
         * Returns an event producer.
         * @param broadcaster the event broadcaster to use
         * @return the requested event producer
         */
        public static ResourceEventProducer get(EventBroadcaster broadcaster) {
            return (ResourceEventProducer)broadcaster.getEventProducerFor(
                    ResourceEventProducer.class);
        }
    }

    /** Event model factory for Accessibility. */
    public static class EventModelFactory extends AbstractEventModelFactory {

        /** {@inheritDoc} */
        public EventModel createEventModel() {
            return loadModel(getClass(), "event-model.xml");
        }

    }

    /**
     * Image not found.
     * @param source the event source
     * @param uri the original URI of the image
     * @param fnfe the "file not found" exception
     * @param loc the location of the error or null
     * @event.severity ERROR
     */
    void imageNotFound(Object source, String uri, FileNotFoundException fnfe, Locator loc);

    /**
     * Error while processing image.
     * @param source the event source
     * @param uri the original URI of the image
     * @param e the image exception
     * @param loc the location of the error or null
     * @event.severity ERROR
     */
    void imageError(Object source, String uri, ImageException e, Locator loc);

    /**
     * I/O error while loading an image.
     * @param source the event source
     * @param uri the original URI of the image
     * @param ioe the I/O exception
     * @param loc the location of the error or null
     * @event.severity ERROR
     */
    void imageIOError(Object source, String uri, IOException ioe, Locator loc);

    /**
     * Error while writing/serializing an image to an output format.
     * @param source the event source
     * @param e the original exception
     * @event.severity ERROR
     */
    void imageWritingError(Object source, Exception e);

    /**
     * Error while handling a URI.
     * @param source the event source
     * @param uri the original URI of the image
     * @param e the original exception
     * @param loc the location of the error or null
     * @event.severity ERROR
     */
    void uriError(Object source, String uri, Exception e, Locator loc);

    /**
     * Intrinsic size of fo:instream-foreign-object could not be determined.
     * @param source the event source
     * @param loc the location of the error or null
     * @event.severity ERROR
     */
    void ifoNoIntrinsicSize(Object source, Locator loc);

    /**
     * Error processing foreign XML content.
     * @param source the event source
     * @param doc the foreign XML
     * @param namespaceURI the namespace URI of the foreign XML
     * @param e the original exception
     * @event.severity ERROR
     */
    void foreignXMLProcessingError(Object source, Document doc, String namespaceURI, Exception e);

    /**
     * No handler for foreign XML content.
     * @param source the event source
     * @param doc the foreign XML
     * @param namespaceURI the namespace URI of the foreign XML
     * @event.severity ERROR
     */
    void foreignXMLNoHandler(Object source, Document doc, String namespaceURI);

    /**
     * Cannot delete a temporary file.
     * @param source the event source
     * @param tempFile the temporary file
     * @event.severity ERROR
     */
    void cannotDeleteTempFile(Object source, File tempFile);

    /**
     * Catalog Resolver not found along the class path
     * @param source the event source
     * @event.severity ERROR
     */
    void catalogResolverNotFound(Object source);

    /**
     * Catalog Resolver not created, due to InstantiationException or IllegalAccessException
     * @param source the event source
     * @param message the exception message
     * @event.severity ERROR
     */
    void catalogResolverNotCreated(Object source, String message);
}
