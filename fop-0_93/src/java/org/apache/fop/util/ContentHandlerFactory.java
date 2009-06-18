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

package org.apache.fop.util;

import java.util.EventListener;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Factory interface implemented by classes that can instantiate ContentHandler subclasses which
 * parse a SAX stream into Java objects.
 */
public interface ContentHandlerFactory {

    /**
     * @return an array of supported namespaces.
     */
    String[] getSupportedNamespaces();
    
    /**
     * @return a new ContentHandler to handle a SAX stream
     * @throws SAXException if there's an error while preparing the ContentHandler
     */
    ContentHandler createContentHandler() throws SAXException;
    
    /**
     * Interface that ContentHandler implementations that parse Java objects from XML can implement
     * to return these objects.
     */
    public interface ObjectSource {
        
        /**
         * @return the object parsed from the SAX stream (call valid after parsing)
         */
        Object getObject();
     
        /**
         * Set a listener which gets notified when the object is fully built.
         * @param listener the listener which gets notified
         */
        void setObjectBuiltListener(ObjectBuiltListener listener);
    }
    
    /**
     * EventListener interface for objects which want to get notified when ContentHandler
     * implementing the ObjectSource interface has finished parsing.
     */
    public interface ObjectBuiltListener extends EventListener {
        
        /**
         * Notifies the listener when the object is fully built.
         * @param obj the newly built object
         */
        void notifyObjectBuilt(Object obj);
        
    }
    
}
