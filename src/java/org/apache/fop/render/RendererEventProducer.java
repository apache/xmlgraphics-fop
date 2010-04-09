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

import java.io.IOException;

import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.events.EventProducer;

/**
 * Event producer interface for rendering-specific events.
 */
public interface RendererEventProducer extends EventProducer {

    /** Provider class for the event producer. */
    class Provider {

        /**
         * Returns an event producer.
         * @param broadcaster the event broadcaster to use
         * @return the event producer
         */
        public static RendererEventProducer get(EventBroadcaster broadcaster) {
            return (RendererEventProducer)broadcaster.getEventProducerFor(
                    RendererEventProducer.class);
        }
    }

    /**
     * I/O error while writing target file.
     * @param source the event source
     * @param ioe the original I/O error
     * @event.severity ERROR
     */
    void ioError(Object source, IOException ioe);
}
