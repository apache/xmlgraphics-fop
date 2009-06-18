/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.xml.sax.ContentHandler;

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
     */
    ContentHandler createContentHandler();
    
    /**
     * Interface that ContentHandler implementations that parse Java objects from XML can implement
     * to return these objects.
     */
    public interface ObjectSource {
        
        /**
         * @return the object parsed from the SAX stream (call valid after parsing)
         */
        Object getObject();
        
    }
    
}
