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

package org.apache.fop.area;

import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.events.EventProducer;

/**
 * Event producer interface for events related to the area tree.
 */
public interface AreaEventProducer extends EventProducer {

    /**
     * Factory class for the event producer.
     */
    class Factory {
        
        /**
         * Creates a new event producer.
         * @param broadcaster the event broadcaster to use
         * @return the new event producer
         */
        public static AreaEventProducer create(EventBroadcaster broadcaster) {
            return (AreaEventProducer)broadcaster.getEventProducerFor(
                    AreaEventProducer.class);
        }
    }

    /**
     * An unresolved ID reference was encountered.
     * @param source the event source
     * @param type the type of reference
     * @param id the unresolved ID
     * @event.severity WARN
     */
    void unresolvedIDReference(Object source, String type, String id);
    
    /**
     * An unresolved ID reference was encountered on a page.
     * @param source the event source
     * @param page the page the ID reference was found on
     * @param id the unresolved ID
     * @event.severity WARN
     */
    void unresolvedIDReferenceOnPage(Object source, String page, String id);
    
    /**
     * A page could not be loaded/deserialized from a file.
     * @param source the event source
     * @param page the page to be loaded
     * @param e the original exception
     * @event.severity ERROR
     */
    void pageLoadError(Object source, String page, Exception e);
    
    /**
     * A page could not be saved/serialized to a file.
     * @param source the event source
     * @param page the page to be serialized
     * @param e the original exception
     * @event.severity ERROR
     */
    void pageSaveError(Object source, String page, Exception e);
    
    /**
     * A page could not be rendered.
     * @param source the event source
     * @param page the page to be serialized
     * @param e the original exception
     * @event.severity ERROR
     */
    void pageRenderingError(Object source, String page, Exception e);
    
}
