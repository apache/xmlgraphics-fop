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

package org.apache.fop.events;

import java.util.Collections;
import java.util.EventObject;
import java.util.Map;

import org.apache.fop.events.model.EventSeverity;

/**
 * This is the default event class used by this package. Each event has a unique event identifier
 * (a String), a severity indicator and a map of name/value pairs.
 */
public class Event extends EventObject {

    private String eventID;
    private EventSeverity severity;
    private Map params;
    
    /**
     * Creates a new Event.
     * @param source the object that creates the event
     * @param eventID the unique identifier of the event
     * @param severity the severity level
     * @param params the event parameters (a map of name/value pairs)
     */
    public Event(Object source, String eventID, EventSeverity severity, Map params) {
        super(source);
        this.eventID = eventID;
        setSeverity(severity);
        this.params = params;
    }
    
    /**
     * Returns the event identifier.
     * @return the event identifier
     */
    public String getEventID() {
        return this.eventID;
    }
    
    /**
     * Returns the event group identifier.
     * @return the event group identifier (or null if there is no group identifier)
     */
    public String getEventGroupID() {
        int pos = this.eventID.lastIndexOf('.');
        if (pos > 0) {
            return this.eventID.substring(0, pos);
        } else {
            return null;
        }
    }
    
    /**
     * Returns the severity level.
     * @return the severity level
     */
    public EventSeverity getSeverity() {
        return this.severity;
    }
    
    /**
     * Sets the event's severity level. This method can be used to increase or decrease the
     * severity level in a listener.
     * @param severity the new event severity
     */
    public void setSeverity(EventSeverity severity) {
        this.severity = severity;
    }

    /**
     * Returns a parameter.
     * @param key the key to the parameter
     * @return the parameter value or null if no value with this key is found
     */
    public Object getParam(String key) {
        if (this.params != null) {
            return this.params.get(key);
        } else {
            return null;
        }
    }
    
    /**
     * Returns an unmodifiable {@link java.util.Map} with all event parameters.
     * @return the parameter map
     */
    public Map getParams() {
        return Collections.unmodifiableMap(this.params);
    }
    
    public String getLocalizedMessage() {
        return null;
    }
    
    /**
     * Creates and returns a fluent builder object for building up the parameter map.
     * @return the parameter builder
     */
    public static ParamsBuilder paramsBuilder() {
        return new ParamsBuilder();
    }
    
    /**
     * This class is a fluent builder class for building up the parameter map.
     */
    public static class ParamsBuilder {
        private Map params;
        
        /**
         * Adds a new parameter (a name/value pair).
         * @param name the name of the parameter
         * @param value the value of the parameter
         * @return this instance
         */
        public ParamsBuilder param(String name, Object value) {
            if (this.params == null) {
                this.params = new java.util.HashMap();
            }
            this.params.put(name, value);
            return this;
        }
        
        /**
         * Returns the accumulated parameter map.
         * @return the accumulated parameter map
         */
        public Map build() {
            return this.params;
        }
    }
    
}
