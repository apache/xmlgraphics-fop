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

package org.apache.fop.svg;

import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.events.EventProducer;
import org.apache.fop.events.model.AbstractEventModelFactory;
import org.apache.fop.events.model.EventModel;

/**
 * Event producer interface for SVG-related events.
 */
public interface SVGEventProducer extends EventProducer {

    /**
     * Provider class for the event producer.
     */
    class Provider {

        /**
         * Returns an event producer.
         * @param broadcaster the event broadcaster to use
         * @return the event producer
         */
        public static SVGEventProducer get(EventBroadcaster broadcaster) {
            return (SVGEventProducer)broadcaster.getEventProducerFor(
                    SVGEventProducer.class);
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
     * Error during SVG processing. Either message or e must be set.
     * @param source the event source
     * @param message the error message (or null)
     * @param e the exception (or null)
     * @event.severity ERROR
     */
    void error(Object source, String message, Exception e);

    /**
     * Alert during SVG processing.
     * @param source the event source
     * @param message the error message
     * @event.severity WARN
     */
    void alert(Object source, String message);

    /**
     * Info during SVG processing.
     * @param source the event source
     * @param message the error message
     * @event.severity INFO
     */
    void info(Object source, String message);

    /**
     * SVG graphic could not be built due to an exception.
     * @param source the event source
     * @param e the original exception
     * @param uri the URI of the SVG graphic
     * @event.severity ERROR
     */
    void svgNotBuilt(Object source, Exception e, String uri);

    /**
     * SVG graphic could not be rendered due to an exception.
     * @param source the event source
     * @param e the original exception
     * @param uri the URI of the SVG graphic
     * @event.severity ERROR
     */
    void svgRenderingError(Object source, Exception e, String uri);

}
