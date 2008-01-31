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

public class Event extends EventObject {

    private String eventID;
    private EventSeverity severity;
    private Map params;
    
    public Event(Object source, String eventID, EventSeverity severity, Map params) {
        super(source);
        this.eventID = eventID;
        this.severity = severity;
        this.params = params;
    }
    
    public String getEventID() {
        return this.eventID;
    }
    
    public EventSeverity getSeverity() {
        return this.severity;
    }

    public Object getParam(String key) {
        if (this.params != null) {
            return this.params.get(key);
        } else {
            return null;
        }
    }
    
    public Map getParams() {
        return Collections.unmodifiableMap(this.params);
    }
    
    public String getLocalizedMessage() {
        return null;
    }
    
    public static ParamsBuilder paramsBuilder() {
        return new ParamsBuilder();
    }
    
    public static class ParamsBuilder {
        private Map params;
        
        public ParamsBuilder param(String name, Object value) {
            if (this.params == null) {
                this.params = new java.util.HashMap();
            }
            this.params.put(name, value);
            return this;
        }
        
        public Map build() {
            return this.params;
        }
    }
    
}
