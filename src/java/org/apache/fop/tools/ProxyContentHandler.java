/*
 * Copyright 2004,2004 The Apache Software Foundation.
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

package org.apache.fop.tools;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Simple proxying ContentHandler. It simply forwards all SAX events to its
 * delegate ContentHandler.
 */
public class ProxyContentHandler implements ContentHandler {

    private ContentHandler delegate;
    
    /**
     * Constructs a new ProxyContentHandler
     * @param delegate the ContentHandler that receives all SAX events.  
     */
    public ProxyContentHandler(ContentHandler delegate) {
        super();
        this.delegate = delegate;
    }

    /**
     * @return the delegate ContentHandler
     */
    public ContentHandler getDelegate() {
        return this.delegate;
    }

    /**
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        getDelegate().startDocument();
    }

    /**
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        getDelegate().endDocument();
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        getDelegate().characters(ch, start, length);
    }

    /**
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        getDelegate().ignorableWhitespace(ch, start, length);
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String namespaceURI,
            String localName, String qName, Attributes atts)
                throws SAXException {
        getDelegate().startElement(namespaceURI, localName, qName, atts);
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        getDelegate().endElement(namespaceURI, localName, qName);
    }

    /**
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    public void skippedEntity(String name) throws SAXException {
        getDelegate().skippedEntity(name);
    }

    /**
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator(Locator locator) {
        getDelegate().setDocumentLocator(locator);
    }

    /**
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
     */
    public void processingInstruction(String target, String data)
            throws SAXException {
        getDelegate().processingInstruction(target, data);
    }

    /**
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
     */
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        getDelegate().startPrefixMapping(prefix, uri);
    }

    /**
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        getDelegate().endPrefixMapping(prefix);
    }

}
