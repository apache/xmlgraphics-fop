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

package org.apache.fop.accessibility;

import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.events.EventProducer;

/**
 * Event producer for accessibility-related events.
 */
public interface AccessibilityEventProducer extends EventProducer {

    /** Provider class for the event producer. */
    public final class Provider {

        private Provider() { }

        /**
         * Returns an event producer.
         *
         * @param broadcaster the event broadcaster to use
         * @return the event producer
         */
        public static AccessibilityEventProducer get(EventBroadcaster broadcaster) {
            return (AccessibilityEventProducer) broadcaster.getEventProducerFor(
                    AccessibilityEventProducer.class);
        }
    }

    /**
     * The structure tree is missing in the XML file.
     *
     * @param source the event source
     * @event.severity FATAL
     */
    void noStructureTreeInXML(Object source);
}
