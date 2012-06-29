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

package org.apache.fop.afp;

import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.events.EventProducer;

/**
 * Event producer interface for AFP-specific events.
 */
public interface AFPEventProducer extends EventProducer {

    /** Provider class for the event producer. */
    static final class Provider {

        private Provider() {
        }

        /**
         * Returns an event producer.
         * @param broadcaster the event broadcaster to use
         * @return the event producer
         */
        public static AFPEventProducer get(EventBroadcaster broadcaster) {
            return (AFPEventProducer) broadcaster.getEventProducerFor(AFPEventProducer.class);
        }
    }

    /**
     * Warn about using default font setup.
     *
     * @param source the event source
     * @event.severity WARN
     */
    void warnDefaultFontSetup(Object source);

    /**
     * Warn about a missing default "any" font configuration.
     *
     * @param source the event source
     * @param style the font style
     * @param weight the font weight
     * @event.severity WARN
     */
    void warnMissingDefaultFont(Object source, String style, int weight);

    /**
     * A character set encoding error occurred.
     *
     * @param source the event source
     * @param charSetName the character set name
     * @param encoding the encoding
     * @event.severity ERROR
     */
    void characterSetEncodingError(Object source, String charSetName, String encoding);

    /**
     * Triggered when an external resource fails to be embedded.
     *
     * @param source the event source
     * @param resourceName the name of the resource where the error occurred
     * @param e the original exception
     * @event.severity ERROR
     */
    void resourceEmbeddingError(Object source, String resourceName, Exception e);

    /**
     * A mandatory font configuration node is missing at location.
     * @param source the event source
     * @param missingConfig the expected configuration element
     * @param location the position of the missing element within the config file.
     * @event.severity ERROR
     */
    void fontConfigMissing(Object source, String missingConfig, String location);

    /**
     * The character set given has an invalid name.
     * @param source the event source
     * @param msg the error message
     * @event.severity ERROR
     */
    void characterSetNameInvalid(Object source, String msg);

    /**
     * The code page for an AFP font could not be found.
     * @param source the event source
     * @param e the original exception
     * @event.severity ERROR
     */
    void codePageNotFound(Object source, Exception e);

    /**
     * This is a generic event for invalid configuration errors.
     * @param source the event source
     * @param e the original exception
     * @event.severity ERROR
     */
    void invalidConfiguration(Object source, Exception e);
}
