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

package org.apache.fop.layoutmgr.inline;

import org.xml.sax.Locator;

import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.events.EventProducer;

/**
 * Event producer interface for inline-level layout managers.
 */
public interface InlineLevelEventProducer extends EventProducer {

    /**
     * Provider class for the event producer.
     */
    class Provider {
        
        /**
         * Returns an event producer.
         * @param broadcaster the event broadcaster to use
         * @return the event producer
         */
        public static InlineLevelEventProducer get(EventBroadcaster broadcaster) {
            return (InlineLevelEventProducer)broadcaster.getEventProducerFor(
                    InlineLevelEventProducer.class);
        }
    }

    /**
     * fo:leader with "use-content" but without children.
     * @param source the event source
     * @param loc the location of the error or null
     * @event.severity ERROR
     */
    void leaderWithoutContent(Object source, Locator loc);
    
    /**
     * A line overflows.
     * @param source the event source
     * @param line the number number
     * @param overflowLength the length by which the content overflows the available space (in mpt)
     * @param loc the location of the error or null
     * @event.severity WARN
     */
    void lineOverflows(Object source, int line, int overflowLength, Locator loc);
    
}
