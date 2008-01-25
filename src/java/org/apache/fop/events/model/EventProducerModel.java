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
import java.util.Iterator;
import java.util.Map;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.xmlgraphics.util.XMLizable;

public class EventProducerModel implements Serializable, XMLizable {

    private String interfaceName;
    private Map methods = new java.util.LinkedHashMap();
    
    public EventProducerModel(String interfaceName) {
        this.interfaceName = interfaceName;
    }
    
    public String getInterfaceName() {
        return this.interfaceName;
    }
    
    public void setInterfaceName(String name) {
        this.interfaceName = name;
    }
    
    public void addMethod(EventMethodModel method) {
        this.methods.put(method.getMethodName(), method);
    }
    
    public EventMethodModel getMethod(String methodName) {
        return (EventMethodModel)this.methods.get(methodName);
    }
    
    public Iterator getMethods() {
        return this.methods.values().iterator();
    }

    /** {@inheritDoc} */
    public void toSAX(ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(null, "name", "name", "CDATA", getInterfaceName());
        String elName = "producer";
        handler.startElement(null, elName, elName, atts);
        Iterator iter = getMethods();
        while (iter.hasNext()) {
            ((XMLizable)iter.next()).toSAX(handler);
        }
        handler.endElement(null, elName, elName);
    }

    
}
