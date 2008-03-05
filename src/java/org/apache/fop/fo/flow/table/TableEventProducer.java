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

package org.apache.fop.fo.flow.table;

import org.xml.sax.Locator;

import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.events.EventProducer;
import org.apache.fop.fo.ValidationException;

/**
 * Event producer interface for table-specific XSL-FO validation messages.
 */
public interface TableEventProducer extends EventProducer {

    /**
     * Factory class for the event producer.
     */
    class Factory {
        
        /**
         * Creates a new event producer.
         * @param broadcaster the event broadcaster to use
         * @return the new event producer
         */
        public static TableEventProducer create(EventBroadcaster broadcaster) {
            return (TableEventProducer)broadcaster.getEventProducerFor(
                    TableEventProducer.class);
        }
    }

    /**
     * A value other than "auto" has been specified on fo:table.
     * @param source the event source
     * @param loc the location of the error or null
     * @event.severity WARN
     */
    void nonAutoBPDOnTable(Object source, Locator loc);

    /**
     * Padding on fo:table is ignored if the collapsing border model is active.
     * @param source the event source
     * @param loc the location of the error or null
     * @event.severity WARN
     */
    void noTablePaddingWithCollapsingBorderModel(Object source, Locator loc);

    /**
     * No mixing of table-rows and table-cells is allowed for direct children of table-body.
     * @param source the event source
     * @param elementName the name of the context node
     * @param loc the location of the error or null
     * @throws ValidationException the validation error provoked by the method call
     * @event.severity FATAL
     */
    void noMixRowsAndCells(Object source, String elementName, Locator loc)
            throws ValidationException;
    
}
