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

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * ContentHandlerFactory which constructs ContentHandlers that build DOM Documents.
 */
public class DOMBuilderContentHandlerFactory implements ContentHandlerFactory {

    private static SAXTransformerFactory tFactory 
            = (SAXTransformerFactory)SAXTransformerFactory.newInstance();

    private String namespaceURI;
    private DOMImplementation domImplementation;
    
    /**
     * Main Constructor
     * @param namespaceURI the main namespace URI for the DOM to be parsed 
     * @param domImplementation the DOMImplementation to use for build the DOM
     */
    public DOMBuilderContentHandlerFactory(String namespaceURI, 
                DOMImplementation domImplementation) {
        this.namespaceURI = namespaceURI;
        this.domImplementation = domImplementation;
    }
    
    /** {@inheritDoc} */
    public String[] getSupportedNamespaces() {
        return new String[] {namespaceURI};
    }

    /** {@inheritDoc} */
    public ContentHandler createContentHandler() throws SAXException {
        return new Handler();
    }
    
    private class Handler extends DelegatingContentHandler
                implements ContentHandlerFactory.ObjectSource {
     
        private Document doc;
        private ObjectBuiltListener obListener;
        
        public Handler() throws SAXException {
            super();
        }

        public Document getDocument() {
            return this.doc;
        }
        
        /**
         * {@inheritDoc}
         */
        public Object getObject() {
            return getDocument();
        }

        /**
         * {@inheritDoc}
         */
        public void setObjectBuiltListener(ObjectBuiltListener listener) {
            this.obListener = listener;
        }
        
        /**
         * {@inheritDoc}
         */
        public void startDocument() throws SAXException {
            //Suppress startDocument() call if doc has not been set, yet. It will be done later.
            if (doc != null) {
                super.startDocument();
            }
        }

        /**
         * {@inheritDoc}
         */
        public void startElement(String uri, String localName, String qName, Attributes atts) 
                    throws SAXException {
            if (doc == null) {
                TransformerHandler handler;
                try {
                    handler = tFactory.newTransformerHandler();
                } catch (TransformerConfigurationException e) {
                    throw new SAXException("Error creating a new TransformerHandler", e);
                }
                doc = domImplementation.createDocument(namespaceURI, qName, null);
                //It's easier to work with an empty document, so remove the root element
                doc.removeChild(doc.getDocumentElement());
                handler.setResult(new DOMResult(doc));
                setDelegateContentHandler(handler);
                setDelegateLexicalHandler(handler);
                setDelegateDTDHandler(handler);
                handler.startDocument();
            }
            super.startElement(uri, localName, qName, atts);
        }

        /**
         * {@inheritDoc}
         */
        public void endDocument() throws SAXException {
            super.endDocument();
            if (obListener != null) {
                obListener.notifyObjectBuilt(getObject());
            }
        }

    }

}
