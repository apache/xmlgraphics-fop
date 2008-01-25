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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.xmlgraphics.util.XMLizable;

public class EventModel implements Serializable, XMLizable {

    private Map producers = new java.util.LinkedHashMap();
    
    public EventModel() {
    }
    
    public void addProducer(EventProducerModel producer) {
        this.producers.put(producer.getInterfaceName(), producer);
    }
    
    public Iterator getProducers() {
        return this.producers.values().iterator();
    }

    public EventProducerModel getProducer(String interfaceName) {
        return (EventProducerModel)this.producers.get(interfaceName);
    }
    
    public EventProducerModel getProducer(Class clazz) {
        return getProducer(clazz.getName());
    }
    
    /** {@inheritDoc} */
    public void toSAX(ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        String elName = "event-model";
        handler.startElement(null, elName, elName, atts);
        Iterator iter = getProducers();
        while (iter.hasNext()) {
            ((XMLizable)iter.next()).toSAX(handler);
        }
        handler.endElement(null, elName, elName);
    }

    private void writeXMLizable(XMLizable object, File outputFile) throws IOException {
        Result res = new StreamResult(outputFile);
        
        try {
            SAXTransformerFactory tFactory
                = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
            TransformerHandler handler = tFactory.newTransformerHandler();
            Transformer transformer = handler.getTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            handler.setResult(res);
            handler.startDocument();
            object.toSAX(handler);
            handler.endDocument();
        } catch (TransformerConfigurationException e) {
            throw new IOException(e.getMessage());
        } catch (TransformerFactoryConfigurationError e) {
            throw new IOException(e.getMessage());
        } catch (SAXException e) {
            throw new IOException(e.getMessage());
        }
    }

    public void saveToXML(File modelFile) throws IOException {
        writeXMLizable(this, modelFile);
    }

}
