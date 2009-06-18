/* 
 * Copyright 2002-2004 The Apache Software Foundation.
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

/* 
 * Copied from Apache Excalibur:
 * https://svn.apache.org/repos/asf/excalibur/trunk/components/xmlutil/
 *                                           src/java/org/apache/excalibur/xml/sax/XMLizable.java
 */

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This interface can be implemented by classes willing to provide an XML representation
 * of their current state as SAX events.
 */
public interface XMLizable {
    
    /**
     * Generates SAX events representing the object's state.
     * @param handler ContentHandler instance to send the SAX events to
     * @throws SAXException if there's a problem generating the SAX events
     */
    void toSAX(ContentHandler handler) throws SAXException;
    
}
