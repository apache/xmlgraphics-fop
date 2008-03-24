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

package org.apache.fop.render.afp;

import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.events.EventProducer;
import org.apache.fop.events.model.AbstractEventModelFactory;
import org.apache.fop.events.model.EventModel;

/**
 * Event producer interface for AFP-specific events.
 */
public interface AFPEventProducer extends EventProducer {

    /** Factory class for the event producer. */
    class Factory {
        
        /**
         * Creates a new event producer.
         * @param broadcaster the event broadcaster to use
         * @return the new event producer
         */
        public static AFPEventProducer create(EventBroadcaster broadcaster) {
            return (AFPEventProducer)broadcaster.getEventProducerFor(
                    AFPEventProducer.class);
        }
    }

    /** Event model factory for AFP. */
    public static class EventModelFactory extends AbstractEventModelFactory {

        /** {@inheritDoc} */
        public EventModel createEventModel() {
            return loadModel(getClass(), "event-model.xml");
        }
        
    }
    
    /**
     * Warn about using default font setup.
     * @param source the event source
     * @event.severity WARN
     */
    void warnDefaultFontSetup(Object source);
    
}
