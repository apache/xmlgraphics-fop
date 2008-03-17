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

import org.apache.fop.apps.FOPException;
import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.events.EventProducer;
import org.apache.fop.fo.expr.PropertyException;

/**
 * Event producer interface for XSL-FO validation messages.
 */
public interface FOValidationEventProducer extends EventProducer {

    /**
     * Factory class for the event producer.
     */
    class Factory {
        
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

    /**
     * Too many child nodes.
     * @param source the event source
     * @param elementName the name of the context node
     * @param offendingNode the offending node
     * @param loc the location of the error or null
     * @throws ValidationException the validation error provoked by the method call 
     * @event.severity FATAL
     */
    void tooManyNodes(Object source, String elementName, QName offendingNode,
            Locator loc) throws ValidationException;
    
    /**
     * The node order is wrong.
     * @param source the event source
     * @param elementName the name of the context node
     * @param tooLateNode string name of node that should be earlier in document
     * @param tooEarlyNode string name of node that should be later in document
     * @param canRecover indicates whether FOP can recover from this problem and continue working
     * @param loc the location of the error or null
     * @throws ValidationException the validation error provoked by the method call
     */
    void nodeOutOfOrder(Object source, String elementName,
            String tooLateNode, String tooEarlyNode, boolean canRecover,
            Locator loc) throws ValidationException;
    
    /**
     * An invalid child was encountered.
     * @param source the event source
     * @param elementName the name of the context node
     * @param offendingNode the offending node
     * @param ruleViolated the rule that was violated or null
     * @param loc the location of the error or null
     * @throws ValidationException the validation error provoked by the method call
     */
    void invalidChild(Object source, String elementName, QName offendingNode, String ruleViolated,
            Locator loc) throws ValidationException;

    /**
     * A required child element is missing.
     * @param source the event source
     * @param elementName the name of the context node
     * @param contentModel the expected content model
     * @param canRecover indicates whether FOP can recover from this problem and continue working
     * @param loc the location of the error or null
     * @throws ValidationException the validation error provoked by the method call
     * @event.severity FATAL
     */
    void missingChildElement(Object source, String elementName,
            String contentModel, boolean canRecover,
            Locator loc) throws ValidationException;

    /**
     * An element is missing a required property.
     * @param source the event source
     * @param elementName the name of the context node
     * @param propertyName the name of the missing property
     * @param loc the location of the error or null
     * @throws ValidationException the validation error provoked by the method call
     * @event.severity FATAL
     */
    void missingProperty(Object source, String elementName, String propertyName,
            Locator loc) throws ValidationException;
    
    /**
     * An id was used twice in a document.
     * @param source the event source
     * @param elementName the name of the context node
     * @param id the id that was reused
     * @param canRecover indicates whether FOP can recover from this problem and continue working
     * @param loc the location of the error or null
     * @throws ValidationException the validation error provoked by the method call
     * @event.severity FATAL
     */
    void idNotUnique(Object source, String elementName, String id, boolean canRecover,
            Locator loc) throws ValidationException;

    /**
     * A marker is not an initial child on a node.
     * @param source the event source
     * @param elementName the name of the context node
     * @param mcname the marker class name
     * @param loc the location of the error or null
     * @event.severity ERROR
     */
    void markerNotInitialChild(Object source, String elementName, String mcname, Locator loc);

    /**
     * A marker class name is not unique within the same parent.
     * @param source the event source
     * @param elementName the name of the context node
     * @param mcname the marker class name
     * @param loc the location of the error or null
     * @event.severity ERROR
     */
    void markerNotUniqueForSameParent(Object source, String elementName,
            String mcname, Locator loc);

    /**
     * An invalid property was found.
     * @param source the event source
     * @param elementName the name of the context node
     * @param attr the invalid attribute
     * @param canRecover indicates whether FOP can recover from this problem and continue working
     * @param loc the location of the error or null
     * @throws ValidationException the validation error provoked by the method call
     * @event.severity FATAL
     */
    void invalidProperty(Object source, String elementName, QName attr, boolean canRecover,
            Locator loc) throws ValidationException;

    /**
     * An invalid property value was encountered.
     * @param source the event source
     * @param elementName the name of the context node
     * @param propName the property name
     * @param propValue the property value
     * @param e the property exception caused by the invalid value
     * @param loc the location of the error or null
     * @event.severity ERROR
     */
    void invalidPropertyValue(Object source, String elementName,
            String propName, String propValue, PropertyException e,
            Locator loc);

    /**
     * A feature is not supported, yet.
     * @param source the event source
     * @param elementName the name of the context node
     * @param feature the unsupported feature
     * @param loc the location of the error or null
     * @event.severity WARN
     */
    void unimplementedFeature(Object source, String elementName, String feature,
            Locator loc);

    /**
     * Missing internal-/external-destination on basic-link.
     * @param source the event source
     * @param elementName the name of the context node
     * @param loc the location of the error or null
     * @throws ValidationException the validation error provoked by the method call
     * @event.severity FATAL
     */
    void missingLinkDestination(Object source, String elementName, Locator loc)
                throws ValidationException;

    /**
     * Indicates a problem while cloning a marker (ex. due to invalid property values).
     * @param source the event source
     * @param markerClassName the "marker-class-name" of the marker
     * @param fe the FOP exception that cause this problem
     * @param loc the location of the error or null
     * @event.severity ERROR
     */
    void markerCloningFailed(Object source, String markerClassName, FOPException fe, Locator loc);

}
