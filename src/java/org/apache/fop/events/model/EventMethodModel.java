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

package org.apache.fop.events.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.xmlgraphics.util.XMLizable;

public class EventMethodModel implements Serializable, XMLizable {

    private String methodName;
    private EventSeverity severity;
    private List params = new java.util.ArrayList();
    
    public EventMethodModel(String methodName, EventSeverity severity) {
        this.methodName = methodName;
        this.severity = severity;
    }
    
    public void addParameter(Parameter param) {
        this.params.add(param);
    }
    
    public Parameter addParameter(Class type, String name) {
        Parameter param = new Parameter(type, name); 
        this.params.add(param);
        return param;
    }
    
    public void setMethodName(String name) {
        this.methodName = name;
    }
    
    public String getMethodName() {
        return this.methodName;
    }
    
    public void setSeverity(EventSeverity severity) {
        this.severity = severity;
    }
    
    public EventSeverity getSeverity() {
        return this.severity;
    }
    
    public List getParameters() {
        return Collections.unmodifiableList(this.params);
    }
    
    /** {@inheritDoc} */
    public void toSAX(ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(null, "name", "name", "CDATA", getMethodName());
        atts.addAttribute(null, "severity", "severity", "CDATA", getSeverity().getName());
        String elName = "method";
        handler.startElement(null, elName, elName, atts);
        Iterator iter = this.params.iterator();
        while (iter.hasNext()) {
            ((XMLizable)iter.next()).toSAX(handler);
        }
        handler.endElement(null, elName, elName);
    }
    
    public static class Parameter implements Serializable, XMLizable {
        
        private Class type;
        private String name;
        
        public Parameter(Class type, String name) {
            this.type = type;
            this.name = name;
        }
        
        public Class getType() {
            return this.type;
        }
        
        public String getName() {
            return this.name;
        }

        /** {@inheritDoc} */
        public void toSAX(ContentHandler handler) throws SAXException {
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute(null, "type", "type", "CDATA", getType().getName());
            atts.addAttribute(null, "name", "name", "CDATA", getName());
            String elName = "parameter";
            handler.startElement(null, elName, elName, atts);
            handler.endElement(null, elName, elName);
        }
        
    }
}
