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

package org.apache.fop.fo;

import org.xml.sax.Locator;

import org.apache.xmlgraphics.util.QName;

import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.events.EventProducer;

public interface FOValidationEventProducer extends EventProducer {

    /**
     * Too many child nodes.
     * @param source
     * @param source the event source
     * @param elementName the name of the context node
     * @param offendingNode the offending node
     * @param loc the location of the error or null
     * @event.severity FATAL
     */
    void tooManyNodes(Object source, String elementName, QName offendingNode,
            Locator loc) throws ValidationException;
    
    void nodeOutOfOrder(Object source, String elementName, String tooLateNode, String tooEarlyNode,
            Locator loc) throws ValidationException;
    
    void invalidChild(Object source, String elementName, QName offendingNode, String ruleViolated,
            Locator loc) throws ValidationException;

    void missingChildElement(Object source, String elementName, String contentModel,
            Locator locator) throws ValidationException;

    /**
     * An element is missing a required property.
     * @param source the event source
     * @param elementName the name of the context node
     * @param propertyName the name of the missing property
     * @event.severity FATAL
     */
    void missingProperty(Object source, String elementName, String propertyName,
            Locator loc) throws ValidationException;
    
    /**
     * Factory class for the event producer.
     */
    public class Factory {
        
        /**
         * Creates a new event producer.
         * @param broadcaster the event broadcaster to use
         * @return the new event producer
         */
        public static FOValidationEventProducer create(EventBroadcaster broadcaster) {
            return (FOValidationEventProducer)broadcaster.getEventProducerFor(
                    FOValidationEventProducer.class);
        }
    }


    
}
