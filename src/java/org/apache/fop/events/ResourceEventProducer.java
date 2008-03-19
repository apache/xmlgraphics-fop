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

import java.io.FileNotFoundException;
import java.io.IOException;

import org.xml.sax.Locator;

import org.apache.xmlgraphics.image.loader.ImageException;

/**
 * Event producer interface for resource events (missing images, fonts etc.).
 */
public interface ResourceEventProducer extends EventProducer {

    /**
     * Factory class for the event producer.
     */
    class Factory {
        
        /**
         * Creates a new event producer.
         * @param broadcaster the event broadcaster to use
         * @return the new event producer
         */
        public static ResourceEventProducer create(EventBroadcaster broadcaster) {
            return (ResourceEventProducer)broadcaster.getEventProducerFor(
                    ResourceEventProducer.class);
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
     * I/O error while processing image.
     * @param source the event source
     * @param uri the original URI of the image
     * @param ioe the I/O exception
     * @param loc the location of the error or null
     * @event.severity ERROR
     */
    void imageIOError(Object source, String uri, IOException ioe, Locator loc);

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
}
