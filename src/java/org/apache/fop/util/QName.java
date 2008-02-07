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

/**
 * Represents a qualified name of an XML element or an XML attribute.
 * <p>
 * Note: This class allows to carry a namespace prefix but it is not used in the equals() and 
 * hashCode() methods.
 */
public class QName extends org.apache.xmlgraphics.util.QName {

    private static final long serialVersionUID = -5225376740044770690L;
    
    /**
     * Main constructor.
     * @param namespaceURI the namespace URI
     * @param prefix the namespace prefix, may be null
     * @param localName the local name
     */
    public QName(String namespaceURI, String prefix, String localName) {
        super(namespaceURI, prefix, localName);
    }
    
    /**
     * Main constructor.
     * @param namespaceURI the namespace URI
     * @param qName the qualified name
     */
    public QName(String namespaceURI, String qName) {
        super(namespaceURI, qName);
    }
    
}
