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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This class is a delegating SAX ContentHandler which has the purpose to provide a few handy
 * methods that make life easier when generating SAX events.
 */
public class GenerationHelperContentHandler extends DelegatingContentHandler {

    private static final Attributes EMPTY_ATTS = new AttributesImpl();

    private String mainNamespace;

    /**
     * Main constructor. If the given handler also implements any of the EntityResolver,
     * DTDHandler, LexicalHandler or ErrorHandler interfaces, these are set automatically.
     * @param handler the SAX content handler to delegate all calls to
     * @param mainNamespace the main namespace used for generated XML content when abbreviated
     *                          ContentHandler calls are used.
     */
    public GenerationHelperContentHandler(ContentHandler handler, String mainNamespace) {
        super(handler);
        this.mainNamespace = mainNamespace;
    }

    /**
     * Returns the main namespace used for generated XML content.
     * @return the main namespace
     */
    public String getMainNamespace() {
        return this.mainNamespace;
    }

    /**
     * Sets the main namespace used for generated XML content when abbreviated ContentHandler
     * calls are used.
     * @param namespaceURI the new main namespace URI
     */
    public void setMainNamespace(String namespaceURI) {
        this.mainNamespace = namespaceURI;
    }

    /**
     * Convenience method to generate a startElement SAX event.
     * @param localName the local name of the element
     * @param atts the attributes
     * @throws SAXException if a SAX exception occurs
     */
    public void startElement(String localName, Attributes atts) throws SAXException {
        getDelegateContentHandler().startElement(getMainNamespace(), localName, localName, atts);
    }

    /**
     * Convenience method to generate a startElement SAX event.
     * @param localName the local name of the element
     * @throws SAXException if a SAX exception occurs
     */
    public void startElement(String localName) throws SAXException {
        getDelegateContentHandler().startElement(getMainNamespace(), localName, localName,
                EMPTY_ATTS);
    }

    /**
     * Convenience method to generate a endElement SAX event.
     * @param localName the local name of the element
     * @throws SAXException if a SAX exception occurs
     */
    public void endElement(String localName) throws SAXException {
        getDelegateContentHandler().endElement(getMainNamespace(), localName, localName);
    }

    /**
     * Convenience method to generate an empty element.
     * @param localName the local name of the element
     * @param atts the attributes
     * @throws SAXException if a SAX exception occurs
     */
    public void element(String localName, Attributes atts) throws SAXException {
        getDelegateContentHandler().startElement(getMainNamespace(), localName, localName, atts);
        getDelegateContentHandler().endElement(getMainNamespace(), localName, localName);
    }

}
